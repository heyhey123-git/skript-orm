package io.github.heyhey123.skriptorm.impl.jdbc.database

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.sql.DatabaseMetaData
import java.sql.ResultSet

/**
 * A server's metadata, faked far enough for [SchemaVerification] to read a table out of it.
 *
 * The comparison itself is tested against values; this exists for the part that asks the server *which*
 * table to compare, which is the part a real server can get wrong and did: MySQL's catalogue search, run
 * with no catalogue pattern, searches every schema it can see, and `performance_schema.USER` was taken for
 * a registered `users`. That cannot be reproduced without a MySQL, so the answers are arranged here.
 *
 * The interfaces are implemented through [Proxy] rather than by a mocking library, because what is being
 * faked is not a collaborator but a data source: the methods answer from lists, and the only behaviour that
 * matters is which rows come back for which pattern.
 */
internal class FakeMetadata(
    private val catalog: String?,
    private val tables: List<FakeTable>,
    val columns: List<FakeColumn>,
    private val primaryKey: List<Pair<String, String>>
) {

    /** Every column lookup, in order, so a test can assert which table was actually read. */
    val columnsAskedFor = mutableListOf<String>()

    /** Every catalogue pattern the table lookup ran with, which is what the fix is about. */
    val catalogPatterns = mutableListOf<String?>()

    val metadata: DatabaseMetaData = proxy(DatabaseMetaData::class.java) { method, args ->
        when (method.name) {
            "getConnection" -> connection()
            "getTables" -> {
                val catalogPattern = arg(args, 0)
                catalogPatterns += catalogPattern
                resultSet(tablesMatching(catalogPattern, arg(args, 2)).map { it.row() })
            }

            "getColumns" -> {
                val requested = arg(args, 2).orEmpty()
                columnsAskedFor += requested
                resultSet(
                    columns.filter { it.table.equals(requested, ignoreCase = true) }
                        .map { it.row() }
                )
            }

            "getPrimaryKeys" -> {
                val requested = arg(args, 2).orEmpty()
                resultSet(
                    primaryKey.filter { it.first.equals(requested, ignoreCase = true) }
                        .map { mapOf<String, Any?>("COLUMN_NAME" to it.second) }
                )
            }

            else -> defaultValue(method.returnType)
        }
    }

    /**
     * The tables a metadata call answers with.
     *
     * A null catalogue pattern means "every catalogue", which is exactly the mistake this fake exists to
     * expose: the registered table is in the connection's catalogue, and a server that answers with another
     * schema's table of the same name would hide it.
     */
    private fun tablesMatching(catalogPattern: String?, namePattern: String?): List<FakeTable> =
        tables.filter { table ->
            (catalogPattern == null || table.catalog == catalogPattern) &&
                (namePattern == null || table.name.equals(namePattern, ignoreCase = true))
        }

    private fun connection(): java.sql.Connection = proxy(java.sql.Connection::class.java) { method, _ ->
        when (method.name) {
            "getCatalog" -> catalog
            else -> defaultValue(method.returnType)
        }
    }

    private fun <T> proxy(type: Class<T>, handler: (Method, Array<Any?>?) -> Any?): T {
        val invocationHandler = InvocationHandler { _, method, args -> handler(method, args) }
        @Suppress("UNCHECKED_CAST")
        return Proxy.newProxyInstance(type.classLoader, arrayOf(type), invocationHandler) as T
    }

    /** A [ResultSet] over [rows], with `wasNull` answering for the column read last. */
    private fun resultSet(rows: List<Map<String, Any?>>): ResultSet {
        var index = -1
        var lastValue: Any? = null
        return proxy(ResultSet::class.java) { method, args ->
            when (method.name) {
                "next" -> {
                    index += 1
                    index < rows.size
                }

                "close" -> Unit
                "wasNull" -> lastValue == null
                "getString", "getInt", "getLong", "getObject" -> {
                    val row = rows.getOrNull(index) ?: return@proxy null
                    val value = when (val column = args?.getOrNull(0)) {
                        is Int -> row.values.elementAtOrNull(column - 1)
                        is String -> row[column]
                        else -> null
                    }
                    lastValue = value
                    when (method.name) {
                        "getInt" -> (value as? Int) ?: 0
                        "getLong" -> (value as? Int)?.toLong() ?: 0L
                        "getString" -> value?.toString()
                        else -> value
                    }
                }

                else -> defaultValue(method.returnType)
            }
        }
    }

    private fun arg(args: Array<Any?>?, index: Int): String? = args?.getOrNull(index) as? String

    private fun defaultValue(type: Class<*>): Any? = when (type) {
        java.lang.Boolean.TYPE -> false
        java.lang.Integer.TYPE -> 0
        java.lang.Long.TYPE -> 0L
        java.lang.Short.TYPE -> 0.toShort()
        java.lang.Byte.TYPE -> 0.toByte()
        java.lang.Double.TYPE -> 0.0
        java.lang.Float.TYPE -> 0f
        java.lang.Character.TYPE -> ' '
        else -> null
    }
}

/** One table a [FakeMetadata] server holds. */
internal data class FakeTable(val catalog: String?, val schema: String?, val name: String) {

    fun row(): Map<String, Any?> =
        mapOf("TABLE_CAT" to catalog, "TABLE_SCHEM" to schema, "TABLE_NAME" to name)
}

/** One column a [FakeMetadata] server reports, for the table named [table]. */
internal data class FakeColumn(
    val table: String,
    val name: String,
    val typeName: String,
    val nullable: Boolean,
    val size: Int?
) {

    fun row(): Map<String, Any?> = mapOf(
        "COLUMN_NAME" to name,
        "TYPE_NAME" to typeName,
        "COLUMN_SIZE" to size,
        "IS_NULLABLE" to if (nullable) "YES" else "NO"
    )
}
