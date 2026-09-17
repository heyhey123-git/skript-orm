package io.github.heyhey123.skriptorm.skript.utils

import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.IntDataType
import io.github.heyhey123.skriptorm.type.StringDataType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Covers the parse phase of a `values` block against Skript's real config parser.
 *
 * Everything here runs before any expression is evaluated, so it needs no booted Skript. It still
 * cannot cover the `bind` path for a real value, because parsing an expression needs Skript's syntax
 * registry; only the literal `null` form and the error paths are reachable without a running server.
 */
class ValuesParserTest : SkriptConfigTestBase() {

    private val users = Table(
        "users",
        listOf(
            Column("id", IntDataType(), isPrimaryKey = true),
            Column("name", StringDataType()),
            Column("nickname", StringDataType())
        )
    )

    // ------------------------------------------------------------------ header detection

    @Test
    fun `a values block is recognised whatever its case`() {
        assertTrue(ValuesParser.isValuesSection(section("values:\n${TAB}name: x")))
        assertTrue(ValuesParser.isValuesSection(section("VALUES:\n${TAB}name: x")))
        assertTrue(ValuesParser.isValuesSection(section("Values:\n${TAB}name: x")))
    }

    @Test
    fun `a mistyped values header is still recognised so it can be reported`() {
        assertTrue(ValuesParser.isValuesSection(section("values extra:\n${TAB}name: x")))
    }

    @Test
    fun `a column that merely starts with values is not a values block`() {
        assertFalse(ValuesParser.isValuesSection(firstNode("values_count: 1")))
        assertFalse(ValuesParser.isValuesSection(section("where:\n${TAB}name = null")))
    }

    @Test
    fun `the header check accepts the bare keyword only`() {
        ValuesParser.requireValuesHeader(section("values:\n${TAB}name: x"))

        val thrown = assertFailsWith<IllegalArgumentException> {
            ValuesParser.requireValuesHeader(section("values extra:\n${TAB}name: x"))
        }
        assertEquals("Invalid values section 'values extra'. Expected 'values'.", thrown.message)
    }

    // ------------------------------------------------------------------ collecting values

    @Test
    fun `a flat body becomes one row of column and expression pairs`() {
        val raw = ValuesParser.collectSingle(
            body(
                """
                values:
                ${TAB}name: "Alice"
                ${TAB}nickname: "Al"
                """.trimIndent()
            )
        )

        assertEquals(
            listOf("name" to "\"Alice\"", "nickname" to "\"Al\""),
            raw.values.map { it.columnName to it.rawExpression }
        )
    }

    @Test
    fun `spacing around the colon is ignored`() {
        val spaced = ValuesParser.collectSingle(body("values:\n${TAB}name : \"Alice\""))
        assertEquals("name", spaced.values.single().columnName)
        assertEquals("\"Alice\"", spaced.values.single().rawExpression)

        val padded = ValuesParser.collectSingle(body("values:\n${TAB}name:    \"Alice\""))
        assertEquals("name", padded.values.single().columnName)
        assertEquals("\"Alice\"", padded.values.single().rawExpression)
    }

    @Test
    fun `unicode column names are accepted`() {
        val raw = ValuesParser.collectSingle(body("values:\n${TAB}名前: \"値\""))

        assertEquals("名前", raw.values.single().columnName)
        assertEquals("\"値\"", raw.values.single().rawExpression)
    }

    @Test
    fun `a line that is not column colon expression is rejected`() {
        val thrown = assertFailsWith<IllegalArgumentException> {
            ValuesParser.collectSingle(body("values:\n${TAB}name \"Alice\""))
        }

        assertEquals(
            "Invalid value 'name \"Alice\"'. Expected 'column: expression'.",
            thrown.message
        )
    }

    @Test
    fun `a nested block is not a single value`() {
        val thrown = assertFailsWith<IllegalArgumentException> {
            ValuesParser.collectSingle(body("values:\n${TAB}details:\n${TAB}${TAB}name: \"Alice\""))
        }

        assertEquals("A value must be a single line, but 'details' is a block.", thrown.message)
    }

