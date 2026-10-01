package io.github.heyhey123.skriptorm.impl.jdbc.integration

/**
 * The server-side count against MariaDB, through MariaDB Connector/J.
 *
 * The two products share a dialect and do not share a driver, and a batch's shape is the driver's
 * decision, so the same measurement on the other driver is the only way to say whether the count
 * describes MySQL or the SQL.
 */
class MariadbServerSideCountIntegrationTest : ServerSideCountIntegrationTest() {

    override val product = MysqlTestServer.Product.MARIADB
}
