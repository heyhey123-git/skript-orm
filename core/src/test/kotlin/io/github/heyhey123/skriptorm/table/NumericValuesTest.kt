package io.github.heyhey123.skriptorm.table

import io.github.heyhey123.skriptorm.type.BigIntDataType
import io.github.heyhey123.skriptorm.type.DoubleDataType
import io.github.heyhey123.skriptorm.type.FloatDataType
import io.github.heyhey123.skriptorm.type.IntDataType
import io.github.heyhey123.skriptorm.type.StringDataType
import io.github.heyhey123.skriptorm.type.TinyIntDataType
import io.github.heyhey123.skriptorm.type.UuidDataType
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * What a number a script wrote becomes for a column, which is the check Skript does not make: its own
 * conversion narrows a value into the column's type without a word, so the plugin does the narrowing
 * itself and refuses what does not fit.
 */
class NumericValuesTest {

    private fun column(type: io.github.heyhey123.skriptorm.type.DataType<*>, name: String = "age") =
        Column(name, type)

    @Test
    fun `a number that fits is narrowed to the column type`() {
        assertEquals(5, NumericValues.narrow(column(IntDataType()), 5L))
        assertEquals(5, NumericValues.narrow(column(IntDataType()), 5))
        assertEquals(7L, NumericValues.narrow(column(BigIntDataType()), 7))
        assertEquals(7.0, NumericValues.narrow(column(DoubleDataType()), 7L))
        assertEquals(3.toByte(), NumericValues.narrow(column(TinyIntDataType()), 3L))
    }

    @Test
    fun `a whole number past the end of the column is refused instead of wrapped`() {
        val failure = assertFailsWith<IllegalArgumentException> {
            NumericValues.narrow(column(IntDataType()), 5000000000L)
        }

        // The message has to name the value and the column: the whole point is that this is not silent.
        assertTrue(failure.message!!.contains("5000000000"), failure.message!!)
        assertTrue(failure.message!!.contains("'age'"), failure.message!!)
        assertTrue(failure.message!!.contains("int"), failure.message!!)
    }

    @Test
    fun `the boundaries of each whole number column are kept`() {
        assertEquals(Int.MAX_VALUE, NumericValues.narrow(column(IntDataType()), Int.MAX_VALUE.toLong()))
        assertEquals(Int.MIN_VALUE, NumericValues.narrow(column(IntDataType()), Int.MIN_VALUE.toLong()))
        assertEquals(Byte.MAX_VALUE, NumericValues.narrow(column(TinyIntDataType()), 127L))
        assertEquals(Long.MAX_VALUE, NumericValues.narrow(column(BigIntDataType()), Long.MAX_VALUE))

        assertFailsWith<IllegalArgumentException> {
            NumericValues.narrow(column(IntDataType()), Int.MAX_VALUE.toLong() + 1)
        }
        assertFailsWith<IllegalArgumentException> {
            NumericValues.narrow(column(TinyIntDataType()), 300L)
        }
        assertFailsWith<IllegalArgumentException> {
            NumericValues.narrow(column(TinyIntDataType()), -129L)
        }
        // A double is compared before it is cut, so a fraction past the end is not turned into a boundary.
        assertFailsWith<IllegalArgumentException> {
            NumericValues.narrow(column(IntDataType()), 3.0e9)
        }
        assertFailsWith<IllegalArgumentException> {
            NumericValues.narrow(column(BigIntDataType()), 1.0e30)
        }
    }

    @Test
    fun `a fraction in a whole number column is still cut towards zero`() {
        assertEquals(1, NumericValues.narrow(column(IntDataType()), 1.7))
        assertEquals(-1, NumericValues.narrow(column(IntDataType()), -1.7))
        assertEquals(1L, NumericValues.narrow(column(BigIntDataType()), 1.5))
    }

    @Test
    fun `a value no number can be is refused rather than stored as a boundary`() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            assertFailsWith<IllegalArgumentException>("$value should be refused") {
                NumericValues.narrow(column(DoubleDataType()), value)
            }
            assertFailsWith<IllegalArgumentException>("$value should be refused") {
                NumericValues.narrow(column(IntDataType()), value)
            }
        }
    }

    @Test
    fun `a float column keeps four bytes and refuses what does not fit in them`() {
        val narrowed = assertIs<Float>(NumericValues.narrow(column(FloatDataType()), 0.1))
        assertEquals(0.1f, narrowed)

        assertFailsWith<IllegalArgumentException> {
            NumericValues.narrow(column(FloatDataType()), 1.0e300)
        }
    }

    @Test
    fun `a column that is not a number is not narrowed at all`() {
        assertFailsWith<IllegalArgumentException> {
            NumericValues.narrow(column(StringDataType(), "name"), 5)
        }
        assertFailsWith<IllegalArgumentException> {
            NumericValues.narrow(column(UuidDataType(), "uid"), 5)
        }
    }

    @Test
    fun `the numeric column types are the five the syntax declares`() {
        listOf(TinyIntDataType(), IntDataType(), BigIntDataType(), FloatDataType(), DoubleDataType())
            .forEach { assertTrue(NumericValues.isNumeric(it.domainType), it.typeCode) }

        assertFalse(NumericValues.isNumeric(StringDataType().domainType))
        assertFalse(NumericValues.isNumeric(UuidDataType().domainType))
        assertFalse(NumericValues.isNumeric(UUID::class.java))
    }
}
