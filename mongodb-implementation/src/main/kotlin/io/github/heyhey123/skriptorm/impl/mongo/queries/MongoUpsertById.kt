package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.skriptorm.queries.UpsertById
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table
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
        return WriteResult(if (result.upsertedId != null) 1L else result.modifiedCount)
    }
}
