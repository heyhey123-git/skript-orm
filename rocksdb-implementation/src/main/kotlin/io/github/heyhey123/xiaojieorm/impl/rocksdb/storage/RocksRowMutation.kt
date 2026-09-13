package io.github.heyhey123.xiaojieorm.impl.rocksdb.storage

import io.github.heyhey123.xiaojieorm.table.Table

/** Builds complete, validated row maps for mutation queries. */
internal object RocksRowMutation {

    fun merge(
        table: Table,
        existing: Map<String, Any?>,
        changes: Map<String, Any?>,
        primaryKeyValue: Any? = null
    ): Map<String, Any?> {
        requireKnownColumns(table, changes)
        val result = existing.toMutableMap().apply {
            putAll(changes)
            if (primaryKeyValue != null) {
                table.primaryKey?.let { this[it.name] = primaryKeyValue }
            }
        }
        validate(table, result)
        return result
    }

    fun requireKnownColumns(table: Table, values: Map<String, Any?>) {
        val unknown = values.keys.firstOrNull { it !in table.columns }
        require(unknown == null) {
            "Column `$unknown` does not exist in table `${table.name}`."
        }
    }

    fun validate(table: Table, values: Map<String, Any?>) {
        table.columns.forEach { (name, column) ->
            require(column.isNullable || values[name] != null) {
                "Column $name cannot be null"
            }
        }
    }
}
