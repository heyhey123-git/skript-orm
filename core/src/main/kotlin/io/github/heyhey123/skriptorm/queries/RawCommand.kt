package io.github.heyhey123.skriptorm.queries

/**
 * A raw command sent to a document backend, whose result is the document it answered with.
 *
 * This is the shape MongoDB's own "run anything" entry point has: a command is a document, and the server
 * always answers with one document. It is deliberately not the shape a SQL backend has, where a statement
 * either returns rows or returns a count — a count does not exist here as a separate result, so this
 * statement does not pretend to produce one. A command that changed documents says how many it changed
 * inside the document it answered with.
 *
 * The command is written as JSON text and parsed by the backend's own parser. Like every raw statement it
 * is sent as it stands: see [RawStatement].
 */
abstract class RawCommand(
    statement: String
) : RawStatement<Map<String, Any?>>(statement, emptyList())
