package io.github.heyhey123.xiaojieorm.table

import io.github.heyhey123.xiaojieorm.type.IntDataType
import io.github.heyhey123.xiaojieorm.type.StringDataType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class TableTest {
    @Test
    fun `table snapshots columns in registration order`() {
        val id = Column("id", IntDataType(), isPrimaryKey = true)
        val name = Column("name", StringDataType())
        val source = mutableListOf(id, name)
        val table = Table("users", source)

        source.clear()

        assertEquals(listOf("id", "name"), table.columns.keys.toList())
        assertSame(id, table.primaryKey)
        assertSame(name, table.getColumnByName("name"))
        assertNull(table.getColumnByName("missing"))
    }

    @Test
    fun `table rejects duplicate column names`() {
        assertFailsWith<IllegalArgumentException> {
            Table(
                "users",
                listOf(
                    Column("id", IntDataType()),
                    Column("id", IntDataType()),
                ),
            )
        }
    }

    @Test
    fun `table rejects multiple primary keys`() {
        assertFailsWith<IllegalArgumentException> {
            Table(
                "users",
                listOf(
                    Column("id", IntDataType(), isPrimaryKey = true),
                    Column("external_id", StringDataType(), isPrimaryKey = true),
                ),
            )
        }
    }

    @Test
    fun `column rejects auto increment without primary key`() {
        assertFailsWith<IllegalArgumentException> {
            Column("id", IntDataType(), isAutoIncrement = true)
        }
    }

    @Test
    fun `column rejects non-positive size`() {
        assertFailsWith<IllegalArgumentException> {
            Column("name", StringDataType(), size = 0)
        }
    }

    @Test
    fun `table and column reject invalid identifiers and empty table definitions`() {
        val invalid = listOf("", "1name", "has space", "name-", ".name", "\u0301")
        invalid.forEach { name ->
            assertFailsWith<IllegalArgumentException> { Column(name, IntDataType()) }
            assertFailsWith<IllegalArgumentException> { Table(name, listOf(Column("id", IntDataType()))) }
        }
        assertFailsWith<IllegalArgumentException> { Table("empty", emptyList()) }
    }

    @Test
    fun `column preserves valid constraints and table may have no primary key`() {
        val column = Column(
            "_value2",
            StringDataType(),
            isPrimaryKey = false,
            isAutoIncrement = false,
            isNullable = false,
            size = 32,
        )
        val table = Table("values_table", listOf(column))

        assertEquals(32, column.size)
        assertEquals(false, column.isNullable)
        assertNull(table.primaryKey)
    }

    @Test
    fun `table and column accept unicode identifiers`() {
        val column = Column("显示名", StringDataType())
        val table = Table("用户表", listOf(column))

        assertSame(column, table.getColumnByName("显示名"))
    }
}
