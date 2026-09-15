package io.github.heyhey123.xiaojieorm.database

import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.type.DataType
import io.github.heyhey123.xiaojieorm.type.DataTypes
import io.github.heyhey123.xiaojieorm.type.TypeId
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DatabaseRegistryTest {
    @Test
    fun `registered factory is discoverable and receives original properties`() {
        val name = "test-${UUID.randomUUID()}"
        var received: Map<String, String>? = null
        val database = FakeDatabase()
        val factory = factory(name) { properties -> received = properties; database }
        val properties = linkedMapOf("url" to "value")

        DatabaseRegistry.register(factory)

        assertTrue(DatabaseRegistry.isSupported(name))
        assertSame(database, DatabaseRegistry.get(name, properties))
        assertSame(properties, received)
        DatabaseRegistry.register(factory)
    }

    @Test
    fun `different factory cannot claim an existing type name`() {
        val name = "test-${UUID.randomUUID()}"
        DatabaseRegistry.register(factory(name) { FakeDatabase() })

        assertFailsWith<IllegalArgumentException> {
            DatabaseRegistry.register(factory(name) { FakeDatabase() })
        }
    }

    @Test
    fun `unsupported type throws documented argument exception`() {
        val name = "missing-${UUID.randomUUID()}"
        assertFalse(DatabaseRegistry.isSupported(name))
        assertFailsWith<IllegalArgumentException> { DatabaseRegistry.get(name, emptyMap()) }
    }

    private fun factory(name: String, create: (Map<String, String>) -> Database) = object : DatabaseFactory {
        override val typeName = name
        override fun create(properties: Map<String, String>) = create(properties)
    }

    private class FakeDatabase : Database() {
        override val dataTypes = object : DataTypes() {
            override val typesRegistry = mutableMapOf<TypeId, DataType<*>>()
        }
        override fun doConnect(url: String, user: String, password: String) = Unit
        override fun doDisconnect() = Unit
        override suspend fun doRegisterTable(table: Table) = Unit
    }
}
