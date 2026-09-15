package io.github.heyhey123.xiaojieorm.impl.mongo.queries

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.xiaojieorm.queries.UpdateById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import org.bson.Document

class MongoUpdateById(
    id: Any,
    values: Map<String, Any?>,
    override val database: MongoDatabase
) : UpdateById(id, values), MongoQuery {

    override suspend fun execute(table: Table): WriteResult {
        val collection = database.getCollection<Document>(table.name)
        val primaryKeyColumn = table.primaryKey
            ?: throw IllegalStateException("Table ${table.name} does not have a primary key.")
        val filter = Filters.eq(primaryKeyColumn.name, id)
        val updateDocument = Updates.combine(
            values.map { (key, value) -> Updates.set(key, value) }
        )
        val updateResult = collection.updateOne(filter, updateDocument)
        return WriteResult(updateResult.modifiedCount)
    }
}
