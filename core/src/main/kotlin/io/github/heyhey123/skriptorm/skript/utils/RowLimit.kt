package io.github.heyhey123.skriptorm.skript.utils

/**
 * How many rows one statement may move between a database and a Skript value.
 *
 * Skript writes a list variable one index at a time, on the server thread, in one uninterrupted stretch.
 * A hundred thousand rows of a six-column table held this server's thread for more than ten seconds — long
 * enough for the watchdog to report it and for every player to feel it. The ceiling below keeps the worst
 * case a hitch of about half a second instead.
 *
 * The two directions treat the ceiling differently, and deliberately.
 *
 * A read past it refuses, and stores nothing. The rows a script did not receive are rows it will reason
 * about anyway, and a table quietly cut in half answers every later question wrong; a refusal is something
 * the author can act on, either by narrowing the result or by walking it page by page.
 *
 * A write past it keeps the first [ROWS] rows, drops the rest and warns. Those rows were assembled by the
 * script and are already in its hands, and the count the statement reports says what was actually written,
 * so nothing is hidden — but the work the author already did is not thrown away either.
 *
 * Neither direction is spread over several ticks. A read that stores into a list variable clears that
 * variable first, so the pieces of one read cannot be appended without reaching past Skript's own API, and a
 * list that grows over several ticks is a list another script can read half-written.
 */
internal object RowLimit {

    /** Rows a single statement may store into, read out of, or write from a Skript value. */
    const val ROWS: Int = 5000

    /** What a select asks the server for: one more than [ROWS], to tell "too many" from "exactly the ceiling". */
    const val PROBE_ROWS: Int = ROWS + 1

    /** What a `select many` reports when the table returned more rows than one statement may store. */
    fun readRefusal(table: String): String =
        "select many read more than $ROWS rows from table '$table' and stored nothing. A Skript list " +
            "variable is written one index at a time on the server thread, and a result this large would " +
            "stop the server from ticking. Narrow the result with a where block, or read it page by page " +
            "with 'select page'."

    /** What a `select page` reports when the page it was asked for is larger than one statement may store. */
    fun pageRefusal(pageSize: Int): String =
        "select page asked for $pageSize rows, but a page may hold at most $ROWS rows. A Skript list " +
            "variable is written one index at a time on the server thread, and a page this large would " +
            "stop the server from ticking. Ask for fewer rows per page and walk the pages one after " +
            "another."

    /**
     * The rows a multi-row write will send: [rows] itself when it fits under [ROWS], and its first [ROWS]
     * rows otherwise, with [warn] told about the tail that was dropped.
     *
     * The warning is a sink rather than an element because this rule is a number and a sentence, and belongs
     * to nothing that runs on a server thread; the caller hands in whatever channel it already reports on.
     *
     * [source] names where the rows came from, so the warning points at the variable or the values block the
     * author has to change.
     */
    fun <T> batch(rows: List<T>, source: String, warn: (String) -> Unit): List<T> {
        if (rows.size <= ROWS) return rows
        warn(writeTruncation(rows.size, source))
        return rows.subList(0, ROWS).toList()
    }

    /** What a multi-row write reports when it was given more rows than one statement may write. */
    fun writeTruncation(rows: Int, source: String): String =
        "insert many was given $rows rows in $source, but one statement moves at most $ROWS rows, so only " +
            "the first $ROWS are written and the rest are dropped. A batch is read out of its source one " +
            "row at a time on the server thread, and sending all of it would stop the server from " +
            "ticking. Split the rows over several statements; the affected row count says how many were " +
            "written."
}

/**
 * A read that would have moved more rows than [RowLimit.ROWS], and therefore moved none.
 *
 * It carries a refusal from the half that runs off the server thread — where a select learns how many rows
 * the server returned — back to the half that runs on it, which is where the variable can be cleared and the
 * refusal reported.
 */
internal class TooManyRowsException(val refusal: String) : RuntimeException(refusal)
