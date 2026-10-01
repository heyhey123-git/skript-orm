package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.client.MongoDatabase
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.queries.RawCommand
import io.github.heyhey123.skriptorm.queries.RawForm
import io.github.heyhey123.skriptorm.queries.SelectById
import io.github.heyhey123.skriptorm.queries.SelectMany
import io.github.heyhey123.skriptorm.queries.SelectOne

/**
 * The query factory of a MongoDB connection, which takes raw commands.
 *
 * [rawForm] is what tells a refusal which alternative to name; [rawQuery] and [rawUpdate] are deliberately
 * not overridden, so a SQL statement written for a relational backend is refused by the shared default
 * rather than parsed as a command document.
 */
class MongoQueries(
    val database: MongoDatabase,
    override val typeName: String = "MongoDB"
) : Queries {

    override val rawForm: RawForm = RawForm.COMMAND_DOCUMENT

    override fun selectById(id: Any): SelectById = MongoSelectById(id, database)

    override fun selectOne(where: WhereClause?): SelectOne = MongoSelectOne(where, database)

    override fun selectMany(where: WhereClause?): SelectMany = MongoSelectMany(where, null, database)

    override fun selectMany(where: WhereClause?, limit: Int): SelectMany = MongoSelectMany(where, limit, database)

    override fun selectPage(
        pageSize: Int,
        pageIndex: Int,
        where: WhereClause?
    ) = MongoSelectPage(pageSize, pageIndex, where, database)

    override fun insertOne(values: Map<String, Any?>) = MongoInsertOne(values, database)

    override fun insertMany(valuesList: List<Map<String, Any?>>) = MongoInsertMany(valuesList, database)

    override fun insertIfAbsent(values: Map<String, Any?>) = MongoInsertIfAbsent(values, database)

    override fun update(
        values: Map<String, Any?>,
        limit: Int?,
        where: WhereClause?
    ) = MongoUpdate(values, limit, where, database)

    override fun updateById(id: Any, values: Map<String, Any?>) = MongoUpdateById(id, values, database)

    override fun upsertById(id: Any, values: Map<String, Any?>) = MongoUpsertById(id, values, database)

    override fun delete(limit: Int?, where: WhereClause?) = MongoDelete(limit, where, database)

    override fun deleteById(id: Any) = MongoDeleteById(id, database)

    override fun rawCommand(command: String): RawCommand = MongoRawCommand(command, database)
}
