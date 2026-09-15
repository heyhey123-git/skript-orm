package io.github.heyhey123.xiaojieorm.condition

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ConditionTest {

    @Test
    fun `condition variants preserve domain values`() {
        val marker = Any()
        assertSame(marker, Condition.Equals("a", marker).right)
        assertEquals(null, Condition.NotEquals("a", null).right)
        assertSame(marker, Condition.GreaterThan("a", marker).right)
        assertSame(marker, Condition.GreaterThanOrEquals("a", marker).right)
        assertSame(marker, Condition.LessThan("a", marker).right)
        assertSame(marker, Condition.LessThanOrEquals("a", marker).right)
        val between = Condition.Between("a", marker, 2)
        assertSame(marker, between.start)
        assertEquals(2, between.end)
    }

    @Test
    fun `where clauses retain flags order and snapshot lists`() {
        val first = Condition.Equals("a", 1)
        val second = Condition.Equals("b", 2)
        val source = mutableListOf<Condition>(first, second)
        val all = WhereClause.All(false, source)
        val any = WhereClause.Any(true, source)
        source.reverse()
        source.clear()

        assertFalse(all.negated)
        assertTrue(any.negated)
        assertEquals(listOf(first, second), all.conditions)
        assertEquals(listOf(first, second), any.conditions)
    }

    @Test
    fun `where clauses reject empty conditions`() {
        assertFailsWith<IllegalArgumentException> { WhereClause.All(false, emptyList()) }
        assertFailsWith<IllegalArgumentException> { WhereClause.Any(false, emptyList()) }
    }
}
