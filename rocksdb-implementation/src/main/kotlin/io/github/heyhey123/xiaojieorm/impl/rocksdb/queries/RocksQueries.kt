package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.queries.Queries

class RocksQueries(
    val database: RocksdbDatabase
) : Queries {

    override fun selectById(id: Any) = RocksSelectById(id, database)

    override fun selectOne(where: WhereClause?) = RocksSelectOne(where, database)

    override fun selectMany(where: WhereClause?) = RocksSelectMany(where, database)

    override fun selectPage(
        pageSize: Int,
        pageIndex: Int,
        where: WhereClause?
    ) = RocksSelectPage(pageSize, pageIndex, where, database)

    override fun insertOne(values: Map<String, Any?>) = RocksInsertOne(values, database)

    override fun insertMany(valuesList: List<Map<String, Any?>>) =
        RocksInsertMany(valuesList, database)

    override fun insertIfAbsent(values: Map<String, Any?>) = RocksInsertIfAbsent(values, database)

    override fun update(
        values: Map<String, Any?>,
        limit: Int?,
        where: WhereClause?
    ) = RocksUpdate(values, limit, where, database)

    override fun updateById(id: Any, values: Map<String, Any?>) = RocksUpdateById(id, values, database)

    override fun upsertById(id: Any, values: Map<String, Any?>) = RocksUpsertById(id, values, database)

    override fun delete(limit: Int?, where: WhereClause?) = RocksDelete(limit, where, database)

    override fun deleteById(id: Any) = RocksDeleteById(id, database)
}
