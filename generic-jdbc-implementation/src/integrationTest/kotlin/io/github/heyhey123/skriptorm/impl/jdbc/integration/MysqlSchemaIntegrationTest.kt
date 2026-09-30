package io.github.heyhey123.skriptorm.impl.jdbc.integration

/** Runs the schema test against MySQL, whose Connector/J reports the column types. */
class MysqlSchemaIntegrationTest : SchemaIntegrationTest() {

    override val product = MysqlTestServer.Product.MYSQL
}
