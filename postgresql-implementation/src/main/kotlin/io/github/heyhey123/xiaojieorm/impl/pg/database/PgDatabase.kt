package io.github.heyhey123.xiaojieorm.impl.pg.database

import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDatabase
import io.github.heyhey123.xiaojieorm.impl.pg.type.PgDataTypes
import io.github.heyhey123.xiaojieorm.type.DataTypes

class PgDatabase(
    statementTimeoutSeconds: Int = JdbcDatabase.DEFAULT_STATEMENT_TIMEOUT_SECONDS
) : JdbcDatabase("org.postgresql.Driver", PgJdbcDialect, statementTimeoutSeconds) {

    override val dataTypes: DataTypes = PgDataTypes
}