    @Test
    fun `an empty body is reported as no values at all`() {
        assertNull(ValuesParser.collect(emptyList(), supportsMultipleRows = false))
        assertNull(ValuesParser.collect(emptyList(), supportsMultipleRows = true))
    }

    @Test
    fun `a flat body is collected as a single row`() {
        val (single, rows) = ValuesParser.collect(
            body("values:\n${TAB}name: \"Alice\""),
            supportsMultipleRows = true
        )!!

        assertEquals(listOf("name"), single?.values?.map { it.columnName })
        assertNull(rows)
    }

    @Test
    fun `nested blocks are collected as one row each`() {
        val (single, rows) = ValuesParser.collect(
            body(
                """
                values:
                ${TAB}1:
                ${TAB}${TAB}name: "Alice"
                ${TAB}2:
                ${TAB}${TAB}name: "Bob"
                """.trimIndent()
            ),
            supportsMultipleRows = true
        )!!

        assertNull(single)
        assertEquals(
            listOf(listOf("name"), listOf("name")),
            rows?.valuesList?.map { row -> row.values.map { it.columnName } }
        )
    }

    @Test
    fun `a nested block is rejected where single values are expected`() {
        val thrown = assertFailsWith<IllegalArgumentException> {
            ValuesParser.collect(
                body("values:\n${TAB}1:\n${TAB}${TAB}name: \"Alice\""),
                supportsMultipleRows = false
            )
        }

        assertEquals("This write operation expects single values, but '1' is a block.", thrown.message)
    }

    @Test
    fun `rows and single values cannot be mixed`() {
        val thrown = assertFailsWith<IllegalArgumentException> {
            ValuesParser.collect(
                body(
                    """
                    values:
                    ${TAB}1:
                    ${TAB}${TAB}name: "Alice"
                    ${TAB}nickname: "Al"
                    """.trimIndent()
                ),
                supportsMultipleRows = true
            )
        }

        assertEquals(
            "Rows and single values cannot be mixed: 'nickname: \"Al\"' is not a row block.",
            thrown.message
        )
    }

    @Test
    fun `a row without values is rejected`() {
        val rows = body("values:\n${TAB}1:\n${TAB}2:\n${TAB}${TAB}name: \"Bob\"")

        val thrown = assertFailsWith<IllegalArgumentException> {
            ValuesParser.collectMultiple(rows.map { it as ch.njol.skript.config.SectionNode })
        }

        assertEquals("Row '1' must declare at least one value.", thrown.message)
    }

    // ------------------------------------------------------------------ binding to a table

    @Test
    fun `a literal null stays a present key whose value is null`() {
        val parsed = ValuesParser.collectSingle(body("values:\n${TAB}nickname: null")).bind(users)

        assertEquals(setOf("nickname"), parsed.values.keys)
        assertNull(parsed.values.getValue("nickname"))

        val resolved = parsed.resolve(null)
        assertEquals(setOf("nickname"), resolved.keys, "an omitted column must stay absent, never null")
        assertTrue(resolved.containsKey("nickname"), "a literal null must remain a present key")
        assertNull(resolved["nickname"])
    }

    @Test
    fun `a column the table does not declare is rejected while parsing`() {
        val literalNull = assertFailsWith<IllegalArgumentException> {
            ValuesParser.collectSingle(body("values:\n${TAB}missing: null")).bind(users)
        }
        assertEquals("Column 'missing' does not exist in table 'users'", literalNull.message)

        // The same typo next to a spelled-out value must fail at the same point, so the error
        // surfaces next to the line that caused it rather than when the statement runs.
        val spelledOut = assertFailsWith<IllegalArgumentException> {
            ValuesParser.collectSingle(body("values:\n${TAB}missing: 1")).bind(users)
        }
        assertEquals("Column 'missing' does not exist in table 'users'", spelledOut.message)
    }

    private companion object {
        const val TAB = "\t"
    }
}
