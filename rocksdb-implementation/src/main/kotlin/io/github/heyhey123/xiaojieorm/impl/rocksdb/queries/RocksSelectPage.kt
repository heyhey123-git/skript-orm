package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.rocksdb.condition.RocksConditionTranslator
import io.github.heyhey123.xiaojieorm.impl.rocksdb.condition.TranslateResult
import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.result.RocksDataCursor
import io.github.heyhey123.xiaojieorm.queries.SelectPage
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.table.Table

class RocksSelectPage(
    pageSize: Int,
    pageIndex: Int,
    where: WhereClause?,
    override val database: RocksdbDatabase
) : SelectPage(pageSize, pageIndex, where), RocksQuery {
    override suspend fun execute(table: Table): CursorResult {
        require(pageSize > 0) { "Page size must be positive." }
        require(pageIndex >= 1) { "Page index must be 1-based and positive." }
        val conditions = RocksConditionTranslator.translate(where, table)
        val cfHandle = database.columnFamilyHandles[table.name]
            ?: throw IllegalStateException("Column family for table ${table.name} not found")

        when (conditions) {
            is TranslateResult.PrimaryKeyLookup -> {
                RocksQueryHelper.lookupByPrimaryKey(table, conditions.pkValue, database.database!!, cfHandle)
                    ?.let { row ->
                        return CursorResult(RocksDataCursor(listOf(row)))
                    }
                return CursorResult(RocksDataCursor(emptyList()))
            }

            is TranslateResult.ScanWithFilter -> {
                val skip = Math.multiplyExact(pageSize, pageIndex - 1)
                val rows = RocksQueryHelper.scanWithFilter(
                    table,
                    conditions.predicate,
                    database.database!!,
                    cfHandle,
                    limit = pageSize,
                    skip = skip
                )
                return CursorResult(RocksDataCursor(rows))
            }
        }
    }
}
