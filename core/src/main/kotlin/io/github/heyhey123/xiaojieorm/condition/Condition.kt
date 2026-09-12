package io.github.heyhey123.xiaojieorm.condition

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
    class Equals(override val left: String, val right: Any?) : Condition()

    /**
     * Not equals condition representing a comparison between two values.
     *
     * @property left The left operand of the comparison.
     * @property right The right operand of the comparison.
     */
    class NotEquals(override val left: String, val right: Any?) : Condition()

    /**
     * Greater than condition representing a comparison between two values.
     *
     * @property left The left operand of the comparison.
     * @property right The right operand of the comparison.
     */
    class GreaterThan(override val left: String, val right: Any) : Condition()

    /**
     * Greater than or equals condition representing a comparison between two values.
     *
     */
    class GreaterThanOrEquals(override val left: String, val right: Any) : Condition()

    /**
     * Less than condition representing a comparison between two values.
     *
     */
    class LessThan(override val left: String, val right: Any) : Condition()

    /**
     * Less than or equals condition representing a comparison between two values.
     *
     */
    class LessThanOrEquals(override val left: String, val right: Any) : Condition()

    /**
     * Between condition representing a range comparison between two values.
     *
     */
    class Between(override val left: String, val start: Any, val end: Any) : Condition()
}
