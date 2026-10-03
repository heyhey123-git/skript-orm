package io.github.heyhey123.skriptorm.impl.jdbc.database

import io.github.heyhey123.skriptorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.skriptorm.table.Table
import java.sql.JDBCType
import java.sql.SQLException

/**
 * Defines SQL rendering decisions for the JDBC implementation: identifier quoting, complete SQL
 * fragments, pagination placeholder order, DDL type names, write limits, and auto-increment syntax.
 * Default implementations of non-portable features fail with [UnsupportedOperationException].
 */
interface JdbcDialect {

    /**
     * Quotes an SQL identifier (table name, column name, etc.) according to the dialect's rules.
     * Valid identifiers may contain Unicode letters, combining marks, digits, and underscores;
     * they must not begin with a digit. Quoting is still required for reserved words and
     * database-specific identifier rules.
     *
     * @param identifier The SQL identifier to quote.
     */
    fun quoteIdentifier(identifier: String): String {
        require(IDENTIFIER_PATTERN.matches(identifier)) {
            "Invalid SQL identifier '$identifier'."
        }
        return renderIdentifier(identifier)
    }

    fun renderIdentifier(identifier: String): String

    /**
     * Takes at most [limit] rows, or every matching row when it is null.
     *
     * The caller validates [limit] before it is inserted into the SQL, as with [update] and [delete].
     */
    fun select(table: String, whereClause: String? = null, limit: Int? = null): String = buildString {
        append("SELECT * FROM ${quoteIdentifier(table)}")
        whereClause?.let { append(" $it") }
        limit?.let { append(" LIMIT $it") }
    }

    /**
     * Takes one row with `LIMIT 1`.
     *
     * The supported databases accept `LIMIT`; MySQL and SQLite do not accept `FETCH FIRST`.
     * A dialect can override this if its database needs different syntax.
     */
    fun selectOne(table: String, whereClause: String? = null): String =
        select(table, whereClause, 1)

    /** Pages with `LIMIT ? OFFSET ?`, for the reason [selectOne] gives. */
    fun selectPage(table: String, orderBy: String, whereClause: String? = null): JdbcPageSql =
        JdbcPageSql(
            sql = "${select(table, whereClause)} ORDER BY ${quoteIdentifier(orderBy)} LIMIT ? OFFSET ?",
            parameterOrder = listOf(JdbcPageParameter.LIMIT, JdbcPageParameter.OFFSET)
        )

    fun insert(table: String, columns: List<String>): String {
        require(columns.isNotEmpty()) { "Insert columns cannot be empty." }
        return "INSERT INTO ${quoteIdentifier(table)} (${columns.joinToString(", ") { quoteIdentifier(it) }}) " +
            "VALUES (${columns.joinToString(", ") { "?" }})"
    }

    fun insertIfAbsent(table: String, columns: List<String>): String =
        unsupported("Insert-if-absent")

    /**
     * Returns whether [error] is a unique or primary key violation. `insert if absent` ignores only
     * this error when the database lacks a suitable insert statement, as MySQL does.
     *
     * `INSERT IGNORE` is unsuitable because it also suppresses other errors, such as oversized values
     * or NULL in a non-nullable column.
     */
    open fun isDuplicateKey(error: SQLException): Boolean = false

    fun update(table: String, columns: List<String>, whereClause: String?, limit: Int?): String {
        require(columns.isNotEmpty()) { "Update columns cannot be empty." }
        require(limit == null || limit > 0) { "Update limit must be positive." }
        val sql = buildString {
            append("UPDATE ${quoteIdentifier(table)} SET ")
            append(columns.joinToString(", ") { "${quoteIdentifier(it)} = ?" })
            whereClause?.let { append(" $it") }
        }
        return if (limit == null) sql else applyUpdateLimit(sql, limit)
    }

    fun upsertById(table: String, primaryKey: String, columns: List<String>): String =
        unsupported("Upsert")

    fun delete(table: String, whereClause: String?, limit: Int?): String {
        require(limit == null || limit > 0) { "Delete limit must be positive." }
        val sql = buildString {
            append("DELETE FROM ${quoteIdentifier(table)}")
            whereClause?.let { append(" $it") }
        }
        return if (limit == null) sql else applyDeleteLimit(sql, limit)
    }

