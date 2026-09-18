package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.client.MongoDatabase
import io.github.heyhey123.skriptorm.impl.mongo.database.MongoSequences
import io.github.heyhey123.skriptorm.impl.mongo.type.MongoValues
import io.github.heyhey123.skriptorm.queries.InsertMany
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table

/**
 * A batch insert. An empty batch succeeds with a count of zero; every other batch reports how many
 * documents the server acknowledged, which is exact.
 *
 * The documents of one batch may differ in which columns they carry, unlike the JDBC batch: a document
 * has no fixed column set, and a row that omits a column is stored without it. What a select reads back
 * for that column is null either way.
 */
class MongoInsertMany(
    valuesList: List<Map<String, Any?>>,
    override val database: MongoDatabase
) : InsertMany(valuesList), MongoQuery {

    override suspend fun execute(table: Table): WriteResult {
        if (valuesList.isEmpty()) return WriteResult(0)
        val documents = valuesList.map { row ->
            require(row.isNotEmpty()) { "Insert values cannot be empty." }
            MongoValues.storageDocument(table, MongoSequences.withKey(database, table, row))
        }
        val result = database.getCollection(table.name).insertMany(documents)
        return WriteResult(result.insertedIds.size.toLong())
    }
}
