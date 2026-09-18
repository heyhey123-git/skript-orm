package io.github.heyhey123.skriptorm.impl.mongo.database

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import io.github.heyhey123.skriptorm.impl.mongo.DOCUMENT_ID
import io.github.heyhey123.skriptorm.impl.mongo.type.MongoValues
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table

/**
 * Auto-increment for a backend that has no identity column.
 *
 * A SQL server allocates the value itself (`AUTO_INCREMENT`, `IDENTITY`), so an insert that leaves the
 * key out gets one back. MongoDB has no such feature, and a unique index — which is what registering a
 * primary key creates — indexes a missing field as null, so the second document inserted without its
 * key would be refused as a duplicate. That leaves two honest options, and this is the one that keeps
 * an `auto increment` column usable: one counter document per key column, incremented with `$inc` in a
 * single server-side step, which is the usual MongoDB equivalent of a sequence.
 *
 * A sequence that only hands values out diverges from its collection as soon as a key is written that it
 * did not hand out, and that divergence shows up much later, as a duplicate key error on an insert that
 * has nothing to do with the key that caused it. So this counter is also told about keys it did not
 * issue: a statement that brings its own key raises it to that key, and registering a table raises it to
 * the highest key the collection already holds. That is what MySQL's `AUTO_INCREMENT` does with an
 * explicit insert, and where PostgreSQL's sequences need a manual `setval`.
 *
 * The counter is created when the table is registered rather than on first use, so allocation is always
 * an update of an existing document: two concurrent upserts of the same missing counter can raise a
 * duplicate key error, and the insert path cannot afford that.
 *
 * The counters live in a collection of their own so that they are not documents of any table, and a
 * table may not be named after it for the same reason. The value is handed on as the column's domain
 * type and converted by the insert like every other value, so what lands in the document follows the
 * column's converter.
 */
internal object MongoSequences {

    /** The collection holding one counter document per auto-increment column. */
    const val COLLECTION = "skript_orm_sequences"

    /** The field of a counter document holding the highest value handed out or seen. */
    private const val VALUE = "value"

    private val INTEGER_DOMAINS = setOf(
        Byte::class.javaObjectType,
        Int::class.javaObjectType,
        Long::class.javaObjectType
    )

    /**
     * Creates the counter of every auto-increment column of [table], and raises it to the highest key the
     * collection already holds.
     *
     * An existing counter is never lowered, so registering a table a second time leaves it where it was.
     *
     * @throws IllegalArgumentException if such a column does not hold whole numbers, since a counter
     *   that counts cannot fill it.
     */
    suspend fun prepare(database: MongoDatabase, table: Table) {
        table.columns.values.filter { it.isAutoIncrement }.forEach { column ->
            require(column.type.domainType in INTEGER_DOMAINS) {
                "Auto-increment column ${column.name} of table ${table.name} must hold whole numbers, but is declared as ${column.type.typeCode}."
            }
            counters(database).updateOne(
                Filters.eq(DOCUMENT_ID, name(table, column)),
                Updates.setOnInsert(VALUE, 0L),
                UpdateOptions().upsert(true)
            )
            // Rows can already be there: written before the table was registered, or written by something
            // that is not this plugin. Registration is where that is noticed, so the first insert after it
            // cannot be refused by a key this counter never handed out.
            storedHighest(database, table, column)?.let { highest ->
                raiseTo(database, table, column, highest)
            }
        }
    }

    /**
     * [values] with the key of an `auto increment` column of [table] decided: the next value the counter
     * holds when the statement left the key out, and the key itself when the statement brought one — which
     * also raises the counter, so that key is never handed out a second time. A statement that brings its
     * own key is not a mistake to refuse: a script may well address rows it knows.
     *
     * A batch pays one raise per row that carries a key, which is a small update, and only ever moves the
     * counter forward.
     *
     * A key supplied as null is filled in as well, which is what every SQL backend does with an explicit
     * `NULL` in an auto-increment column.
     */
    suspend fun withKey(database: MongoDatabase, table: Table, values: Map<String, Any?>): Map<String, Any?> {
        val column = autoIncrement(table) ?: return values
        val provided = values[column.name]
        if (provided != null) {
            raise(database, table, column, provided)
            return values
        }
        return values + (column.name to value(next(database, table, column), column.type.domainType))
    }

    /**
     * Raises the counter of [table]'s key to [id], for a statement that writes a key it did not allocate —
     * `upsert by id`, which can create the row that key names.
     */
    suspend fun track(database: MongoDatabase, table: Table, id: Any) {
        val column = autoIncrement(table) ?: return
        raise(database, table, column, id)
    }

    /** [stored] as the domain value of a column whose domain type is [domainType]. */
    fun value(stored: Long, domainType: Class<*>): Any = when (domainType) {
        Byte::class.javaObjectType -> stored.toByte()
        Int::class.javaObjectType -> stored.toInt()
        else -> stored
    }

    private suspend fun next(database: MongoDatabase, table: Table, column: Column<*>): Long {
        val updated = counters(database).findOneAndUpdate(
            Filters.eq(DOCUMENT_ID, name(table, column)),
            Updates.inc(VALUE, 1L),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
        val stored = updated?.get(VALUE) as? Number
            ?: error(
                "The counter of column ${column.name} of table ${table.name} is missing, or holds no number. " +
                    "Set its '$VALUE' field to the highest key already in use, or register the table again " +
                    "if the collection is empty."
            )
        return stored.toLong()
    }

    /** The highest value of [column] the collection holds, or null when it holds no document at all. */
    private fun storedHighest(database: MongoDatabase, table: Table, column: Column<*>): Long? {
        val document = database.getCollection(table.name)
            .find()
            .sort(Sorts.descending(column.name))
            .limit(1)
            .first()
        return (document?.get(column.name) as? Number)?.toLong()
    }

    private suspend fun raise(database: MongoDatabase, table: Table, column: Column<*>, id: Any) {
        val stored = MongoValues.storage(table, column.name, id) as? Number ?: return
        raiseTo(database, table, column, stored.toLong())
    }

    /**
     * Moves the counter of [column] up to [stored], and never down: `$max` applies only when the value is
     * greater, which is what makes two statements raising the same counter at once safe, and what keeps a
     * counter that is already ahead of the collection from being pulled back.
     *
     * Upserting is deliberate: a counter that was removed is restored by the next statement that brings a
     * key, which is the only way it can come back without counting from zero over the stored rows.
     */
    private suspend fun raiseTo(database: MongoDatabase, table: Table, column: Column<*>, stored: Long) {
        counters(database).updateOne(
            Filters.eq(DOCUMENT_ID, name(table, column)),
            Updates.max(VALUE, stored),
            UpdateOptions().upsert(true)
        )
    }

    private fun autoIncrement(table: Table): Column<*>? = table.primaryKey?.takeIf { it.isAutoIncrement }

    private fun name(table: Table, column: Column<*>) = "${table.name}.${column.name}"

    private fun counters(database: MongoDatabase) = database.getCollection(COLLECTION)
}