    fun createTable(table: Table): String = buildString {
        append("CREATE TABLE IF NOT EXISTS ${quoteIdentifier(table.name)} (")
        append(table.columns.values.joinToString(", ") { renderColumn(it.name, it.type, it.size, it.isNullable, it.isPrimaryKey, it.isAutoIncrement) })
        append(")")
    }

    fun renderColumn(
        name: String,
        type: io.github.heyhey123.skriptorm.type.DataType<*>,
        size: Int?,
        nullable: Boolean,
        primaryKey: Boolean,
        autoIncrement: Boolean
    ): String {
        val jdbcType = requireNotNull(type as? JdbcDataType<*>) {
            "Data type ${type.typeCode} is not supported by the JDBC implementation."
        }
        val typeName = typeName(jdbcType)
        val effectiveSize = size ?: jdbcType.defaultSize.takeIf { it >= 0 }
        if (effectiveSize != null) {
            require(jdbcType.supportsSize) {
                "JDBC type $typeName does not support a column size."
            }
        }
        if (autoIncrement) {
            require(jdbcType.jdbcType in INTEGER_TYPES) {
                "Auto-increment column $name must use an integer JDBC type."
            }
        }
        return buildString {
            append(quoteIdentifier(name))
            append(' ')
            append(typeName)
            effectiveSize?.let { append("($it)") }
            if (!nullable) append(" NOT NULL")
            if (primaryKey) append(" PRIMARY KEY")
            if (autoIncrement) append(autoIncrementClause())
        }
    }

    fun typeName(type: JdbcDataType<*>): String = type.storageName

    fun applyUpdateLimit(sql: String, limit: Int): String = unsupported("Limited update")
    fun applyDeleteLimit(sql: String, limit: Int): String = unsupported("Limited delete")
    fun autoIncrementClause(): String = unsupported("Auto increment")

    fun unsupported(feature: String): Nothing =
        throw UnsupportedOperationException("$feature is not supported by this JDBC dialect.")

    /**
     * Additional type names reported by this database's driver, mapped to the names used in table
     * declarations. Keys are uppercase and omit column sizes.
     *
     * These aliases are database-specific. For example, MySQL treats `TINYINT` as boolean storage,
     * while H2 distinguishes the two. Schema verification also applies shared SQL and JDBC aliases.
     */
    val typeAliases: Map<String, String>
        get() = emptyMap()

    companion object {

        /**
         * Valid SQL identifiers may contain Unicode letters, combining marks, digits, and underscores;
         * they must not begin with a digit. This regex pattern is used to validate identifiers before quoting them.
         */
        private val IDENTIFIER_PATTERN = Regex("[\\p{L}\\p{Nl}_][\\p{L}\\p{Nl}\\p{M}\\p{Nd}_]*")
        private val INTEGER_TYPES = setOf(
            JDBCType.TINYINT,
            JDBCType.SMALLINT,
            JDBCType.INTEGER,
            JDBCType.BIGINT
        )
    }
}

enum class JdbcPageParameter {
    LIMIT,
    OFFSET
}

data class JdbcPageSql(
    val sql: String,
    val parameterOrder: List<JdbcPageParameter>
) {

    init {
        require(
            parameterOrder.size == 2 &&
                parameterOrder.count { it == JdbcPageParameter.LIMIT } == 1 &&
                parameterOrder.count { it == JdbcPageParameter.OFFSET } == 1
        ) {
            "Pagination must bind LIMIT and OFFSET exactly once."
        }
    }
}

/**
 * Generic SQL dialect with double-quoted identifiers and `LIMIT` queries. Insert-if-absent,
 * upsert, limited writes, and auto-increment are unsupported.
 */
object GenericJdbcDialect : JdbcDialect {

