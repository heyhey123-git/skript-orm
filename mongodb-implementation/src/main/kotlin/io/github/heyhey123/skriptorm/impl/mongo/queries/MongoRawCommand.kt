package io.github.heyhey123.skriptorm.impl.mongo.queries

import io.github.heyhey123.skriptorm.queries.RawCommand
import org.bson.Document

/**
 * A raw command sent to a document backend, answered with the document the server returned.
 *
 * The command is written as JSON text and parsed by the driver's own parser, so what is accepted is what
 * MongoDB accepts — including its extended JSON forms, which is how a command names a date or an object id.
 * Nothing about the command is checked before it is sent: see
 * [io.github.heyhey123.skriptorm.queries.RawStatement].
 *
 * The answer is handed over as it stands, except for the shapes a Skript variable cannot hold, which
 * [readable] documents. A command that changed documents reports how many inside that answer — there is no
 * separate count here, and inventing one would be this addon explaining a server it does not understand.
 */
open class MongoRawCommand(
    command: String,
    private val database: com.mongodb.client.MongoDatabase
) : RawCommand(command) {

    override suspend fun execute(): Map<String, Any?> {
        val answer = readable(database.runCommand(Document.parse(statement)))
        // The server always answers a command with a document, and readable turns a document into a map, so
        // anything else would mean the driver changed shape underneath this call. Saying so beats a cast
        // that would fail later with no account of what arrived.
        return answer as? Map<String, Any?>
            ?: throw IllegalStateException(
                "The server answered a command with ${answer?.javaClass?.name ?: "nothing"}, but a command " +
                    "document was expected."
            )
    }

    /**
     * A document as a value a Skript variable can hold.
     *
     * A [Document] becomes a map and a list becomes a list, so a script reads a field of the answer by name
     * and a listed answer by index. Everything else is left as it is when a variable can carry it, and named
     * by its text when it cannot: an object id, a date or a driver's own class has no Skript form, and a
     * value that cannot be stored would fail somewhere further away from the command that produced it.
     */
    private fun readable(value: Any?): Any? = when (value) {
        null, is String, is Number, is Boolean -> value
        is Document -> value.mapValues { readable(it.value) }.toMap()
        is Map<*, *> -> value.entries.associate { it.key.toString() to readable(it.value) }
        is List<*> -> value.map { readable(it) }
        else -> value.toString()
    }
}
