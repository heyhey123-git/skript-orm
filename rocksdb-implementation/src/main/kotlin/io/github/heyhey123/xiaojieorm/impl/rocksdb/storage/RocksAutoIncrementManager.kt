package io.github.heyhey123.xiaojieorm.impl.rocksdb.storage

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.table.Table
import org.rocksdb.ColumnFamilyHandle
import java.nio.ByteBuffer

/**
 * 自增ID持久化管理器
 * 使用特殊的 key 前缀存储每个表每列的当前自增值
 */
object RocksAutoIncrementManager {

    private const val AUTO_INCREMENT_PREFIX = "__auto_increment__:"

    /**
     * 获取并递增自增值
     */
    fun getAndIncrement(
        database: RocksdbDatabase,
        cfHandle: ColumnFamilyHandle,
        table: Table,
        columnName: String
    ): Long {
        val key = encodeKey(table.name, columnName)
        val db = database.database!!

        // 读取当前值
        val currentBytes = db.get(cfHandle, key)
        val currentValue = if (currentBytes != null) {
            ByteBuffer.wrap(currentBytes).getLong()
        } else {
            0L
        }

        // 递增并写回
        val newValue = currentValue + 1
        val newBytes = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(newValue).array()
        db.put(cfHandle, key, newBytes)

        return newValue
    }

    fun getAndIncrementBatch(database: RocksdbDatabase, cfHandle: ColumnFamilyHandle, table: Table, columnName: String, count: Int): LongRange {
        val key = encodeKey(table.name, columnName)
        val db = database.database!!

        // 读取当前值
        val currentBytes = db.get(cfHandle, key)
        val currentValue = if (currentBytes != null) {
            ByteBuffer.wrap(currentBytes).getLong()
        } else {
            0L
        }

        // 计算新的范围
        val newValue = currentValue + count
        val newBytes = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(newValue).array()
        db.put(cfHandle, key, newBytes)

        return (currentValue + 1)..newValue
    }


    /**
     * 获取当前自增值（不递增）
     */
    fun getCurrent(
        database: RocksdbDatabase,
        cfHandle: ColumnFamilyHandle,
        table: Table,
        columnName: String
    ): Long {
        val key = encodeKey(table.name, columnName)
        val bytes = database.database!!.get(cfHandle, key) ?: return 0L
        return ByteBuffer.wrap(bytes).getLong()
    }

    /**
     * 设置自增值（用于初始化或重置）
     */
    fun set(
        database: RocksdbDatabase,
        cfHandle: ColumnFamilyHandle,
        table: Table,
        columnName: String,
        value: Long
    ) {
        val key = encodeKey(table.name, columnName)
        val bytes = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(value).array()
        database.database!!.put(cfHandle, key, bytes)
    }

    private fun encodeKey(tableName: String, columnName: String): ByteArray {
        return "$AUTO_INCREMENT_PREFIX$tableName.$columnName".toByteArray(Charsets.UTF_8)
    }
}

