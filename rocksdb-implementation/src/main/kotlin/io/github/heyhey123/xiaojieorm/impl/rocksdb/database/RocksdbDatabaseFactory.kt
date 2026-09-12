package io.github.heyhey123.xiaojieorm.impl.rocksdb.database

import io.github.heyhey123.xiaojieorm.database.DatabaseFactory
import io.github.heyhey123.xiaojieorm.database.DatabaseRegistry
import org.rocksdb.RocksDB

object RocksdbDatabaseFactory : DatabaseFactory {

    init {
        RocksDB.loadLibrary()
        DatabaseRegistry.register(this)
    }

    override val typeName: String = "RocksDB"

    override fun create(properties: Map<String, String>) = RocksdbDatabase()
}
