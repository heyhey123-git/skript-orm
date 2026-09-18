package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
import io.github.heyhey123.skriptorm.impl.mongo.type.MongoPrimaryKey
import io.github.heyhey123.skriptorm.impl.mongo.type.MongoValues
import io.github.heyhey123.skriptorm.queries.UpdateById
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table

/**
 * Updates the document whose primary key is the given identifier.
 *
 * The count reported is how many documents the filter matched, for the reason [MongoUpdate] explains.
 */
class MongoUpdateById(
    id: Any,
    values: Map<String, Any?>,
    override val database: MongoDatabase
) : UpdateById(id, values), MongoQuery {

    override suspend fun execute(table: Table): WriteResult {
        require(values.isNotEmpty()) { "Update values cannot be empty." }
        val primaryKey = MongoPrimaryKey.of(table)
        require(primaryKey.name !in values) { "The primary key must not be included in update values." }
        val update = Updates.combine(
            values.map { (key, value) -> Updates.set(key, MongoValues.storage(table, key, value)) }
        )
        val filter = MongoPrimaryKey.filter(table, primaryKey, id)
        val result = database.getCollection(table.name).updateOne(filter, update)
        return WriteResult(result.matchedCount)
    }
}
