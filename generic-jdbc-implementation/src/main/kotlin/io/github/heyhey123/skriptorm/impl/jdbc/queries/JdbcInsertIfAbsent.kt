package io.github.heyhey123.skriptorm.impl.jdbc.queries

import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.skriptorm.queries.InsertIfAbsent
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table
import java.sql.SQLException

/**
 * Writes a row and treats a key that is already taken as "the row is there", leaving that row alone.
 *
 * Which error means that is the dialect's answer ([JdbcDialect.isDuplicateKey]): PostgreSQL and the
 * portable dialect say it in the statement itself, while MySQL sends a plain insert and lets the one
 * error that means "already there" be caught here. Everything else is a failure like any other write,
 * which is the point: a value the column cannot hold has to reach the script rather than be stored
 * adjusted.
 */
open class JdbcInsertIfAbsent(
    values: Map<String, Any?>,
    override val connectionSource: JdbcConnectionSource,
    override val dialect: JdbcDialect
) : InsertIfAbsent(values), JdbcQuery {

    override suspend fun execute(table: Table): WriteResult {
        require(values.isNotEmpty()) { "Insert values cannot be empty." }
        val columns = values.keys.toList()
        val sql = dialect.insertIfAbsent(table.name, columns)
        return try {
            executeUpdate(sql) { statement ->
                columns.forEachIndexed { index, key ->
                    val column = requireNotNull(table.getColumnByName(key)) { "Table ${table.name} does not have column $key." }
                    statement.bindValue(index + 1, values[key], column.type)
                }
            }
        } catch (error: SQLException) {
            if (dialect.isDuplicateKey(error)) {
                // The row is already there, so nothing was written — which is what the count says.
                WriteResult(0)
            } else {
                throw error
            }
        }
    }
}
