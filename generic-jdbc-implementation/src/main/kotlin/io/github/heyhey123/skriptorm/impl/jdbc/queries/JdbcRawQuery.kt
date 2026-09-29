package io.github.heyhey123.skriptorm.impl.jdbc.queries

import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.skriptorm.queries.RawQuery
import java.sql.ResultSet

/**
 * A raw SQL statement that returns rows, read in full before the statement is done.
 *
 * The rows are read here rather than handed out as a cursor because a raw statement has no declaration to
 * read them back into: [io.github.heyhey123.skriptorm.result.DataCursor.get] takes the type a column is
 * expected to hold, and there is no expected type. Reading through the driver's own result set is therefore
 * the only honest way to do it, and it is also what lets the column labels become the keys.
 *
 * The keys are the labels the server reported, so an alias is the name a script reads, and the rows are
 * numbered from one — the shape `select many` stores and the shape the cookbook's examples use.
 */
open class JdbcRawQuery(
    statement: String,
    parameters: List<Any?>,
    override val connectionSource: JdbcConnectionSource,
    override val dialect: JdbcDialect
) : RawQuery(statement, parameters), JdbcQuery {

    override suspend fun execute(): Map<String, Any?> {
        val connection = connectionSource.borrow()
        try {
            return connection.prepareStatement(statement).use { prepared ->
                prepared.withBoundResources {
                    val timeout = configureStatement(prepared)
                    prepared.bindRawParameters(parameters)
                    prepared.executeQuery().use { rows ->
                        read(rows, timeout)
                    }
                }
            }
        } finally {
            connectionSource.release(connection)
        }
    }

    /**
     * Every row, keyed by the column label the server reported.
     *
     * A label that repeats keeps its last value, as a map does everywhere else. Nothing here converts a
     * value: what the driver returned is what a script reads, except for the shapes a Skript variable
     * cannot hold, which [readableValue] documents.
     */
    private fun read(rows: ResultSet, timeoutSeconds: Int): Map<String, Any?> {
        val labels = (1..rows.metaData.columnCount).map { rows.metaData.getColumnLabel(it) }
        val result = linkedMapOf<String, Any?>()
        var index = 0
        while (executeWithTimeoutReported(timeoutSeconds) { rows.next() }) {
            index += 1
            labels.forEachIndexed { column, label ->
                result["$index::$label"] = readableValue(rows.getObject(column + 1))
            }
        }
        return result
    }

    /**
     * A value a Skript variable can hold.
     *
     * This is not the conversion a declared statement makes — there is no column type here to convert to
     * — it is the boundary of what a variable can carry at all: a `Blob`, a `Clob` or a driver's own class
     * cannot be stored, so it is named by its text rather than thrown into a variable that would fail
     * somewhere further away from the statement that produced it.
     */
    private fun readableValue(value: Any?): Any? = when (value) {
        null -> null
        is String, is Number, is Boolean -> value
        is ByteArray -> "[binary ${value.size} bytes]"
        else -> value.toString()
    }
}
