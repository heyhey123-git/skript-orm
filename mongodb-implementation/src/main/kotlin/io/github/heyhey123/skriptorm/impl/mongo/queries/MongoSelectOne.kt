package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.client.MongoDatabase
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.impl.mongo.condition.MongoConditionTranslator
import io.github.heyhey123.skriptorm.impl.mongo.result.MongoDataCursor
import io.github.heyhey123.skriptorm.queries.SelectOne
import io.github.heyhey123.skriptorm.result.CursorResult
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.StorageValues

class MongoSelectOne(
    where: WhereClause?,
    override val database: MongoDatabase
) : SelectOne(where), MongoQuery {

    override suspend fun execute(table: Table): CursorResult {
        val where = StorageValues.where(table, this.where)
        val collection = database.getCollection(table.name)
        val filter = where?.let {
            MongoConditionTranslator.translate(it, table)
        }
        val findFlow = filter?.let { collection.find(it) } ?: collection.find()
        val documents = findFlow.limit(1).toList()
        return CursorResult(MongoDataCursor(documents, table.columns.keys.toList()))
    }
}
