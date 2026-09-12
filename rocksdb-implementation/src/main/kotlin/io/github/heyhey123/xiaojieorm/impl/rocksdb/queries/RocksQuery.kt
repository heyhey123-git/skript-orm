package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase

/**
 * Rocks query interface.
 *
 */
interface RocksQuery {
    /**
     * The RocksDB database instance.
     */
    val database: RocksdbDatabase
}
