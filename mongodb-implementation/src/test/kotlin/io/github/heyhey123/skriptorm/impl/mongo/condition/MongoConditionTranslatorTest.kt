package io.github.heyhey123.skriptorm.impl.mongo.condition

import com.mongodb.MongoClientSettings
import io.github.heyhey123.skriptorm.condition.Condition
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.impl.mongo.type.UuidMongoConverter
import io.github.heyhey123.skriptorm.impl.mongo.type.UuidMongoDataType
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.DoubleDataType
import io.github.heyhey123.skriptorm.type.IntDataType
import io.github.heyhey123.skriptorm.type.StringDataType
import org.bson.BsonBinary
import org.bson.BsonDocument
import org.bson.conversions.Bson
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

/**
 * Covers the filter a where clause becomes, without a server.
 *
 * Two things here are worth more than the shape of the filter. Every operand has to be converted the way
 * the column stores it, or the filter compares a uuid against bytes and quietly matches nothing; the uuid
 * case below fails loudly if that conversion is ever dropped, because the driver cannot encode a raw
 * `UUID`. And a negated clause has to become `$nor`: `$not` is a field operator, so a top level `$not` is
 * refused by the server rather than being a wrong answer that only shows up as a missing row.
 */
class MongoConditionTranslatorTest {

    private val table = Table(
        "users",
        listOf(
            Column("id", IntDataType(), isPrimaryKey = true),
            Column("name", StringDataType()),
            Column("uid", UuidMongoDataType),
            Column("score", DoubleDataType())
        )
    )

    @Test
    fun `an equality becomes a field comparison`() {
        assertEquals(BsonDocument.parse("""{"id": 1}"""), condition(Condition.Equals("id", 1)))
    }

    @Test
    fun `a comparison keeps its operator`() {
        assertEquals(BsonDocument.parse("""{"score": {"${'$'}gt": 1.5}}"""), condition(Condition.GreaterThan("score", 1.5)))
        assertEquals(BsonDocument.parse("""{"score": {"${'$'}gte": 1.5}}"""), condition(Condition.GreaterThanOrEquals("score", 1.5)))
        assertEquals(BsonDocument.parse("""{"score": {"${'$'}lt": 1.5}}"""), condition(Condition.LessThan("score", 1.5)))
        assertEquals(BsonDocument.parse("""{"score": {"${'$'}lte": 1.5}}"""), condition(Condition.LessThanOrEquals("score", 1.5)))
    }

    @Test
    fun `a between is its two comparisons`() {
        assertEquals(
            BsonDocument.parse("""{"${'$'}and": [{"score": {"${'$'}gte": 1.5}}, {"score": {"${'$'}lte": 2.5}}]}"""),
            condition(Condition.Between("score", 1.5, 2.5))
        )
    }

    @Test
    fun `a null is compared with the operator MongoDB has for it`() {
        assertEquals(BsonDocument.parse("""{"name": null}"""), condition(Condition.Equals("name", null)))
        assertEquals(BsonDocument.parse("""{"name": {"${'$'}ne": null}}"""), condition(Condition.NotEquals("name", null)))
    }

    @Test
    fun `every clause joins its conditions with the operator it declares`() {
        val conditions = listOf(Condition.Equals("id", 1), Condition.Equals("name", "first"))

        assertEquals(
            BsonDocument.parse("""{"${'$'}and": [{"id": 1}, {"name": "first"}]}"""),
            clause(WhereClause.All(false, conditions))
        )
        assertEquals(
            BsonDocument.parse("""{"${'$'}or": [{"id": 1}, {"name": "first"}]}"""),
            clause(WhereClause.Any(false, conditions))
        )
    }

    @Test
    fun `a negated clause negates the whole filter`() {
        val conditions = listOf(Condition.Equals("id", 1), Condition.Equals("name", "first"))

        assertEquals(
            BsonDocument.parse("""{"${'$'}nor": [{"${'$'}and": [{"id": 1}, {"name": "first"}]}]}"""),
            clause(WhereClause.All(true, conditions))
        )
        assertEquals(
            BsonDocument.parse("""{"${'$'}nor": [{"${'$'}or": [{"id": 1}, {"name": "first"}]}]}"""),
            clause(WhereClause.Any(true, conditions))
        )
    }

    @Test
    fun `a uuid is compared as the bytes the column stores`() {
        val uuid = UUID.fromString("6f9619ff-8b86-d011-b42d-00cf4fc964ff")

        val stored = assertIs<BsonBinary>(condition(Condition.Equals("uid", uuid))["uid"])

        assertContentEquals(UuidMongoConverter.toStorage(uuid).data, stored.data)
    }

    @Test
    fun `a column the table does not declare is refused`() {
        val error = assertFailsWith<IllegalArgumentException> { condition(Condition.Equals("nope", 1)) }

        assertEquals("Table users does not have column nope.", error.message)
    }

    @Test
    fun `a value the column cannot hold is refused`() {
        val error = assertFailsWith<IllegalArgumentException> { condition(Condition.Equals("id", "1")) }

        assertEquals(
            "Value for type int must be java.lang.Integer, but was java.lang.String.",
            error.message
        )
    }

    private fun condition(condition: Condition): BsonDocument =
        bson(MongoConditionTranslator.translateCondition(condition, table))

    private fun clause(where: WhereClause): BsonDocument =
        bson(MongoConditionTranslator.translate(where, table))

    private fun bson(filter: Bson): BsonDocument =
        filter.toBsonDocument(BsonDocument::class.java, MongoClientSettings.getDefaultCodecRegistry())
}
