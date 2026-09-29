package io.github.heyhey123.skriptorm.impl.jdbc.queries

import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.impl.jdbc.database.GenericJdbcDialect
import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.queries.RawForm
import io.github.heyhey123.skriptorm.queries.RawQuery
import io.github.heyhey123.skriptorm.queries.RawUpdate

/**
 * The query factory of a JDBC connection, which takes raw SQL statements.
 *
 * [rawForm] is what tells a refusal which alternative to name; [rawCommand] is deliberately not
 * overridden, so a command document written for a document backend is refused by the shared default rather
 * than sent to a relational server that has no idea what to do with it.
 */
open class JdbcQueries(
    protected val connectionSource: JdbcConnectionSource,
    protected val dialect: JdbcDialect = GenericJdbcDialect,
    override val typeName: String = "JDBC"
) : Queries {

    override val rawForm: RawForm = RawForm.SQL_STATEMENT

    override fun selectById(id: Any) = JdbcSelectById(id, connectionSource, dialect)
    override fun selectOne(where: WhereClause?) = JdbcSelectOne(where, connectionSource, dialect)
    override fun selectMany(where: WhereClause?) = JdbcSelectMany(where, connectionSource, dialect)
    override fun selectPage(pageSize: Int, pageIndex: Int, where: WhereClause?) =
        JdbcSelectPage(pageSize, pageIndex, where, connectionSource, dialect)

    override fun insertOne(values: Map<String, Any?>) = JdbcInsertOne(values, connectionSource, dialect)
    override fun insertMany(valuesList: List<Map<String, Any?>>) = JdbcInsertMany(valuesList, connectionSource, dialect)
    override fun insertIfAbsent(values: Map<String, Any?>) = JdbcInsertIfAbsent(values, connectionSource, dialect)
    override fun update(values: Map<String, Any?>, limit: Int?, where: WhereClause?) =
        JdbcUpdate(values, limit, where, connectionSource, dialect)

    override fun updateById(id: Any, values: Map<String, Any?>) = JdbcUpdateById(id, values, connectionSource, dialect)
    override fun upsertById(id: Any, values: Map<String, Any?>) = JdbcUpsertById(id, values, connectionSource, dialect)
    override fun delete(limit: Int?, where: WhereClause?) = JdbcDelete(limit, where, connectionSource, dialect)
    override fun deleteById(id: Any) = JdbcDeleteById(id, connectionSource, dialect)

    override fun rawQuery(statement: String, parameters: List<Any?>): RawQuery =
        JdbcRawQuery(statement, parameters, connectionSource, dialect)

    override fun rawUpdate(statement: String, parameters: List<Any?>): RawUpdate =
        JdbcRawUpdate(statement, parameters, connectionSource, dialect)
}
