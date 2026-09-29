package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.result.WriteResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Covers what the core promises about raw statements: which of them a backend serves is the backend's own
 * declaration, and the ones it does not serve are refused by name, with the alternative that does exist.
 *
 * No backend is named here. A refusal is built from [Queries.rawForm], so a case that asserts the wording
 * for a SQL backend and for a document backend is also what says the core does not enumerate backends: if
 * it did, a third form would have to be added to this file rather than to the implementation that has it.
 */
class RawStatementTest {

    @Test
    fun `a backend with no raw entry point refuses every raw statement and says so`() {
        val queries = NoRawQueries

        assertNull(queries.rawForm, "a backend that declares no form must not advertise one")

        val query = assertFailsWith<UnsupportedOperationException> { queries.rawQuery("SELECT 1", emptyList()) }
        val update = assertFailsWith<UnsupportedOperationException> { queries.rawUpdate("SELECT 1", emptyList()) }
        val command = assertFailsWith<UnsupportedOperationException> { queries.rawCommand("{}") }

        assertEquals(
            "Raw SQL statements are not supported by database 'NoRaw'. It takes no raw statements. " +
                "See the Raw statements page for what a raw statement does and does not guarantee.",
            query.message
        )
        assertEquals(query.message, update.message, "the two SQL forms are one capability")
        assertEquals(
            "Raw commands are not supported by database 'NoRaw'. It takes no raw statements. " +
                "See the Raw statements page for what a raw statement does and does not guarantee.",
            command.message
        )
    }

    /**
     * The mistake this refusal exists for: a command document written for a connection that takes SQL.
     */
    @Test
    fun `a SQL backend refuses a command and names the statements it does take`() {
        val thrown = assertFailsWith<UnsupportedOperationException> { SqlQueries.rawCommand("{}") }

        assertEquals(
            "Raw commands are not supported by database 'Sql'. It takes raw SQL statements instead: " +
                "'execute query …' and 'execute update …'. See the Raw statements page for what a raw " +
                "statement does and does not guarantee.",
            thrown.message
        )
    }

    /**
     * And the one on the other side: a SQL statement written for a connection that takes commands.
     */
    @Test
    fun `a document backend refuses SQL and names the command it does take`() {
        val thrown = assertFailsWith<UnsupportedOperationException> {
            DocumentQueries.rawQuery("SELECT 1", emptyList())
        }

        assertEquals(
            "Raw SQL statements are not supported by database 'Documents'. It takes raw commands instead: " +
                "'execute command \"…\"'. See the Raw statements page for what a raw statement does and " +
                "does not guarantee.",
            thrown.message
        )
    }

    @Test
    fun `a backend that serves a form returns the statement it was given`() {
        val query = SqlQueries.rawQuery("SELECT ?", listOf(1))

        assertEquals(RawForm.SQL_STATEMENT, SqlQueries.rawForm)
        assertEquals("SELECT ?", query.statement)
        assertEquals(listOf<Any?>(1), query.parameters)
        assertIs<SqlRawQuery>(query)
    }

    /**
     * The parameters are snapshotted when the statement is built, because the values arrive from the server
     * thread and are bound on another one: a list the script mutates afterwards must not change what a
     * running statement binds.
     */
    @Test
    fun `the parameters are a snapshot of the list they were given`() {
        val given = mutableListOf<Any?>(1)
        val query = SqlQueries.rawQuery("SELECT ?, ?", given)

        given += "changed"

        assertEquals(listOf<Any?>(1), query.parameters)
    }

    @Test
    fun `a command carries no parameters, because its statement is the whole command`() {
        assertEquals(emptyList(), DocumentQueries.rawCommand("{}").parameters)
    }

    @Test
    fun `every raw form a backend serves is built by the same factory`() {
        assertIs<SqlRawQuery>(SqlQueries.rawQuery("SELECT 1", emptyList()))
        assertIs<SqlRawUpdate>(SqlQueries.rawUpdate("UPDATE t SET c = 1", emptyList()))
        assertIs<DocumentRawCommand>(DocumentQueries.rawCommand("{}"))
    }
}

/**
 * A backend with no raw entry point, which is what the defaults are: it serves the declared statements and
 * refuses all three raw ones.
 */
private object NoRawQueries : Queries {

    override val typeName: String = "NoRaw"

