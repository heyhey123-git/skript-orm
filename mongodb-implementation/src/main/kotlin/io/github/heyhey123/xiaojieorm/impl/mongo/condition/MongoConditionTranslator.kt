package io.github.heyhey123.xiaojieorm.impl.mongo.condition

import com.mongodb.client.model.Filters
import io.github.heyhey123.xiaojieorm.condition.Condition
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import org.bson.conversions.Bson

object MongoConditionTranslator {
    fun translate(where: WhereClause): Bson {
        if (where.conditions.isEmpty()) {
            return Filters.empty()
        }

        val filters = where.conditions.map(::translateCondition)

        val combinedFilter = when (where) {
            is WhereClause.All -> Filters.and(filters)
            is WhereClause.Any -> Filters.or(filters)
        }

        return if (where.negated) {
            Filters.not(combinedFilter)
        } else {
            combinedFilter
        }
    }

    fun translateCondition(condition: Condition): Bson =
        when (condition) {
            is Condition.Equals -> Filters.eq(condition.left, condition.right)
            is Condition.NotEquals -> Filters.ne(condition.left, condition.right)
            is Condition.Between -> Filters.and(
                Filters.gte(condition.left, condition.start),
                Filters.lte(condition.left, condition.end)
            )

            is Condition.GreaterThan -> Filters.gt(condition.left, condition.right)
            is Condition.GreaterThanOrEquals -> Filters.gte(condition.left, condition.right)
            is Condition.LessThan -> Filters.lt(condition.left, condition.right)
            is Condition.LessThanOrEquals -> Filters.lte(condition.left, condition.right)
        }
}
