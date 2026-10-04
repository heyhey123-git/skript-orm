package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.client.MongoDatabase
import io.github.heyhey123.skriptorm.impl.mongo.result.MongoDataCursor
import io.github.heyhey123.skriptorm.impl.mongo.type.MongoPrimaryKey
import io.github.heyhey123.skriptorm.queries.SelectById
import io.github.heyhey123.skriptorm.result.CursorResult
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.StorageValues

class MongoSelectById(
    id: Any,
    override val database: MongoDatabase
) : SelectById(id), MongoQuery {

    override suspend fun execute(table: Table): CursorResult {
        val id = requireNotNull(StorageValues.prepare(MongoPrimaryKey.of(table).type, this.id))
        val collection = database.getCollection(table.name)
        val documents = collection.find(MongoPrimaryKey.filter(table, id)).limit(1).toList()
        return CursorResult(MongoDataCursor(documents, table.columns.keys.toList()))
    }
}
