package io.github.heyhey123.skriptorm.impl.jdbc.database

import io.github.heyhey123.skriptorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.IntDataType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class JdbcDialectTest {

    @Test
    fun `generic dialect renders quoted sql`() {
        assertEquals("SELECT * FROM \"users\"", GenericJdbcDialect.select("users"))
        assertEquals("SELECT * FROM \"users\" WHERE \"id\" = ? LIMIT 1", GenericJdbcDialect.selectOne("users", "WHERE \"id\" = ?"))
        // A ceiling is written into the statement, the way a write limit is, so the rows past it never
        // leave the server.
        assertEquals("SELECT * FROM \"users\" LIMIT 25", GenericJdbcDialect.select("users", limit = 25))
        assertEquals(
            "SELECT * FROM \"users\" WHERE \"id\" = ? LIMIT 5001",
            GenericJdbcDialect.select("users", "WHERE \"id\" = ?", 5001)
        )
        assertEquals("INSERT INTO \"users\" (\"id\", \"name\") VALUES (?, ?)", GenericJdbcDialect.insert("users", listOf("id", "name")))
        assertEquals("UPDATE \"users\" SET \"name\" = ? WHERE \"id\" = ?", GenericJdbcDialect.update("users", listOf("name"), "WHERE \"id\" = ?", null))
        assertEquals("DELETE FROM \"users\" WHERE \"id\" = ?", GenericJdbcDialect.delete("users", "WHERE \"id\" = ?", null))
    }

    @Test
    fun `pagination declares exact placeholder order`() {
        val generic = GenericJdbcDialect.selectPage("users", "id")
        assertEquals("SELECT * FROM \"users\" ORDER BY \"id\" LIMIT ? OFFSET ?", generic.sql)
        assertEquals(listOf(JdbcPageParameter.LIMIT, JdbcPageParameter.OFFSET), generic.parameterOrder)

        // MySQL pages the same way, which is why it inherits this instead of overriding it.
        val mysql = MysqlJdbcDialect.selectPage("users", "id")
        assertEquals("SELECT * FROM `users` ORDER BY `id` LIMIT ? OFFSET ?", mysql.sql)
        assertEquals(listOf(JdbcPageParameter.LIMIT, JdbcPageParameter.OFFSET), mysql.parameterOrder)
    }

    @Test
    fun `mysql renders its supported write extensions`() {
        assertEquals("SELECT * FROM `users` LIMIT 1", MysqlJdbcDialect.selectOne("users"))
        // A plain insert: the duplicate key is caught by the query layer, so that a value the column
        // cannot hold still fails instead of being stored adjusted.
        assertEquals("INSERT INTO `users` (`id`) VALUES (?)", MysqlJdbcDialect.insertIfAbsent("users", listOf("id")))
        assertEquals(
            "INSERT INTO `users` (`id`, `name`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `name` = VALUES(`name`)",
            MysqlJdbcDialect.upsertById("users", "id", listOf("name"))
        )
        assertEquals("UPDATE `users` SET `name` = ? LIMIT 2", MysqlJdbcDialect.update("users", listOf("name"), null, 2))
        assertEquals("DELETE FROM `users` LIMIT 2", MysqlJdbcDialect.delete("users", null, 2))
    }

    @Test
    fun `generic dialect rejects unsupported extensions`() {
        assertFailsWith<UnsupportedOperationException> { GenericJdbcDialect.insertIfAbsent("users", listOf("id")) }
        assertFailsWith<UnsupportedOperationException> { GenericJdbcDialect.upsertById("users", "id", listOf("name")) }
        assertFailsWith<UnsupportedOperationException> { GenericJdbcDialect.update("users", listOf("name"), null, 1) }
        assertFailsWith<UnsupportedOperationException> { GenericJdbcDialect.delete("users", null, 1) }
    }

    @Test
    fun `dialect validates identifiers columns limits and pagination declaration`() {
        listOf("", "1name", "has space", "name-").forEach { invalid ->
            assertFailsWith<IllegalArgumentException> { GenericJdbcDialect.quoteIdentifier(invalid) }
        }
        assertEquals("\"用户_名\"", GenericJdbcDialect.quoteIdentifier("用户_名"))
        assertFailsWith<IllegalArgumentException> { GenericJdbcDialect.insert("users", emptyList()) }
        assertFailsWith<IllegalArgumentException> { MysqlJdbcDialect.insertIfAbsent("users", emptyList()) }
        assertFailsWith<IllegalArgumentException> { MysqlJdbcDialect.upsertById("users", "id", emptyList()) }
        assertFailsWith<IllegalArgumentException> { GenericJdbcDialect.update("users", listOf("id"), null, 0) }
        assertFailsWith<IllegalArgumentException> { GenericJdbcDialect.delete("users", null, -1) }
        assertFailsWith<IllegalArgumentException> { JdbcPageSql("sql", listOf(JdbcPageParameter.LIMIT, JdbcPageParameter.LIMIT)) }
    }

    @Test
    fun `ddl applies sizes nullability keys and mysql auto increment`() {
        val table = Table(
            "users",
            listOf(
                Column("id", IntJdbcDataType(), isPrimaryKey = true, isAutoIncrement = true),
                Column("name", StringJdbcDataType(), size = 40, isNullable = true)
            )
        )

        assertEquals(
            "CREATE TABLE IF NOT EXISTS `users` (`id` INT PRIMARY KEY AUTO_INCREMENT, `name` VARCHAR(40))",
            MysqlJdbcDialect.createTable(table)
        )
    }

    @Test
    fun `ddl rejects non jdbc types unsupported sizes and non integer auto increment`() {
        assertFailsWith<IllegalArgumentException> {
            GenericJdbcDialect.renderColumn("id", IntDataType(), null, false, false, false)
        }
        assertFailsWith<IllegalArgumentException> {
            GenericJdbcDialect.renderColumn("id", IntJdbcDataType(), 10, false, false, false)
        }
        assertFailsWith<IllegalArgumentException> {
            MysqlJdbcDialect.renderColumn("name", StringJdbcDataType(), null, false, true, true)
        }
    }

    /**
     * A type name belongs to the dialect whose server reports it, and the values are the storage names that
     * dialect's declarations land on: `float4` is a `FLOAT` to the generic dialect and a `REAL` to
     * PostgreSQL's, because those are the names their own types write, and `longvarchar` is a `VARCHAR` there
     * because it is long text rather than the bytes it was once read as. What is absent matters as much as
     * what is here — MySQL's and PostgreSQL's names must not answer for each other's servers.
     */
    @Test
    fun `each dialect carries the type names its own server reports`() {
        assertEquals("TINYINT", GenericJdbcDialect.typeAliases.getValue("INT2"))
        assertEquals("INTEGER", GenericJdbcDialect.typeAliases.getValue("INT4"))
        assertEquals("BIGINT", GenericJdbcDialect.typeAliases.getValue("INT8"))
        assertEquals("TINYINT", GenericJdbcDialect.typeAliases.getValue("SMALLSERIAL"))
        assertEquals("FLOAT", GenericJdbcDialect.typeAliases.getValue("FLOAT4"))
        assertEquals("DOUBLE", GenericJdbcDialect.typeAliases.getValue("FLOAT8"))
        assertEquals("BOOLEAN", GenericJdbcDialect.typeAliases.getValue("BOOL"))
        assertEquals("VARCHAR", GenericJdbcDialect.typeAliases.getValue("LONGVARCHAR"))
        assertEquals("BLOB", GenericJdbcDialect.typeAliases.getValue("LONGVARBINARY"))
        assertTrue("BYTEA" !in GenericJdbcDialect.typeAliases, "PostgreSQL's own name is not this dialect's")

        assertEquals("BOOLEAN", MysqlJdbcDialect.typeAliases.getValue("TINYINT"))
        assertEquals("BOOLEAN", MysqlJdbcDialect.typeAliases.getValue("BIT"))
        assertEquals("VARCHAR", MysqlJdbcDialect.typeAliases.getValue("LONGTEXT"))
        assertEquals("BLOB", MysqlJdbcDialect.typeAliases.getValue("LONGBLOB"))
        assertTrue("INT8" !in MysqlJdbcDialect.typeAliases, "PostgreSQL's driver name is not this dialect's")

        // The standard names are shared instead, and a name only one product has is answered only where it
        // was measured: `tinytext` is a MySQL spelling and nothing to the generic dialect.
        assertEquals("VARCHAR", SchemaVerification.normalizeTypeName("character varying"))
        assertEquals("VARCHAR", SchemaVerification.normalizeTypeName("tinytext", MysqlJdbcDialect.typeAliases))
        assertEquals("TINYTEXT", SchemaVerification.normalizeTypeName("tinytext"))
    }

    /**
     * A dialect maps a name into a name of its own, so every value has to be one that a declaration of that
     * dialect normalizes to. `SMALLINT` is not: the smallest integer the generic dialect declares writes
     * `TINYINT`, so an entry landing on `SMALLINT` could never match a column of any table — an alias written
     * from what the name means rather than from what the comparison does with it.
     *
     * What this cannot catch is an entry whose value is reachable and wrong, which is how `LONGVARCHAR` once
     * reached `BLOB`: only a server can say what a name means, which is what the integration tests are for.
     */
    @Test
    fun `every alias of a dialect lands on a name that dialect declares`() {
        // The storages a declaration of either dialect writes. The Bukkit-backed types cannot be built in
        // this JVM — the build file keeps those classes off this classpath on purpose — so the names they
        // write are named here instead of read from the registry the server builds.
        val declared = listOf(
            "BOOLEAN", "TINYINT", "INT", "BIGINT", "DOUBLE", "FLOAT", "VARCHAR", "BINARY", "VARBINARY",
            "BLOB", "DATE"
        )

        for (dialect in listOf(GenericJdbcDialect, MysqlJdbcDialect)) {
            val reachable = declared
                .flatMap { listOf(it, SchemaVerification.normalizeTypeName(it, dialect.typeAliases)) }
                .toSet()

            for ((name, target) in dialect.typeAliases) {
                assertTrue(
                    target in reachable,
                    "$dialect maps '$name' to '$target', which no declaration of it normalizes to"
                )
                // A value is compared as it stands from there: one table answers a name and the next is not
                // asked, so a value that is itself an alias leaves the reported side and the declared side of
                // a comparison standing on two different names. That is what the PostgreSQL job caught when
                // the shared table learned the standard spelling of `DOUBLE`.
                assertEquals(
                    target,
                    SchemaVerification.normalizeTypeName(target),
                    "$dialect maps '$name' to '$target', which another table reads further"
                )
            }
        }
    }
}
