package io.github.heyhey123.xiaojieorm.impl.rocksdb.database

import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.impl.rocksdb.queries.RocksQueries
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.impl.rocksdb.type.RocksDataTypes
import io.github.heyhey123.xiaojieorm.table.Table
import kotlinx.coroutines.Job
import org.rocksdb.ColumnFamilyDescriptor
import org.rocksdb.ColumnFamilyHandle
import org.rocksdb.DBOptions
import org.rocksdb.Options
import org.rocksdb.RocksDB
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

class RocksdbDatabase : Database() {

    companion object {
        const val METADATA_COLUMN_FAMILY = "__xiaojieorm_metadata__"
    }

    /**
     * Lifecycle lock used to ensure that the database is only opened once.
     */
    private val lifecycleLock = Any()

    var databasePath: String? = null
        private set

    val database: RocksDB?
        get() {
            if (!isConnected) return null
            synchronized(lifecycleLock) {
                if (internalDatabase == null) {
                    internalDatabase = openDatabase()
                }
                return internalDatabase
            }
        }

    var internalDatabase: RocksDB? = null
        private set

    val columnFamilyHandles: MutableMap<String, ColumnFamilyHandle> = ConcurrentHashMap()

    val metadataColumnFamilyHandle: ColumnFamilyHandle
        get() {
            database ?: error("Database is not connected")
            return columnFamilyHandles[METADATA_COLUMN_FAMILY]
                ?: error("Metadata column family is not available")
        }

    private val requestedTables: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private var options: DBOptions? = null

    override val dataTypes: RocksDataTypes = RocksDataTypes

    override fun doConnect(url: String, user: String, password: String) {
        databasePath = url
        queries = RocksQueries(this)
    }

    private fun openDatabase(): RocksDB {
        val path = checkNotNull(databasePath) { "Database path is not set." }

        val existingColumnFamilies = Options().use { listOptions ->
            try {
                RocksDB.listColumnFamilies(listOptions, path)
                    .map { String(it, StandardCharsets.UTF_8) }
                    .toSet()
            } catch (_: org.rocksdb.RocksDBException) {
                setOf(RocksDB.DEFAULT_COLUMN_FAMILY.toString(StandardCharsets.UTF_8))
            }
        }

        val allColumnFamilies = linkedSetOf<String>().apply {
            add(RocksDB.DEFAULT_COLUMN_FAMILY.toString(StandardCharsets.UTF_8))
            add(METADATA_COLUMN_FAMILY)
            addAll(existingColumnFamilies)
            addAll(requestedTables)
        }

        val descriptors = allColumnFamilies.map { name ->
            ColumnFamilyDescriptor(name.toByteArray(StandardCharsets.UTF_8))
        }
        val handles = mutableListOf<ColumnFamilyHandle>()
        val dbOptions = DBOptions().apply {
            setCreateIfMissing(true)
            setCreateMissingColumnFamilies(true)
        }

        return try {
            RocksDB.open(dbOptions, path, descriptors, handles).also { db ->
                options = dbOptions
                allColumnFamilies.zip(handles).forEach { (name, handle) ->
                    columnFamilyHandles[name] = handle
                }
            }
        } catch (exception: Exception) {
            handles.forEach(ColumnFamilyHandle::close)
            dbOptions.close()
            throw exception
        }
    }

    override fun doDisconnect() {
        synchronized(lifecycleLock) {
            RocksRowValueCodec.cache.clear()

            columnFamilyHandles.values.forEach(ColumnFamilyHandle::close)
            columnFamilyHandles.clear()

            internalDatabase?.close()
            internalDatabase = null

            options?.close()
            options = null

            requestedTables.clear()
            databasePath = null
        }
    }

    override fun doRegisterTable(table: Table): Job {
        synchronized(lifecycleLock) {
            val tableName = table.name
            requestedTables.add(tableName)

            val db = database
            if (db != null && tableName !in columnFamilyHandles) {
                val descriptor = ColumnFamilyDescriptor(tableName.toByteArray(StandardCharsets.UTF_8))
                columnFamilyHandles[tableName] = db.createColumnFamily(descriptor)
            }
        }

        return Job().apply { complete() }
    }
}
