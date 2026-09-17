package io.github.heyhey123.xiaojieorm.impl.jdbc.integration

import io.github.heyhey123.xiaojieorm.database.ConnectionSettings
import org.junit.jupiter.api.Assumptions
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration

/**
 * Resolves the MySQL server the JDBC integration tests run against.
 *
 * Resolution order:
 * 1. An externally managed server, configured through the `xiaojie.test.mysql.*` system properties
 *    or their `XIAOJIE_TEST_MYSQL_*` environment equivalents.
 * 2. A Testcontainers-managed MySQL container, started once per test JVM, when a Docker daemon is
 *    reachable.
 *
 * When neither is available the tests abort with an explanation instead of passing silently. The
 * tests are destructive: they drop and recreate the tables they use, so the configured database
 * must be dedicated to testing.
 */
object MysqlTestServer {

    const val URL_PROPERTY = "xiaojie.test.mysql.url"
    const val USERNAME_PROPERTY = "xiaojie.test.mysql.username"
    const val PASSWORD_PROPERTY = "xiaojie.test.mysql.password"
    const val DRIVER_PROPERTY = "xiaojie.test.mysql.driver"
    const val IMAGE_PROPERTY = "xiaojie.test.mysql.image"

    const val URL_ENV = "XIAOJIE_TEST_MYSQL_URL"
    const val USERNAME_ENV = "XIAOJIE_TEST_MYSQL_USERNAME"
    const val PASSWORD_ENV = "XIAOJIE_TEST_MYSQL_PASSWORD"
    const val DRIVER_ENV = "XIAOJIE_TEST_MYSQL_DRIVER"
    const val IMAGE_ENV = "XIAOJIE_TEST_MYSQL_IMAGE"

    /** Pinned to the MySQL 8.4 LTS line; override [IMAGE_PROPERTY] to test another version. */
    const val DEFAULT_IMAGE = "mysql:8.4"

    const val DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver"

    private val STARTUP_TIMEOUT: Duration = Duration.ofMinutes(3)

    /** Connection details for one MySQL server. */
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

    /** Whether this JVM has a MySQL server to run the integration tests against. */
    val isAvailable: Boolean
        get() = resolution is Resolution.Available

    /** Explains why the integration tests have no server to run against. */
    val unavailableReason: String?
        get() = (resolution as? Resolution.Unavailable)?.reason

    /**
     * Returns the shared endpoint, or aborts the calling test when no server is available.
     */
    fun requireEndpoint(): Endpoint = when (val current = resolution) {
        is Resolution.Available -> current.endpoint
        is Resolution.Unavailable -> {
            Assumptions.assumeTrue(false, "MySQL integration test aborted: ${current.reason}")
            error("Unreachable: assumeTrue always throws for an unmet assumption.")
        }
    }

    private fun resolve(): Resolution {
        externalEndpoint()?.let { return Resolution.Available(it) }

        if (!isDockerAvailable()) {
            return Resolution.Unavailable(
                "no Docker daemon is reachable and no external server was configured through " +
                    "$URL_PROPERTY or $URL_ENV."
            )
        }

        return try {
            Resolution.Available(startContainer())
        } catch (error: Throwable) {
            Resolution.Unavailable("the MySQL container could not be started: $error")
        }
    }

    private fun externalEndpoint(): Endpoint? {
        val url = setting(URL_PROPERTY, URL_ENV) ?: return null
        return Endpoint(
            jdbcUrl = url,
            username = setting(USERNAME_PROPERTY, USERNAME_ENV) ?: "root",
            password = setting(PASSWORD_PROPERTY, PASSWORD_ENV).orEmpty(),
            driverClassName = setting(DRIVER_PROPERTY, DRIVER_ENV) ?: DEFAULT_DRIVER
        )
    }

    private fun startContainer(): Endpoint {
        val image = setting(IMAGE_PROPERTY, IMAGE_ENV) ?: DEFAULT_IMAGE
        // MySQLContainer is self-typed, so its configuration methods return SELF rather than the
        // container type; configuring the instance instead of chaining keeps the SELF parameter
        // resolved to the container itself.
        val container = MySQLContainer<Nothing>(DockerImageName.parse(image)).apply {
            withDatabaseName("xiaojie_orm_test")
            withUsername("xiaojie")
            withPassword("xiaojie")
            withStartupTimeout(STARTUP_TIMEOUT)
        }
        container.start()
        Runtime.getRuntime().addShutdownHook(
            Thread({ runCatching { container.stop() } }, "xiaojie-mysql-container-shutdown")
        )
        return Endpoint(
            jdbcUrl = container.jdbcUrl,
            username = container.username,
            password = container.password,
            driverClassName = DEFAULT_DRIVER
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
}
