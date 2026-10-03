package io.github.heyhey123.skriptorm.impl.jdbc.database

import io.github.heyhey123.skriptorm.impl.jdbc.type.BigIntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.BooleanJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.UuidJdbcDataType
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests schema verification when registration encounters an existing table. `CREATE TABLE IF NOT
 * EXISTS` does not update that table, so mismatches need to be reported before a query uses it.
 * Compatible differences in driver metadata should still be accepted.
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
     * A table may have extra columns supplied by another tool or an older declaration. The
     * registered columns must exist, but registration does not require an exact column set.
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

    /**
     * A variable-length column with more capacity than declared is compatible.
     */
    @Test
    fun `a column wider than the declaration asks for is accepted`() {
        assertEquals(
            emptyList(),
            SchemaVerification.compare(
                users,
                actual(users, "name" to column("name", "VARCHAR", nullable = false, size = 500))
            )
        )
    }

    /**
     * `BINARY` is fixed-width: a wider column pads the UUID, so its size must match exactly.
     */
    @Test
    fun `a wider fixed width column is not accepted for a narrower declaration`() {
        val keys = keyedTable("keys", Column("uid", UuidJdbcDataType(), isNullable = false))

        val mismatches = SchemaVerification.compare(
            keys,
            actual(keys, "uid" to column("uid", "BINARY", nullable = false, size = 32))
        )

        assertEquals(1, mismatches.size)
        assertTrue("size of 16 but the table holds 32" in mismatches.single(), mismatches.single())
    }

    /**
     * Checks each type's size rule directly: variable-length storage may be wider, while fixed-width
     * storage requires an exact match.
     */
    @Test
    fun `a storage answers for its own sizes`() {
        assertTrue(StringJdbcDataType().servesSize(storedSize = 500, declaredSize = 64))
        assertFalse(StringJdbcDataType().servesSize(storedSize = 32, declaredSize = 64))
        assertTrue(UuidJdbcDataType().servesSize(storedSize = 16, declaredSize = 16))
        assertFalse(UuidJdbcDataType().servesSize(storedSize = 32, declaredSize = 16))
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
     * Primary keys are non-null even when declared nullable. Driver metadata for identity columns
     * varies, so this difference must not fail verification.
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
     * If the table's key includes undeclared columns, the declared key may match multiple rows.
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
     * A keyless declaration makes no uniqueness claim, even if the existing table has a key.
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
     * An absent table is represented by empty metadata, which reports all declared columns as missing.
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
        assertEquals("DOUBLE", SchemaVerification.normalizeTypeName("double precision"))
    }

    /**
     * H2 reports a declared `FLOAT` as `DOUBLE PRECISION`. This alias applies only when the
     * connected database identifies itself as H2; `H2SchemaTest` verifies the behavior on a server.
     */
    @Test
    fun `a name one server gives its own storage is answered only for that server`() {
        assertEquals("DOUBLE", SchemaVerification.normalizeTypeName("float", productName = "H2"))
        assertEquals("DOUBLE", SchemaVerification.normalizeTypeName("FLOAT(24)", productName = "h2"))
        assertEquals("FLOAT", SchemaVerification.normalizeTypeName("float", productName = "MySQL"))
        assertEquals(
            "FLOAT",
            SchemaVerification.normalizeTypeName("float", MysqlJdbcDialect.typeAliases, productName = "MySQL")
        )
        assertEquals("FLOAT", SchemaVerification.normalizeTypeName("float"))
    }

    /**
     * JDBC defines `LONGVARCHAR` as character data. It belongs to the generic dialect's aliases
     * because other drivers normalize it before reporting column metadata.
     */
    @Test
    fun `a long text name is text and not the blob storage`() {
        assertEquals("LONGVARCHAR", SchemaVerification.normalizeTypeName("longvarchar"))
        assertEquals("LONGVARCHAR", SchemaVerification.normalizeTypeName("longvarchar", MysqlJdbcDialect.typeAliases))
        assertEquals("VARCHAR", SchemaVerification.normalizeTypeName("longvarchar", GenericJdbcDialect.typeAliases))
        assertEquals("BLOB", SchemaVerification.normalizeTypeName("longvarbinary", GenericJdbcDialect.typeAliases))
    }

    /**
     * Product-specific names such as PostgreSQL's `int4` and MySQL's `tinytext` remain unchanged
     * without that product's dialect. `bit` is especially ambiguous: MySQL uses it for boolean
     * storage, while PostgreSQL has a bit-string type.
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
     * MySQL may report a declared boolean column as `TINYINT`, depending on `tinyInt1isBit`.
     * The MySQL dialect accepts that alias; H2 must still distinguish `TINYINT` from `BOOLEAN`.
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
     * Restricts metadata lookup to the active catalog. An unrestricted MySQL lookup could mistake
     * `performance_schema.USER` for the registered `users` table.
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
     * Finds a differently cased table name within the active catalog, even when the driver's
     * pattern lookup is case-sensitive.
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
     * Reports a missing table with names from the active catalog instead of comparing columns from
     * an unrelated table.
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

        /** A keyed table with one column of the test's own, for a storage the `users` table does not carry. */
        fun keyedTable(name: String, column: Column<*>): Table = Table(
            name,
            listOf(
                Column("id", BigIntJdbcDataType(), isPrimaryKey = true, isAutoIncrement = true, isNullable = false),
                column
            )
        )
    }
}
