package io.github.heyhey123.skriptorm.impl.jdbc.integration

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What the pool does when far more statements are in flight than it has connections.
 *
 * This is the stress case that cannot be written in the server test harness: it needs a real backend with a
 * real pool, and the count it asserts — `pool.activeConnections` — is a property of the pool rather than of
 * the script that used it. The number of statements started below is deliberately larger than the pool, so
 * the excess has to queue rather than fail: every statement must come back with its own answer, the pool
 * must return to rest with no slot held, and the database must still answer afterwards. A connection leaked
 * by a statement that failed or was abandoned is invisible until the pool runs dry, which is what this test
 * forces.
 *
 * Needs a container. There is no skip here: [MysqlTestServer.requireEndpoint] fails this test when no
 * container and no external server are configured, so a machine without Docker reports the case as failed
 * rather than passing it in silence.
 *
 * Not yet written, and awaiting a fixture that can provide it: killing the server mid-statement, and a
 * plugin reload with a query in flight. Both need a server that can be stopped underneath a running
 * statement. [MysqlTestServer] starts its container once per JVM and shares it with every other integration
 * test, so stopping it from one test would break the rest of the run; an external, stoppable server is the
 * fixture those two cases need.
 */
class MysqlPoolStressIntegrationTest : MysqlIntegrationTestBase() {

    override val product = MysqlTestServer.Product.MYSQL

    @Test
    fun `more statements than pool connections all finish and give their slots back`() = runBlocking<Unit> {
        recreateTable()
        queries.insertMany((1..8).map { userValues(id = it, name = "user-$it", age = it) }).execute(usersTable)

        val statements = 64
        val answers = (1..statements).map { index ->
            async(Dispatchers.IO) {
                queries.selectById((index % 8) + 1).execute(usersTable).readUsers().map { it.id }
            }
        }.awaitAll()

        assertEquals(statements, answers.size)
        assertTrue(
            answers.all { it.size == 1 },
            "every statement must return the one row it asked for, whatever it had to queue behind"
        )

        // Zero, not "small": a slot still checked out here is a leak the next burst would pay for.
        assertEquals(0, pool.activeConnections, "The connection pool did not settle.")
        assertEquals(1, queries.selectById(1).execute(usersTable).readUsers().size)
    }
}
