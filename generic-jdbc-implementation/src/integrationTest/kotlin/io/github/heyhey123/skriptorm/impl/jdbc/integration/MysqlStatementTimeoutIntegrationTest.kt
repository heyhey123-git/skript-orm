package io.github.heyhey123.skriptorm.impl.jdbc.integration

/** Runs the statement timeout test against MySQL, where Connector/J kills the query server-side. */
class MysqlStatementTimeoutIntegrationTest : StatementTimeoutIntegrationTest() {

    override val product = MysqlTestServer.Product.MYSQL
}
