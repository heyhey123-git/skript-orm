package io.github.heyhey123.skriptorm.condition

/**
 * Non-empty group of conditions joined by AND or OR, optionally negated as a whole.
 * Implementations defensively copy the supplied condition list.
 */
sealed class WhereClause {

    /** Whether the combined predicate is negated (`NOT ANY` or `NOT ALL`). */
    abstract val negated: Boolean

    /**
     * The list of conditions in the WHERE clause.
     * The list should not be empty.
     */
    abstract val conditions: List<Condition>

    /**
     * It means "any of the conditions must be true" (logical OR).
     *
     */
    class Any(
        override val negated: Boolean,
        conditions: List<Condition>
    ) : WhereClause() {

        override val conditions: List<Condition> = conditions.toList()

        init {
            require(this.conditions.isNotEmpty()) { "A WHERE clause must contain at least one condition." }
        }
    }

    /**
     * It means "all the conditions must be true" (logical AND).
     */
    class All(
        override val negated: Boolean,
        conditions: List<Condition>
    ) : WhereClause() {

        override val conditions: List<Condition> = conditions.toList()

        init {
            require(this.conditions.isNotEmpty()) { "A WHERE clause must contain at least one condition." }
        }
    }
}
