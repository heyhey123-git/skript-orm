package io.github.heyhey123.xiaojieorm.impl.pg.database

import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDatabase
import io.github.heyhey123.xiaojieorm.impl.pg.type.PgDataTypes
import io.github.heyhey123.xiaojieorm.type.DataTypes

class PgDatabase : JdbcDatabase("org.postgresql.Driver", PgJdbcDialect) {
    override val dataTypes: DataTypes = PgDataTypes
}
