package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.GenericJdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.queries.Queries

open class JdbcQueries(
    protected val connectionSource: JdbcConnectionSource,
    protected val dialect: JdbcDialect = GenericJdbcDialect
) : Queries {

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
}
