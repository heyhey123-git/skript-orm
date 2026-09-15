package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.condition.Condition
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.GenericJdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.MysqlJdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.table.Table
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class JdbcConcreteQueriesTest {

    private val table = Table(
        "users",
        listOf(
            Column("id", IntJdbcDataType(), isPrimaryKey = true),
            Column("name", StringJdbcDataType()),
            Column("age", IntJdbcDataType())
        )
    )
    private val noPrimaryKey = Table("users", listOf(Column("age", IntJdbcDataType())))

    @Test
    fun `select queries render sql and bind filters in order`() = runBlocking {
        val fixture = cursorFixture()
        val where = WhereClause.All(false, listOf(Condition.Equals("age", 18), Condition.Equals("name", null)))

        JdbcSelectMany(where, fixture.dataSource, GenericJdbcDialect).execute(table).cursor.close()
        verify { fixture.connection.prepareStatement("SELECT * FROM \"users\" WHERE \"age\" = ? AND \"name\" IS NULL") }
        verify { fixture.statement.setObject(1, 18, JDBCType.INTEGER) }

        val one = cursorFixture()
        JdbcSelectOne(null, one.dataSource, GenericJdbcDialect).execute(table).cursor.close()
        verify { one.connection.prepareStatement("SELECT * FROM \"users\" FETCH FIRST 1 ROW ONLY") }

        val byId = cursorFixture()
        JdbcSelectById(7, byId.dataSource, GenericJdbcDialect).execute(table).cursor.close()
        verify { byId.connection.prepareStatement("SELECT * FROM \"users\" WHERE \"id\" = ? FETCH FIRST 1 ROW ONLY") }
        verify { byId.statement.setObject(1, 7, JDBCType.INTEGER) }
    }

    @Test
    fun `insert variants bind sparse maps in key order including null`() = runBlocking {
        val insert = updateFixture()
        JdbcInsertOne(linkedMapOf("name" to "A", "age" to null), insert.dataSource, GenericJdbcDialect).execute(table)
        verify { insert.connection.prepareStatement("INSERT INTO \"users\" (\"name\", \"age\") VALUES (?, ?)") }
        verifyOrder {
            insert.statement.setObject(1, "A", JDBCType.VARCHAR)
            insert.statement.setNull(2, JDBCType.INTEGER.vendorTypeNumber)
        }

        val absent = updateFixture()
        JdbcInsertIfAbsent(mapOf("name" to "A"), absent.dataSource, MysqlJdbcDialect).execute(table)
        verify { absent.connection.prepareStatement("INSERT IGNORE INTO `users` (`name`) VALUES (?)") }
    }

    @Test
    fun `update places set values before where values and keeps sparse columns`() = runBlocking {
        val fixture = updateFixture()
        val where = WhereClause.All(false, listOf(Condition.Equals("age", 18)))

        JdbcUpdate(linkedMapOf("name" to "B"), 2, where, fixture.dataSource, MysqlJdbcDialect).execute(table)

        verify { fixture.connection.prepareStatement("UPDATE `users` SET `name` = ? WHERE `age` = ? LIMIT 2") }
        verifyOrder {
            fixture.statement.setObject(1, "B", JDBCType.VARCHAR)
            fixture.statement.setObject(2, 18, JDBCType.INTEGER)
        }
        verify(exactly = 0) { fixture.statement.setObject(any(), any(), JDBCType.BIGINT) }
    }

    @Test
    fun `id writes bind values and primary key at their specified positions`() = runBlocking {
        val update = updateFixture()
        JdbcUpdateById(5, linkedMapOf("name" to "C", "age" to 20), update.dataSource, GenericJdbcDialect).execute(table)
        verifyOrder {
            update.statement.setObject(1, "C", JDBCType.VARCHAR)
            update.statement.setObject(2, 20, JDBCType.INTEGER)
            update.statement.setObject(3, 5, JDBCType.INTEGER)
        }

        val upsert = updateFixture()
        JdbcUpsertById(5, mapOf("name" to "C"), upsert.dataSource, MysqlJdbcDialect).execute(table)
        verifyOrder {
            upsert.statement.setObject(1, 5, JDBCType.INTEGER)
            upsert.statement.setObject(2, "C", JDBCType.VARCHAR)
        }

        val delete = updateFixture()
        JdbcDeleteById(5, delete.dataSource, GenericJdbcDialect).execute(table)
        verify { delete.connection.prepareStatement("DELETE FROM \"users\" WHERE \"id\" = ?") }
        verify { delete.statement.setObject(1, 5, JDBCType.INTEGER) }
    }

    @Test
    fun `delete renders limit and null equality consumes no placeholder`() = runBlocking {
        val fixture = updateFixture()
        val where = WhereClause.Any(false, listOf(Condition.Equals("name", null), Condition.GreaterThan("age", 10)))

        JdbcDelete(1, where, fixture.dataSource, MysqlJdbcDialect).execute(table)

        verify { fixture.connection.prepareStatement("DELETE FROM `users` WHERE `name` IS NULL OR `age` > ? LIMIT 1") }
        verify { fixture.statement.setObject(1, 10, JDBCType.INTEGER) }
    }

    @Test
    fun `pagination binds dialect order and computes long offset`() = runBlocking {
        val mysql = cursorFixture()
        JdbcSelectPage(25, 3, null, mysql.dataSource, MysqlJdbcDialect).execute(table).cursor.close()
        verifyOrder {
            mysql.statement.setObject(1, 25, JDBCType.INTEGER)
            mysql.statement.setObject(2, 50L, JDBCType.BIGINT)
        }

        val generic = cursorFixture()
        JdbcSelectPage(25, 3, null, generic.dataSource, GenericJdbcDialect).execute(table).cursor.close()
        verifyOrder {
            generic.statement.setObject(1, 50L, JDBCType.BIGINT)
            generic.statement.setObject(2, 25, JDBCType.INTEGER)
        }
    }

    @Test
    fun `concrete queries reject missing keys empty values and invalid columns`() {
        val source = mockk<DataSource>(relaxed = true)
        assertFailsWith<IllegalArgumentException> { runBlocking { JdbcSelectById(1, source, GenericJdbcDialect).execute(noPrimaryKey) } }
        assertFailsWith<IllegalArgumentException> { runBlocking { JdbcSelectPage(1, 1, null, source, GenericJdbcDialect).execute(noPrimaryKey) } }
        assertFailsWith<IllegalArgumentException> { runBlocking { JdbcInsertOne(emptyMap(), source, GenericJdbcDialect).execute(table) } }
        assertFailsWith<IllegalArgumentException> { runBlocking { JdbcInsertIfAbsent(emptyMap(), source, MysqlJdbcDialect).execute(table) } }
        assertFailsWith<IllegalArgumentException> { runBlocking { JdbcUpdate(emptyMap(), null, null, source, GenericJdbcDialect).execute(table) } }
        assertFailsWith<IllegalArgumentException> { runBlocking { JdbcUpdateById(1, emptyMap(), source, GenericJdbcDialect).execute(table) } }
        assertFailsWith<IllegalArgumentException> { runBlocking { JdbcUpsertById(1, emptyMap(), source, MysqlJdbcDialect).execute(table) } }
        assertFailsWith<IllegalArgumentException> { runBlocking { JdbcUpdateById(1, mapOf("id" to 2), source, GenericJdbcDialect).execute(table) } }
        assertFailsWith<IllegalArgumentException> { runBlocking { JdbcUpsertById(1, mapOf("id" to 2), source, MysqlJdbcDialect).execute(table) } }
        assertFailsWith<IllegalArgumentException> { runBlocking { JdbcInsertOne(mapOf("missing" to 1), source, GenericJdbcDialect).execute(table) } }
    }

    @Test
    fun `query factory returns every concrete implementation`() {
        val source = mockk<DataSource>(relaxed = true)
        val queries = JdbcQueries(source, MysqlJdbcDialect)
        assertIs<JdbcSelectById>(queries.selectById(1))
        assertIs<JdbcSelectOne>(queries.selectOne(null))
        assertIs<JdbcSelectMany>(queries.selectMany(null))
        assertIs<JdbcSelectPage>(queries.selectPage(1, 1, null))
        assertIs<JdbcInsertOne>(queries.insertOne(mapOf("id" to 1)))
        assertIs<JdbcInsertMany>(queries.insertMany(emptyList()))
        assertIs<JdbcInsertIfAbsent>(queries.insertIfAbsent(mapOf("id" to 1)))
        assertIs<JdbcUpdate>(queries.update(mapOf("age" to 1), null, null))
        assertIs<JdbcUpdateById>(queries.updateById(1, mapOf("age" to 1)))
        assertIs<JdbcUpsertById>(queries.upsertById(1, mapOf("age" to 1)))
        assertIs<JdbcDelete>(queries.delete(null, null))
        assertIs<JdbcDeleteById>(queries.deleteById(1))
    }

    private fun updateFixture(): Fixture {
        val statement = mockk<PreparedStatement>(relaxed = true)
        val connection = mockk<Connection>(relaxed = true)
        val dataSource = mockk<DataSource>()
        every { dataSource.connection } returns connection
        every { connection.prepareStatement(any()) } returns statement
        every { statement.executeLargeUpdate() } returns 1
        return Fixture(dataSource, connection, statement)
    }

    private fun cursorFixture(): Fixture {
        val fixture = updateFixture()
        every { fixture.statement.executeQuery() } returns mockk<ResultSet>(relaxed = true)
        return fixture
    }

    private data class Fixture(
        val dataSource: DataSource,
        val connection: Connection,
        val statement: PreparedStatement
    )
}
