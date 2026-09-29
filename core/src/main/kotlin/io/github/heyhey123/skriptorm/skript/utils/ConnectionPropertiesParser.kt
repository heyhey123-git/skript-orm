package io.github.heyhey123.skriptorm.skript.utils

import ch.njol.skript.config.SectionNode
import io.github.heyhey123.skriptorm.database.ConnectionProperties
import io.github.heyhey123.skriptorm.database.DatabaseFactory

/**
 * Reads the literal properties of a `create a connection` section.
 *
 * A section body line arrives as one node whose key is the whole line, `url: "jdbc:..."` and not
 * just `url`, which [SkriptConfigProbeTest] pins. A property name is therefore the text before the
 * first colon and its value is the text after it, the same way [ValuesParser], [WhereParser] and the
 * register-table section read the bodies they own.
 *
 * The text is read here rather than through Skript's literal entry data, which reads the node key as
 * well but returned no value for these bodies on a real server.
 *
 * A value is taken literally. Wrapping it in double quotes is allowed and the quotes are dropped;
 * nothing else about it is interpreted.
 */
object ConnectionPropertiesParser {

    private const val URL = ConnectionProperties.URL

    /** Runs of whitespace inside a property name, collapsed so that spacing cannot hide a property. */
    private val WHITESPACE = Regex("\\s+")

    /** The properties a connection always reads, so that no caller has to check for them. */
    private val CONNECTION_PROPERTIES = listOf(URL, ConnectionProperties.USERNAME, ConnectionProperties.PASSWORD)

    /**
     * Collects the properties a `create a connection` body declares.
     *
     * The result always holds every connection property. `url` has to be declared and be non-empty;
     * `username` and `password` fall back to an empty string, which is what a connection without
     * credentials wants, so a body only needs the properties it actually sets. Any other property is
     * kept as it was written, and [requireAcceptedBy] is what stops one that nothing reads from being
     * passed over in silence.
     *
     * A name is trimmed and its inner runs of whitespace are collapsed to one space, so that
     * `statement  timeout` and `statement timeout` are the same property: a property name is looked up
     * by name, and spacing that changed the name would otherwise hide the property.
     *
     * @param section the body of the section
     * @return every connection property, plus every extra property the body declares
     * @throws IllegalArgumentException if a property is a block, is malformed, or is declared without
     *         a value
     */
    fun collectFrom(section: SectionNode): Map<String, String> {
        val declared = linkedMapOf<String, String>()
        for (node in section) {
            val line = requireNotNull(node.key) { "A connection property cannot be empty." }
            val name = line.substringBefore(':').trim().replace(WHITESPACE, " ")
            // A property written without a value ends its line with a colon, which is what turns it
            // into a section node, empty or not.
            if (node is SectionNode) {
                throw IllegalArgumentException(
                    if (node.none()) {
                        "Connection property '$name' has no value."
                    } else {
                        "Connection property '$name' must be a value, not a block."
                    }
                )
            }
            val separator = line.indexOf(':')
            require(separator > 0) { "Invalid connection property '$line'. Expected 'name: value'." }
            val value = line.substring(separator + 1).trim()
            require(value.isNotEmpty()) { "Connection property '$name' has no value." }
            declared[name] = unquote(value)
        }

        require(!declared[URL].isNullOrEmpty()) { "A connection requires a url property." }

        val properties = linkedMapOf<String, String>()
        CONNECTION_PROPERTIES.forEach { name -> properties[name] = declared[name] ?: "" }
        declared.forEach { (name, value) -> properties.putIfAbsent(name, value) }
        return properties
    }

    /**
     * Refuses a property the implementation does not read, naming what it does read instead.
     *
     * [collectFrom] deliberately keeps every property it finds: it cannot know what an implementation
     * reads, and handing the body over whole is what lets an implementation add a property without this
     * parser changing. The check therefore belongs here, where both the collected properties and the
     * implementation are known, and it happens before the connection is opened.
     *
     * Reading a property and ignoring it is the one outcome this refuses. A body declaring
     * `database: "sicilia_db"` against MySQL used to connect to a server with no default database and
     * fail later, on the first statement that needed one, with a message that named neither the property
     * nor the connection. A typo in a property name was the same failure with less to go on.
     *
     * @param properties the properties [collectFrom] returned
     * @param factory the implementation the connection was asked for
     * @throws IllegalArgumentException naming the property, and every name that would have been read
     */
    fun requireAcceptedBy(properties: Map<String, String>, factory: DatabaseFactory) {
        val accepted = ConnectionProperties.ALWAYS_READ + factory.acceptedConnectionProperties
        for (name in properties.keys) {
            if (name in accepted) continue
            val specific = factory.describeRejectedProperty(name)
            if (specific != null) throw IllegalArgumentException(specific)
            throw IllegalArgumentException(
                "Connection property '$name' is not read by database '${factory.typeName}'. " +
                    "It reads: ${accepted.sorted().joinToString(", ")}."
            )
        }
    }

    private fun unquote(value: String): String = value.removeSurrounding("\"")
}
