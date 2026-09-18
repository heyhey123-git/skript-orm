package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import io.github.heyhey123.skriptorm.impl.mongo.database.MongoSequences
import io.github.heyhey123.skriptorm.impl.mongo.type.MongoPrimaryKey
import io.github.heyhey123.skriptorm.impl.mongo.type.MongoValues
import io.github.heyhey123.skriptorm.queries.UpsertById
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table

/**
 * Writes the given values to the document with the given identifier, creating it when there is none.
 *
 * One row is affected either way, whether it was written or inserted, which is what the JDBC
 * implementation's update count reports. A document that was matched but already held those values
 * counts as one, for the reason [MongoUpdate] explains.
 */
class MongoUpsertById(
    id: Any,
    values: Map<String, Any?>,
    override val database: MongoDatabase
) : UpsertById(id, values), MongoQuery {

    override suspend fun execute(table: Table): WriteResult {
        require(values.isNotEmpty()) { "Upsert values cannot be empty." }
        val primaryKey = MongoPrimaryKey.of(table)
        require(primaryKey.name !in values) { "The primary key must not be included in upsert values." }
        // Before the write, not after: an upsert can create the row this key names, and a crash between the
        // two would otherwise leave a key stored that the counter was never told about — the exact thing
        // that comes back later as a duplicate key error. Raising first can only leave a gap.
        MongoSequences.track(database, table, id)
        val update = Updates.combine(
            values.map { (key, value) -> Updates.set(key, MongoValues.storage(table, key, value)) }
        )
        val filter = MongoPrimaryKey.filter(table, primaryKey, id)
        val result = database.getCollection(table.name)
            .updateOne(filter, update, UpdateOptions().upsert(true))
        return WriteResult(if (result.upsertedId != null) 1L else result.matchedCount)
    }
}
