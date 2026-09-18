package io.github.heyhey123.skriptorm.impl.jdbc.database

import io.github.heyhey123.skriptorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.IntDataType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JdbcDialectTest {

    @Test
    fun `generic dialect renders quoted sql`() {
        assertEquals("SELECT * FROM \"users\"", GenericJdbcDialect.select("users"))
        assertEquals("SELECT * FROM \"users\" WHERE \"id\" = ? LIMIT 1", GenericJdbcDialect.selectOne("users", "WHERE \"id\" = ?"))
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
}
