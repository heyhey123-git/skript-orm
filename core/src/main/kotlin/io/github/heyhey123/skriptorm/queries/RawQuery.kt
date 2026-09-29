package io.github.heyhey123.skriptorm.queries

/**
 * A raw statement whose result is a set of rows.
 *
 * The rows are read before the statement is done, keyed by the column label the backend reported and
 * numbered from one, which is the shape `select many` stores: `{_rows::1::name}`. Nothing about the value
 * is interpreted — a raw read has no declaration to be converted back into — so what a script reads is
 * what the backend's own driver returned.
 */
abstract class RawQuery(
    statement: String,
    parameters: List<Any?>
) : RawStatement<Map<String, Any?>>(statement, parameters)
