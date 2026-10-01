package io.github.heyhey123.skriptorm.impl.jdbc.database

import io.github.heyhey123.skriptorm.impl.jdbc.type.BigIntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.BooleanJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Covers the comparison registration makes between the declared table and the one the server holds.
 *
 * The failure this exists for is `CREATE TABLE IF NOT EXISTS` doing nothing to a table that is already
 * there: the declaration moves on, the table does not, and the first statement to name the new column is
 * refused by the server with a message about that statement. Everything here is about that comparison
 * naming the difference instead, and about it staying quiet for the parts the drivers report differently
 * without meaning anything is wrong.
 */
class SchemaVerificationTest {

    @Test
    fun `a table that matches is accepted`() {
        assertEquals(emptyList(), SchemaVerification.compare(users, actual(users)))
    }

    @Test
    fun `a column the declaration has and the table does not is reported with both sides`() {
        val mismatches = SchemaVerification.compare(
            users,
            SchemaVerification.ActualTable(
                columns = listOf(
                    column("id", "BIGINT", nullable = false, size = 19),
                    column("name", "VARCHAR", nullable = false, size = 64)
                ),
                primaryKey = setOf("id")
            )
        )

        assertEquals(1, mismatches.size)
        assertTrue("'age'" in mismatches.single(), mismatches.single())
        assertTrue("'id', 'name'" in mismatches.single(), mismatches.single())
    }

    /**
     * The two directions are not symmetrical. A declaration asks for the columns it names to be there with the
     * storage it named, and asks for nothing else, so a table carrying a column the declaration never
     * mentions still runs every statement the script can write: a table shared with another tool, or one an
     * older declaration of the same script created. The key is the other way round, and is checked as such.
     */
    @Test
    fun `a column the table has and the declaration does not is accepted`() {
        val mismatches = SchemaVerification.compare(
            users,
            SchemaVerification.ActualTable(
                columns = actual(users).columns + column("legacy", "INT", nullable = true, size = 10),
                primaryKey = setOf("id")
            )
        )

        assertEquals(emptyList(), mismatches)
    }

    @Test
    fun `a type the table holds differently is reported`() {
        val mismatches = SchemaVerification.compare(
            users,
            actual(users, "age" to column("age", "VARCHAR", nullable = true, size = 10))
        )

        assertEquals(1, mismatches.size)
        assertTrue("'age' is declared as INT but the table holds VARCHAR" in mismatches.single())
    }

    @Test
    fun `a size the table holds differently is reported`() {
        val mismatches = SchemaVerification.compare(
            users,
            actual(users, "name" to column("name", "VARCHAR", nullable = false, size = 32))
        )

        assertEquals(1, mismatches.size)
        assertTrue("size of 64 but the table holds 32" in mismatches.single(), mismatches.single())
    }

    @Test
    fun `a not null column the table allows null in is reported`() {
        val mismatches = SchemaVerification.compare(
            users,
            actual(users, "name" to column("name", "VARCHAR", nullable = true, size = 64))
        )

        assertEquals(1, mismatches.size)
        assertTrue("'name' is declared 'not null' but the table allows null" in mismatches.single())
    }

    /**
     * A key is not null on every server, whatever the declaration says, and the drivers disagree about how
     * they report an identity column. Comparing it would refuse a table that is exactly what was asked for.
     */
    @Test
    fun `a primary key the server made not null is not a difference`() {
        val declaredNullable = Table(
            "keyed",
            listOf(Column("id", BigIntJdbcDataType(), isPrimaryKey = true, isAutoIncrement = true))
        )

        assertEquals(
            emptyList(),
            SchemaVerification.compare(
                declaredNullable,
                SchemaVerification.ActualTable(
                    columns = listOf(column("id", "BIGINT", nullable = false, size = 19)),
                    primaryKey = setOf("id")
                )
            )
        )
    }

