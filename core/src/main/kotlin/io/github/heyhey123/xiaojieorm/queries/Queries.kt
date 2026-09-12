package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause


/**
 * Queries factory interface for creating different types of query objects.
 *
 */
interface Queries {

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
}
