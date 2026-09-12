package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import javax.sql.DataSource

open class JdbcQueries(
    protected val dataSource: DataSource
) : Queries {

    override fun selectById(id: Any) = JdbcSelectById(id, dataSource.connection)

    override fun selectOne(where: WhereClause?) = JdbcSelectOne(where, dataSource.connection)

    override fun selectMany(where: WhereClause?) = JdbcSelectMany(where, dataSource.connection)

    override fun selectPage(
        pageSize: Int,
        pageIndex: Int,
        where: WhereClause?
    ) = JdbcSelectPage(pageSize, pageIndex, where, dataSource.connection)

    override fun insertOne(values: Map<String, Any?>) = JdbcInsertOne(values, dataSource.connection)

    override fun insertMany(valuesList: List<Map<String, Any?>>) = JdbcInsertMany(valuesList, dataSource.connection)

    override fun insertIfAbsent(
        values: Map<String, Any?>
    ) = JdbcInsertIfAbsent(values, dataSource.connection)

    override fun update(
        values: Map<String, Any?>,
        limit: Int?,
        where: WhereClause?
    ) = JdbcUpdate(values, limit, where, dataSource.connection)

    override fun updateById(id: Any, values: Map<String, Any?>) = JdbcUpdateById(id, values, dataSource.connection)

    override fun upsertById(id: Any, values: Map<String, Any?>) = JdbcUpsertById(id, values, dataSource.connection)

    override fun delete(limit: Int?, where: WhereClause?) = JdbcDelete(limit, where, dataSource.connection)

    override fun deleteById(id: Any) = JdbcDeleteById(id, dataSource.connection)
}
