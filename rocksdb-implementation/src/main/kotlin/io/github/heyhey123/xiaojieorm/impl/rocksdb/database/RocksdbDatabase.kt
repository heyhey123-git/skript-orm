package io.github.heyhey123.xiaojieorm.impl.rocksdb.database

import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.impl.rocksdb.queries.RocksQueries
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.impl.rocksdb.type.RocksDataTypes
import io.github.heyhey123.xiaojieorm.table.Table
import kotlinx.coroutines.Job
import org.rocksdb.*
import java.util.concurrent.ConcurrentHashMap

class RocksdbDatabase : Database() {

    var databasePath: String? = null

    val database: RocksDB?
        get() {
            if (!isConnected) return null
            if (internalDatabase == null) internalDatabase = openDatabase()
            return internalDatabase
        }

    var internalDatabase: RocksDB? = null
        private set

    val columnFamilyHandles: MutableMap<String, ColumnFamilyHandle> = ConcurrentHashMap()

    private val requestedTables: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private val options = DBOptions().apply {
        setCreateIfMissing(true)
        setCreateMissingColumnFamilies(true)
    }

    override val dataTypes: RocksDataTypes = RocksDataTypes

    override fun doConnect(url: String, user: String, password: String) {
        databasePath = url
        queries = RocksQueries(this)
    }

    private fun openDatabase(): RocksDB {
        check(databasePath != null) { "Database path is not set." }

        val existingColumnFamilies = try {
            RocksDB.listColumnFamilies(Options(), databasePath!!)
                .map { String(it) }
                .toSet()
        } catch (_: Exception) {
            setOf("default")
        }

        val allColumnFamilies = (existingColumnFamilies + requestedTables + "default").toSet()

        val descriptors = allColumnFamilies.map {
            ColumnFamilyDescriptor(it.toByteArray())
        }
        val handles = mutableListOf<ColumnFamilyHandle>()

        val database = RocksDB.open(
            options,
            databasePath!!,
            descriptors,
            handles
        )

        allColumnFamilies.zip(handles).forEach { (name, handle) ->
            columnFamilyHandles[name] = handle
        }

        return database
    }

    override fun doDisconnect() {
        RocksRowKeyEncoder.encodedPrimaryKeys.clear()
        RocksRowValueCodec.cache.clear()

        internalDatabase?.close()
        internalDatabase = null
        val iterator = columnFamilyHandles.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.close()
            iterator.remove()
        }
        requestedTables.clear()
        databasePath = null
    }

    override fun doRegisterTable(table: Table): Job {
        val completedJob = Job().apply { complete() }

        val tableName = table.name
        if (tableName in requestedTables) return completedJob

        val descriptor = ColumnFamilyDescriptor(tableName.toByteArray())
        requestedTables.add(tableName)

        if (database == null) return completedJob

        val handle = database!!.createColumnFamily(descriptor)
        columnFamilyHandles[tableName] = handle

        return completedJob
    }
}
