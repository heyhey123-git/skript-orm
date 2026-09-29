package io.github.heyhey123.skriptorm.impl.jdbc.database

import io.github.heyhey123.skriptorm.impl.jdbc.type.BigIntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import kotlin.test.Test
import kotlin.test.assertEquals
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

    @Test
    fun `a column the table has and the declaration does not is reported`() {
        val mismatches = SchemaVerification.compare(
            users,
            SchemaVerification.ActualTable(
                columns = actual(users).columns + column("legacy", "INT", nullable = true, size = 10),
                primaryKey = setOf("id")
            )
        )

        assertEquals(1, mismatches.size)
        assertTrue("'legacy'" in mismatches.single(), mismatches.single())
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
    fun `the aliases the two servers use stand for the same storage`() {
        assertEquals("BIGINT", SchemaVerification.normalizeTypeName("int8"))
        assertEquals("INTEGER", SchemaVerification.normalizeTypeName("INT"))
        assertEquals("INTEGER", SchemaVerification.normalizeTypeName("int4"))
        assertEquals("SMALLINT", SchemaVerification.normalizeTypeName("int2"))
        assertEquals("REAL", SchemaVerification.normalizeTypeName("float4"))
        assertEquals("DOUBLE PRECISION", SchemaVerification.normalizeTypeName("float8"))
        assertEquals("BOOLEAN", SchemaVerification.normalizeTypeName("bool"))
        // What MySQL's driver reports for the `BOOLEAN` column the dialect wrote, which is `TINYINT(1)`.
        assertEquals("BOOLEAN", SchemaVerification.normalizeTypeName("bit"))
        assertEquals("VARCHAR", SchemaVerification.normalizeTypeName("character varying"))
        assertEquals("VARCHAR", SchemaVerification.normalizeTypeName("VARCHAR(64)"))
        assertEquals("BLOB", SchemaVerification.normalizeTypeName("bytea"))
        assertEquals("BLOB", SchemaVerification.normalizeTypeName("BINARY LARGE OBJECT"))
    }

    @Test
    fun `a name that is not an alias is compared as it is`() {
        assertEquals("GEOMETRY", SchemaVerification.normalizeTypeName("geometry"))
        assertTrue(
            SchemaVerification.normalizeTypeName("int8") != SchemaVerification.normalizeTypeName("geometry")
        )
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
