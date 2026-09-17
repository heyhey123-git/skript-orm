package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.skriptorm.queries.InsertOne
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table
import org.bson.Document

class MongoInsertOne(
    values: Map<String, Any?>,
    override val database: MongoDatabase
) : InsertOne(values), MongoQuery {

    override suspend fun execute(table: Table): WriteResult {
        val collection = database.getCollection<Document>(table.name)
        val document = Document(values)
        val result = collection.insertOne(document)
        return WriteResult(if (result.insertedId != null) 1L else 0L)
    }
}
