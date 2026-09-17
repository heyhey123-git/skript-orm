package io.github.heyhey123.skriptorm.impl.jdbc.queries

import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.skriptorm.queries.SelectMany
import io.github.heyhey123.skriptorm.result.CursorResult
import io.github.heyhey123.skriptorm.table.Table

open class JdbcSelectMany(
    where: WhereClause?,
    override val connectionSource: JdbcConnectionSource,
    override val dialect: JdbcDialect
) : SelectMany(where), JdbcQuery {

    override suspend fun execute(table: Table): CursorResult {
        val whereSql = where?.let { JdbcConditionTranslator.translate(it, dialect) }
        return executeCursor(dialect.select(table.name, whereSql)) { statement ->
            bindWhere(table, where, statement)
        }
    }
}
