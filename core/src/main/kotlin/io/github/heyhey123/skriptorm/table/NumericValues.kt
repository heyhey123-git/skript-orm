package io.github.heyhey123.skriptorm.table

import kotlin.math.abs

/**
 * What a number a script wrote becomes when it meets a column.
 *
 * Skript converts a number into the type it is asked for, and every one of those conversions narrows:
 * `Number.intValue()` keeps the low 32 bits, `byteValue()` the low 8, and a fraction is cut towards
 * zero. Asked for the column's own type — which is what parsing a `values` entry or a `where` value
 * would do — Skript hands back a number that already fits, so nothing is left to compare against what
 * the script wrote: an `int` column, the value `5000000000`, and the row holds `705032704` with no
 * error anywhere, because the value the database receives is a perfectly good `INT`.
 *
 * The way out is to ask Skript for the number itself and narrow it here, where the column is known and
 * a value that does not fit can be refused. It is the same conversion Skript would have done, with one
 * difference: the plugin says so instead of storing a number the script never wrote.
 *
 * What deliberately stays as Skript does it everywhere else:
 *
 * - a fraction written into a whole-number column is cut towards zero (`int` column, `1.7`, stores `1`);
 * - `float` keeps four bytes, so `0.1` does not come back as the `double` that was written;
 * - `double` is exact only up to 2^53.
 *
 * What changed is the case that was silently wrong: a whole number outside the column's range.
 */
object NumericValues {

    /**
     * Whether [domainType] is one of the number types a column can declare.
     *
     * The values that reach this are read as a [Number] exactly when their column is one of these, so
     * the answer also decides how a `values` entry or a `where` value is parsed.
     */
    fun isNumeric(domainType: Class<*>): Boolean = domainType in NUMERIC_DOMAINS

    /**
     * The number a column of [column]'s type can hold.
     *
     * @throws IllegalArgumentException if [value] does not fit the column, or is not a finite number
     */
    fun narrow(column: Column<*>, value: Number): Number = when (column.type.domainType) {
        Byte::class.javaObjectType ->
            whole(column, value, Byte.MIN_VALUE.toLong(), Byte.MAX_VALUE.toLong()).toByte()

        Int::class.javaObjectType ->
            whole(column, value, Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()

        Long::class.javaObjectType ->
            whole(column, value, Long.MIN_VALUE, Long.MAX_VALUE)

        Float::class.javaObjectType ->
            finite(column, value).also {
                require(abs(it) <= Float.MAX_VALUE.toDouble()) {
                    "Value $value does not fit column '${column.name}' " +
                        "(${column.type.typeCode}, ${-Float.MAX_VALUE} to ${Float.MAX_VALUE})."
                }
            }.toFloat()

        Double::class.javaObjectType -> finite(column, value)

        else -> throw IllegalArgumentException(
            "Column '${column.name}' is ${column.type.typeCode}, which does not hold a number."
        )
    }

    /**
     * The whole part of [value], which is what a whole-number column stores, as long as that part is
     * inside [min]..[max].
     *
     * The comparison is made on the number as it was written, before the cut towards zero: a fraction
     * that reaches past the end of the range is refused rather than turned into a boundary value.
     */
    private fun whole(column: Column<*>, value: Number, min: Long, max: Long): Long {
        val number = finite(column, value)
        require(number >= min.toDouble() && number <= max.toDouble()) {
            "Value $value does not fit column '${column.name}' " +
                "(${column.type.typeCode}, $min to $max)."
        }
        return value.toLong()
    }

    /** The number a fractional column stores, which is the value as long as it is a number at all. */
    private fun finite(column: Column<*>, value: Number): Double {
        val number = value.toDouble()
        require(number.isFinite()) {
            "Value '$value' is not a number column '${column.name}' can hold."
        }
        return number
    }

    private val NUMERIC_DOMAINS = setOf(
        Byte::class.javaObjectType,
        Int::class.javaObjectType,
        Long::class.javaObjectType,
        Float::class.javaObjectType,
        Double::class.javaObjectType
    )
}
