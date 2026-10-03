package io.github.heyhey123.skriptorm.skript.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BatchedVariableRowsTest {
    @Test
    fun `time budget yields before the entry budget is exhausted`() {
        val source = (1..1024).associate { "column$it" to it }
        var conversions = 0
        val reader = BatchedVariableRows(source, "row") { _, value ->
            conversions++
            value
        }
        assertFalse(reader.advance(maxEntries = 4096, timeBudgetNanos = 1))
        assertEquals(64, conversions)
        while (!reader.advance()) Unit
        assertEquals(1024, reader.rows.single().size)
    }

    @Test
    fun `small advances preserve order and fill a column found in the last row`() {
        val source = linkedMapOf<Any?, Any?>(
            "2" to linkedMapOf("name" to "Bob"),
            "1" to linkedMapOf("name" to "Alice", "age" to 20)
        )
        val reader = BatchedVariableRows(source, "rows") { _, value -> value }

        assertFalse(reader.advance(maxEntries = 1))
        assertFailsWith<IllegalStateException> { reader.rows }
        var advances = 1
        while (!reader.advance(maxEntries = 1, timeBudgetNanos = 1)) advances++

        assertTrue(advances > source.size)
        assertEquals(
            listOf(
                linkedMapOf("name" to "Bob", "age" to null),
                linkedMapOf("name" to "Alice", "age" to 20)
            ),
            reader.rows
        )
        assertEquals(listOf("name", "age"), reader.rows.first().keys.toList())
    }

    @Test
    fun `time budget ends an advance without losing the remaining entries`() {
        val source = linkedMapOf<String, Any?>()
        repeat(1000) { source["column$it"] = it }
        val reader = BatchedVariableRows(source, "row") { _, value -> value }

        assertFalse(reader.advance(maxEntries = 10_000, timeBudgetNanos = 1))
        while (!reader.advance(maxEntries = 10_000, timeBudgetNanos = 1)) Unit
        assertEquals(source, reader.rows.single())
    }

    @Test
    fun `flat input stays one sparse row`() {
        val reader = BatchedVariableRows(linkedMapOf("id" to 7, "name" to "Alice"), "row") { key, value ->
            if (key == "id") (value as Int) + 1 else value
        }
        while (!reader.advance(maxEntries = 1)) Unit
        assertEquals(listOf(linkedMapOf("id" to 8, "name" to "Alice")), reader.rows)
    }

    @Test
    fun `mixed shapes and scalar values alongside keys are rejected`() {
        val mixed = BatchedVariableRows(linkedMapOf("1" to mapOf("id" to 1), "name" to "x"), "rows") { _, v -> v }
        assertFailsWith<IllegalArgumentException> { while (!mixed.advance(maxEntries = 1)) Unit }

        val rootScalar = BatchedVariableRows(linkedMapOf(null to "x", "name" to "A"), "row") { _, v -> v }
        assertFailsWith<IllegalArgumentException> { while (!rootScalar.advance(maxEntries = 1)) Unit }

        val nestedScalar = BatchedVariableRows(
            linkedMapOf("1" to linkedMapOf(null to "x", "id" to 1)),
            "rows"
        ) { _, v -> v }
        assertFailsWith<IllegalArgumentException> { while (!nestedScalar.advance(maxEntries = 1)) Unit }

        val numericKey = BatchedVariableRows(linkedMapOf(1 to mapOf("id" to 1)), "rows") { _, v -> v }
        assertFailsWith<IllegalArgumentException> { while (!numericKey.advance(maxEntries = 1)) Unit }
    }

    @Test
    fun `a late conversion failure does not expose partial rows`() {
        val reader = BatchedVariableRows(
            linkedMapOf("1" to mapOf("age" to 1), "2" to mapOf("age" to "bad")),
            "rows"
        ) { _, value -> (value as? Int) ?: throw IllegalArgumentException("bad age") }
        assertFailsWith<IllegalArgumentException> { while (!reader.advance(maxEntries = 1)) Unit }
        assertFailsWith<IllegalStateException> { reader.rows }
        assertEquals("bad age", assertFailsWith<IllegalArgumentException> { reader.advance() }.message)
    }

    @Test
    fun `structural changes between ticks are reported`() {
        val firstRow = linkedMapOf("a" to 1, "b" to 2)
        val source = linkedMapOf<Any?, Any?>("1" to firstRow, "2" to mapOf("a" to 3))
        val reader = BatchedVariableRows(source, "rows") { _, value -> value }
        assertFalse(reader.advance(maxEntries = 1))
        firstRow["c"] = 4
        assertTrue(assertFailsWith<IllegalArgumentException> { reader.advance() }.message!!.contains("changed"))

        val outer = linkedMapOf<Any?, Any?>("1" to mapOf("a" to 1))
        val outerReader = BatchedVariableRows(outer, "rows") { _, value -> value }
        assertFalse(outerReader.advance(maxEntries = 1))
        outer["2"] = mapOf("a" to 2)
        assertTrue(assertFailsWith<IllegalArgumentException> { outerReader.advance() }.message!!.contains("changed"))
    }

    @Test
    fun `replacing the active row is rejected even when sizes match`() {
        val source = linkedMapOf<Any?, Any?>("1" to linkedMapOf("a" to 1))
        val reader = BatchedVariableRows(source, "rows") { _, value -> value }
        assertFalse(reader.advance(maxEntries = 1))
        source["1"] = linkedMapOf("a" to 2)
        assertTrue(assertFailsWith<IllegalArgumentException> { reader.advance() }.message!!.contains("changed"))
    }
}
