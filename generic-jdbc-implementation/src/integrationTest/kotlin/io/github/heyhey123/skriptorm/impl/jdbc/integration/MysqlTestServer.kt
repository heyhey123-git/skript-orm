package io.github.heyhey123.skriptorm.impl.jdbc.integration

import io.github.heyhey123.skriptorm.database.ConnectionSettings
import org.junit.jupiter.api.Assumptions
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration

/**
 * Resolves the MySQL-family server the JDBC integration tests run against.
 *
 * "MySQL" names the protocol family here, as it does in [MysqlJdbcDialect]: one dialect serves both
 * products this project supports, so the suite is shared and a test class names the product it runs
 * against. Resolution happens per product, so a run may configure both, one, or neither — MySQL is
 * what a developer usually has, and a machine without MariaDB still runs the MySQL half instead of
 * skipping everything with it.
 *
 * Resolution order, per product:
 * 1. An externally managed server, configured through that product's `skriptorm.test.<product>.*`
 *    system properties or their `SKRIPTORM_TEST_<PRODUCT>_*` environment equivalents.
 * 2. A Testcontainers-managed container of that product's image, started once per test JVM, when a
 *    Docker daemon is reachable.
 *
 * When neither is available the tests abort with an explanation instead of passing silently. The
 * tests are destructive: they drop and recreate the tables they use, so the configured database
 * must be dedicated to testing.
 */
class MysqlTestServer private constructor(private val product: Product) {

    /**
     * The products this suite can be run against.
     *
     * Each one carries its own image and driver rather than borrowing MySQL's, because the two
     * drivers accept different URL schemes: an endpoint has to be resolved by the product that will
     * open it.
     */
    enum class Product(
        val displayName: String,
        val defaultImage: String,
        val defaultDriver: String,
        private val prefix: String,
        private val environmentPrefix: String
    ) {

        /** Reached through MySQL Connector/J, which Paper already makes visible to plugins. */
        MYSQL(
            displayName = "MySQL",
            defaultImage = "mysql:8.4",
            defaultDriver = "com.mysql.cj.jdbc.Driver",
            prefix = "skriptorm.test.mysql",
            environmentPrefix = "SKRIPTORM_TEST_MYSQL"
        ),

        /** Reached through MariaDB Connector/J, which this plugin's jar asks Paper to download. */
        MARIADB(
            displayName = "MariaDB",
            defaultImage = "mariadb:11.4",
            defaultDriver = "org.mariadb.jdbc.Driver",
            prefix = "skriptorm.test.mariadb",
            environmentPrefix = "SKRIPTORM_TEST_MARIADB"
        );

        val urlProperty: String = "$prefix.url"
        val usernameProperty: String = "$prefix.username"
        val passwordProperty: String = "$prefix.password"
        val driverProperty: String = "$prefix.driver"
        val imageProperty: String = "$prefix.image"

        val urlEnvironment: String = "${environmentPrefix}_URL"
        val usernameEnvironment: String = "${environmentPrefix}_USERNAME"
        val passwordEnvironment: String = "${environmentPrefix}_PASSWORD"
        val driverEnvironment: String = "${environmentPrefix}_DRIVER"
        val imageEnvironment: String = "${environmentPrefix}_IMAGE"
    }

    /** Connection details for one server. */
    data class Endpoint(
        val jdbcUrl: String,
        val username: String,
        val password: String,
        val driverClassName: String
    ) {

        /** The same details as the value a connection is opened with. */
        val settings: ConnectionSettings
            get() = ConnectionSettings(jdbcUrl, username, password)
    }

    private sealed interface Resolution {
        data class Available(val endpoint: Endpoint) : Resolution
        data class Unavailable(val reason: String) : Resolution
    }

    private val resolution: Resolution by lazy { resolve() }

    /** Whether this JVM has a server of this product to run the integration tests against. */
    val isAvailable: Boolean
        get() = resolution is Resolution.Available

    /** Explains why the integration tests have no server of this product to run against. */
    val unavailableReason: String?
        get() = (resolution as? Resolution.Unavailable)?.reason

    /**
     * Returns the shared endpoint, or aborts the calling test when no server is available.
     */
    fun requireEndpoint(): Endpoint = when (val current = resolution) {
        is Resolution.Available -> current.endpoint
        is Resolution.Unavailable -> {
            Assumptions.assumeTrue(false, "${product.displayName} integration test aborted: ${current.reason}")
            error("Unreachable: assumeTrue always throws for an unmet assumption.")
        }
    }

    private fun resolve(): Resolution {
        externalEndpoint()?.let { return Resolution.Available(it) }

        if (!isDockerAvailable()) {
            return Resolution.Unavailable(
                "no Docker daemon is reachable and no external ${product.displayName} server was " +
                    "configured through ${product.urlProperty} or ${product.urlEnvironment}."
            )
        }

        return try {
            Resolution.Available(startContainer())
        } catch (error: Throwable) {
            Resolution.Unavailable("the ${product.displayName} container could not be started: $error")
        }
    }

    private fun externalEndpoint(): Endpoint? {
        val url = setting(product.urlProperty, product.urlEnvironment) ?: return null
        return Endpoint(
            jdbcUrl = url,
            username = setting(product.usernameProperty, product.usernameEnvironment) ?: "root",
            password = setting(product.passwordProperty, product.passwordEnvironment).orEmpty(),
            driverClassName = setting(product.driverProperty, product.driverEnvironment)
                ?: product.defaultDriver
        )
    }

    private fun startContainer(): Endpoint {
        val image = setting(product.imageProperty, product.imageEnvironment) ?: product.defaultImage
        // Both container classes are self-typed, so their configuration methods return SELF rather
        // than the container type; configuring through the base type keeps that parameter resolved
        // to the container itself, which is also what lets the two products share this method.
        val container: JdbcDatabaseContainer<*> = when (product) {
            Product.MYSQL -> MySQLContainer<Nothing>(DockerImageName.parse(image))
            Product.MARIADB -> MariaDBContainer<Nothing>(DockerImageName.parse(image))
        }
        container.withDatabaseName("skriptorm_test")
        container.withUsername("skriptorm")
        container.withPassword("skriptorm")
        container.withStartupTimeout(STARTUP_TIMEOUT)
        container.start()
        Runtime.getRuntime().addShutdownHook(
            Thread(
                { runCatching { container.stop() } },
                "skriptorm-${product.name.lowercase()}-container-shutdown"
            )
        )
        return Endpoint(
            jdbcUrl = container.jdbcUrl,
            username = container.username,
            password = container.password,
            driverClassName = product.defaultDriver
        )
    }

    private fun isDockerAvailable(): Boolean = try {
        DockerClientFactory.instance().isDockerAvailable
    } catch (error: Throwable) {
        false
    }

    private fun setting(property: String, environment: String): String? =
        System.getProperty(property)?.takeIf { it.isNotBlank() }
            ?: System.getenv(environment)?.takeIf { it.isNotBlank() }

    companion object {

        private val STARTUP_TIMEOUT: Duration = Duration.ofMinutes(3)

        private val servers: Map<Product, MysqlTestServer> =
            Product.entries.associateWith { MysqlTestServer(it) }

        /** The shared resolver for [product], so each product starts at most one server per JVM. */
        fun of(product: Product): MysqlTestServer = servers.getValue(product)
    }
}
