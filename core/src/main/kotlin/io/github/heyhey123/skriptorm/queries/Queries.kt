package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.condition.WhereClause

/**
 * Creates backend-specific queries from the addon's common query operations.
 */
interface Queries {

    /**
     * The backend name used in script-facing messages. This is the connection type supplied by the
     * script, not an implementation class name.
     */
    val typeName: String

    /**
     * The raw statement format this backend accepts, or null if it accepts none. Used to explain
     * unsupported calls to [rawQuery], [rawUpdate], or [rawCommand].
     */
    val rawForm: RawForm?
        get() = null

    /**
     * Creates a query for one row identified by its primary key.
     *
     * @param id primary key value
     */
    fun selectById(id: Any): SelectById

    /**
     * Creates a query for the first row matching [where].
     *
     * @param where optional filter
     */
    fun selectOne(where: WhereClause?): SelectOne

    /**
     * Creates a query for all rows matching [where].
     *
     * @param where optional filter
     */
    fun selectMany(where: WhereClause?): SelectMany

    /**
     * Creates a query that asks the server for at most [limit] matching rows. A caller can request
     * one row beyond its storage limit to detect an oversized result without fetching the rest.
     *
     * @param where optional filter
     * @param limit maximum rows returned by the server
     */
    fun selectMany(where: WhereClause?, limit: Int): SelectMany

    /**
     * Creates a query for one page of matching rows.
     *
     * @param pageSize rows per page
     * @param pageIndex one-based page number
     * @param where optional filter
     */
    fun selectPage(pageSize: Int, pageIndex: Int, where: WhereClause?): SelectPage

    /**
     * Creates an insert for one row.
     *
     * @param values column names and values to insert
     */
    fun insertOne(values: Map<String, Any?>): InsertOne

    /**
     * Creates an insert for multiple rows.
     *
     * @param valuesList one column-to-value map per row
     */
    fun insertMany(valuesList: List<Map<String, Any?>>): InsertMany

    /**
     * Creates an insert that leaves an existing row with the same key untouched.
     *
     * @param values column names and values to insert
     */
    fun insertIfAbsent(values: Map<String, Any?>): InsertIfAbsent

    /**
     * Creates an update for rows matching [where].
     *
     * @param values column names and replacement values
     * @param limit optional maximum number of rows to update
     * @param where optional filter
     */
    fun update(values: Map<String, Any?>, limit: Int?, where: WhereClause?): Update

    /**
     * Creates an update for the row identified by [id].
     *
     * @param id primary key value
     * @param values column names and replacement values
     */
    fun updateById(id: Any, values: Map<String, Any?>): UpdateById

    /**
     * Creates an insert or update for the row identified by [id].
     *
     * @param id primary key value
     * @param values column names and values to write
     */
    fun upsertById(id: Any, values: Map<String, Any?>): UpsertById

    /**
     * Creates a delete for rows matching [where].
     *
     * @param limit optional maximum number of rows to delete
     * @param where optional filter
     */
    fun delete(limit: Int?, where: WhereClause?): Delete

    /**
     * Creates a delete for the row identified by [id].
     *
     * @param id primary key value
     */
    fun deleteById(id: Any): DeleteById

    /**
     * Builds a raw SQL statement that returns rows.
     *
     * Backends without raw SQL support throw an error naming the selected backend.
     *
     * @param statement the statement text, as the script wrote it
     * @param parameters the values to bind to its `?` placeholders, in order
     */
    fun rawQuery(statement: String, parameters: List<Any?>): RawQuery =
        throw UnsupportedOperationException(rawRefusal("SQL statements"))

    /**
     * Builds a raw SQL statement that changes the database, such as `ALTER TABLE` or `UPDATE`.
     *
     * @param statement the statement text, as the script wrote it
     * @param parameters the values to bind to its `?` placeholders, in order
     */
    fun rawUpdate(statement: String, parameters: List<Any?>): RawUpdate =
        throw UnsupportedOperationException(rawRefusal("SQL statements"))

    /**
     * Builds a raw command for a document backend.
     *
     * Backends without raw command support throw an error naming the selected backend.
     *
     * @param command the command document, written as JSON text
     */
    fun rawCommand(command: String): RawCommand =
        throw UnsupportedOperationException(rawRefusal("commands"))

    /**
     * Names the backend and its supported raw format when a raw operation is unavailable.
     */
    private fun rawRefusal(kind: String): String {
        val alternative = when (rawForm) {
            RawForm.SQL_STATEMENT ->
                "It takes raw SQL statements instead: 'execute query …' and 'execute update …'."
            RawForm.COMMAND_DOCUMENT ->
                "It takes raw commands instead: 'execute command \"…\"'."
            null ->
                "It takes no raw statements."
        }
        return "Raw $kind are not supported by database '$typeName'. $alternative " +
            "See the Raw statements page for what a raw statement does and does not guarantee."
    }
}
