package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.condition.WhereClause

/**
 * Queries factory interface for creating different types of query objects.
 *
 */
interface Queries {

    /**
     * The type name a script connected with, for messages that have to name the backend — such as the
     * refusal a raw statement of the wrong kind meets.
     *
     * It is the name from [io.github.heyhey123.skriptorm.database.DatabaseFactory.typeName], not a class
     * name, because it is the name the script wrote and the only one it can act on. It names the backend;
     * it is never what decides whether a statement is accepted.
     */
    val typeName: String

    /**
     * The shape of this backend's own raw entry point, or null when it has none.
     *
     * This is the whole of what the core knows about backends: it never asks what kind of database it is
     * talking to, and it enumerates nothing. An implementation that takes raw SQL overrides [rawQuery] and
     * [rawUpdate]; one that takes command documents overrides [rawCommand]. The methods that are left
     * alone refuse, and this value is what lets the refusal name the alternative that does exist.
     */
    val rawForm: RawForm?
        get() = null

    /**
     * Selects an entity by its unique identifier.
     *
     * @param id The primary key of the entity to select.
     * @return A SelectById query object.
     */
    fun selectById(id: Any): SelectById

    /**
     * Selects a single entity based on a specified condition.
     *
     * @param where The WHERE clause defining the selection condition.
     * @return A SelectOne query object.
     */
    fun selectOne(where: WhereClause?): SelectOne

    /**
     * Selects multiple entities based on a selection condition.
     *
     * @param where The WHERE clause defining the selection condition.
     * @return A SelectMany query object.
     */
    fun selectMany(where: WhereClause?): SelectMany

    /**
     * Selects multiple entities, asking the server for at most [limit] rows.
     *
     * The ceiling belongs to the server rather than to this side: a caller that would refuse a result larger
     * than it can store has to learn that the result is larger *before* the rows are on their way, and a
     * caller that asks for one row more than it accepts can tell the two cases apart by reading [limit] rows.
     *
     * @param where The WHERE clause defining the selection condition.
     * @param limit The most rows to read.
     * @return A SelectMany query object carrying the ceiling.
     */
    fun selectMany(where: WhereClause?, limit: Int): SelectMany

    /**
     * Selects a page of results based on pagination parameters and an optional condition.
     *
     * @param pageSize The number of items per page
     * @param pageIndex The index of the page to select (1-based)
     * @param where The WHERE clause defining the selection condition
     * @return A SelectPage query object.
     */
    fun selectPage(pageSize: Int, pageIndex: Int, where: WhereClause?): SelectPage

    /**
     * Inserts a single record with the specified values.
     *
     * @param values A map of column names to their corresponding values for the new record.
     * @return An InsertOne query object.
     */
    fun insertOne(values: Map<String, Any?>): InsertOne

    /**
     * Inserts multiple records with the specified values.
     *
     * @param valuesList A list of maps, each containing column names and their corresponding values for the new records.
     * @return An InsertMany query object.
     */
    fun insertMany(valuesList: List<Map<String, Any?>>): InsertMany

    /**
     * Inserts a record if it does not already exist.
     *
     * @param values A map of column names to their corresponding values for the new record.
     * @return An InsertIfAbsent query object.
     */
    fun insertIfAbsent(values: Map<String, Any?>): InsertIfAbsent

    /**
     * Updates records that match the specified condition.
     *
     * @param values A map of column names to their new values.
     * @param limit The maximum number of records to be updated.
     * @param where The WHERE clause to filter records to be updated.
     * @return An Update query object.
     */
    fun update(values: Map<String, Any?>, limit: Int?, where: WhereClause?): Update

    /**
     * Updates a record by its unique identifier.
     *
     * @param id The primary key of the entity to update.
     * @param values A map of column names to their new values.
     * @return An UpdateById query object.
     */
    fun updateById(id: Any, values: Map<String, Any?>): UpdateById

    /**
     * Inserts or updates a record by its unique identifier.
     *
     * @param id The primary key of the entity to upsert.
     * @param values A map of column names to their corresponding values.
     * @return An UpsertById query object.
     */
    fun upsertById(id: Any, values: Map<String, Any?>): UpsertById

    /**
     * Deletes records that match the specified condition.
     *
     * @param limit The maximum number of records to be deleted.
     * @param where The WHERE clause to filter records to be deleted.
     * @return A Delete query object.
     */
    fun delete(limit: Int?, where: WhereClause?): Delete

    /**
     * Deletes a record by its primary key.
     *
     * @param id The primary key of the entity to delete.
     * @return A DeleteById query object.
     */
    fun deleteById(id: Any): DeleteById

    /**
     * Builds a raw SQL statement that returns rows.
     *
     * The default refuses. An implementation that speaks SQL overrides it; one that does not has no way to
     * send a statement it cannot construct, and says so by name — a script told "not supported by database
     * 'MongoDB'" can act on that, while one told nothing at all cannot.
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
     * The default refuses, for the reason [rawQuery] gives; a document backend overrides it.
     *
     * @param command the command document, written as JSON text
     */
    fun rawCommand(command: String): RawCommand =
        throw UnsupportedOperationException(rawRefusal("commands"))

    /**
     * What a backend that cannot serve this kind of raw statement reports.
     *
     * It names the backend and the alternative that backend does take. The alternative comes from
     * [rawForm], which the implementation declared, so this function knows no backends: it cannot go stale
     * when one is added, and it never decides what a connection accepts.
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
