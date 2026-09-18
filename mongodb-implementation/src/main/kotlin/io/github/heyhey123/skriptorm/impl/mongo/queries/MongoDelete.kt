package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.impl.mongo.DOCUMENT_ID
import io.github.heyhey123.skriptorm.impl.mongo.condition.MongoConditionTranslator
import io.github.heyhey123.skriptorm.queries.Delete
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table
import org.bson.Document
import org.bson.conversions.Bson

/**
 * Deletes every document the filter matches, or the first [limit] of them.
 *
 * MongoDB's delete takes no limit, so a limit greater than one is applied by reading the identifiers of
 * as many matching documents as the limit allows and deleting those. Deleting without one would remove
 * rows a script asked to keep, which is why the limit is honoured here rather than passed over.
 * Which rows a limited delete takes is decided by the server, exactly as it is for a SQL backend.
 */
class MongoDelete(
    limit: Int?,
    where: WhereClause?,
    override val database: MongoDatabase
) : Delete(limit, where), MongoQuery {

    override suspend fun execute(table: Table): WriteResult {
        val effectiveLimit = limit
        require(effectiveLimit == null || effectiveLimit > 0) { "Delete limit must be positive." }
        val collection = database.getCollection(table.name)
        val filter = where?.let {
            MongoConditionTranslator.translate(it, table)
        } ?: Filters.empty()

        return when {
            effectiveLimit == null -> WriteResult(collection.deleteMany(filter).deletedCount)

            effectiveLimit == 1 -> WriteResult(collection.deleteOne(filter).deletedCount)

            else -> WriteResult(deleteLimited(collection, filter, effectiveLimit))
        }
    }

    private suspend fun deleteLimited(
        collection: MongoCollection<Document>,
        filter: Bson,
        limit: Int
    ): Long {
        val ids = collection.find(filter).limit(limit).mapNotNull { it.get(DOCUMENT_ID) }
        if (ids.isEmpty()) return 0
        return collection.deleteMany(Filters.`in`(DOCUMENT_ID, ids)).deletedCount
    }
}
