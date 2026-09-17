package io.github.heyhey123.skriptorm.database

/**
 * Where a connection is opened and who it opens as.
 *
 * These three values are the part of connecting that belongs to the script; the implementation owns
 * everything else, which is its driver or client, its dialect, and its pool. They are grouped rather
 * than passed one after another because three consecutive strings say nothing about themselves: a
 * call site can swap the username and the password and still compile, and the reader has to count
 * arguments to find out which is which.
 */
data class ConnectionSettings(
    val url: String,
    val username: String,
    val password: String
)
