package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.skriptorm.queries.InsertIfAbsent
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table
import kotlinx.coroutines.flow.firstOrNull
import org.bson.Document

class MongoInsertIfAbsent(
    values: Map<String, Any?>,
    override val database: MongoDatabase
) : InsertIfAbsent(values), MongoQuery {

    override suspend fun execute(table: Table): WriteResult {
        val collection = database.getCollection<Document>(table.name)
        val document = Document(values)
        val existing = collection.find(document).firstOrNull()

        return if (existing == null) {
            val result = collection.insertOne(document)
            WriteResult(if (result.insertedId != null) 1L else 0L)
        } else {
            WriteResult(0L)
        }
    }
}
