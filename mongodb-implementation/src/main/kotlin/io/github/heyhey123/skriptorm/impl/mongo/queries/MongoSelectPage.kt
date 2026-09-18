package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.impl.mongo.condition.MongoConditionTranslator
import io.github.heyhey123.skriptorm.impl.mongo.result.MongoDataCursor
import io.github.heyhey123.skriptorm.queries.SelectPage
import io.github.heyhey123.skriptorm.result.CursorResult
import io.github.heyhey123.skriptorm.table.Table

/**
 * One page of a collection, ordered by its primary key.
 *
 * The order is not a nicety: without one, MongoDB returns documents in an order a later insert, delete
 * or compaction is free to change, so two reads of the same page could show a row twice or not at all.
 * That order only exists if the table declares a primary key, so a table without one is refused here
 * rather than paged unpredictably, which is what the JDBC implementation does too.
 *
 * `skip` reads and discards the pages before this one, so deep pages cost more than the page they
 * return. A key-set cursor — the last key of the previous page as the filter of the next — avoids that
 * but cannot express a page number, which is what this statement takes.
 */
class MongoSelectPage(
    pageSize: Int,
    pageIndex: Int,
    where: WhereClause?,
    override val database: MongoDatabase
) : SelectPage(pageSize, pageIndex, where), MongoQuery {

    override suspend fun execute(table: Table): CursorResult {
        val collection = database.getCollection(table.name)
        val primaryKey = requireNotNull(table.primaryKey) {
            "Stable pagination requires table ${table.name} to define a primary key."
        }
        val offset = Math.multiplyExact(pageIndex - 1, pageSize)
        val filter = where?.let {
            MongoConditionTranslator.translate(it, table)
        } ?: Filters.empty()
        val documents = collection.find(filter)
            .sort(Sorts.ascending(primaryKey.name))
            .skip(offset)
            .limit(pageSize)
            .toList()

        return CursorResult(MongoDataCursor(documents, table.columns.keys.toList()))
    }
}
