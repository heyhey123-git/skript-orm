package io.github.heyhey123.xiaojieorm.database

/**
 * Thrown when a transaction cannot take a statement any more: its timeout expired, the server is
 * shutting down, or the connection under it was closed.
 *
 * It is separate from a database failure so that an element can tell "this transaction is gone" apart
 * from "this statement failed", which are different things to tell a script author.
 */
class TransactionAbortedException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