    @Test
    fun `a primary key the table does not have is reported`() {
        val mismatches = SchemaVerification.compare(
            users,
            SchemaVerification.ActualTable(columns = actual(users).columns, primaryKey = emptySet())
        )

        assertEquals(1, mismatches.size)
        assertTrue("primary key is declared as 'id' but the table holds none" in mismatches.single())
    }

    /**
     * The direction that is not safe: a table keyed on a column the declaration leaves out means the declared
     * key can name several rows, so `update one` would write every one of them.
     */
    @Test
    fun `a table keyed on a column the declaration does not key on is reported`() {
        val mismatches = SchemaVerification.compare(
            users,
            SchemaVerification.ActualTable(columns = actual(users).columns, primaryKey = setOf("id", "name"))
        )

        assertEquals(1, mismatches.size)
        assertTrue("keys on 'name'" in mismatches.single(), mismatches.single())
        assertTrue("can match more than one" in mismatches.single(), mismatches.single())
    }

    /**
     * A keyless declaration promises nothing about identity, so a key the table carries is not a difference:
     * the declaration cannot address a row by key, and every statement it allows still runs.
     */
    @Test
    fun `a keyless declaration is not compared against the table's key`() {
        val keyless = Table(
            "users",
            listOf(
                Column("id", BigIntJdbcDataType(), isNullable = false),
                Column("name", StringJdbcDataType(), isNullable = false, size = 64),
                Column("age", IntJdbcDataType())
            )
        )

        assertEquals(emptyList(), SchemaVerification.compare(keyless, actual(keyless)))
    }

    /**
     * A table the server does not hold reads as one with no columns, so a script meets the missing-column
     * report rather than a second message shape. The server says nothing about having no columns; the
     * declaration is what lists them, which is the side the script can act on.
     */
    @Test
    fun `a table that is not there reports every column as missing`() {
        val mismatches = SchemaVerification.compare(
            users,
            SchemaVerification.ActualTable(emptyList(), emptySet())
        )

        assertEquals(2, mismatches.size)
        assertTrue("'id', 'name', 'age'" in mismatches[0], mismatches[0])
        assertTrue("The table holds: ." in mismatches[0], mismatches[0])
        assertTrue("The primary key is declared as 'id' but the table holds none" in mismatches[1])
    }

    @Test
    fun `the shared table answers the names that are no product's own`() {
        assertEquals("INTEGER", SchemaVerification.normalizeTypeName("INT"))
        assertEquals("VARCHAR", SchemaVerification.normalizeTypeName("character varying"))
        assertEquals("VARCHAR", SchemaVerification.normalizeTypeName("VARCHAR(64)"))
        assertEquals("VARCHAR", SchemaVerification.normalizeTypeName("text"))
        assertEquals("VARBINARY", SchemaVerification.normalizeTypeName("binary varying"))
        assertEquals("BLOB", SchemaVerification.normalizeTypeName("BINARY LARGE OBJECT"))
    }

    /**
     * `LONGVARCHAR` is not the blob storage it was read as: JDBC defines it as long character data, H2 lists
     * it among the names of its `VARCHAR`, and MySQL's mapping turns a declared `LONG VARCHAR` into a
     * `MEDIUMTEXT`. It is a text spelling wherever it appears, and only a server that keeps a declared name
     * — SQLite — reports it, which is why its dialect answers it and the shared list does not.
     */
    @Test
    fun `a long text name is text and not the blob storage`() {
        assertEquals("LONGVARCHAR", SchemaVerification.normalizeTypeName("longvarchar"))
        assertEquals("LONGVARCHAR", SchemaVerification.normalizeTypeName("longvarchar", MysqlJdbcDialect.typeAliases))
        assertEquals("VARCHAR", SchemaVerification.normalizeTypeName("longvarchar", GenericJdbcDialect.typeAliases))
        assertEquals("BLOB", SchemaVerification.normalizeTypeName("longvarbinary", GenericJdbcDialect.typeAliases))
    }

