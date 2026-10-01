package io.github.heyhey123.skriptorm.skript.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The budgets themselves, and the sentences a script is left with when a read crosses one.
 *
 * These are unit tests because they are the whole of the contract: the numbers, the one-row probe that tells
 * "too many" from "exactly the ceiling", and the two refusals. What each element does with them is covered
 * against a real server instead, and so is what a write past its boundary does — it is sent as several
 * statements rather than cut, which
 * [io.github.heyhey123.skriptorm.queries.InsertManyInStatementsTest] pins down.
 */
class RowLimitTest {

    @Test
    fun `the read ceiling is the measured batch, said in rows`() {
        // The budget was measured as a batch of values and is spent by a write as such. The read side states
        // it in rows, and six columns is the shape it was measured on: if one of these numbers moves without
        // the other, one direction has quietly left the measurement behind.
        assertEquals(RowLimit.VALUES_PER_STATEMENT, RowLimit.ROWS * 6)
    }

    @Test
    fun `a statement's boundary is a value budget, so a wider table carries fewer rows`() {
        assertEquals(RowLimit.ROWS, RowLimit.rowsPerStatement(6), "a six-column table gets the read ceiling's rows")
        assertEquals(1500, RowLimit.rowsPerStatement(20))
        assertEquals(RowLimit.VALUES_PER_STATEMENT, RowLimit.rowsPerStatement(1))
    }

    @Test
    fun `a table wider than the budget still moves one row per statement`() {
        assertEquals(1, RowLimit.rowsPerStatement(RowLimit.VALUES_PER_STATEMENT + 1))
        assertEquals(1, RowLimit.rowsPerStatement(Int.MAX_VALUE))
    }

    @Test
    fun `a select asks for one row past the ceiling`() {
        // Asking for the ceiling would make "exactly 5000 rows" and "more than 5000 rows, cut off at 5000"
        // the same answer, and one of those has to be refused while the other may be stored.
        assertEquals(RowLimit.ROWS + 1, RowLimit.PROBE_ROWS)
    }

    @Test
    fun `a select refusal names the table the ceiling and the two ways out`() {
        val refusal = RowLimit.readRefusal("users")

        assertTrue("'users'" in refusal, refusal)
        assertTrue("${RowLimit.ROWS}" in refusal, refusal)
        assertTrue("where" in refusal && "select page" in refusal, refusal)
    }

    @Test
    fun `a page refusal names the page that was asked for and the ceiling`() {
        val refusal = RowLimit.pageRefusal(90_000)

        assertTrue("90000" in refusal, refusal)
        assertTrue("${RowLimit.ROWS}" in refusal, refusal)
    }
}
