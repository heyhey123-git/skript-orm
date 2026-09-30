package io.github.heyhey123.skriptorm.impl.jdbc.integration

/**
 * Runs the schema test against MariaDB, whose driver reports the column types.
 *
 * Registration compares a declaration against those types, so a server that names one of them
 * differently fails here instead of at the first insert.
 */
class MariadbSchemaIntegrationTest : SchemaIntegrationTest() {

    override val product = MysqlTestServer.Product.MARIADB
}
