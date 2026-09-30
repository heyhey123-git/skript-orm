package io.github.heyhey123.skriptorm.table

import io.github.heyhey123.skriptorm.type.BigIntDataType
import io.github.heyhey123.skriptorm.type.IntDataType
import io.github.heyhey123.skriptorm.type.StringDataType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

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
                    Column("id", IntDataType())
                )
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
                    Column("external_id", StringDataType(), isPrimaryKey = true)
                )
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
            size = 32
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

    @Test
    fun `a declaration matches the same columns in any order`() {
        val declaration = Table(
            "users",
            listOf(
                Column("id", BigIntDataType(), isPrimaryKey = true, isAutoIncrement = true, isNullable = false),
                Column("name", StringDataType(), isNullable = false, size = 64)
            )
        )
        val same = Table(
            "users",
            listOf(
                Column("name", StringDataType(), isNullable = false, size = 64),
                Column("id", BigIntDataType(), isPrimaryKey = true, isAutoIncrement = true, isNullable = false)
            )
        )

        assertTrue(same.sameDeclarationAs(declaration), "column order is not part of a declaration")
    }

    @Test
    fun `a declaration differs when any part of the table differs`() {
        val registered = Table(
            "users",
            listOf(
                Column("id", IntDataType(), isPrimaryKey = true, isNullable = false),
                Column("name", StringDataType(), size = 64)
            )
        )
        val differences = mapOf(
            "another table name" to Table("customers", registered.columns.values.toList()),
            "a different column name" to Table(
                "users",
                listOf(registered.getColumnByName("id")!!, Column("label", StringDataType(), size = 64))
            ),
            "a different type" to Table(
                "users",
                listOf(registered.getColumnByName("id")!!, Column("name", BigIntDataType(), size = 64))
            ),
            "a different size" to Table(
                "users",
                listOf(registered.getColumnByName("id")!!, Column("name", StringDataType()))
            ),
            "a different nullability" to Table(
                "users",
                listOf(Column("id", IntDataType(), isPrimaryKey = true), Column("name", StringDataType(), size = 64))
            ),
            "a different primary key" to Table(
                "users",
                listOf(Column("id", IntDataType(), isNullable = false), Column("name", StringDataType(), size = 64))
            ),
            "an extra column" to Table(
                "users",
                listOf(
                    registered.getColumnByName("id")!!,
                    registered.getColumnByName("name")!!,
                    Column("age", IntDataType())
                )
            ),
            "a missing column" to Table("users", listOf(registered.getColumnByName("id")!!))
        )

        differences.forEach { (reason, declared) ->
            assertFalse(declared.sameDeclarationAs(registered), reason)
        }
    }
}
