package io.github.heyhey123.xiaojieorm.impl.mongo.queries

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.xiaojieorm.queries.InsertMany
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import org.bson.Document

class MongoInsertMany(
    valuesList: List<Map<String, Any?>>,
    override val database: MongoDatabase
) : InsertMany(valuesList), MongoQuery {

    override suspend fun execute(table: Table): WriteResult {
        val collection = database.getCollection<Map<String, Any?>>(table.name)
        val result = collection.insertMany(valuesList.map { Document(it) })
        return WriteResult(result.insertedIds.size.toLong())
    }
}
