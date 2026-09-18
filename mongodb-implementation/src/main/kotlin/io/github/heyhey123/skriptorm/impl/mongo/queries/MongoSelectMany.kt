package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.client.MongoDatabase
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.impl.mongo.condition.MongoConditionTranslator
import io.github.heyhey123.skriptorm.impl.mongo.result.MongoDataCursor
import io.github.heyhey123.skriptorm.queries.SelectMany
import io.github.heyhey123.skriptorm.result.CursorResult
import io.github.heyhey123.skriptorm.table.Table

class MongoSelectMany(
    where: WhereClause?,
    override val database: MongoDatabase
) : SelectMany(where), MongoQuery {

    override suspend fun execute(table: Table): CursorResult {
        val collection = database.getCollection(table.name)
        val filter = where?.let {
            MongoConditionTranslator.translate(it, table)
        }
        val findFlow = filter?.let { collection.find(it) } ?: collection.find()
        return CursorResult(MongoDataCursor(findFlow.toList(), table.columns.keys.toList()))
    }
}