    override fun selectById(id: Any) = unused()
    override fun selectOne(where: WhereClause?) = unused()
    override fun selectMany(where: WhereClause?) = unused()
    override fun selectPage(pageSize: Int, pageIndex: Int, where: WhereClause?) = unused()
    override fun insertOne(values: Map<String, Any?>) = unused()
    override fun insertMany(valuesList: List<Map<String, Any?>>) = unused()
    override fun insertIfAbsent(values: Map<String, Any?>) = unused()
    override fun update(values: Map<String, Any?>, limit: Int?, where: WhereClause?) = unused()
    override fun updateById(id: Any, values: Map<String, Any?>) = unused()
    override fun upsertById(id: Any, values: Map<String, Any?>) = unused()
    override fun delete(limit: Int?, where: WhereClause?) = unused()
    override fun deleteById(id: Any) = unused()
}

/**
 * A backend that takes raw SQL, which is the shape a relational implementation overrides.
 *
 * It is deliberately not named after one: this suite is about what the core promises, and a name like
 * "MySQL" here would read as if the contract mentioned a particular product.
 */
private object SqlQueries : Queries {

    override val typeName: String = "Sql"

    override val rawForm: RawForm = RawForm.SQL_STATEMENT

    override fun rawQuery(statement: String, parameters: List<Any?>) = SqlRawQuery(statement, parameters)
    override fun rawUpdate(statement: String, parameters: List<Any?>) = SqlRawUpdate(statement, parameters)

    override fun selectById(id: Any) = unused()
    override fun selectOne(where: WhereClause?) = unused()
    override fun selectMany(where: WhereClause?) = unused()
    override fun selectPage(pageSize: Int, pageIndex: Int, where: WhereClause?) = unused()
    override fun insertOne(values: Map<String, Any?>) = unused()
    override fun insertMany(valuesList: List<Map<String, Any?>>) = unused()
    override fun insertIfAbsent(values: Map<String, Any?>) = unused()
    override fun update(values: Map<String, Any?>, limit: Int?, where: WhereClause?) = unused()
    override fun updateById(id: Any, values: Map<String, Any?>) = unused()
    override fun upsertById(id: Any, values: Map<String, Any?>) = unused()
    override fun delete(limit: Int?, where: WhereClause?) = unused()
    override fun deleteById(id: Any) = unused()
}

/** A backend that takes command documents, which is the shape a document implementation overrides. */
private object DocumentQueries : Queries {

    override val typeName: String = "Documents"

    override val rawForm: RawForm = RawForm.COMMAND_DOCUMENT

    override fun rawCommand(command: String) = DocumentRawCommand(command)

    override fun selectById(id: Any) = unused()
    override fun selectOne(where: WhereClause?) = unused()
    override fun selectMany(where: WhereClause?) = unused()
    override fun selectPage(pageSize: Int, pageIndex: Int, where: WhereClause?) = unused()
    override fun insertOne(values: Map<String, Any?>) = unused()
    override fun insertMany(valuesList: List<Map<String, Any?>>) = unused()
    override fun insertIfAbsent(values: Map<String, Any?>) = unused()
    override fun update(values: Map<String, Any?>, limit: Int?, where: WhereClause?) = unused()
    override fun updateById(id: Any, values: Map<String, Any?>) = unused()
    override fun upsertById(id: Any, values: Map<String, Any?>) = unused()
    override fun delete(limit: Int?, where: WhereClause?) = unused()
    override fun deleteById(id: Any) = unused()
}

/** Stands in for a statement this suite never executes. */
private fun unused(): Nothing =
    throw UnsupportedOperationException("The raw statement tests never build a declared statement.")

/** A raw SQL query with no backend behind it, so nothing is sent anywhere. */
private class SqlRawQuery(statement: String, parameters: List<Any?>) : RawQuery(statement, parameters) {

    override suspend fun execute(): Map<String, Any?> =
        throw UnsupportedOperationException("The raw statement tests never execute a statement.")
}

/** A raw SQL update with no backend behind it, for the same reason. */
private class SqlRawUpdate(statement: String, parameters: List<Any?>) : RawUpdate(statement, parameters) {

    override suspend fun execute(): WriteResult =
        throw UnsupportedOperationException("The raw statement tests never execute a statement.")
}

/** A raw command with no backend behind it, for the same reason. */
private class DocumentRawCommand(statement: String) : RawCommand(statement) {

    override suspend fun execute(): Map<String, Any?> =
        throw UnsupportedOperationException("The raw statement tests never execute a statement.")
}
