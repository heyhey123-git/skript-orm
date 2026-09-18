package io.github.heyhey123.skriptorm.impl.mongo.database

import io.github.heyhey123.skriptorm.database.DatabaseRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Covers what the factory tells the registry, without a server.
 *
 * A factory registers itself while its object is being initialized, so the name it registers under has to
 * be readable at that moment. Storing it in a property does not survive that: the property is still null
 * when the `init` block runs, the registry refuses a null name, and the whole plugin fails to enable —
 * which is what happened the first time this module was put in the jar. Touching the object here is what
 * makes that a test failure rather than a server that will not start.
 */
class MongodbDatabaseFactoryTest {

    @Test
    fun `the factory registers itself under the name a script writes`() {
        assertEquals("MongoDB", MongodbDatabaseFactory.typeName)

        assertTrue(DatabaseRegistry.isSupported("MongoDB"), "the factory registered itself while it initialized")
        assertIs<MongodbDatabase>(DatabaseRegistry.get("MongoDB", emptyMap()))
    }

    @Test
    fun `the properties of the connection reach the database`() {
        val database = assertIs<MongodbDatabase>(
            DatabaseRegistry.get("MongoDB", mapOf("statement timeout" to "15"))
        )

        assertEquals(20, database.closeWaitTimeout.seconds, "a fifteen second statement closes five seconds later")
    }
}
