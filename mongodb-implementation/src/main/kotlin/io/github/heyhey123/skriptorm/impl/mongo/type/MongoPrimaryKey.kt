package io.github.heyhey123.skriptorm.impl.mongo.type

import com.mongodb.client.model.Filters
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import org.bson.conversions.Bson

/**
 * The primary key of a table, and the filter that addresses one of its rows.
 *
 * Every by-id statement needs the same two things: the key column, refused with an explanation when the
 * table declares none, and a filter built from a converted identifier. Both are here rather than in the
 * four statements that use them, because the failure a script sees for a keyless table is part of the
 * implementation's contract and has to stay the same in all four.
 */
internal object MongoPrimaryKey {

    /**
     * [table]'s primary key column.
     *
     * @throws IllegalArgumentException if the table declares no primary key, worded as the JDBC
     *   implementation words it so a script gets one answer wherever it runs.
     */
    fun of(table: Table): Column<*> = requireNotNull(table.primaryKey) {
        "Table ${table.name} does not have a primary key."
    }

    /** The filter matching the row of [table] whose primary key is [id]. */
    fun filter(table: Table, id: Any): Bson = filter(table, of(table), id)

    /** The filter matching the row of [table] whose [column] holds [id]. */
    fun filter(table: Table, column: Column<*>, id: Any): Bson =
        Filters.eq(column.name, MongoValues.storage(table, column.name, id))
}