    /**
     * A name one product's driver reports is that product's dialect's entry rather than a shared one: `int4`
     * is a four-byte integer to PostgreSQL's driver and nothing in particular to another server, `bit` is the
     * one-byte boolean to the MySQL family and a bit string to PostgreSQL, and `tinytext` is a MySQL spelling
     * no other server has. Read without a dialect they are compared as they stand, which is what leaves each
     * product's answer to be measured against its own server instead of guessed for all of them.
     */
    @Test
    fun `a name one product's driver reports is not answered without that dialect`() {
        assertEquals("INT4", SchemaVerification.normalizeTypeName("int4"))
        assertEquals("INT8", SchemaVerification.normalizeTypeName("int8"))
        assertEquals("FLOAT4", SchemaVerification.normalizeTypeName("float4"))
        assertEquals("BOOL", SchemaVerification.normalizeTypeName("bool"))
        assertEquals("BIT", SchemaVerification.normalizeTypeName("bit"))
        assertEquals("BYTEA", SchemaVerification.normalizeTypeName("bytea"))
        assertEquals("TINYTEXT", SchemaVerification.normalizeTypeName("tinytext"))
        assertEquals("TINYINT", SchemaVerification.normalizeTypeName("tinyint"))
    }

    @Test
    fun `a name that is not an alias is compared as it is`() {
        assertEquals("GEOMETRY", SchemaVerification.normalizeTypeName("geometry"))
        assertTrue(
            SchemaVerification.normalizeTypeName("int8") != SchemaVerification.normalizeTypeName("geometry")
        )
    }

    /**
     * A spelling that belongs to one product is that dialect's to declare, and the comparison is told it. The
     * one measured so far is the boolean column: MySQL stores `BOOLEAN` as `TINYINT(1)`, and a connection URL
     * carrying `tinyInt1isBit=false` makes the driver report it as `TINYINT` — the same name and the same size
     * a column declared `tinyint` has, so nothing in the metadata tells the two apart. H2, where `TINYINT` and
     * `BOOLEAN` are two different types, must not inherit the answer, which is what passing the mapping in
     * rather than keeping it beside the comparison gets. What `MysqlJdbcDialect` declares there is what the
     * JDBC tests read back from a real server.
     */
    @Test
    fun `a spelling the dialect owns is compared through the dialect`() {
        val flags = Table(
            "flags",
            listOf(
                Column("id", BigIntJdbcDataType(), isPrimaryKey = true, isAutoIncrement = true, isNullable = false),
                Column("active", BooleanJdbcDataType(), isNullable = false)
            )
        )
        val reported = actual(
            flags,
            "active" to SchemaVerification.ActualColumn("active", "TINYINT", nullable = false, size = null)
        )

        assertEquals(1, SchemaVerification.compare(flags, reported).size)
        assertEquals(emptyList(), SchemaVerification.compare(flags, reported, MysqlJdbcDialect.typeAliases))
        assertEquals("BOOLEAN", SchemaVerification.normalizeTypeName("tinyint", MysqlJdbcDialect.typeAliases))
        assertEquals("TINYINT", SchemaVerification.normalizeTypeName("tinyint"))
    }

    /**
     * The failure continuous integration found, pinned: MySQL has a `performance_schema` holding a table
     * for each instrument, and one of them is `USER`. Searching for `users` with no catalog pattern
     * searches every schema, so that table was taken for the registered one, and every declared column was
     * reported missing from a table the script had never named.
     */
    @Test
    fun `a table of another schema is not taken for the registered one`() {
        val metadata = FakeMetadata(
            catalog = "skriptorm_test",
            tables = listOf(
                FakeTable("performance_schema", null, "USER"),
                FakeTable("skriptorm_test", null, "users")
            ),
            columns = listOf(
                FakeColumn("users", "id", "BIGINT", false, 19),
                FakeColumn("users", "name", "VARCHAR", false, 64),
                FakeColumn("users", "age", "INT", true, 10)
            ),
            primaryKey = listOf("users" to "id")
        )

        val actual = SchemaVerification.readWith(metadata.metadata, "users")

        assertEquals(listOf("id", "name", "age"), actual.columns.map { it.name })
        assertEquals(setOf("id"), actual.primaryKey)
        assertTrue(
            SchemaVerification.compare(users, actual).isEmpty(),
            "the declared table is the one the catalog holds: ${SchemaVerification.compare(users, actual)}"
        )
        assertEquals(
            listOf<String?>("skriptorm_test"),
            metadata.catalogPatterns,
            "the lookup must be confined to the connection's own catalog, or another schema's table answers"
        )
    }

