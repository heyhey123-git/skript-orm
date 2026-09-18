package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.impl.mongo.DOCUMENT_ID
import io.github.heyhey123.skriptorm.impl.mongo.condition.MongoConditionTranslator
import io.github.heyhey123.skriptorm.impl.mongo.type.MongoValues
import io.github.heyhey123.skriptorm.queries.Update
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table
import org.bson.Document
import org.bson.conversions.Bson

/**
 * Updates every document the filter matches, or the first [limit] of them.
 *
 * The count reported is how many documents the filter matched, not how many the server changed:
 * writing the value a column already holds leaves the document as it was but is still a row this
 * statement affected, which is what the SQL backends report. Reading the count to find out whether a
 * row exists would otherwise report zero for a row that is there.
 *
 * MongoDB's update takes no limit, so a limit greater than one is applied by reading the identifiers of
 * as many matching documents as the limit allows and updating those. Which rows those are is decided by
 * the server and is not stable, exactly like the rows a SQL backend picks for `UPDATE ... LIMIT`.
 */
class MongoUpdate(
    values: Map<String, Any?>,
    limit: Int?,
    where: WhereClause?,
    override val database: MongoDatabase
) : Update(values, limit, where), MongoQuery {

    override suspend fun execute(table: Table): WriteResult {
        require(values.isNotEmpty()) { "Update values cannot be empty." }
        val effectiveLimit = limit
        require(effectiveLimit == null || effectiveLimit > 0) { "Update limit must be positive." }
        val collection = database.getCollection(table.name)
        val filter = where?.let {
            MongoConditionTranslator.translate(it, table)
        } ?: Filters.empty()
        // Built before the write: converting every value is also what refuses a column the table does
        // not declare, and that has to happen before the first document is touched.
        val update = Updates.combine(
            values.map { (key, value) -> Updates.set(key, MongoValues.storage(table, key, value)) }
        )

        return when {
            effectiveLimit == null -> WriteResult(collection.updateMany(filter, update).matchedCount)

            effectiveLimit == 1 -> WriteResult(collection.updateOne(filter, update).matchedCount)

            else -> WriteResult(updateLimited(collection, filter, update, effectiveLimit))
        }
    }

    private suspend fun updateLimited(
        collection: MongoCollection<Document>,
        filter: Bson,
        update: Bson,
        limit: Int
    ): Long {
        val ids = collection.find(filter).limit(limit).mapNotNull { it.get(DOCUMENT_ID) }
        if (ids.isEmpty()) return 0
        return collection.updateMany(Filters.`in`(DOCUMENT_ID, ids), update).matchedCount
    }
}
