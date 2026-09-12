package io.github.heyhey123.xiaojieorm.condition

/**
 * Represents a WHERE clause in a database query.
 *
 */
sealed class WhereClause {
    /**
     * Indicates whether the clause is negated (i.e., NO ANY or NOT ALL).
     */
    abstract val negated: Boolean

    /**
     * The list of conditions in the WHERE clause.
     * The list should not be empty.
     */
    abstract val conditions: List<Condition>

    class Any(
        override val negated: Boolean,
        override val conditions: List<Condition>
    ) : WhereClause()

    class All(
        override val negated: Boolean,
        override val conditions: List<Condition>
    ) : WhereClause()
}

