package io.github.heyhey123.xiaojieorm.impl.mongo.queries

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.xiaojieorm.impl.mongo.condition.MongoConditionTranslator
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Delete
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import org.bson.Document

class MongoDelete(
    limit: Int?,
    where: WhereClause?,
    override val database: MongoDatabase
) : Delete(limit, where), MongoQuery {
    override suspend fun execute(table: Table): WriteResult {
        val collection = database.getCollection<Document>(table.name)
        val filter = where?.let { MongoConditionTranslator.translate(it) } ?: Document()
        val result = if (limit == 1) {
            collection.deleteOne(filter)
        } else {
            collection.deleteMany(filter)
        }
        return WriteResult(result.deletedCount)
    }
}
