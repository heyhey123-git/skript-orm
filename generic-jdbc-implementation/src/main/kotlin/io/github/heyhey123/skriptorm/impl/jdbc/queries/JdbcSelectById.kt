package io.github.heyhey123.skriptorm.impl.jdbc.queries

import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.skriptorm.queries.SelectById
import io.github.heyhey123.skriptorm.result.CursorResult
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.StorageValues

open class JdbcSelectById(
    id: Any,
    override val connectionSource: JdbcConnectionSource,
    override val dialect: JdbcDialect
) : SelectById(id), JdbcQuery {

    override suspend fun execute(table: Table): CursorResult {
        val primaryKey = requireNotNull(table.primaryKey) { "Table ${table.name} does not have a primary key." }
        val id = StorageValues.prepare(primaryKey.type, this.id)
        val whereSql = "WHERE ${dialect.quoteIdentifier(primaryKey.name)} = ?"
        return executeCursor(dialect.selectOne(table.name, whereSql)) { statement ->
            statement.bindValue(1, id, primaryKey.type)
        }
    }
}
