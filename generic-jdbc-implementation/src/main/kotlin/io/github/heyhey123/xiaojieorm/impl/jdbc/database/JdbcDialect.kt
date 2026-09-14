package io.github.heyhey123.xiaojieorm.impl.jdbc.database

interface JdbcDialect {
    fun insertIfAbsent(table: String, columns: List<String>): String
    fun upsertById(table: String, primaryKey: String, columns: List<String>): String
    fun applyUpdateLimit(sql: String, limit: Int): String
    fun applyDeleteLimit(sql: String, limit: Int): String
    fun autoIncrementClause(): String
}

object GenericJdbcDialect : JdbcDialect {
    private fun unsupported(feature: String): Nothing =
        throw UnsupportedOperationException("$feature is not portable JDBC SQL; configure a database-specific dialect.")

    override fun insertIfAbsent(table: String, columns: List<String>): String = unsupported("Insert-if-absent")
    override fun upsertById(table: String, primaryKey: String, columns: List<String>): String = unsupported("Upsert")
    override fun applyUpdateLimit(sql: String, limit: Int): String = unsupported("Limited update")
    override fun applyDeleteLimit(sql: String, limit: Int): String = unsupported("Limited delete")
    override fun autoIncrementClause(): String = unsupported("Auto increment")
}

object MysqlJdbcDialect : JdbcDialect {
    override fun insertIfAbsent(table: String, columns: List<String>): String =
        "INSERT IGNORE INTO $table (${columns.joinToString(", ")}) VALUES (${columns.joinToString(", ") { "?" }})"

    override fun upsertById(table: String, primaryKey: String, columns: List<String>): String =
        "INSERT INTO $table ($primaryKey, ${columns.joinToString(", ")}) VALUES (?, ${columns.joinToString(", ") { "?" }}) " +
            "ON DUPLICATE KEY UPDATE ${columns.joinToString(", ") { "$it = VALUES($it)" }}"

    override fun applyUpdateLimit(sql: String, limit: Int): String = "$sql LIMIT $limit"
    override fun applyDeleteLimit(sql: String, limit: Int): String = "$sql LIMIT $limit"
    override fun autoIncrementClause(): String = " AUTO_INCREMENT"
}
