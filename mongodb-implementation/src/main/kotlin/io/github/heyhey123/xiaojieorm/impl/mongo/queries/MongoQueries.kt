package io.github.heyhey123.xiaojieorm.impl.mongo.queries

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.queries.SelectById
import io.github.heyhey123.xiaojieorm.queries.SelectMany
import io.github.heyhey123.xiaojieorm.queries.SelectOne

class MongoQueries(val database: MongoDatabase) : Queries {

    override fun selectById(id: Any): SelectById = MongoSelectById(id, database)

    override fun selectOne(where: WhereClause?): SelectOne = MongoSelectOne(where, database)

    override fun selectMany(where: WhereClause?): SelectMany = MongoSelectMany(where, database)

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
}
