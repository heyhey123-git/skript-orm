package io.github.heyhey123.xiaojieorm.impl.jdbc.integration

import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDatabase
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.MysqlJdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataTypes
import io.github.heyhey123.xiaojieorm.type.TypeId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Guards the integration test runtime rather than the database.
 *
 * `JdbcDataTypes` resolves the Bukkit, NBT and Skript classes it declares while the object
 * initializes, so constructing any `JdbcDatabase` requires those jars to be on the test classpath.
 * The unit tests deliberately run without them; here they are present because the integration test
 * JVM is meant to be the plugin runtime without a server. If one of those jars ever drops off the
 * classpath, every MySQL test fails while opening its pool, so this check reports the real cause
 * first. It needs no server and therefore also runs when Docker is unavailable.
 */
class JdbcRuntimeClasspathIntegrationTest {

    @Test
    fun `the jdbc data type registry covers every type id`() {
        assertEquals(TypeId.entries.toSet(), JdbcDataTypes.typesRegistry.keys)
    }

    @Test
    fun `every registered jdbc data type declares a storage name`() {
        JdbcDataTypes.typesRegistry.forEach { (id, type) ->
            val jdbcType = assertIs<JdbcDataType<*>>(type)
            assertTrue(jdbcType.storageName.isNotBlank(), "type $id must declare a storage name")
        }
    }

    @Test
    fun `a jdbc database can be constructed without connecting`() {
        val database = JdbcDatabase("com.mysql.cj.jdbc.Driver", MysqlJdbcDialect)

        assertSame(JdbcDataTypes, database.dataTypes)
        assertFalse(database.isConnected)
        assertNull(database.dataSource)
        assertNull(database.queries)
    }
}
