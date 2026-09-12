package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import java.sql.Connection

/**
 * Jdbc query.
 * Represents a JDBC-based query operation.
 *
 */
interface JdbcQuery {
    /**
     * The JDBC connection used for executing the query.
     */
    val connection: Connection
}
