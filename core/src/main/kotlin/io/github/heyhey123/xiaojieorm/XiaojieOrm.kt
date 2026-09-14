package io.github.heyhey123.xiaojieorm

import ch.njol.skript.Skript
import io.github.heyhey123.xiaojieorm.database.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin

class XiaojieOrm: JavaPlugin() {
    companion object {
        lateinit var instance: XiaojieOrm
            private set

        internal var ioScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            private set
    }

    override fun onEnable() {
        instance = this
        Database.beginLifecycle()
        ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        Skript.registerAddon(this)
            .loadClasses("io.github.heyhey123.xiaojieorm.skript", "elements")

        val candidates = listOf(
            "io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDatabaseFactory",
            "io.github.heyhey123.xiaojieorm.impl.jdbc.database.MysqlDatabaseFactory",
            "io.github.heyhey123.xiaojieorm.impl.pg.database.PgDatabaseFactory",
            "io.github.heyhey123.xiaojieorm.impl.mongo.database.MongodbDatabaseFactory",
            "io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabaseFactory"
        )

        for (candidate in candidates) {
            try {
                Class.forName(candidate)
            } catch (_: ClassNotFoundException) { }
        }
    }

    override fun onDisable() {
        try {
            runBlocking(Dispatchers.IO) {
                Database.shutdown()
            }
        } catch (error: Throwable) {
            logger.severe("Failed to disconnect the database during plugin shutdown: ${error.message}")
        } finally {
            ioScope.cancel("Plugin disabled")
        }
    }
}
