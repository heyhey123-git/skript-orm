package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.client.MongoDatabase
import io.github.heyhey123.skriptorm.impl.mongo.database.MongoSequences
import io.github.heyhey123.skriptorm.impl.mongo.type.MongoValues
import io.github.heyhey123.skriptorm.queries.InsertOne
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table

class MongoInsertOne(
    values: Map<String, Any?>,
    override val database: MongoDatabase
) : InsertOne(values), MongoQuery {

    override suspend fun execute(table: Table): WriteResult {
        require(values.isNotEmpty()) { "Insert values cannot be empty." }
        val document = MongoValues.storageDocument(table, MongoSequences.withKey(database, table, values))
        val result = database.getCollection(table.name).insertOne(document)
        return WriteResult(if (result.insertedId != null) 1L else 0L)
    }
}
