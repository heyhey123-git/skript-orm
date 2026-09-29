package io.github.heyhey123.skriptorm.queries

/**
 * A statement a script wrote itself, sent to the backend as it stands.
 *
 * A raw statement is the one kind of statement this addon does not understand. It is not translated, not
 * checked against a registered table, and not held to the column names, types or tables a declaration
 * names: there is nothing here that could be compared with anything, which is exactly what makes it
 * useful and exactly what makes it unsafe. Everything the account can do, a raw statement can do.
 *
 * What a declared statement gets from its [io.github.heyhey123.skriptorm.table.Table] — the SQL that is
 * built, the storage type each value is bound as, the type each result column is read as — a raw statement
 * gets from nowhere. That is why this class does not extend [Query]: there is no table to execute against,
 * and pretending there were one would suggest a check that does not happen.
 *
 * The parameters are a snapshot of the values the script gave, taken when the statement is built on the
 * server thread. They are not converted for the backend: an implementation binds them as they are and
 * refuses a value it cannot bind, rather than guessing what was meant.
 *
 * @property statement the statement text, as the script wrote it
 * @property parameters the values to bind to its placeholders, in order
 *
 * @param T what the backend reported, in the shape this kind of statement defines
 */
abstract class RawStatement<T : Any>(
    val statement: String,
    parameters: List<Any?>
) {

    val parameters: List<Any?> = parameters.toList()

    /**
     * Executes the statement asynchronously.
     *
     * @return what the backend reported, in the shape this kind of statement defines
     */
    abstract suspend fun execute(): T
}
