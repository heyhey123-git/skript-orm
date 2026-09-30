package io.github.heyhey123.skriptorm.impl.jdbc.integration

/** Runs the type round trip against MariaDB, whose connector converts the bound values. */
class MariadbTypeRoundTripIntegrationTest : TypeRoundTripIntegrationTest() {

    override val product = MysqlTestServer.Product.MARIADB
}
