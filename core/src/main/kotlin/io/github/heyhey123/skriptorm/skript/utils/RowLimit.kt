package io.github.heyhey123.skriptorm.skript.utils

/**
 * How much one statement may move between a database and a Skript value.
 *
 * Skript reads and writes a list variable one index at a time, on the server thread, in one uninterrupted
 * stretch, and what that costs is counted in values rather than in rows: a hundred thousand rows of a
 * six-column table is six hundred thousand of them. Measured on a development server, reading a batch of
 * values out of a variable costs about a third of a microsecond per value, and storing a result into one
 * about two, the difference being that a store clears the variable first and then reaches every index
 * through Skript's own name lookup, which takes a fair lock and walks a tree per value. A hundred thousand
 * rows of a six-column table is therefore about a second of the server thread in total, and the larger half
 * of it is the result being stored; the budgets below keep each half to a hitch a server can afford rather
 * than to seconds of one.
 *
 * A read past [ROWS] refuses, and stores nothing. The rows a script did not receive are rows it will reason
 * about anyway, and a table quietly cut in half answers every later question wrong; a refusal is something
 * the author can act on, either by narrowing the result or by walking it page by page. A read is held to a
 * row count because a stored result is what the author counts, and because `select page` has to be able to
 * refuse a page before anything is sent.
 *
 * A write past it is neither cut nor refused. Reading the values of a batch costs the server thread in
 * proportion to its size, and that part cannot leave the thread: resolving a variable name evaluates the
 * expressions written inside it, and Skript's variables are read under a lock it does not expose. The rows
 * are the author's, though, and dropping some of them leaves a batch that neither the script nor its author
 * can reason about — the script is told nothing it can check, and half the rows are in the table. Such a
 * batch is sent as several statements instead, each carrying at most [rowsPerStatement] rows and every row
 * written; see [io.github.heyhey123.skriptorm.queries.insertManyInStatements], which is the one place that
 * does it.
 *
 * A batch needs a statement boundary from the driver's side anyway, because an unbounded row count is a
 * statement a driver may refuse for the number of values bound to it. That is why a write's boundary is a
 * value budget rather than a row count: values are what a driver binds and values are what the batch costs.
 * A six-column table gets the same 5000 rows a read is held to, a wider one fewer, and a narrower one more.
 *
 * Neither direction is spread over several ticks. A read that stores into a list variable clears that
 * variable first, so the pieces of one read cannot be appended without reaching past Skript's own API, and a
 * list that grows over several ticks is a list another script can read half-written.
 */
internal object RowLimit {

    /**
     * Values one statement may bind. Reading a batch this large out of a variable costs the server thread
     * about ten milliseconds, which is the stall the split exists to bound.
     */
    const val VALUES_PER_STATEMENT: Int = 30_000

    /**
     * Rows a single statement may store into a read result. Storing this many rows costs the server thread
     * about fifty milliseconds on a six-column table, and proportionally more on a wider one.
     */
    const val ROWS: Int = 5000

    /**
     * Rows one write statement may carry when the table has [columns] columns.
     *
     * The value budget split over the table's width, and never fewer than one row: a table wider than the
     * budget still moves, one row per statement, rather than refusing every write to it.
     */
    fun rowsPerStatement(columns: Int): Int = maxOf(1, VALUES_PER_STATEMENT / maxOf(1, columns))

    /** What a select asks the server for: one more than [ROWS], to tell "too many" from "exactly the ceiling". */
    const val PROBE_ROWS: Int = ROWS + 1

    /** What a `select many` reports when the table returned more rows than one statement may store. */
    fun readRefusal(table: String): String =
        "select many read more than $ROWS rows from table '$table' and stored nothing. A Skript list " +
            "variable is written one index at a time on the server thread, and a result this large would " +
            "stop the server from ticking. Narrow the result with a where block, or read it with " +
            "'select page'."

    /** What a `select page` reports when the page it was asked for is larger than one statement may store. */
    fun pageRefusal(pageSize: Int): String =
        "select page asked for $pageSize rows, but a page may hold at most $ROWS rows. A Skript list " +
            "variable is written one index at a time on the server thread, and a page this large would " +
            "stop the server from ticking. Ask for fewer rows per page and walk the pages one after " +
            "another."
}

/**
 * A read that would have moved more rows than [RowLimit.ROWS], and therefore moved none.
 *
 * It carries a refusal from the half that runs off the server thread — where a select learns how many rows
 * the server returned — back to the half that runs on it, which is where the variable can be cleared and the
 * refusal reported.
 */
internal class TooManyRowsException(val refusal: String) : RuntimeException(refusal)
