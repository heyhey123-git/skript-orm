package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.impl.mongo.condition.MongoConditionTranslator
import io.github.heyhey123.skriptorm.impl.mongo.result.MongoDataCursor
import io.github.heyhey123.skriptorm.queries.SelectPage
import io.github.heyhey123.skriptorm.result.CursorResult
import io.github.heyhey123.skriptorm.table.Table
import kotlinx.coroutines.flow.toList
import org.bson.Document

class MongoSelectPage(
    pageSize: Int,
    pageIndex: Int,
    where: WhereClause?,
    override val database: MongoDatabase
) : SelectPage(pageSize, pageIndex, where), MongoQuery {

    override suspend fun execute(table: Table): CursorResult {
        val collection = database.getCollection<Document>(table.name)
        val offset = (pageIndex - 1) * pageSize
        val filter = where?.let {
            MongoConditionTranslator.translate(it)
        } ?: Document()
        val documents = collection.find(filter)
            .skip(offset)
            .limit(pageSize)
            .toList()

        return CursorResult(MongoDataCursor(documents))
    }
}
