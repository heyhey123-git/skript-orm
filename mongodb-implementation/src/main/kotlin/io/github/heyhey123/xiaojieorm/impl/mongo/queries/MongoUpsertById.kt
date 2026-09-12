package io.github.heyhey123.xiaojieorm.impl.mongo.queries

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.xiaojieorm.queries.UpsertById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import org.bson.Document

class MongoUpsertById(
    id: Any,
    values: Map<String, Any?>,
    override val database: MongoDatabase
) : UpsertById(id, values), MongoQuery {
    override suspend fun execute(table: Table): WriteResult {
        val collection = database.getCollection<Document>(table.name)
        val primaryKeyColumn = table.primaryKey!!
        val filter = Filters.eq(primaryKeyColumn.name, id)
        val updateDoc = Updates.combine(
            values.map { Updates.set(it.key, it.value) }
        )
        val options = UpdateOptions().upsert(true)
        val result = collection.updateOne(filter, updateDoc, options)
        return WriteResult(result.upsertedId?.let { 1 } ?: 0)
    }
}
