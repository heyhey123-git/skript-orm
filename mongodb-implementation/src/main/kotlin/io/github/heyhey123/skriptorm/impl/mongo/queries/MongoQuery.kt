package io.github.heyhey123.skriptorm.impl.mongo.queries

import com.mongodb.client.MongoDatabase

/**
 * Shared blocking MongoDB execution boundary; `suspend` methods do not switch dispatchers, exactly as the
 * JDBC implementation treats its driver.
 *
 * The blocking driver is deliberate. The Kotlin coroutine driver's API is built on kotlinx.coroutines,
 * which this plugin shades and relocates, so a bundled module calling `toList` on the driver's `FindFlow`
 * would ask for the relocated `Flow` while the driver implemented the original one. A driver whose API
 * names no Kotlin type sidesteps that, and a statement that blocks holds its lease the way a JDBC one does.
 */
interface MongoQuery {

    val database: MongoDatabase
}
