package io.github.heyhey123.xiaojieorm.skript.utils

import ch.njol.skript.config.SectionNode

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

    private const val URL = "url"

    /** Runs of whitespace inside a property name, collapsed so that spacing cannot hide a property. */
    private val WHITESPACE = Regex("\\s+")

    /** The properties a connection always reads, so that no caller has to check for them. */
    private val CONNECTION_PROPERTIES = listOf(URL, "username", "password")

    /**
     * Collects the properties a `create a connection` body declares.
     *
     * The result always holds every connection property. `url` has to be declared and be non-empty;
     * `username` and `password` fall back to an empty string, which is what a connection without
     * credentials wants, so a body only needs the properties it actually sets. Any other property is
     * passed through to the implementation.
     *
     * A name is trimmed and its inner runs of whitespace are collapsed to one space, so that
     * `statement  timeout` and `statement timeout` are the same property. Anything else would be
     * ignored silently, since an implementation only looks up the names it knows.
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

    private fun unquote(value: String): String = value.removeSurrounding("\"")
}
