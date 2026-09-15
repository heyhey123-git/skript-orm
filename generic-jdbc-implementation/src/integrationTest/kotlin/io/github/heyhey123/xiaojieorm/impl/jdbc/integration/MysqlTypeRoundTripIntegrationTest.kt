package io.github.heyhey123.xiaojieorm.impl.jdbc.integration

import io.github.heyhey123.xiaojieorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.UuidJdbcDataType
import kotlinx.coroutines.runBlocking
import java.sql.SQLException
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Proves that each driver-independent JDBC type survives a full round trip through the converter,
 * the prepared statement, MySQL storage, and the typed cursor read. Mocked tests stop at the
 * converter boundary, so they cannot catch a type that MySQL normalises differently than assumed.
 */
class MysqlTypeRoundTripIntegrationTest : MysqlIntegrationTestBase() {

    @Test
    fun `extreme scalar values round trip exactly`() = runBlocking<Unit> {
        recreateTable()
        val uid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

        queries.insertOne(
            userValues(
                id = 1,
                name = "scalars",
                age = Int.MIN_VALUE,
                score = -1.5,
                ratio = 2.25f,
                big = Long.MAX_VALUE,
                tiny = Byte.MIN_VALUE,
                active = true,
                uid = uid
            )
        ).execute(usersTable)

        val row = allUsers().single()
        assertEquals(Int.MIN_VALUE, row.age)
        assertEquals(-1.5, row.score)
        assertEquals(2.25f, row.ratio)
        assertEquals(Long.MAX_VALUE, row.big)
        assertEquals(Byte.MIN_VALUE, row.tiny)
        assertEquals(true, row.active)
        assertEquals(uid, row.uid)
    }

    @Test
    fun `explicit sql null binding reads back as null`() = runBlocking<Unit> {
        recreateTable()

        // userValues always writes every nullable column, so this binds SQL NULL rather than
        // relying on the column default.
        queries.insertOne(userValues(name = "nulls")).execute(usersTable)

        val row = allUsers().single()
        assertEquals(1, row.id)
        assertNull(row.age)
        assertNull(row.score)
        assertNull(row.ratio)
        assertNull(row.big)
        assertNull(row.tiny)
        assertNull(row.active)
        assertNull(row.uid)
    }

    @Test
    fun `false and null are distinguishable for a nullable boolean`() = runBlocking<Unit> {
        recreateTable()
        queries.insertMany(
            listOf(
                userValues(id = 1, name = "false", active = false),
                userValues(id = 2, name = "null", active = null),
                userValues(id = 3, name = "true", active = true)
            )
        ).execute(usersTable)

        val rows = allUsers().associateBy { it.id }
        assertEquals(false, rows.getValue(1).active)
        assertNull(rows.getValue(2).active)
        assertEquals(true, rows.getValue(3).active)
    }

    @Test
    fun `strings round trip including empty and unicode values`() = runBlocking<Unit> {
        recreateTable()
        val unicode = "数据库 🚀 ünïcödé"

        queries.insertMany(
            listOf(
                userValues(id = 1, name = ""),
                userValues(id = 2, name = unicode)
            )
        ).execute(usersTable)

        assertEquals(listOf("", unicode), allUsers().map { it.name })
    }

    @Test
    fun `the declared varchar size is enforced by mysql`() = runBlocking<Unit> {
        recreateTable()
        val atLimit = "x".repeat(100)

        queries.insertOne(userValues(id = 1, name = atLimit)).execute(usersTable)
        assertEquals(atLimit, allUsers().single().name)

        assertFailsWith<SQLException> {
            queries.insertOne(userValues(id = 2, name = "x".repeat(101))).execute(usersTable)
        }
    }

    @Test
    fun `uuid values are stored as exactly sixteen bytes`() = runBlocking<Unit> {
        recreateTable()
        val uid = UUID.randomUUID()

        queries.insertOne(userValues(id = 1, name = "uuid", uid = uid)).execute(usersTable)

        assertEquals(uid, allUsers().single().uid)
        assertEquals(16L, queryLong("SELECT LENGTH(`uid`) FROM `users`"))
    }

    @Test
    fun `values can also be read by one based column index`() = runBlocking<Unit> {
        recreateTable()
        val uid = UUID.randomUUID()
        queries.insertOne(userValues(id = 1, name = "indexed", age = 42, uid = uid)).execute(usersTable)

        val read = queries.selectMany(null).execute(usersTable).cursor.use { cursor ->
            assertTrue(cursor.next(), "the seeded row must be readable")
            Triple(
                cursor.get(1, IntJdbcDataType()),
                cursor.get(2, StringJdbcDataType()),
                cursor.get(9, UuidJdbcDataType())
            )
        }

        // SELECT * returns columns in declaration order: id, name, age, score, ratio, big, tiny, active, uid.
        assertEquals(Triple(1, "indexed", uid), read)
    }
}
