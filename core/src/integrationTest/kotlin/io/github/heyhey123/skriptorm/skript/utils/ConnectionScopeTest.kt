package io.github.heyhey123.skriptorm.skript.utils

import io.github.heyhey123.skriptorm.database.ConnectionSettings
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.DataType
import io.github.heyhey123.skriptorm.type.DataTypes
import io.github.heyhey123.skriptorm.type.TypeId
import kotlinx.coroutines.runBlocking
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Covers the connection a statement resolves to: which frame wins, what leaving a section takes back,
 * and what a scope does not touch.
 *
 * No connection is really opened here. The frames only carry a [Database] around, and the resolution
 * rules are the part worth pinning down, so the databases below are never connected.
 */
class ConnectionScopeTest {

    @BeforeEach
    fun prepareLifecycle() = resetLifecycle()

    @AfterEach
    fun releaseLifecycle() = resetLifecycle()

    @Test
    fun `nothing in effect resolves to no connection`() {
        assertNull(ConnectionScope.resolve(StubEvent()))
    }

    @Test
    fun `a pushed connection wins, and the last push wins over it`() {
        val event = StubEvent()
        val first = StubDatabase("first")
        val second = StubDatabase("second")

        ConnectionScope.push(event, first, owner = null)
        assertSame(first, ConnectionScope.resolve(event))

        ConnectionScope.push(event, second, owner = null)
        assertSame(second, ConnectionScope.resolve(event))
    }

    @Test
    fun `a frame only affects the event it was pushed for`() {
        val scoped = StubEvent()
        val other = StubEvent()

        ConnectionScope.push(scoped, StubDatabase("scoped"), owner = null)

        assertNull(ConnectionScope.resolve(other))
    }

    @Test
    fun `leaving the owner takes its frame and the ones pushed above it`() {
        val event = StubEvent()
        val outer = StubDatabase("outer")
        val inner = StubDatabase("inner")
        val switched = StubDatabase("switched")
        val owner = Any()

        ConnectionScope.push(event, outer, owner = null)
        ConnectionScope.push(event, inner, owner = owner)
        ConnectionScope.push(event, switched, owner = null)
        assertSame(switched, ConnectionScope.resolve(event))

        ConnectionScope.popOwned(event, owner)

        // The section's own frame goes too, and the `use connection` written inside it does not leak
        // into the rest of the event.
        assertSame(outer, ConnectionScope.resolve(event))
    }

    @Test
    fun `popping an owner that pushed nothing changes nothing`() {
        val event = StubEvent()
        val connection = StubDatabase("kept")

        ConnectionScope.push(event, connection, owner = null)
        ConnectionScope.popOwned(event, Any())

        assertSame(connection, ConnectionScope.resolve(event))
    }

    @Test
    fun `clearing forgets every frame of an event`() {
        val event = StubEvent()
        ConnectionScope.push(event, StubDatabase("dropped"), owner = null)

        ConnectionScope.clear(event)

        assertNull(ConnectionScope.resolve(event))
    }

    @Test
    fun `an unknown name lists the connections that do exist`() {
        val message = ConnectionScope.unknownConnectionMessage("logs")

        assertTrue("logs" in message)
        assertTrue("no connection has been created yet" in message, "unexpected message: $message")
    }

    @Test
    fun `no connection in effect keeps the message scripts already document`() {
        assertEquals("No database connected.", ConnectionScope.noConnectionMessage())
    }

    private fun resetLifecycle() = runBlocking {
        Database.shutdown()
        Database.beginLifecycle()
    }
}

/** A connection that is never opened: these tests only ever ask which one is in effect. */
private class StubDatabase(private val label: String) : Database() {

    override val dataTypes: DataTypes = NoDataTypes

    override fun doConnect(settings: ConnectionSettings) = Unit

    override fun doDisconnect() = Unit

    override suspend fun doRegisterTable(table: Table) = Unit

    override fun toString(): String = "StubDatabase($label)"
}

private object NoDataTypes : DataTypes() {
    override val typesRegistry = mutableMapOf<TypeId, DataType<*>>()
}

/** The smallest event that can key the scope's weak map. */
private class StubEvent : Event() {

    override fun getHandlers(): HandlerList = HANDLERS

    override fun getEventName(): String = "SkriptConnectionScopeTest"

    companion object {
        private val HANDLERS = HandlerList()
    }
}
