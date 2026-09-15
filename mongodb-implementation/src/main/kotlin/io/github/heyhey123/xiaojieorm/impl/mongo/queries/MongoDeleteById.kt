package io.github.heyhey123.xiaojieorm.impl.mongo.queries

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.xiaojieorm.queries.DeleteById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import org.bson.Document

class MongoDeleteById(
    id: Any,
    override val database: MongoDatabase
) : DeleteById(id), MongoQuery {

    override suspend fun execute(table: Table): WriteResult {
        val collection = database.getCollection<Document>(table.name)
        val primaryKeyColumn = table.primaryKey
            ?: throw IllegalStateException("Table ${table.name} does not have a primary key.")
        val filter = Filters.eq(primaryKeyColumn.name, id)
        val deleteResult = collection.deleteOne(filter)
        return WriteResult(deleteResult.deletedCount)
    }
}
