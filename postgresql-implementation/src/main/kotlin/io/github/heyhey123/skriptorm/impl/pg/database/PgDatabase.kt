package io.github.heyhey123.skriptorm.impl.pg.database

import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDatabase
import io.github.heyhey123.skriptorm.impl.pg.type.PgDataTypes
import io.github.heyhey123.skriptorm.type.DataTypes

class PgDatabase(
    statementTimeoutSeconds: Int = JdbcDatabase.DEFAULT_STATEMENT_TIMEOUT_SECONDS
) : JdbcDatabase("org.postgresql.Driver", PgJdbcDialect, statementTimeoutSeconds) {

    override val dataTypes: DataTypes = PgDataTypes
}
