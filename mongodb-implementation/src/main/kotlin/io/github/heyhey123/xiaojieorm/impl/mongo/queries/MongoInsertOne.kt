package io.github.heyhey123.xiaojieorm.impl.mongo.queries

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.xiaojieorm.queries.InsertOne
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import org.bson.Document

class MongoInsertOne(
    values: Map<String, Any?>,
    override val database: MongoDatabase
): InsertOne(values), MongoQuery {
    override suspend fun execute(table: Table): WriteResult {
        val collection = database.getCollection<Document>(table.name)
        val document = Document(values)
        val result = collection.insertOne(document)
        return WriteResult(result.insertedId?.let { 1 } ?: 0)
    }
}