    /**
     * A server that stores the name in another case is still the registered table. The metadata call asks
     * for the name as written and MySQL on a case-sensitive filesystem compares it as written, so `users`
     * does not find a table the server hands over as `USERS` — and the answer must still be the one in the
     * connection's own catalog, not the first match anywhere.
     */
    @Test
    fun `a table the server stores under another case is found in its own catalog`() {
        val metadata = FakeMetadata(
            catalog = "skriptorm_test",
            tables = listOf(
                FakeTable("performance_schema", null, "USER"),
                FakeTable("skriptorm_test", null, "USERS")
            ),
            columns = listOf(
                FakeColumn("USERS", "id", "BIGINT", false, 19),
                FakeColumn("USERS", "NAME", "VARCHAR", false, 64)
            ),
            primaryKey = listOf("USERS" to "id")
        )

        val actual = SchemaVerification.readWith(metadata.metadata, "users")

        assertEquals(listOf("id", "NAME"), actual.columns.map { it.name })
        assertEquals(listOf<String?>("skriptorm_test"), metadata.catalogPatterns)
    }

    /**
     * A table that is not where the connection can see it ends the registration, and the message names what
     * is there. Reading *some* table's columns instead would report a column difference in a table nobody
     * identified: the script would be told its `age` column is missing from a table it had never named,
     * which is what the first continuous-integration round reported about `performance_schema`.
     */
    @Test
    fun `a table that does not exist where the connection looks is refused, naming what is there`() {
        val metadata = FakeMetadata(
            catalog = "sicilia_db",
            tables = listOf(
                FakeTable("sicilia_db", null, "players"),
                FakeTable("sicilia_db", null, "user_settings")
            ),
            columns = listOf(FakeColumn("players", "id", "INT", false, 10)),
            primaryKey = listOf("players" to "id")
        )

        val thrown = assertFailsWith<IllegalArgumentException> {
            SchemaVerification.readWith(metadata.metadata, "users")
        }

        val message = thrown.message.orEmpty()
        assertTrue("Registered table 'users' does not exist in database 'sicilia_db'" in message, message)
        assertTrue("'players'" in message && "'user_settings'" in message, message)
    }

    private fun actual(
        table: Table,
        vararg replaced: Pair<String, SchemaVerification.ActualColumn>
    ): SchemaVerification.ActualTable {
        val replacements = replaced.toMap()
        val columns = table.columns.values.map { declared ->
            replacements[declared.name] ?: SchemaVerification.ActualColumn(
                name = declared.name,
                typeName = typeNameOf(declared),
                nullable = declared.isNullable,
                size = declared.size
            )
        }
        return SchemaVerification.ActualTable(columns, setOf("id"))
    }

    private fun typeNameOf(column: Column<*>): String =
        (column.type as JdbcDataType<*>).storageName

    private fun column(name: String, typeName: String, nullable: Boolean, size: Int?) =
        SchemaVerification.ActualColumn(name, typeName, nullable, size)

    private companion object {

        /** The registration from the reported bug: a key, a name and an age. */
        val users = Table(
            "users",
            listOf(
                Column("id", BigIntJdbcDataType(), isPrimaryKey = true, isAutoIncrement = true, isNullable = false),
                Column("name", StringJdbcDataType(), isNullable = false, size = 64),
                Column("age", IntJdbcDataType())
            )
        )
    }
}
