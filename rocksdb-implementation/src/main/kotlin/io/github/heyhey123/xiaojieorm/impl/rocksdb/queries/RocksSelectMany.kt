package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.rocksdb.condition.RocksConditionTranslator
import io.github.heyhey123.xiaojieorm.impl.rocksdb.condition.TranslateResult
import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.result.RocksDataCursor
import io.github.heyhey123.xiaojieorm.queries.SelectMany
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.table.Table

class RocksSelectMany(
    where: WhereClause?,
    override val database: RocksdbDatabase
) : SelectMany(where), RocksQuery {

    override suspend fun execute(table: Table): CursorResult {
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
                val rows = RocksQueryHelper.scanWithFilter(
                    table,
                    conditions.predicate,
                    database.database!!,
                    cfHandle
                )
                return CursorResult(RocksDataCursor(rows))
            }
        }
    }
}
