package io.github.heyhey123.xiaojieorm.impl.pg.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.queries.JdbcQueries
import javax.sql.DataSource

class PgQueries(dataSource: DataSource) : JdbcQueries(dataSource) {

    override fun insertIfAbsent(values: Map<String, Any?>) = PgInsertIfAbsent(values, dataSource.connection)

    override fun upsertById(id: Any, values: Map<String, Any?>) = PgUpsertById(id, values, dataSource.connection)
}
