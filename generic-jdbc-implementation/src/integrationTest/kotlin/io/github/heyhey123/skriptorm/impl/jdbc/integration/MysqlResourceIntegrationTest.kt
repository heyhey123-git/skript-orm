package io.github.heyhey123.skriptorm.impl.jdbc.integration

import io.github.heyhey123.skriptorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.ValueConverter
import kotlinx.coroutines.runBlocking
import java.sql.Blob
import java.sql.JDBCType
import java.sql.SQLException
import javax.sql.rowset.serial.SerialBlob
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Checks that every code path returns its connection to the pool and frees the temporary JDBC
 * values it created. Hikari's own metrics make a leak visible as a checked-out connection that
 * never comes back, which a mocked `DataSource` cannot show.
 */
class MysqlResourceIntegrationTest : MysqlIntegrationTestBase() {

    @Test
    fun `an open cursor holds its connection until it is closed`() = runBlocking<Unit> {
        recreateTable()
        queries.insertOne(userValues(id = 1, name = "held")).execute(usersTable)
        awaitIdleConnections()

        val result = queries.selectMany(null).execute(usersTable)
        assertEquals(1, pool.activeConnections, "an unclosed cursor must keep its connection checked out")
        assertTrue(result.cursor.next())

        result.cursor.close()
        awaitIdleConnections()
    }

    @Test
    fun `closing a cursor early still returns the connection`() = runBlocking<Unit> {
        recreateTable()
        queries.insertMany(
            listOf(
                userValues(id = 1, name = "a"),
                userValues(id = 2, name = "b"),
                userValues(id = 3, name = "c")
            )
        ).execute(usersTable)

        val result = queries.selectMany(null).execute(usersTable)
        assertTrue(result.cursor.next())
        result.cursor.close()

        awaitIdleConnections()
    }

    @Test
    fun `repeated reads return every connection to the pool`() = runBlocking<Unit> {
        recreateTable()
        queries.insertOne(userValues(id = 1, name = "loop")).execute(usersTable)

        repeat(25) {
            assertEquals(1, queries.selectMany(null).execute(usersTable).readUsers().size)
        }

        awaitIdleConnections()
    }

    @Test
    fun `repeated writes return every connection to the pool`() = runBlocking<Unit> {
        recreateTable()

        repeat(25) { index ->
            queries.insertOne(userValues(id = index + 1, name = "row-$index")).execute(usersTable)
        }

        assertEquals(25L, rawRowCount())
        awaitIdleConnections()
    }

    @Test
    fun `statement failures still return the connection`() = runBlocking<Unit> {
        recreateTable()

        repeat(5) {
            // Fails on the server: name is NOT NULL.
            assertFailsWith<SQLException> {
                queries.insertOne(linkedMapOf<String, Any?>("name" to null)).execute(usersTable)
            }
        }

        awaitIdleConnections()
        queries.insertOne(userValues(id = 1, name = "after-failure")).execute(usersTable)
        assertEquals(1L, rawRowCount())
    }

    @Test
    fun `binding failures still return the connection`() = runBlocking<Unit> {
        recreateTable()

        repeat(5) {
            // Fails before touching the server: the table has no such column.
            assertFailsWith<IllegalArgumentException> {
                queries.insertOne(linkedMapOf<String, Any?>("missing" to 1)).execute(usersTable)
            }
        }

        awaitIdleConnections()
        queries.insertOne(userValues(id = 1, name = "after-failure")).execute(usersTable)
        assertEquals(1L, rawRowCount())
    }

    @Test
    fun `batch failures still return the connection`() = runBlocking<Unit> {
        recreateTable()
        queries.insertOne(userValues(id = 1, name = "taken")).execute(usersTable)

        assertFailsWith<SQLException> {
            queries.insertMany(
                listOf(userValues(id = 1, name = "duplicate"), userValues(id = 2, name = "ok"))
            ).execute(usersTable)
        }

        awaitIdleConnections()
        // Whether MySQL keeps the statements that ran before the failure is driver behaviour, so the
        // assertions stay on what this test is about: the pool survives and stays usable.
        assertEquals(1L, queryLong("SELECT COUNT(*) FROM `users` WHERE `id` = 1"))
        queries.insertOne(userValues(id = 3, name = "after-failure")).execute(usersTable)
        assertEquals(1L, queryLong("SELECT COUNT(*) FROM `users` WHERE `id` = 3"))
    }

    @Test
    fun `temporary blobs are freed after a write`() = runBlocking<Unit> {
        val blobs = Table(
            "blobs",
            listOf(
                Column("id", IntJdbcDataType(), isPrimaryKey = true, isNullable = false),
                Column("payload", BlobJdbcDataType())
            )
        )
        recreateTable(blobs)
        RecordedBlobs.clear()

        queries.insertOne(linkedMapOf<String, Any?>("id" to 1, "payload" to byteArrayOf(1, 2, 3)))
            .execute(blobs)

        // The implementation owns the converted Blob and must free it once the write completes.
        val created = RecordedBlobs.single()
        assertFailsWith<SQLException> { created.length() }

        val stored = dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT `payload` FROM `blobs`").use { rows ->
                    assertTrue(rows.next(), "the blob row must exist")
                    rows.getBytes(1)
                }
            }
        }
        assertContentEquals(byteArrayOf(1, 2, 3), stored)
    }
}

/** Collects every Blob the converter creates so a test can assert that it was freed. */
private object RecordedBlobs {
    private val created = mutableListOf<Blob>()

    fun record(blob: Blob) {
        synchronized(created) { created.add(blob) }
    }

    fun clear() {
        synchronized(created) { created.clear() }
    }

    fun single(): Blob = synchronized(created) { created.single() }
}

/** A Blob-backed type that does not depend on Bukkit serialization. */
private object RecordingBlobConverter : ValueConverter<ByteArray, Blob>(ByteArray::class.java, Blob::class.java) {

    override fun toStorage(value: ByteArray): Blob = SerialBlob(value).also { RecordedBlobs.record(it) }

    override fun fromStorage(value: Blob): ByteArray = value.binaryStream.use { it.readBytes() }
}

private class BlobJdbcDataType : JdbcDataType<ByteArray> {

    override val domainType: Class<ByteArray> = ByteArray::class.java
    override val typeCode: String = "bytes"
    override val jdbcType: JDBCType = JDBCType.BLOB
    override val storageName: String = "BLOB"
    override val converter: ValueConverter<ByteArray, Blob> = RecordingBlobConverter
}
