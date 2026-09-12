package io.github.heyhey123.xiaojieorm

import ch.njol.skript.Skript
import io.github.heyhey123.xiaojieorm.database.Database
import org.bukkit.plugin.java.JavaPlugin

class XiaojieOrm: JavaPlugin() {
    companion object {
        lateinit var instance: XiaojieOrm
            private set
    }

    override fun onEnable() {
        instance = this
        Skript.registerAddon(this)
            .loadClasses("io.github.heyhey123.xiaojieorm.skript", "elements")

        val candidates = listOf(
            "io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDatabaseFactory",
            "\"io.github.heyhey123.xiaojieorm.impl.jdbc.database.MysqlDatabaseFactory",
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
        Database.current?.disconnect()
    }
}
