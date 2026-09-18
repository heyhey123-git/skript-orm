package io.github.heyhey123.skriptorm.impl.mongo.database

import com.mongodb.ServerAddress
import io.github.heyhey123.skriptorm.database.ConnectionSettings
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Covers what a `create a connection to database "MongoDB"` section turns into, without a server: which
 * host is contacted, which database is used, and what the driver is given as credentials.
 *
 * The credentials are the reason this is worth pinning. A connection string carries them inside a url, so
 * the obvious implementation pastes them into one, and every password containing `@`, `:` or `/` then
 * either fails to parse or authenticates as somebody else. Handing the driver the two strings as they are
 * removes the question entirely, and these tests say so.
 */
class MongoConnectionTest {

    @Test
    fun `a bare host and port becomes the cluster`() {
        val settings = clientSettings(url = "db.example.com:27018")

        assertEquals(listOf(ServerAddress("db.example.com", 27018)), settings.clusterSettings.hosts)
        assertNull(settings.credential, "a connection with no username has no credential")
    }

    @Test
    fun `a connection string is taken as it is`() {
        val settings = clientSettings(url = "mongodb://db.example.com:27017/logs?retryWrites=false")

        assertEquals(listOf(ServerAddress("db.example.com", 27017)), settings.clusterSettings.hosts)
        assertNull(settings.credential, "the url carries no credentials and none were declared")
    }

    @Test
    fun `credentials survive whatever characters they contain`() {
        val settings = clientSettings(
            url = "localhost:27017",
            username = "user@example.com",
            password = "p@ss:w/ord?"
        )

        val credential = assertNotNull(settings.credential)
        assertEquals("user@example.com", credential.userName)
        assertEquals("p@ss:w/ord?", requireNotNull(credential.password).concatToString())
    }

    @Test
    fun `a declared username authenticates against the database being used`() {
        val credential = assertNotNull(
            clientSettings(url = "localhost:27017", username = "app", password = "secret").credential
        )

        assertEquals("app", credential.userName)
        assertEquals(MongoConnection.DEFAULT_DATABASE, credential.source)
    }

    @Test
    fun `the auth database can name where the credentials belong`() {
        val credential = assertNotNull(
            clientSettings(
                url = "localhost:27017",
                username = "root",
                password = "secret",
                properties = mapOf(MongoConnection.AUTH_DATABASE_PROPERTY to "admin")
            ).credential
        )

        assertEquals("admin", credential.source)
    }

    @Test
    fun `the database is the declared one, else the url's, else the default`() {
        assertEquals(
            "declared",
            MongoConnection.database(mapOf(MongoConnection.DATABASE_PROPERTY to "declared"), "localhost:27017")
        )
        assertEquals("logs", MongoConnection.database(emptyMap(), "mongodb://localhost:27017/logs"))
        assertEquals(MongoConnection.DEFAULT_DATABASE, MongoConnection.database(emptyMap(), "localhost:27017"))
        assertEquals(
            "declared",
            MongoConnection.database(
                mapOf(MongoConnection.DATABASE_PROPERTY to "declared"),
                "mongodb://localhost:27017/logs"
            )
        )
    }

    @Test
    fun `a statement is given thirty seconds unless the connection says otherwise`() {
        assertEquals(30, MongoConnection.statementTimeoutSeconds(emptyMap()))
        assertEquals(15, MongoConnection.statementTimeoutSeconds(mapOf("statement timeout" to "15")))
        assertEquals(0, MongoConnection.statementTimeoutSeconds(mapOf("statement timeout" to "0")))
    }

    @Test
    fun `a statement timeout that is not a number of seconds is refused`() {
        val notANumber = assertFailsWith<IllegalArgumentException> {
            MongoConnection.statementTimeoutSeconds(mapOf("statement timeout" to "soon"))
        }
        assertEquals(
            "Connection property 'statement timeout' must be a whole number of seconds, but was 'soon'.",
            notANumber.message
        )

        val negative = assertFailsWith<IllegalArgumentException> {
            MongoConnection.statementTimeoutSeconds(mapOf("statement timeout" to "-1"))
        }
        assertEquals(
            "Connection property 'statement timeout' must not be negative, but was -1.",
            negative.message
        )
    }

    @Test
    fun `the driver is told how long a statement may take`() {
        assertEquals(30_000, settings().socketSettings.getReadTimeout(TimeUnit.MILLISECONDS))
        assertEquals(
            15_000,
            settings(properties = mapOf("statement timeout" to "15")).socketSettings.getReadTimeout(TimeUnit.MILLISECONDS)
        )
        assertEquals(
            0,
            settings(properties = mapOf("statement timeout" to "0")).socketSettings.getReadTimeout(TimeUnit.MILLISECONDS),
            "zero leaves the driver waiting, which is what it does by default"
        )
    }

    private fun clientSettings(
        url: String,
        username: String = "",
        password: String = "",
        properties: Map<String, String> = emptyMap()
    ) = MongoConnection.clientSettings(
        ConnectionSettings(url, username, password),
        MongoConnection.database(properties, url),
        properties
    )

    private fun settings(
        url: String = "localhost:27017",
        properties: Map<String, String> = emptyMap()
    ) = clientSettings(url = url, properties = properties)
}
