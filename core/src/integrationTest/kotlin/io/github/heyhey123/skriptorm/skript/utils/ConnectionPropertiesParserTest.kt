package io.github.heyhey123.skriptorm.skript.utils

import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.database.DatabaseFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Covers how a `create a connection` body is read.
 *
 * The body is read through Skript's real config parser because the shape it produces is what broke
 * this: a body line arrives with the whole line as its key, so reading a property name from anything
 * other than the text before the colon declared a property actually named `url: "jdbc:..."`, found no
 * value for it, and rejected the section's own documented example.
 */
class ConnectionPropertiesParserTest : SkriptConfigTestBase() {

    @Test
    fun `the documented example is read property by property`() {
        val section = section(
            """
            create a connection to database "MySQL" with properties:
            ${TAB}url: "jdbc:mysql://localhost:3306/mydb"
            ${TAB}username: "root"
            ${TAB}password: "123456"
            """.trimIndent()
        )

        assertEquals(
            mapOf(
                "url" to "jdbc:mysql://localhost:3306/mydb",
                "username" to "root",
                "password" to "123456"
            ),
            ConnectionPropertiesParser.collectFrom(section)
        )
    }

    @Test
    fun `a value does not have to be quoted`() {
        val section = section(
            """
            create a connection to database "MySQL" with properties:
            ${TAB}url: jdbc:mysql://localhost:3306/mydb
            ${TAB}username: root
            """.trimIndent()
        )

        assertEquals(
            mapOf(
                "url" to "jdbc:mysql://localhost:3306/mydb",
                "username" to "root",
                "password" to ""
            ),
            ConnectionPropertiesParser.collectFrom(section)
        )
    }

    @Test
    fun `credentials left out become empty strings`() {
        val section = section(
            """
            create a connection to database "MySQL" with properties:
            ${TAB}url: "jdbc:mysql://localhost:3306/mydb"
            """.trimIndent()
        )

        assertEquals(
            mapOf(
                "url" to "jdbc:mysql://localhost:3306/mydb",
                "username" to "",
                "password" to ""
            ),
            ConnectionPropertiesParser.collectFrom(section)
        )
    }

    @Test
    fun `a property the implementation understands is passed through`() {
        val section = section(
            """
            create a connection to database "MySQL" with properties:
            ${TAB}url: "jdbc:mysql://localhost:3306/mydb"
            ${TAB}driver: "com.mysql.cj.jdbc.Driver"
            """.trimIndent()
        )

        assertEquals(
            mapOf(
                "url" to "jdbc:mysql://localhost:3306/mydb",
                "username" to "",
                "password" to "",
                "driver" to "com.mysql.cj.jdbc.Driver"
            ),
            ConnectionPropertiesParser.collectFrom(section)
        )
    }

    /**
     * A property name is looked up by name, and an implementation reads the names it knows, so a name
     * written with different spacing has to mean the same property rather than become a second one that
     * [ConnectionPropertiesParser.requireAcceptedBy] then refuses.
     */
    @Test
    fun `extra spaces inside a property name do not make it a different property`() {
        val section = section(
            """
            create a connection to database "MySQL" with properties:
            ${TAB}url: "jdbc:mysql://localhost:3306/mydb"
            ${TAB}statement   timeout: 5
            """.trimIndent()
        )

        assertEquals(
            mapOf(
                "url" to "jdbc:mysql://localhost:3306/mydb",
                "username" to "",
                "password" to "",
                "statement timeout" to "5"
            ),
            ConnectionPropertiesParser.collectFrom(section)
        )
    }

    @Test
    fun `a body without a url is rejected`() {
        val section = section(
            """
            create a connection to database "MySQL" with properties:
            ${TAB}username: "root"
            """.trimIndent()
        )

        val thrown = assertFailsWith<IllegalArgumentException> {
            ConnectionPropertiesParser.collectFrom(section)
        }
        assertEquals("A connection requires a url property.", thrown.message)
    }

    @Test
    fun `a property without a value is reported by name`() {
        val section = section(
            """
            create a connection to database "MySQL" with properties:
            ${TAB}url: "jdbc:mysql://localhost:3306/mydb"
            ${TAB}username:
            """.trimIndent()
        )

        val thrown = assertFailsWith<IllegalArgumentException> {
            ConnectionPropertiesParser.collectFrom(section)
        }
        assertEquals("Connection property 'username' has no value.", thrown.message)
    }

    @Test
    fun `a body that declares a block is rejected`() {
        val section = section(
            """
            create a connection to database "MySQL" with properties:
            ${TAB}url: "jdbc:mysql://localhost:3306/mydb"
            ${TAB}extra:
            ${TAB}${TAB}nested: "value"
            """.trimIndent()
        )

        val thrown = assertFailsWith<IllegalArgumentException> {
            ConnectionPropertiesParser.collectFrom(section)
        }
        assertEquals("Connection property 'extra' must be a value, not a block.", thrown.message)
    }

    /**
     * The refusal answers from what the implementation declares and from nothing else. This is the one
     * message for every name it does not read, including a name another implementation would have read:
     * an implementation that described another one's properties would have to be edited every time that
     * other one changed, to answer a question it does not own.
     */
    @Test
    fun `a property the implementation does not read is refused by name, and only by its own names`() {
        val factory = StubFactory(accepted = setOf("statement timeout"))

        val thrown = assertFailsWith<IllegalArgumentException> {
            ConnectionPropertiesParser.requireAcceptedBy(
                mapOf("url" to "jdbc:mysql://localhost:3306/mydb", "database" to "sicilia_db"),
                factory
            )
        }

        assertEquals(
            "Connection property 'database' is not read by database 'Stub'. " +
                "It reads: password, statement timeout, url, username.",
            thrown.message
        )
    }

    @Test
    fun `a property nothing reads is refused by name and the readable ones are listed`() {
        val factory = StubFactory(accepted = setOf("statement timeout", "driver"))

        val thrown = assertFailsWith<IllegalArgumentException> {
            ConnectionPropertiesParser.requireAcceptedBy(
                mapOf(
                    "url" to "jdbc:sqlite:data.db",
                    "username" to "",
                    "password" to "",
                    "databse" to "sicilia_db"
                ),
                factory
            )
        }

        assertEquals(
            "Connection property 'databse' is not read by database 'Stub'. " +
                "It reads: driver, password, statement timeout, url, username.",
            thrown.message
        )
    }

    @Test
    fun `the three properties every implementation reads are always accepted`() {
        val factory = StubFactory(accepted = emptySet())

        // No throw: url, username and password are how a connection is opened, not something an
        // implementation opts into, so a factory never has to list them.
        ConnectionPropertiesParser.requireAcceptedBy(
            mapOf("url" to "jdbc:sqlite:data.db", "username" to "root", "password" to "secret"),
            factory
        )
    }

    /**
     * An implementation with no driver, no dialect and no connection behind it: the check under test asks
     * a factory what it reads, which is a question a stub can answer where a real implementation would
     * need a server.
     */
    private class StubFactory(
        private val accepted: Set<String>
    ) : DatabaseFactory {

        override val typeName: String = "Stub"

        override val acceptedConnectionProperties: Set<String> = accepted

        override fun create(properties: Map<String, String>): Database =
            error("The stub factory never opens a connection.")
    }

    private companion object {
        const val TAB = "\t"
    }
}
