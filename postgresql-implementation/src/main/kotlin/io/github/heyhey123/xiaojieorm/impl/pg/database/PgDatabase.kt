package io.github.heyhey123.xiaojieorm.impl.pg.database

import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDatabase
import io.github.heyhey123.xiaojieorm.impl.pg.queries.PgQueries

class PgDatabase: JdbcDatabase("org.postgresql.Driver") {
    override fun doConnect(url: String, user: String, password: String) {
        super.doConnect(url, user, password)
        queries = PgQueries(dataSource!!)
    }
}
