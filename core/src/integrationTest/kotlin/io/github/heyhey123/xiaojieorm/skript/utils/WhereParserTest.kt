package io.github.heyhey123.xiaojieorm.skript.utils

import io.github.heyhey123.xiaojieorm.condition.Condition
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.type.IntDataType
import io.github.heyhey123.xiaojieorm.type.StringDataType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Covers the parse phase of a `where` block against Skript's real config parser.
 *
 * As in [ValuesParserTest], the reachable surface is the structural one plus the literal `null` and
 * error paths, because parsing a real value expression needs Skript's syntax registry.
 */
class WhereParserTest : SkriptConfigTestBase() {

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
    fun `a where block is recognised whatever its case and mode`() {
        assertTrue(WhereParser.isWhereSection(section("where:\n${TAB}name = null")))
        assertTrue(WhereParser.isWhereSection(section("where any:\n${TAB}name = null")))
        assertTrue(WhereParser.isWhereSection(section("WHERE NOT ALL:\n${TAB}name = null")))
    }

    @Test
    fun `a mistyped where header is still recognised so it can be reported`() {
        assertTrue(WhereParser.isWhereSection(section("where nonsense:\n${TAB}name = null")))
    }

    @Test
    fun `a column that merely starts with where is not a where block`() {
        assertFalse(WhereParser.isWhereSection(firstNode("where_clause: 1")))
        assertFalse(WhereParser.isWhereSection(section("values:\n${TAB}name: x")))
    }

    // ------------------------------------------------------------------ header parsing

    @Test
    fun `the bare header means all conditions are required`() {
        val clause = WhereParser.collectFromSection(section("where:\n${TAB}name = null"))!!

        assertEquals(listOf("name = null"), clause.conditions.map { it.statement })
        assertTrue(!clause.any)
        assertTrue(!clause.neg)
    }

    @Test
    fun `the all header means all conditions are required`() {
        val clause = WhereParser.collectFromSection(section("where all:\n${TAB}name = null"))!!

        assertTrue(!clause.any)
        assertTrue(!clause.neg)
    }

    @Test
    fun `the any header means one condition is enough`() {
        val clause = WhereParser.collectFromSection(section("where any:\n${TAB}name = null"))!!

        assertTrue(clause.any, "'where any' must combine conditions with OR")
        assertTrue(!clause.neg)
    }

    @Test
    fun `the negated all header negates the whole clause`() {
        val clause = WhereParser.collectFromSection(section("where not all:\n${TAB}name = null"))!!

        assertTrue(!clause.any)
        assertTrue(clause.neg)
    }

    @Test
    fun `the negated any header negates the whole clause`() {
        val clause = WhereParser.collectFromSection(section("where no any:\n${TAB}name = null"))!!

        assertTrue(clause.any)
        assertTrue(clause.neg)
    }

    @Test
    fun `an unknown header is reported instead of silently defaulting`() {
        val thrown = assertFailsWith<IllegalArgumentException> {
            WhereParser.collectFromSection(section("where nonsense:\n${TAB}name = null"))
        }

        assertEquals(
            "Invalid where section 'where nonsense'. Expected 'where', 'where any' or 'where all', " +
                "optionally negated with 'no' or 'not'.",
            thrown.message
        )
    }

    @Test
    fun `a section without conditions declares no clause`() {
        assertNull(WhereParser.collectFromSection(section("where:")))
    }

    @Test
    fun `a nested section is not a condition`() {
        val thrown = assertFailsWith<IllegalArgumentException> {
            WhereParser.collectFromSection(section("where:\n${TAB}nested:\n${TAB}${TAB}name = null"))
        }

        assertEquals("A where condition must be a single line, but 'nested' is a section.", thrown.message)
    }

    // ------------------------------------------------------------------ condition parsing

    @Test
    fun `equals against a literal null keeps the condition and drops the value`() {
        val condition = WhereParser.parseCondition(users, "name = null")

        val equals = assertIs<ParsedCondition.Equals>(condition)
        assertEquals("name", equals.columnName)
        assertNull(equals.valueExpr, "a literal null must not be parsed as an expression")
        assertEquals(Condition.Equals("name", null), equals.resolve(null))
    }

    @Test
    fun `not equals against a literal null keeps the condition and drops the value`() {
        val condition = WhereParser.parseCondition(users, "name != null")

        val notEquals = assertIs<ParsedCondition.NotEquals>(condition)
        assertNull(notEquals.valueExpr)
        assertEquals(Condition.NotEquals("name", null), notEquals.resolve(null))
    }

    @Test
    fun `a statement that is not a comparison is rejected`() {
        val thrown = assertFailsWith<IllegalArgumentException> {
            WhereParser.parseCondition(users, "just some words")
        }

        assertEquals("Invalid condition statement: 'just some words'", thrown.message)
    }

    @Test
    fun `a column the table does not declare is rejected while parsing`() {
        val thrown = assertFailsWith<IllegalArgumentException> {
            WhereParser.parseCondition(users, "missing = null")
        }

        assertEquals("Column 'missing' does not exist in table 'users'", thrown.message)
    }

    // ------------------------------------------------------------------ resolving a clause

    @Test
    fun `binding keeps the mode and the negation`() {
        val raw = WhereParser.collectFromSection(section("where any:\n${TAB}name = null"))!!

        val parsed = raw.bind(users)

        val any = assertIs<ParsedWhereClause.Any>(parsed)
        assertTrue(!any.neg)
        assertEquals(1, any.conditions.size)
    }

    @Test
    fun `resolving produces the matching core clause`() {
        val raw = WhereParser.collectFromSection(
            section("where not all:\n${TAB}name = null\n${TAB}nickname != null")
        )!!

        val resolved = raw.bind(users).resolve(null)

        val all = assertIs<WhereClause.All>(resolved)
        assertTrue(all.negated)
        assertEquals(
            listOf(Condition.Equals("name", null), Condition.NotEquals("nickname", null)),
            all.conditions
        )
    }

    private companion object {
        const val TAB = "\t"
    }
}
