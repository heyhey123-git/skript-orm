package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import ch.njol.skript.effects.Delay
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.Trigger
import ch.njol.skript.lang.TriggerItem
import ch.njol.skript.util.Timespan
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.SkriptOrm
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.database.Transaction
import io.github.heyhey123.skriptorm.skript.utils.ConnectionScope
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.ErrorPrinter
import io.github.heyhey123.skriptorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.skriptorm.skript.utils.SkriptLocalVariables
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.utils.SyncDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon
import java.time.Duration

/**
 * Runs its body as one database transaction.
 *
 * Every statement inside runs on the connection the transaction pinned, which is what makes them see
 * each other's uncommitted work and what makes them all or nothing. Reaching the end of the body
 * commits, `exit`, `stop` or `return` rolls back, and a statement that fails puts the transaction into
 * rollback-only: the statements after it do nothing rather than running work that is about to be
 * thrown away.
 *
 * The statements inside run on one connection, so they cannot run at the same time and they cannot
 * overtake each other. That is the same thing every statement outside a transaction does: every statement
 * waits for its work, and the transaction is what makes them one unit on top of that.
 *
 * A transaction section reached from inside another one joins it rather than starting a second, which
 * is what happens when a function that opens one is called from inside one. Only the section that
 * began the transaction commits it.
 */
