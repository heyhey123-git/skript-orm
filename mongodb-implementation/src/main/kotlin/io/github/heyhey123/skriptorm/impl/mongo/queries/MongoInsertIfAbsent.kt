package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.MongoWriteException
import com.mongodb.client.MongoDatabase
import io.github.heyhey123.skriptorm.impl.mongo.database.MongoSequences
import io.github.heyhey123.skriptorm.impl.mongo.type.MongoValues
import io.github.heyhey123.skriptorm.queries.InsertIfAbsent
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table

/**
 * An insert that a key the table already holds turns into nothing.
 *
 * The reference behaviour is SQL's `INSERT IGNORE`: write the row, and treat a violation of any unique
 * index as success with no row written. MongoDB raises `E11000` for exactly that, so the insert is left
 * to the server, which makes the check atomic — reading first and inserting after would let two
 * concurrent inserts both find nothing and both write.
 *
 * A table without a unique index has nothing to violate, so this inserts every time. That is what the
 * same statement does in SQL, where the row is written because no key refused it.
 */
class MongoInsertIfAbsent(
    values: Map<String, Any?>,
    override val database: MongoDatabase
) : InsertIfAbsent(values), MongoQuery {

    override suspend fun execute(table: Table): WriteResult {
        require(values.isNotEmpty()) { "Insert values cannot be empty." }
        val document = MongoValues.storageDocument(table, MongoSequences.withKey(database, table, values))
        return try {
            database.getCollection(table.name).insertOne(document)
            WriteResult(1)
        } catch (error: MongoWriteException) {
            if (error.code != DUPLICATE_KEY) throw error
            WriteResult(0)
        }
    }

    private companion object {

        /** The server error code MongoDB reports when a unique index already holds the key. */
        const val DUPLICATE_KEY = 11000
    }
}
