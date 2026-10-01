package io.github.heyhey123.skriptorm.impl.jdbc.integration

/**
 * The server-side count against MySQL, where the finding that asked for it was measured: the driver
 * rewriting a batch into one multi-row statement is a Connector/J decision, and Connector/J is what
 * this class reaches.
 */
class MysqlServerSideCountIntegrationTest : ServerSideCountIntegrationTest() {

    override val product = MysqlTestServer.Product.MYSQL
}
