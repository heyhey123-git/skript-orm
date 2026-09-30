package io.github.heyhey123.skriptorm.impl.jdbc.integration

/**
 * Runs the statement timeout test against MariaDB, where the connector sends the statement with a
 * `max_statement_time` prefix instead of cancelling it from another connection.
 */
class MariadbStatementTimeoutIntegrationTest : StatementTimeoutIntegrationTest() {

    override val product = MysqlTestServer.Product.MARIADB
}
