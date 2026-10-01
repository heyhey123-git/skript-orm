package io.github.heyhey123.skriptorm.skript.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The ceiling itself, and the sentences a script is left with when a statement crosses it.
 *
 * These are unit tests because they are the whole of the contract: the number, the one-row probe that tells
 * "too many" from "exactly the ceiling", and the three messages. What each element does with them is covered
 * against a real server instead.
 */
class RowLimitTest {

    @Test
    fun `a select asks for one row past the ceiling`() {
        // Asking for the ceiling would make "exactly 5000 rows" and "more than 5000 rows, cut off at 5000"
        // the same answer, and one of those has to be refused while the other may be stored.
        assertEquals(RowLimit.ROWS + 1, RowLimit.PROBE_ROWS)
    }

    @Test
    fun `a write under the ceiling sends every row and says nothing`() {
        val rows = List(RowLimit.ROWS) { it }
        val warnings = mutableListOf<String>()

        val sent = RowLimit.batch(rows, "the values block") { warnings += it }

        assertSame(rows, sent, "a batch that fits is sent as it is, not copied")
        assertTrue(warnings.isEmpty(), "a batch that fits is not worth a warning: $warnings")
    }

    @Test
    fun `a write over the ceiling sends its first rows and warns about the tail`() {
        val given = RowLimit.ROWS + 250
        val rows = List(given) { it }
        val warnings = mutableListOf<String>()

        val sent = RowLimit.batch(rows, "the list variable {_rows::*}") { warnings += it }

        assertEquals(RowLimit.ROWS, sent.size)
        assertEquals(rows.take(RowLimit.ROWS), sent)
        assertEquals(1, warnings.size, "the tail is dropped once, in one warning: $warnings")
        val warning = warnings.single()
        assertTrue("$given" in warning, "the warning says how many rows were given: $warning")
        assertTrue("${RowLimit.ROWS}" in warning, "the warning says how many are written: $warning")
        assertTrue("{_rows::*}" in warning, "the warning names the source to split: $warning")
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

    @Test
    fun `a write truncation names the batch the ceiling and the count to check`() {
        val truncation = RowLimit.writeTruncation(RowLimit.ROWS + 1, "the values block")

        assertTrue("${RowLimit.ROWS + 1}" in truncation, truncation)
        assertTrue("${RowLimit.ROWS}" in truncation, truncation)
        assertTrue("the values block" in truncation, truncation)
        assertTrue("affected row count" in truncation, truncation)
    }
}
