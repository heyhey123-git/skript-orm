package io.github.heyhey123.xiaojieorm.skript.utils

import ch.njol.skript.config.SectionNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Pins the shape Skript's config parser produces, because both parsers are written against it.
 *
 * A section body arrives with each line as one node whose key is the whole line, which is why a value
 * is read from `node.key` and matched against `column: expression` rather than from a node's value.
 * If Skript ever stops parsing bodies this way, `values` and `where` blocks break, and this test says
 * so directly instead of leaving a puzzle inside a parser failure.
 */
class SkriptConfigProbeTest : SkriptConfigTestBase() {

    @Test
    fun `a section body keeps the whole line as the node key`() {
        val values = body(
            """
            values:
            ${TAB}name: "Alice"
            ${TAB}nickname: null
            """.trimIndent()
        )

        assertEquals(listOf("name: \"Alice\"", "nickname: null"), values.map { it.key })
        assertTrue(values.none { it is SectionNode }, "flat lines must not become sections")
    }

    @Test
    fun `an indented line that ends with a colon becomes a nested section`() {
        val rows = body(
            """
            values:
            ${TAB}1:
            ${TAB}${TAB}name: "Alice"
            """.trimIndent()
        )

        assertIs<SectionNode>(rows.single())
        assertEquals(listOf("name: \"Alice\""), (rows.single() as SectionNode).toList().map { it.key })
    }

    @Test
    fun `a section header keeps its own text as the key`() {
        assertEquals("values", section("values:\n${TAB}name: x").key)
        assertEquals("values extra", section("values extra:\n${TAB}name: x").key)
    }

    private companion object {
        const val TAB = "\t"
    }
}
