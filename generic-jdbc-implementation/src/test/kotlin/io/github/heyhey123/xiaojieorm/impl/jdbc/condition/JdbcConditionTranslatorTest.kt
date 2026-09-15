package io.github.heyhey123.xiaojieorm.impl.jdbc.condition

import io.github.heyhey123.xiaojieorm.condition.Condition
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.GenericJdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.IntJdbcDataType
import io.mockk.mockk
import io.mockk.verify
import java.sql.JDBCType
import java.sql.PreparedStatement
import kotlin.test.Test
import kotlin.test.assertEquals

class JdbcConditionTranslatorTest {
    @Test
    fun `translate renders every comparison and null equality`() {
        val conditions = listOf(
            Condition.Equals("a", null) to "\"a\" IS NULL",
            Condition.NotEquals("a", null) to "\"a\" IS NOT NULL",
            Condition.Equals("a", 1) to "\"a\" = ?",
            Condition.NotEquals("a", 1) to "\"a\" <> ?",
            Condition.GreaterThan("a", 1) to "\"a\" > ?",
            Condition.GreaterThanOrEquals("a", 1) to "\"a\" >= ?",
            Condition.LessThan("a", 1) to "\"a\" < ?",
            Condition.LessThanOrEquals("a", 1) to "\"a\" <= ?",
            Condition.Between("a", 1, 2) to "\"a\" BETWEEN ? AND ?"
        )

        conditions.forEach { (condition, expected) ->
            assertEquals(expected, JdbcConditionTranslator.translateCondition(condition, GenericJdbcDialect))
        }
    }

    @Test
    fun `translate preserves condition order conjunction and group negation`() {
        val conditions = listOf(Condition.Equals("first", 1), Condition.LessThan("second", 2))

        assertEquals(
            "WHERE \"first\" = ? AND \"second\" < ?",
            JdbcConditionTranslator.translate(WhereClause.All(false, conditions), GenericJdbcDialect)
        )
        assertEquals(
            "WHERE NOT( \"first\" = ? OR \"second\" < ? )",
            JdbcConditionTranslator.translate(WhereClause.Any(true, conditions), GenericJdbcDialect)
        )
    }

    @Test
    fun `fill parameters skips null comparisons and binds between in order`() {
        val statement = mockk<PreparedStatement>(relaxed = true)
        val type = IntJdbcDataType()

        var offset = JdbcConditionTranslator.fillConditionParameters(
            Condition.Equals("value", null),
            statement,
            3,
            type
        )
        offset = JdbcConditionTranslator.fillConditionParameters(
            Condition.Between("value", 10, 20),
            statement,
            offset,
            type
        )

        assertEquals(5, offset)
        verify(exactly = 1) { statement.setObject(3, 10, JDBCType.INTEGER) }
        verify(exactly = 1) { statement.setObject(4, 20, JDBCType.INTEGER) }
    }
}
