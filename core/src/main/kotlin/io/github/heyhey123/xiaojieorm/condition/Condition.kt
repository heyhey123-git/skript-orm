package io.github.heyhey123.xiaojieorm.condition

/**
 * A single comparison in a WHERE clause.
 *
 * Every variant is a value object: two conditions are equal when they compare the same column the
 * same way against equal operands. That makes a parsed condition comparable with a hand-built one,
 * which is what a test or a query plan wants to do.
 *
 * The right-hand operand is a domain value, not a storage value, so it is stored exactly as the
 * caller supplied it.
 */
sealed class Condition {

    /**
     * The left operand of the comparison.
     * Used to identify the field or column being compared.
     */
    abstract val left: String

    /**
     * Equals condition representing a comparison between two values.
     *
     * @property left The left operand of the comparison.
     * @property right The right operand of the comparison.
     */
    data class Equals(override val left: String, val right: Any?) : Condition()

    /**
     * Not equals condition representing a comparison between two values.
     *
     * @property left The left operand of the comparison.
     * @property right The right operand of the comparison.
     */
    data class NotEquals(override val left: String, val right: Any?) : Condition()

    /**
     * Greater than condition representing a comparison between two values.
     *
     * @property left The left operand of the comparison.
     * @property right The right operand of the comparison.
     */
    data class GreaterThan(override val left: String, val right: Any) : Condition()

    /**
     * Greater than or equals condition representing a comparison between two values.
     *
     * @property left The left operand of the comparison.
     * @property right The right operand of the comparison.
     */
    data class GreaterThanOrEquals(override val left: String, val right: Any) : Condition()

    /**
     * Less than condition representing a comparison between two values.
     *
     * @property left The left operand of the comparison.
     * @property right The right operand of the comparison.
     */
    data class LessThan(override val left: String, val right: Any) : Condition()

    /**
     * Less than or equals condition representing a comparison between two values.
     *
     * @property left The left operand of the comparison.
     * @property right The right operand of the comparison.
     */
    data class LessThanOrEquals(override val left: String, val right: Any) : Condition()

    /**
     * Between condition representing a range comparison between two values.
     *
     * @property left The left operand of the comparison.
     * @property start The start of the range.
     * @property end The end of the range.
     */
    data class Between(override val left: String, val start: Any, val end: Any) : Condition()
}