@Name("Database Transaction")
@Description("Runs the code inside as one database transaction on the connection in effect, or on a named one. Reaching the end of the body commits; `exit`, `stop` and `return` roll back. A statement that fails makes the rest of the body's database statements do nothing, and the transaction is rolled back when the body ends. The timeout defaults to 30 seconds and rolls the transaction back on its own if it is still open after that.")
@Example(
    """database transaction:
    update one entity in table "accounts" by id {_from} and wait:
        values:
            balance: {_from::balance} - {_amount}
    update one entity in table "accounts" by id {_to} and wait:
        values:
            balance: {_to::balance} + {_amount}
if last database error is set:
    send "The transfer was rolled back: %last database error%"
"""
)
@Example(
    """database transaction on connection "logs" with timeout 2 minutes:
    delete entities from table "old_entries" with limit 500 and wait
"""
)
@Since("1.0.0")
class SecTransaction : ScopedBodySection() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(
                addon,
                SecTransaction::class.java,
                // Both expressions are nullable (`%-...%`) because Skript refuses a required one inside an
                // optional group: it has to know what to put there when the group is left out. Which of
                // them was written is answered by the expression being absent, not by a default value.
                "database transaction [on connection %-string%] [with [a] timeout [of] %-timespan%]"
            )
        }
    }

    private var connectionNameExpr: Expression<String>? = null
    private var timeoutExpr: Expression<Timespan>? = null

    @Suppress("UNCHECKED_CAST")
    override fun prepare(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        connectionNameExpr = expressions.getOrNull(0) as? Expression<String>
        timeoutExpr = expressions.getOrNull(1) as? Expression<Timespan>
        parser.hasDelayBefore = Kleenean.TRUE
        return true
    }

    override fun validateBody(): Boolean {
        if (first == null) {
            Skript.error("A database transaction section has to contain at least one statement.")
            return false
        }
        return true
    }

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return walk(event, false)
        val trigger = this.trigger ?: return walk(actualEvent, false)

        val database = resolveConnection(actualEvent, trigger) ?: return walk(actualEvent, false)
        val timeout = resolveTimeout(actualEvent, trigger) ?: return walk(actualEvent, false)

        val open = ConnectionScope.transaction(actualEvent)
        if (open != null) {
            // Joined rather than begun: a second transaction on the same connection is not a thing a
            // database can do, and one on another connection would have to commit before this body is
            // finished with the first.
            if (connectionNameExpr != null && open.database !== database) {
                report(actualEvent, trigger, "A database transaction is already open on another connection.")
                return walk(actualEvent, false)
            }
            ConnectionScope.push(actualEvent, open.database, this, transaction = open, ownsTransaction = false)
            return walk(actualEvent, true)
        }

        return beginAndWalk(actualEvent, trigger, database, timeout)
    }

    override fun onBodyEnd(event: Event, continuation: TriggerItem?): TriggerItem? {
        val transaction = ConnectionScope.transaction(event)
        val owns = ConnectionScope.popOwned(event, this)
        if (transaction == null || !owns) return continuation

        return when {
            // The ordinary ending: the body ran and nothing went wrong in it.
            transaction.isActive -> commitAndContinue(event, transaction, continuation)

            // A statement failed, so the body was allowed to finish and the whole thing is undone now.
            // The failure stays in `last database error`, which is what the script reads afterwards.
            transaction.state == Transaction.State.ROLLBACK_ONLY ->
                rollbackAndContinue(event, transaction, continuation)

            // Ended from outside: its timeout, or the connection being closed. Nothing left to do, but
            // a body whose last statement was a `wait` never heard about it, so it is said here.
            else -> {
                val reason = transaction.failure
                val trigger = this.trigger
                if (reason != null && trigger != null) {
                    DatabaseWork.report(event, trigger, reason.message ?: "The transaction was aborted.")
                }
                continuation
            }
        }
    }

    private fun commitAndContinue(
        event: Event,
        transaction: Transaction,
        continuation: TriggerItem?
    ): TriggerItem? {
        val trigger = this.trigger
        return DatabaseWork.run(
            event = event,
            continuation = continuation,
            query = { transaction.commit() },
            onFailure = { error ->
                val message = "The database transaction could not be committed, and whether the server " +
                    "applied it is unknown: ${error.message}"
                SkriptDatabaseErrors.set(event, message)
                trigger?.let { ErrorPrinter.printErrorMessageWithDetail(it, message) }
            }
        )
    }

    private fun rollbackAndContinue(
        event: Event,
        transaction: Transaction,
        continuation: TriggerItem?
    ): TriggerItem? {
        val trigger = this.trigger
        return DatabaseWork.run(
            event = event,
            continuation = continuation,
            query = { transaction.rollback() },
            clearErrorOnSuccess = false,
            onFailure = { error ->
                trigger?.let { ErrorPrinter.printErrorWithDetail(it, error) }
            }
        )
    }

    override fun leaveBody(event: Event) {
        val transaction = ConnectionScope.transaction(event)
        val owns = ConnectionScope.popOwned(event, this)
        if (transaction == null || !owns) return

        // The frame is gone first, so nothing later in this event can join a transaction that is on its
        // way out. The rollback itself goes to the background: `exit` runs on the server thread and
        // cannot wait for JDBC, and the connection is the transaction's to hand back either way.
        SkriptOrm.ioScope.launch {
            try {
                transaction.rollback()
            } catch (error: Throwable) {
                SkriptOrm.instance.logger.warning(
                    "Failed to roll back a database transaction: ${error.message}"
                )
            }
        }
    }

    /**
     * Opens the transaction, which means pinning a connection, and then walks the body.
     *
     * The event is parked for it, the way every other statement that touches the database does: the
     * body must not start before the transaction exists, and starting it means borrowing a pooled
     * connection.
     */
    private fun beginAndWalk(
        event: Event,
        trigger: Trigger,
        database: Database,
        timeout: Duration
    ): TriggerItem? {
        val localVariables = SkriptLocalVariables.remove(event)
        Delay.addDelayedEvent(event)

        SkriptOrm.ioScope.launch {
            var transaction: Transaction? = null
            var failure: Throwable? = null
            try {
                transaction = database.beginTransaction(timeout)
            } catch (_: CancellationException) {
                return@launch
            } catch (error: Throwable) {
                failure = error
            }

            withContext(NonCancellable + SyncDispatcher) {
                if (!SkriptOrm.instance.isEnabled || Database.isShuttingDown) return@withContext
                try {
                    if (localVariables != null) {
                        SkriptLocalVariables.restore(event, localVariables)
                    }

                    val started = transaction
                    if (started == null) {
                        failure?.let {
                            SkriptDatabaseErrors.set(event, it)
                            ErrorPrinter.printErrorWithDetail(trigger, it)
                        }
                        TriggerItem.walk(next, event)
                    } else {
                        SkriptDatabaseErrors.clear(event)
                        ConnectionScope.push(
                            event,
                            database,
                            this@SecTransaction,
                            transaction = started,
                            ownsTransaction = true
                        )
                        TriggerItem.walk(first, event)
                    }
                } finally {
                    SkriptLocalVariables.clear(event)
                }
            }
        }

        return null
    }

    /** The connection the transaction pins: the one named, or the one already in effect. */
    private fun resolveConnection(event: Event, trigger: Trigger): Database? {
        val expression = connectionNameExpr
        if (expression == null) {
            val resolved = ConnectionScope.resolve(event)
            if (resolved == null) {
                report(event, trigger, ConnectionScope.noConnectionMessage())
            }
            return resolved
        }

        val name = expression.getSingle(event)
        if (name == null) {
            report(event, trigger, "Connection name is null.")
            return null
        }

        val connection = Database.connection(name)
        if (connection == null) {
            report(event, trigger, ConnectionScope.unknownConnectionMessage(name))
            return null
        }
        if (!connection.isConnected) {
            report(event, trigger, "Connection '$name' is not connected.")
            return null
        }
        return connection
    }

    private fun resolveTimeout(event: Event, trigger: Trigger): Duration? {
        val expression = timeoutExpr ?: return Database.DEFAULT_TRANSACTION_TIMEOUT
        val timespan = expression.getSingle(event)
        if (timespan == null) {
            report(event, trigger, "The transaction timeout is not set.")
            return null
        }

        val millis = timespan.getAs(Timespan.TimePeriod.MILLISECOND)
        if (millis <= 0) {
            report(event, trigger, "The transaction timeout has to be positive.")
            return null
        }
        return Duration.ofMillis(millis)
    }

    private fun report(event: Event, trigger: Trigger, message: String) {
        SkriptDatabaseErrors.set(event, message)
        ErrorPrinter.printErrorMessageWithDetail(trigger, message)
    }

    override fun toString(event: Event?, debug: Boolean) = buildString {
        append("database transaction")
        connectionNameExpr?.let { append(" on connection ${it.toString(event, debug)}") }
        timeoutExpr?.let { append(" with timeout ${it.toString(event, debug)}") }
    }
}
