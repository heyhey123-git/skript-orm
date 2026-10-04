package io.github.heyhey123.skriptorm.type

/** Where a converter may run; ANY requires that it neither reads nor changes server state. */
enum class ConversionThread { ANY, SERVER }
