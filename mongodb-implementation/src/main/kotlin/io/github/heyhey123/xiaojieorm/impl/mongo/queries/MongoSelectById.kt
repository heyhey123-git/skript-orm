package io.github.heyhey123.xiaojieorm.impl.mongo.queries

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.xiaojieorm.impl.mongo.result.MongoDataCursor
import io.github.heyhey123.xiaojieorm.queries.SelectById
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.table.Table
import kotlinx.coroutines.flow.toList
import org.bson.Document

class MongoSelectById(
    id: Any,
    override val database: MongoDatabase
) : SelectById(id), MongoQuery {

    override suspend fun execute(table: Table): CursorResult {
        val collection = database.getCollection<Document>(table.name)
        val primaryKey = table.primaryKey!!
        val filter = Document(primaryKey.name, id)
        val findFlow = collection.find(filter)
        val resultList = findFlow.limit(1).toList()
        return CursorResult(MongoDataCursor(resultList))
    }
}
