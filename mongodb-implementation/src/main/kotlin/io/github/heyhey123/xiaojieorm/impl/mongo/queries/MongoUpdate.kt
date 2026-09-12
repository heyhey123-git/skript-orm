package io.github.heyhey123.xiaojieorm.impl.mongo.queries

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.xiaojieorm.impl.mongo.condition.MongoConditionTranslator
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Update
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.bson.Document

class MongoUpdate(
    values: Map<String, Any?>,
    limit: Int?,
    where: WhereClause?,
    override val database: MongoDatabase
) : Update(values, limit, where), MongoQuery {
    override suspend fun execute(table: Table): WriteResult {
        val collection = database.getCollection<Document>(table.name)
        val filter = where?.let { MongoConditionTranslator.translate(it) } ?: Document()
        val updateDocument = Updates.combine(
            values.map { (key, value) -> Updates.set(key, value) }
        )

        if (limit == 1) {
            val result = collection.updateOne(filter, updateDocument)
            return WriteResult(result.modifiedCount.toInt())
        }

        if (limit == null) {
            val result = collection.updateMany(filter, updateDocument)
            return WriteResult(result.modifiedCount.toInt())
        }

        val idsToUpdate = collection
            .find(filter)
            .limit(limit!!)
            .map { it.get("_id") }
            .toList()

        if (idsToUpdate.isEmpty()) {
            return WriteResult(0)
        }

        val idFilter = Filters.`in`("_id", idsToUpdate)
        val result = collection.updateMany(idFilter, updateDocument)
        return WriteResult(result.modifiedCount.toInt())
    }
}
