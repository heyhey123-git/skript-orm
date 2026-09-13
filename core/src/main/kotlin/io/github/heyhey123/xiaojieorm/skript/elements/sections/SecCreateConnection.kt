package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.config.SectionNode
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Example
import ch.njol.skript.doc.Name
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Section
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.Trigger
import ch.njol.skript.lang.TriggerItem
import ch.njol.util.Kleenean
import io.github.heyhey123.xiaojieorm.database.DatabaseRegistry
import io.github.heyhey123.xiaojieorm.skript.utils.ErrorPrinter
import org.bukkit.event.Event
import org.skriptlang.skript.lang.entry.EntryValidator
import org.skriptlang.skript.lang.entry.util.LiteralEntryData

@Name("Create Connection")
@Description(
    "Creates a connection to a specified database with given properties.",
    "Supported databases must be registered in the Database Registry.",
    "The section requires properties must contain URL, username, and password.",
    "Other properties should be supported by the specific database implementation."
)
@Example(
    """
connect to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"
"""
)
class SecCreateConnection: Section()  {

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
        val databaseName = databaseNameExpr.getSingle(event)
        val firstLine: Trigger = this.first!!.trigger!!

        if (databaseName == null) {
            ErrorPrinter.printErrorMessageWithDetail(
                firstLine,
                "Database name in create connection section can't be null."
            )
            return walk(event, false)
        }

        if (!DatabaseRegistry.isSupported(databaseName)) {
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

        val database = DatabaseRegistry.get(databaseName, implementationProperties)
        try {
            database.connect(url, username, password)
        } catch (e: Exception) {
            ErrorPrinter.printErrorWithDetail(
                firstLine,
                e
            )
            return walk(event, false)
        }

        return walk(event, false)
    }

    override fun toString(event: Event?, debug: Boolean) = "create connection to database ${databaseNameExpr.toString(event, debug)}"

}
