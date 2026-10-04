package io.github.heyhey123.skriptorm.type

/** A conversion whose input is independent of a database cursor or connection. */
class DeferredDatabaseValue(
    val thread: ConversionThread,
    private val conversion: () -> Any
) {
    fun resolve(): Any = conversion()
}
