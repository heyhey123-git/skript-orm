package io.github.heyhey123.skriptorm.impl.mongo.condition

import com.mongodb.client.model.Filters
import io.github.heyhey123.skriptorm.condition.Condition
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.impl.mongo.type.MongoValues
import io.github.heyhey123.skriptorm.table.Table
import org.bson.conversions.Bson

/**
 * Turns a where clause into a MongoDB filter.
 *
 * A condition carries a domain value and a document holds a storage value, so every operand goes through
 * [MongoValues] on the way in — the same conversion the JDBC implementation applies when it binds a
 * parameter. A column the table does not declare is refused rather than turned into a filter on a field
 * nothing writes, which would quietly match nothing.
 *
 * Null equality is MongoDB's: `{column: null}` matches a document where the column is null *or absent*,
 * and `{column: {$ne: null}}` matches one where it is present and not null. Between them they cover every
 * document, which is what `IS NULL` and `IS NOT NULL` do for a column a table always writes.
 *
 * A negated clause becomes `$nor` around the combined filter, which is the only way MongoDB negates a
 * whole filter: `$not` applies to a single field's operator expression.
 */
object MongoConditionTranslator {

    fun translate(where: WhereClause, table: Table): Bson {
        val filters = where.conditions.map { translateCondition(it, table) }

        val combinedFilter = when (where) {
            is WhereClause.All -> Filters.and(filters)
            is WhereClause.Any -> Filters.or(filters)
        }

        return if (where.negated) {
            // `$nor` with the combined filter is MongoDB's `NOT`: `$not` is an operator that belongs to
            // a field and cannot wrap a whole filter, and a `$not` at the top level is refused by the
            // server as an unknown operator. A clause carries at least one condition, so there is
            // nothing to combine before negating.
            Filters.nor(combinedFilter)
        } else {
            combinedFilter
        }
    }

    fun translateCondition(condition: Condition, table: Table): Bson {
        val column = condition.left
        return when (condition) {
            is Condition.Equals -> Filters.eq(column, MongoValues.storage(table, column, condition.right))

            is Condition.NotEquals -> Filters.ne(column, MongoValues.storage(table, column, condition.right))

            is Condition.Between -> Filters.and(
                Filters.gte(column, operand(table, column, condition.start)),
                Filters.lte(column, operand(table, column, condition.end))
            )

            is Condition.GreaterThan ->
                Filters.gt(column, operand(table, column, condition.right))

            is Condition.GreaterThanOrEquals ->
                Filters.gte(column, operand(table, column, condition.right))

            is Condition.LessThan ->
                Filters.lt(column, operand(table, column, condition.right))

            is Condition.LessThanOrEquals ->
                Filters.lte(column, operand(table, column, condition.right))
        }
    }

    /**
     * A range operand in its stored form. Every condition but equality and inequality declares its
     * operands as non-null, so this cannot be a null; the same conversion refuses a column the table
     * does not declare.
     */
    private fun operand(table: Table, column: String, value: Any): Any =
        requireNotNull(MongoValues.storage(table, column, value)) {
            "The value $column is compared against must not be null."
        }
}
