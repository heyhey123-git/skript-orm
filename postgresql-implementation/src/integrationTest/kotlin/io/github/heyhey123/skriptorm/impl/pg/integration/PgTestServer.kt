package io.github.heyhey123.skriptorm.impl.pg.integration

import io.github.heyhey123.skriptorm.database.ConnectionSettings
import org.junit.jupiter.api.Assumptions
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration

/**
 * Resolves the PostgreSQL server the integration tests run against.
 *
 * Resolution order:
 * 1. An externally managed server, configured through the `skriptorm.test.postgres.*` system properties
 *    or their `SKRIPTORM_TEST_POSTGRES_*` environment equivalents. This is how CI points the tests at
 *    the server the runner already installs.
 * 2. A Testcontainers-managed container, started once per test JVM, when a Docker daemon is reachable.
 *
 * When neither is available the tests abort with an explanation instead of passing silently. The tests
 * are destructive: they drop and recreate the tables they use, so the configured database must be
 * dedicated to testing.
 */
object PgTestServer {

    const val URL_PROPERTY = "skriptorm.test.postgres.url"
    const val USERNAME_PROPERTY = "skriptorm.test.postgres.username"
    const val PASSWORD_PROPERTY = "skriptorm.test.postgres.password"
    const val DRIVER_PROPERTY = "skriptorm.test.postgres.driver"
    const val IMAGE_PROPERTY = "skriptorm.test.postgres.image"

    const val URL_ENV = "SKRIPTORM_TEST_POSTGRES_URL"
    const val USERNAME_ENV = "SKRIPTORM_TEST_POSTGRES_USERNAME"
    const val PASSWORD_ENV = "SKRIPTORM_TEST_POSTGRES_PASSWORD"
    const val DRIVER_ENV = "SKRIPTORM_TEST_POSTGRES_DRIVER"
    const val IMAGE_ENV = "SKRIPTORM_TEST_POSTGRES_IMAGE"

    /** Pinned to the PostgreSQL 17 line; override [IMAGE_PROPERTY] to test another version. */
    const val DEFAULT_IMAGE = "postgres:17-alpine"

    const val DEFAULT_DRIVER = "org.postgresql.Driver"

    private val STARTUP_TIMEOUT: Duration = Duration.ofMinutes(3)

    /** Connection details for one PostgreSQL server. */
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

    /** Whether this JVM has a PostgreSQL server to run the integration tests against. */
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
            Assumptions.assumeTrue(false, "PostgreSQL integration test aborted: ${current.reason}")
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
            Resolution.Unavailable("the PostgreSQL container could not be started: $error")
        }
    }

    private fun externalEndpoint(): Endpoint? {
        val url = setting(URL_PROPERTY, URL_ENV) ?: return null
        return Endpoint(
            jdbcUrl = url,
            username = setting(USERNAME_PROPERTY, USERNAME_ENV) ?: "postgres",
            password = setting(PASSWORD_PROPERTY, PASSWORD_ENV).orEmpty(),
            driverClassName = setting(DRIVER_PROPERTY, DRIVER_ENV) ?: DEFAULT_DRIVER
        )
    }

    private fun startContainer(): Endpoint {
        val image = setting(IMAGE_PROPERTY, IMAGE_ENV) ?: DEFAULT_IMAGE
        // PostgreSQLContainer is self-typed, so its configuration methods return SELF rather than the
        // container type; configuring the instance instead of chaining keeps the SELF parameter
        // resolved to the container itself.
        val container = PostgreSQLContainer<Nothing>(DockerImageName.parse(image)).apply {
            withDatabaseName("skriptorm_test")
            withUsername("skriptorm")
            withPassword("skriptorm")
            withStartupTimeout(STARTUP_TIMEOUT)
        }
        container.start()
        Runtime.getRuntime().addShutdownHook(
            Thread({ runCatching { container.stop() } }, "skriptorm-postgres-container-shutdown")
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
