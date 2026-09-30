package io.github.heyhey123.skriptorm.impl.jdbc.integration

/** Runs the type round trip against MySQL, whose Connector/J converts the bound values. */
class MysqlTypeRoundTripIntegrationTest : TypeRoundTripIntegrationTest() {

    override val product = MysqlTestServer.Product.MYSQL
}
