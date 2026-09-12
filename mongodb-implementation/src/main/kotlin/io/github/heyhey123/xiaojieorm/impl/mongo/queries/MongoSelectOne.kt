package io.github.heyhey123.xiaojieorm.impl.mongo.queries

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.xiaojieorm.impl.mongo.condition.MongoConditionTranslator
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.mongo.result.MongoDataCursor
import io.github.heyhey123.xiaojieorm.queries.SelectOne
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.table.Table
import kotlinx.coroutines.flow.toList
import org.bson.Document

class MongoSelectOne(
    where: WhereClause?,
    override val database: MongoDatabase
) : SelectOne(where), MongoQuery {
    override suspend fun execute(table: Table): CursorResult {
        val collection = database.getCollection<Document>(table.name)
        val filter = where?.let {
            MongoConditionTranslator.translate(it)
        }
        val findFlow = filter?.let { collection.find(it) } ?: collection.find()
        val resultList = findFlow.limit(1).toList()
        return CursorResult(MongoDataCursor(resultList))
    }
}
