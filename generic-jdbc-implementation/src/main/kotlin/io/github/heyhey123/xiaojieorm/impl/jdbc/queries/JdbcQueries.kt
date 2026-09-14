package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.GenericJdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.queries.Queries
import javax.sql.DataSource

open class JdbcQueries(
    protected val dataSource: DataSource,
    protected val dialect: JdbcDialect = GenericJdbcDialect
) : Queries {
    override fun selectById(id: Any) = JdbcSelectById(id, dataSource, dialect)
    override fun selectOne(where: WhereClause?) = JdbcSelectOne(where, dataSource, dialect)
    override fun selectMany(where: WhereClause?) = JdbcSelectMany(where, dataSource, dialect)
    override fun selectPage(pageSize: Int, pageIndex: Int, where: WhereClause?) =
        JdbcSelectPage(pageSize, pageIndex, where, dataSource, dialect)
    override fun insertOne(values: Map<String, Any?>) = JdbcInsertOne(values, dataSource, dialect)
    override fun insertMany(valuesList: List<Map<String, Any?>>) = JdbcInsertMany(valuesList, dataSource, dialect)
    override fun insertIfAbsent(values: Map<String, Any?>) = JdbcInsertIfAbsent(values, dataSource, dialect)
    override fun update(values: Map<String, Any?>, limit: Int?, where: WhereClause?) =
        JdbcUpdate(values, limit, where, dataSource, dialect)
    override fun updateById(id: Any, values: Map<String, Any?>) = JdbcUpdateById(id, values, dataSource, dialect)
    override fun upsertById(id: Any, values: Map<String, Any?>) = JdbcUpsertById(id, values, dataSource, dialect)
    override fun delete(limit: Int?, where: WhereClause?) = JdbcDelete(limit, where, dataSource, dialect)
    override fun deleteById(id: Any) = JdbcDeleteById(id, dataSource, dialect)
}
