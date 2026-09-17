package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.config.SectionNode
import ch.njol.skript.doc.*
import ch.njol.skript.effects.Delay
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Section
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.Trigger
import ch.njol.skript.lang.TriggerItem
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.SkriptOrm
import io.github.heyhey123.skriptorm.database.ConnectionSettings
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.database.DatabaseRegistry
import io.github.heyhey123.skriptorm.skript.utils.ConnectionPropertiesParser
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

@Name("Create Database Connection")
@Description("Connects to a registered database implementation. Without a name the connection becomes the default one and replaces whatever was the default; with a name it is registered under that name and leaves every other connection alone. The first connection to succeed becomes the default. This section always waits. The url property is required; username and password may be empty strings. Additional literal properties are passed to the implementation. Failures are logged and exposed as the last database error.")
@Example(
    """
create a connection to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"
if last database error is set:
    send "Connection failed: %last database error%"
"""
)
@Example(
    """
create a connection named "logs" to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/logs"
    username: "root"
    password: "123456"
"""
)
@Since("1.0.0")
class SecCreateConnection : Section() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(
                addon,
                SecCreateConnection::class.java,
                // The named form comes first: its literal `named` is what keeps the two apart, but the
                // unnamed pattern lists no expression before the database type, so order still decides
                // which one a line is tried against first.
                "create [a] connection named %string% to [database] %string% [with properties]",
                "create [a] connection to [database] %string% [with properties]"
            )
        }
    }

    /** The name to register under, or null for the form that replaces the default connection. */
    private var nameExpr: Expression<String>? = null

    private lateinit var databaseNameExpr: Expression<String>
    private lateinit var connectionProperties: Map<String, String>

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult,
        sectionNode: SectionNode,
        triggerItems: List<TriggerItem?>
    ): Boolean {
        parser.hasDelayBefore = Kleenean.TRUE
        if (matchedPattern == 0) {
            nameExpr = expressions[0] as Expression<String>
            databaseNameExpr = expressions[1] as Expression<String>
        } else {
            databaseNameExpr = expressions[0] as Expression<String>
        }
        return parseNode(sectionNode)
    }

    private fun parseNode(sectionNode: SectionNode): Boolean {
        connectionProperties = try {
            ConnectionPropertiesParser.collectFrom(sectionNode)
        } catch (error: IllegalArgumentException) {
            Skript.error(error.message ?: "Invalid connection properties in create connection section.")
            return false
        }
        return true
    }

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return walk(event, false)
        val firstLine: Trigger = this.trigger ?: return walk(event, false)
        DatabaseWork.clearErrorForStatement(actualEvent)

        // Connecting can disconnect the connection it replaces, and a transaction on that connection
        // would be rolled back by it. A script that wants a new connection can finish first.
        if (ConnectionScope.transaction(actualEvent) != null) {
            DatabaseWork.report(
                actualEvent,
                firstLine,
                "A connection cannot be created inside a database transaction. Roll it back first."
            )
            return walk(actualEvent, false)
        }

        val databaseName = databaseNameExpr.getSingle(actualEvent)

        if (databaseName == null) {
            SkriptDatabaseErrors.set(actualEvent, "Database name in create connection section can't be null.")
            ErrorPrinter.printErrorMessageWithDetail(
                firstLine,
                "Database name in create connection section can't be null."
            )
            return walk(event, false)
        }

        // Only the named form reads this, and a name that resolved to nothing is a mistake worth
        // stopping for: connecting anyway would quietly replace the default connection instead.
        val connectionName = nameExpr?.getSingle(actualEvent)
        if (nameExpr != null && connectionName == null) {
            SkriptDatabaseErrors.set(actualEvent, "Connection name in create connection section can't be null.")
            ErrorPrinter.printErrorMessageWithDetail(
                firstLine,
                "Connection name in create connection section can't be null."
            )
            return walk(event, false)
        }

        if (!DatabaseRegistry.isSupported(databaseName)) {
            SkriptDatabaseErrors.set(actualEvent, "Database '$databaseName' is not supported.")
            ErrorPrinter.printErrorMessageWithDetail(
                firstLine,
                "Database '$databaseName' is not supported."
            )
            return walk(event, false)
        }

        val settings = ConnectionSettings(
            url = connectionProperties.getValue("url"),
            username = connectionProperties.getValue("username"),
            password = connectionProperties.getValue("password")
        )
        val implementationProperties = connectionProperties.filterKeys {
            it != "url" && it != "username" && it != "password"
        }

        val database = try {
            DatabaseRegistry.get(databaseName, implementationProperties)
        } catch (error: Throwable) {
            SkriptDatabaseErrors.set(actualEvent, error)
            ErrorPrinter.printErrorWithDetail(firstLine, error)
            return walk(actualEvent, false)
        }

        if (!SkriptOrm.instance.isEnabled || Database.isShuttingDown) {
            SkriptDatabaseErrors.set(actualEvent, "Database lifecycle is shutting down.")
            ErrorPrinter.printErrorMessageWithDetail(firstLine, "Database lifecycle is shutting down.")
            return walk(actualEvent, false)
        }

        val continuation = next
        val localVariables = SkriptLocalVariables.remove(actualEvent)
        Delay.addDelayedEvent(actualEvent)
        SkriptOrm.ioScope.launch {
            var failure: Throwable? = null
            try {
                if (connectionName != null) {
                    Database.connectNamed(connectionName, database, settings)
                } else {
                    Database.connectDefault(database, settings)
                }
            } catch (_: CancellationException) {
                return@launch
            } catch (error: Throwable) {
                failure = error
            }

            withContext(NonCancellable + SyncDispatcher) {
                if (!SkriptOrm.instance.isEnabled || Database.isShuttingDown) {
                    return@withContext
                }
                try {
                    if (localVariables != null) {
                        SkriptLocalVariables.restore(actualEvent, localVariables)
                    }
                    if (failure != null) {
                        SkriptDatabaseErrors.set(actualEvent, failure)
                        ErrorPrinter.printErrorWithDetail(firstLine, failure)
                    } else {
                        SkriptDatabaseErrors.clear(actualEvent)
                    }
                    walk(continuation, actualEvent)
                } finally {
                    SkriptLocalVariables.clear(actualEvent)
                }
            }
        }

        return null
    }

    override fun toString(event: Event?, debug: Boolean): String =
        if (nameExpr == null) {
            "create connection to database ${databaseNameExpr.toString(event, debug)}"
        } else {
            "create connection named ${nameExpr?.toString(event, debug)} to database " +
                databaseNameExpr.toString(event, debug)
        }
}
