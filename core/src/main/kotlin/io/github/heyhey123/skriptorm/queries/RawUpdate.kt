package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.result.WriteResult

/**
 * A raw statement whose result is a count of affected rows.
 *
 * This is the statement for anything that changes the database rather than reading from it, including the
 * statements no declaration could express — `ALTER TABLE`, `CREATE INDEX`, a server-specific command. What
 * the count means is the backend's answer, exactly as [WriteResult] documents for the declared writes.
 */
abstract class RawUpdate(
    statement: String,
    parameters: List<Any?>
) : RawStatement<WriteResult>(statement, parameters)
