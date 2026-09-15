package io.github.heyhey123.xiaojieorm.impl.jdbc.database

import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.JDBCType

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

    fun select(table: String, whereClause: String? = null): String = buildString {
        append("SELECT * FROM ${quoteIdentifier(table)}")
        whereClause?.let { append(" $it") }
    }

    fun selectOne(table: String, whereClause: String? = null): String =
        "${select(table, whereClause)} FETCH FIRST 1 ROW ONLY"

    fun selectPage(table: String, orderBy: String, whereClause: String? = null): JdbcPageSql =
        JdbcPageSql(
            sql = "${select(table, whereClause)} ORDER BY ${quoteIdentifier(orderBy)} OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
            parameterOrder = listOf(JdbcPageParameter.OFFSET, JdbcPageParameter.LIMIT)
        )

    fun insert(table: String, columns: List<String>): String {
        require(columns.isNotEmpty()) { "Insert columns cannot be empty." }
        return "INSERT INTO ${quoteIdentifier(table)} (${columns.joinToString(", ") { quoteIdentifier(it) }}) " +
            "VALUES (${columns.joinToString(", ") { "?" }})"
    }

    fun insertIfAbsent(table: String, columns: List<String>): String =
        unsupported("Insert-if-absent")

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
        type: io.github.heyhey123.xiaojieorm.type.DataType<*>,
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
 * ANSI-oriented default dialect. Insert-if-absent, upsert, limited writes, and auto-increment
 * deliberately throw [UnsupportedOperationException].
 */
object GenericJdbcDialect : JdbcDialect {
    override fun renderIdentifier(identifier: String): String =
        "\"${identifier.replace("\"", "\"\"")}\""
}

/**
 * MySQL rendering with backtick identifiers, LIMIT/OFFSET, INSERT IGNORE,
 * ON DUPLICATE KEY UPDATE, limited writes, and AUTO_INCREMENT.
 */
object MysqlJdbcDialect : JdbcDialect {
    override fun renderIdentifier(identifier: String): String =
        "`${identifier.replace("`", "``")}`"

    override fun selectOne(table: String, whereClause: String?): String =
        "${select(table, whereClause)} LIMIT 1"

    override fun selectPage(table: String, orderBy: String, whereClause: String?): JdbcPageSql =
        JdbcPageSql(
            sql = "${select(table, whereClause)} ORDER BY ${quoteIdentifier(orderBy)} LIMIT ? OFFSET ?",
            parameterOrder = listOf(JdbcPageParameter.LIMIT, JdbcPageParameter.OFFSET)
        )

    override fun insertIfAbsent(table: String, columns: List<String>): String {
        require(columns.isNotEmpty()) { "Insert columns cannot be empty." }
        return "INSERT IGNORE INTO ${quoteIdentifier(table)} (${columns.joinToString(", ") { quoteIdentifier(it) }}) " +
            "VALUES (${columns.joinToString(", ") { "?" }})"
    }

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
}
