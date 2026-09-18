package io.github.heyhey123.skriptorm.impl.mongo.integration

import io.github.heyhey123.skriptorm.database.ConnectionSettings
import org.junit.jupiter.api.Assumptions
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration

/**
 * Resolves the MongoDB server the integration tests run against.
 *
 * Resolution order:
 * 1. An externally managed server, configured through the `skriptorm.test.mongo.*` system properties or
 *    their `SKRIPTORM_TEST_MONGO_*` environment equivalents. This is how CI points the tests at a server,
 *    and how a developer runs them against a MongoDB they already have.
 * 2. A Testcontainers-managed container, started once per test JVM, when a Docker daemon is reachable.
 *
 * When neither is available the tests abort with an explanation instead of passing silently. The tests are
 * destructive: they drop the collections they use, so the configured database must be dedicated to testing.
 *
 * The container runs a standalone server rather than the replica set `MongoDBContainer` sets up. Nothing
 * here uses a session or a transaction, and a replica set advertises the address its members know each
 * other by, which is not the address a test can reach on the host.
 */
object MongoTestServer {

    const val URL_PROPERTY = "skriptorm.test.mongo.url"
    const val USERNAME_PROPERTY = "skriptorm.test.mongo.username"
    const val PASSWORD_PROPERTY = "skriptorm.test.mongo.password"
    const val DATABASE_PROPERTY = "skriptorm.test.mongo.database"
    const val IMAGE_PROPERTY = "skriptorm.test.mongo.image"

    const val URL_ENV = "SKRIPTORM_TEST_MONGO_URL"
    const val USERNAME_ENV = "SKRIPTORM_TEST_MONGO_USERNAME"
    const val PASSWORD_ENV = "SKRIPTORM_TEST_MONGO_PASSWORD"
    const val DATABASE_ENV = "SKRIPTORM_TEST_MONGO_DATABASE"
    const val IMAGE_ENV = "SKRIPTORM_TEST_MONGO_IMAGE"

    /** Pinned to the MongoDB 8 line; override [IMAGE_PROPERTY] to test another version. */
    const val DEFAULT_IMAGE = "mongo:8"

    /** The database the tests use when no external server names one. */
    const val DEFAULT_DATABASE = "skriptorm_test"

    private const val PORT = 27017
    private val STARTUP_TIMEOUT: Duration = Duration.ofMinutes(3)

    /**
     * Connection details for one MongoDB server.
     *
     * @property url what a `create a connection` section would write as its url: a whole connection string.
     */
    data class Endpoint(
        val url: String,
        val username: String,
        val password: String,
        val database: String
    ) {

        /** The same details as the value a connection is opened with. */
        val settings: ConnectionSettings
            get() = ConnectionSettings(url, username, password)

        /**
         * The properties a `create a connection` section would declare beside the url. `database` is the
         * name the plugin reads for the database to use, which a url without a path cannot carry.
         */
        val properties: Map<String, String>
            get() = mapOf(DATABASE_PROPERTY_NAME to database)

        private companion object {
            const val DATABASE_PROPERTY_NAME = "database"
        }
    }

    private sealed interface Resolution {
        data class Available(val endpoint: Endpoint) : Resolution
        data class Unavailable(val reason: String) : Resolution
    }

    private val resolution: Resolution by lazy { resolve() }

    /** Whether this JVM has a MongoDB server to run the integration tests against. */
    val isAvailable: Boolean
        get() = resolution is Resolution.Available

    /** Explains why the integration tests have no server to run against. */
    val unavailableReason: String?
        get() = (resolution as? Resolution.Unavailable)?.reason

    /** Returns the shared endpoint, or aborts the calling test when no server is available. */
    fun requireEndpoint(): Endpoint = when (val current = resolution) {
        is Resolution.Available -> current.endpoint
        is Resolution.Unavailable -> {
            Assumptions.assumeTrue(false, "MongoDB integration test aborted: ${current.reason}")
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
            Resolution.Unavailable("the MongoDB container could not be started: $error")
        }
    }

    private fun externalEndpoint(): Endpoint? {
        val url = setting(URL_PROPERTY, URL_ENV) ?: return null
        return Endpoint(
            url = url,
            username = setting(USERNAME_PROPERTY, USERNAME_ENV).orEmpty(),
            password = setting(PASSWORD_PROPERTY, PASSWORD_ENV).orEmpty(),
            database = setting(DATABASE_PROPERTY, DATABASE_ENV) ?: DEFAULT_DATABASE
        )
    }

    private fun startContainer(): Endpoint {
        val image = setting(IMAGE_PROPERTY, IMAGE_ENV) ?: DEFAULT_IMAGE
        val container = GenericContainer(DockerImageName.parse(image))
            .withExposedPorts(PORT)
            .withStartupTimeout(STARTUP_TIMEOUT)
        container.start()
        Runtime.getRuntime().addShutdownHook(
            Thread({ runCatching { container.stop() } }, "skriptorm-mongo-container-shutdown")
        )
        // A standalone server, reached directly: without `directConnection` the driver would try to
        // discover a replica set that is not there and time out looking for it.
        val url = "mongodb://${container.host}:${container.getMappedPort(PORT)}/?directConnection=true"
        return Endpoint(url = url, username = "", password = "", database = DEFAULT_DATABASE)
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
