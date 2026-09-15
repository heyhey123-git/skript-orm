package io.github.heyhey123.xiaojieorm.impl.rocksdb.database

import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.impl.rocksdb.queries.RocksQueries
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.impl.rocksdb.type.RocksDataTypes
import io.github.heyhey123.xiaojieorm.table.Table
import org.rocksdb.ColumnFamilyDescriptor
import org.rocksdb.ColumnFamilyHandle
import org.rocksdb.DBOptions
import org.rocksdb.Options
import org.rocksdb.RocksDB
import org.rocksdb.RocksDBException
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

/**
 * RocksDB-backed database implementation.
 *
 * Each registered table uses its own column family. Internal state, such as
 * auto-increment high-water marks, is stored in a dedicated metadata column family.
 */
class RocksdbDatabase : Database() {

    companion object {
        /** Name of the column family reserved for ORM metadata. */
        const val METADATA_COLUMN_FAMILY = "__xiaojieorm_metadata__"

        private val DEFAULT_COLUMN_FAMILY =
            RocksDB.DEFAULT_COLUMN_FAMILY.toString(StandardCharsets.UTF_8)
    }

    private val lifecycleLock = Any()
    private val mutationLock = Any()
    private val requestedTables: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private var options: DBOptions? = null

    /** Filesystem path of the currently configured RocksDB database. */
    var databasePath: String? = null
        private set

    /**
     * Lazily opened RocksDB instance, or `null` while disconnected.
     * Opening is serialized by [lifecycleLock].
     */
    val database: RocksDB?
        get() {
            if (!isConnected) return null
            return synchronized(lifecycleLock) {
                internalDatabase ?: openDatabase().also { internalDatabase = it }
            }
        }

    /** Underlying database instance after it has been opened. */
    var internalDatabase: RocksDB? = null
        private set

    /** Open column-family handles indexed by their UTF-8 names. */
    val columnFamilyHandles: MutableMap<String, ColumnFamilyHandle> = ConcurrentHashMap()

    /** Handle of the ORM-owned metadata column family. */
    val metadataColumnFamilyHandle: ColumnFamilyHandle
        get() {
            database ?: error("Database is not connected")
            return columnFamilyHandles[METADATA_COLUMN_FAMILY]
                ?: error("Metadata column family is not available")
        }

    override val dataTypes: RocksDataTypes = RocksDataTypes

    /**
     * Serializes mutation read-check-write sequences for this database instance.
     * RocksDB WriteBatch keeps each final multi-key write atomic.
     */
    internal fun <T> withMutationLock(block: () -> T): T = synchronized(mutationLock, block)

    override fun doConnect(url: String, user: String, password: String) {
        databasePath = url
        queries = RocksQueries(this)
    }

    private fun openDatabase(): RocksDB {
        val path = checkNotNull(databasePath) { "Database path is not set." }
        val columnFamilyNames = collectColumnFamilyNames(path)
        val descriptors = columnFamilyNames.map(::createDescriptor)
        val handles = mutableListOf<ColumnFamilyHandle>()
        val dbOptions = createDatabaseOptions()

        return try {
            openDatabase(path, columnFamilyNames, descriptors, handles, dbOptions)
        } catch (exception: Exception) {
            handles.forEach(ColumnFamilyHandle::close)
            dbOptions.close()
            throw exception
        }
    }

    private fun collectColumnFamilyNames(path: String): List<String> =
        linkedSetOf<String>().apply {
            add(DEFAULT_COLUMN_FAMILY)
            add(METADATA_COLUMN_FAMILY)
            addAll(listExistingColumnFamilies(path))
            addAll(requestedTables)
        }.toList()

    private fun listExistingColumnFamilies(path: String): Set<String> =
        Options().use { listOptions ->
            try {
                RocksDB.listColumnFamilies(listOptions, path)
                    .map { String(it, StandardCharsets.UTF_8) }
                    .toSet()
            } catch (_: RocksDBException) {
                setOf(DEFAULT_COLUMN_FAMILY)
            }
        }

    private fun createDescriptor(name: String): ColumnFamilyDescriptor =
        ColumnFamilyDescriptor(name.toByteArray(StandardCharsets.UTF_8))

    private fun createDatabaseOptions(): DBOptions = DBOptions().apply {
        setCreateIfMissing(true)
        setCreateMissingColumnFamilies(true)
    }

    private fun openDatabase(
        path: String,
        names: List<String>,
        descriptors: List<ColumnFamilyDescriptor>,
        handles: MutableList<ColumnFamilyHandle>,
        dbOptions: DBOptions
    ): RocksDB = RocksDB.open(dbOptions, path, descriptors, handles).also {
        options = dbOptions
        names.zip(handles).forEach { (name, handle) ->
            columnFamilyHandles[name] = handle
        }
    }

    override fun doDisconnect() {
        synchronized(lifecycleLock) {
            RocksRowValueCodec.cache.clear()
            closeColumnFamilies()
            closeDatabase()
            requestedTables.clear()
            databasePath = null
        }
    }

    private fun closeColumnFamilies() {
        columnFamilyHandles.values.forEach(ColumnFamilyHandle::close)
        columnFamilyHandles.clear()
    }

    private fun closeDatabase() {
        internalDatabase?.close()
        internalDatabase = null
        options?.close()
        options = null
    }

    override suspend fun doRegisterTable(table: Table) {
        synchronized(lifecycleLock) {
            registerColumnFamily(table.name)
        }
    }

    private fun registerColumnFamily(tableName: String) {
        requestedTables.add(tableName)
        val db = database ?: return
        if (tableName !in columnFamilyHandles) {
            columnFamilyHandles[tableName] = db.createColumnFamily(createDescriptor(tableName))
        }
    }
}
