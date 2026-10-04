package io.github.heyhey123.skriptorm.skript.utils

/**
 * Limits result size and the number of values bound in one write statement.
 *
 * Global result variables and server-only conversions can still consume a server tick. Reads
 * refuse results above [ROWS] instead of truncating them; scripts can narrow a query or use `select page`.
 * Writes retain every row, but [io.github.heyhey123.skriptorm.queries.insertManyInStatements]
 * splits a large batch across statements to stay within the binding budget. `insert many` reads
 * global variable values in bounded steps across ticks. Event-owned locals are read in the background.
 *
 * Global results are published synchronously. A parked trigger's local results can be assembled
 * in the background before its local context is restored.
 */
internal object RowLimit {

    /**
     * Maximum number of values bound per write statement.
     */
    const val VALUES_PER_STATEMENT: Int = 30_000

    /**
     * Maximum rows returned by a read into a Skript list variable.
     */
    const val ROWS: Int = 5000

    /**
     * Rows one write statement may carry when the table has [columns] columns.
     *
     * Divides the value budget by table width, allowing at least one row per statement.
     */
    fun rowsPerStatement(columns: Int): Int = maxOf(1, VALUES_PER_STATEMENT / maxOf(1, columns))

    /** What a select asks the server for: one more than [ROWS], to tell "too many" from "exactly the ceiling". */
    const val PROBE_ROWS: Int = ROWS + 1

    /** What a `select many` reports when the table returned more rows than one statement may store. */
    fun readRefusal(table: String): String =
        "select many read more than $ROWS rows from table '$table' and stored nothing. " +
            "The per-read limit bounds result memory use and server-thread work. " +
            "Narrow the result with a where block, or read it with " +
            "'select page'."

    /** What a `select page` reports when the page it was asked for is larger than one statement may store. */
    fun pageRefusal(pageSize: Int): String =
        "select page asked for $pageSize rows, but a page may hold at most $ROWS rows. " +
            "The per-page limit bounds result memory use and server-thread work. " +
            "Ask for fewer rows per page and walk the pages one after " +
            "another."
}

/**
 * Signals that a query exceeded [RowLimit.ROWS] before any rows were stored in a Skript variable.
 * Carries the refusal message from query execution back to the server thread.
 */
internal class TooManyRowsException(val refusal: String) : RuntimeException(refusal)
