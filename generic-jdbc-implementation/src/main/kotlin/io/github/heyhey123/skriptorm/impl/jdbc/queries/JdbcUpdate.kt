package io.github.heyhey123.skriptorm.impl.jdbc.queries

import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.skriptorm.queries.Update
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table

open class JdbcUpdate(
    values: Map<String, Any?>,
    limit: Int?,
    where: WhereClause?,
    override val connectionSource: JdbcConnectionSource,
    override val dialect: JdbcDialect
) : Update(values, limit, where), JdbcQuery {

    override suspend fun execute(table: Table): WriteResult {
        require(values.isNotEmpty()) { "Update values cannot be empty." }
        // The SET list is exactly the supplied keys, so the map has to stay sparse: an absent key
        // means the column is not updated and keeps its stored value. A key that is present and holds
        // null is a different thing entirely and is bound as SQL NULL. Callers must not pad the map
        // with the remaining columns, because that would turn every update into a full-row write and
        // clear any column the caller did not supply.
        val columns = values.keys.toList()
        val whereSql = where?.let { JdbcConditionTranslator.translate(it, dialect) }
        val sql = dialect.update(table.name, columns, whereSql, limit)
        return executeUpdate(sql) { statement ->
            var index = 1
            columns.forEach { key ->
                val column = requireNotNull(table.getColumnByName(key)) { "Table ${table.name} does not have column $key." }
                statement.bindValue(index++, values[key], column.type)
            }
            where?.conditions?.forEach { condition ->
                val column = requireNotNull(table.getColumnByName(condition.left)) { "Table ${table.name} does not have column ${condition.left}." }
                index = JdbcConditionTranslator.fillConditionParameters(condition, statement, index, column.type)
            }
        }
    }
}
