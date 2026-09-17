package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.result.ExecutionResult
import io.github.heyhey123.skriptorm.table.Table

/**
 * Represents a database query that can be executed asynchronously.
 *
 * @param T The type of the result returned by the query.
 */
abstract class Query<T : ExecutionResult> {

    /**
     * Executes the query asynchronously.
     *
     * @return The result of the query.
     */
    abstract suspend fun execute(table: Table): T
}
