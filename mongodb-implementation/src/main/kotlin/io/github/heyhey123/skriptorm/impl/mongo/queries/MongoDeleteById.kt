package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.client.MongoDatabase
import io.github.heyhey123.skriptorm.impl.mongo.type.MongoPrimaryKey
import io.github.heyhey123.skriptorm.queries.DeleteById
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table

class MongoDeleteById(
    id: Any,
    override val database: MongoDatabase
) : DeleteById(id), MongoQuery {

    override suspend fun execute(table: Table): WriteResult {
        val filter = MongoPrimaryKey.filter(table, id)
        val result = database.getCollection(table.name).deleteOne(filter)
        return WriteResult(result.deletedCount)
    }
}
