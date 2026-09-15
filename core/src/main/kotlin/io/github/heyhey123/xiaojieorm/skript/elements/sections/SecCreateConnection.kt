package io.github.heyhey123.xiaojieorm.skript.elements.sections

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
import io.github.heyhey123.xiaojieorm.XiaojieOrm
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.database.DatabaseRegistry
import io.github.heyhey123.xiaojieorm.skript.utils.ErrorPrinter
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptLocalVariables
import io.github.heyhey123.xiaojieorm.utils.SyncDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.event.Event
import org.skriptlang.skript.lang.entry.EntryValidator
import org.skriptlang.skript.lang.entry.util.LiteralEntryData

@Name("Create Database Connection")
@Description("Connects to a registered database implementation and makes it current. This section always waits. The url property is required; username and password may be empty strings. Additional literal properties are passed to the implementation. Failures are logged and exposed as the last database error.")
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
@Since("1.0")
class SecCreateConnection : Section() {

    companion object {
        init {
            Skript.registerSection(
                SecCreateConnection::class.java,
                "create [a] connection to [database] %string% [with properties]"
            )
        }
    }

    private lateinit var databaseNameExpr: Expression<String>
    private lateinit var connectionProperties: MutableMap<String, String>

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
        databaseNameExpr = expressions[0] as Expression<String>
        return parseNode(sectionNode)
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseNode(sectionNode: SectionNode): Boolean {
        val properties = mutableMapOf<String, String?>(
            "url" to null,
            "username" to null,
            "password" to null
        )
        val builder = EntryValidator.builder()
            .addEntryData(LiteralEntryData("url", null, false, String::class.java))
            .addEntryData(LiteralEntryData("username", null, true, String::class.java))
            .addEntryData(LiteralEntryData("password", null, true, String::class.java))

        for (node in sectionNode) {
            if (node.name() in listOf("url", "username", "password")) continue
            builder.addEntryData(LiteralEntryData(node.name(), null, true, String::class.java))
            properties[node.name()!!] = null
        }

        val container = builder.build().validate(sectionNode)
        if (container == null) {
            Skript.error("Invalid connection properties in create connection section.")
            return false
        }

        for (key in properties.keys) {
            val value = container.getOptional(key, true) as String?
            if (value == null) {
                Skript.error("The property values can't be null.")
                return false
            }
            properties[key] = value
        }

        connectionProperties = properties as MutableMap<String, String>
        return true
    }

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return walk(event, false)
        val firstLine: Trigger = this.trigger ?: return walk(event, false)
        SkriptDatabaseErrors.clear(actualEvent)
        val databaseName = databaseNameExpr.getSingle(actualEvent)

        if (databaseName == null) {
            SkriptDatabaseErrors.set(actualEvent, "Database name in create connection section can't be null.")
            ErrorPrinter.printErrorMessageWithDetail(
                firstLine,
                "Database name in create connection section can't be null."
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

        val url = connectionProperties.getValue("url")
        val username = connectionProperties.getValue("username")
        val password = connectionProperties.getValue("password")
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

        if (!XiaojieOrm.instance.isEnabled || Database.isShuttingDown) {
            SkriptDatabaseErrors.set(actualEvent, "Database lifecycle is shutting down.")
            ErrorPrinter.printErrorMessageWithDetail(firstLine, "Database lifecycle is shutting down.")
            return walk(actualEvent, false)
        }

        val continuation = next
        val localVariables = SkriptLocalVariables.remove(actualEvent)
        Delay.addDelayedEvent(actualEvent)
        XiaojieOrm.ioScope.launch {
            var failure: Throwable? = null
            try {
                Database.replaceWith(database, url, username, password)
            } catch (_: CancellationException) {
                return@launch
            } catch (error: Throwable) {
                failure = error
            }

            withContext(NonCancellable + SyncDispatcher) {
                if (!XiaojieOrm.instance.isEnabled || Database.isShuttingDown) {
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

    override fun toString(event: Event?, debug: Boolean) =
        "create connection to database ${databaseNameExpr.toString(event, debug)}"
}
