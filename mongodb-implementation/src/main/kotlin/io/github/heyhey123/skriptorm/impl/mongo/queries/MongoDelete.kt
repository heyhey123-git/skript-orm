package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.impl.mongo.condition.MongoConditionTranslator
import io.github.heyhey123.skriptorm.queries.Delete
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table
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