    override fun renderIdentifier(identifier: String): String =
        "\"${identifier.replace("\"", "\"\"")}\""

    /**
     * Type names that a generic JDBC database may report for columns created outside the addon.
     * SQLite preserves names such as `INT8`, `SERIAL`, and `BOOL`; H2 also recognizes the JDBC names
     * `LONGVARCHAR` and `LONGVARBINARY`. Each alias maps to a type this dialect can declare.
     *
     * `INT2` and `SMALLSERIAL` map to `TINYINT`, the smallest integer type declared here. The
     * PostgreSQL dialect maps its own `float4` to `REAL`; this dialect declares it as `FLOAT`.
     * Product-specific names such as MySQL's `TINYTEXT` and PostgreSQL's `BYTEA` belong to their
     * respective dialects.
     */
    override val typeAliases: Map<String, String> = mapOf(
        "INT2" to "TINYINT",
        "INT4" to "INTEGER",
        "INT8" to "BIGINT",
        "FLOAT4" to "FLOAT",
        "FLOAT8" to "DOUBLE",
        "SERIAL" to "INTEGER",
        "BIGSERIAL" to "BIGINT",
        "SMALLSERIAL" to "TINYINT",
        "BOOL" to "BOOLEAN",
        "BIT" to "BOOLEAN",
        "LONGVARCHAR" to "VARCHAR",
        "LONGVARBINARY" to "BLOB"
    )
}

/**
 * Rendering for the MySQL family: backtick identifiers, LIMIT/OFFSET, a duplicate key treated as
 * "already there", ON DUPLICATE KEY UPDATE, limited writes, and AUTO_INCREMENT.
 *
 * MySQL and MariaDB both support the duplicate-key error code, limited writes, and the `VALUES(col)`
 * upsert syntax used here. MySQL deprecated `VALUES(col)` in 8.0.20, but its replacement syntax is
 * not available in all supported MariaDB versions.
 */
object MysqlJdbcDialect : JdbcDialect {

    /** MySQL's `ER_DUP_ENTRY`, the error the insert below is allowed to treat as "already there". */
    private const val DUPLICATE_KEY = 1062

    override fun renderIdentifier(identifier: String): String =
        "`${identifier.replace("`", "``")}`"

    /**
     * Uses a plain insert so [JdbcInsertIfAbsent] can catch duplicate keys without suppressing other
     * database errors. See [isDuplicateKey].
     */
    override fun insertIfAbsent(table: String, columns: List<String>): String = insert(table, columns)

    override fun isDuplicateKey(error: SQLException): Boolean = error.errorCode == DUPLICATE_KEY

    override fun upsertById(table: String, primaryKey: String, columns: List<String>): String {
        require(columns.isNotEmpty()) { "Upsert columns cannot be empty." }
        val allColumns = listOf(primaryKey) + columns
        return "${insert(table, allColumns)} ON DUPLICATE KEY UPDATE " +
            columns.joinToString(", ") {
                val quoted = quoteIdentifier(it)
                "$quoted = VALUES($quoted)"
            }
    }

    override fun applyUpdateLimit(sql: String, limit: Int): String = "$sql LIMIT $limit"
    override fun applyDeleteLimit(sql: String, limit: Int): String = "$sql LIMIT $limit"
    override fun autoIncrementClause(): String = " AUTO_INCREMENT"

    /**
     * Maps MySQL and MariaDB metadata names to the types declared by this dialect. `BOOLEAN` uses
     * `TINYINT` storage; depending on the driver's `tinyInt1isBit` setting, metadata may call it
     * `BIT`, `BOOLEAN`, or `TINYINT`. Text and BLOB aliases also allow verification of tables
     * created outside the addon.
     *
     * MySQL normalizes DDL aliases such as `INT8`, `BOOL`, and `SERIAL` before reporting metadata,
     * so they do not need entries here.
     */
    override val typeAliases: Map<String, String> = mapOf(
        "TINYINT" to "BOOLEAN",
        "BIT" to "BOOLEAN",
        "TINYTEXT" to "VARCHAR",
        "MEDIUMTEXT" to "VARCHAR",
        "LONGTEXT" to "VARCHAR",
        "TINYBLOB" to "BLOB",
        "MEDIUMBLOB" to "BLOB",
        "LONGBLOB" to "BLOB"
    )
}
