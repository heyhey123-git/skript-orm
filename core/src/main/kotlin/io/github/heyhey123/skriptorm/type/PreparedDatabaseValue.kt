package io.github.heyhey123.skriptorm.type

import io.github.heyhey123.skriptorm.condition.Condition
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.table.Table
import java.sql.Blob
import javax.sql.rowset.serial.SerialBlob

/** A converted storage value. Binders must not run its converter again. */
class PreparedDatabaseValue internal constructor(
    val type: DataType<*>,
    private val storage: Any,
    private val blob: Boolean
) {
    /** A fresh statement-owned Blob is created only when a JDBC binder needs one. */
    fun storageValue(): Any = if (blob) SerialBlob(storage as ByteArray) else storage

    /** JDBC accepts BLOB bytes directly, including drivers such as SQLite that cannot bind Blob objects. */
    fun jdbcValue(): Any = storage
}

/** Prepares an operation's inputs before its driver acquires resources. */
object StorageValues {

    suspend fun prepare(type: DataType<*>, value: Any?): Any? = prepareBatch(listOf(type to value)).single()

    suspend fun prepareBatch(values: List<Pair<DataType<*>, Any?>>): List<Any?> {
        val prepared = arrayOfNulls<Any>(values.size)
        val serverActions = mutableListOf<() -> Unit>()
        values.forEachIndexed { index, (type, value) ->
            if (value == null) return@forEachIndexed
            if (value is PreparedDatabaseValue) {
                require(value.type === type) { "A prepared value belongs to a different data type." }
                prepared[index] = value
                return@forEachIndexed
            }
            require(type.domainType.isInstance(value)) {
                "Value for type ${type.typeCode} must be ${type.domainType.name}, but was ${value.javaClass.name}."
            }
            @Suppress("UNCHECKED_CAST")
            val converter = type.converter as ValueConverter<Any, Any>
            val action = {
                val storage = converter.toStorage(value)
                val detached = when (storage) {
                    is Blob -> readAndFree(storage)
                    is ByteArray -> storage.copyOf()
                    else -> storage
                }
                prepared[index] = PreparedDatabaseValue(type, detached, storage is Blob)
            }
            if (converter.writeThread == ConversionThread.SERVER) serverActions += action else action()
        }
        if (serverActions.isNotEmpty()) ConversionWork.executeBatch(serverActions)
        return prepared.toList()
    }

    suspend fun rows(table: Table, rows: List<Map<String, Any?>>): List<Map<String, Any?>> {
        val inputs = rows.flatMap { row -> row.map { (key, value) -> columnType(table, key) to value } }
        val values = prepareBatch(inputs).iterator()
        return rows.map { row -> row.keys.associateWith { values.next() } }
    }

    suspend fun where(table: Table, where: WhereClause?): WhereClause? {
        if (where == null) return null
        val inputs = where.conditions.flatMap { condition ->
            operands(condition).map { columnType(table, condition.left) to it }
        }
        val values = prepareBatch(inputs).iterator()
        val conditions = where.conditions.map { condition ->
            val left = condition.left
            when (condition) {
                is Condition.Equals -> Condition.Equals(left, values.next())
                is Condition.NotEquals -> Condition.NotEquals(left, values.next())
                is Condition.Between -> Condition.Between(left, requireNotNull(values.next()), requireNotNull(values.next()))
                is Condition.GreaterThan -> Condition.GreaterThan(left, requireNotNull(values.next()))
                is Condition.GreaterThanOrEquals -> Condition.GreaterThanOrEquals(left, requireNotNull(values.next()))
                is Condition.LessThan -> Condition.LessThan(left, requireNotNull(values.next()))
                is Condition.LessThanOrEquals -> Condition.LessThanOrEquals(left, requireNotNull(values.next()))
            }
        }
        return when (where) {
            is WhereClause.All -> WhereClause.All(where.negated, conditions)
            is WhereClause.Any -> WhereClause.Any(where.negated, conditions)
        }
    }

    private fun columnType(table: Table, name: String): DataType<*> =
        requireNotNull(table.getColumnByName(name)) { "Table ${table.name} does not have column $name." }.type

    private fun operands(condition: Condition): List<Any?> = when (condition) {
        is Condition.Equals -> listOf(condition.right)
        is Condition.NotEquals -> listOf(condition.right)
        is Condition.Between -> listOf(condition.start, condition.end)
        is Condition.GreaterThan -> listOf(condition.right)
        is Condition.GreaterThanOrEquals -> listOf(condition.right)
        is Condition.LessThan -> listOf(condition.right)
        is Condition.LessThanOrEquals -> listOf(condition.right)
    }

    private fun readAndFree(blob: Blob): ByteArray {
        var failure: Throwable? = null
        try {
            return blob.binaryStream.use { it.readBytes() }
        } catch (error: Throwable) {
            failure = error
            throw error
        } finally {
            try {
                blob.free()
            } catch (error: Throwable) {
                if (failure != null) failure.addSuppressed(error) else throw error
            }
        }
    }
}
