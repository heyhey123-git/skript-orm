package io.github.heyhey123.xiaojieorm

import ch.njol.skript.Skript
import ch.njol.skript.util.Version
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.logging.LogoPrinter
import io.github.heyhey123.xiaojieorm.skript.registerElements
import io.github.heyhey123.xiaojieorm.type.nbt.NbtSupport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin
import org.skriptlang.skript.addon.SkriptAddon

class XiaojieOrm : JavaPlugin() {
    companion object {

        /**
         * The oldest Skript this plugin can run on.
         *
         * Every element in [io.github.heyhey123.xiaojieorm.skript] is registered through the addon
         * API, and the plugin links against this version at compile time. `plugin.yml` cannot state
         * the requirement: Bukkit runs every `depend` entry through
         * `PluginDescriptionFile.makePluginNameList`, which replaces spaces with underscores, so
         * `Skript 2.16+` would be looked up as a plugin named `Skript_2.16+` and the plugin would not
         * load on *any* server. The floor therefore has to be checked in code, in [onEnable].
         */
        val MINIMUM_SKRIPT_VERSION = Version("2.16.2")

        lateinit var instance: XiaojieOrm
            private set

        internal var ioScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            private set
    }

    override fun onEnable() {
        instance = this
        Database.beginLifecycle()

        if (!skriptIsSupported()) {
            server.pluginManager.disablePlugin(this)
            return
        }

        ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        LogoPrinter.print(pluginMeta.version)
        registerElements(registerSkriptAddon())
        reportNbtSupport()

        val candidates = listOf(
            "io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDatabaseFactory",
            "io.github.heyhey123.xiaojieorm.impl.jdbc.database.MysqlDatabaseFactory",
            "io.github.heyhey123.xiaojieorm.impl.pg.database.PgDatabaseFactory",
            "io.github.heyhey123.xiaojieorm.impl.mongo.database.MongodbDatabaseFactory"
        )

        for (candidate in candidates) {
            try {
                Class.forName(candidate)
            } catch (_: ClassNotFoundException) {
            }
        }
    }

    /**
     * Tells [NbtSupport] where SkBee keeps its classes, and says in the console what it found.
     *
     * SkBee is a Paper plugin, and Paper does not put a Paper plugin's classes where a Bukkit plugin's
     * own class loader can reach them, so this hands over SkBee's loader. Without it an NBT column
     * could never be used, however plainly SkBee was installed.
     *
     * The line it logs earns its place: NBT is the one column type that depends on another plugin, so
     * a server owner who reads "unavailable" here already knows why registering such a table was
     * refused, and does not have to guess at the message the refusal gave them.
     */
    private fun reportNbtSupport() {
        NbtSupport.useClassLoaderLookup { server.pluginManager.getPlugin("SkBee")?.javaClass?.classLoader }
        val reason = NbtSupport.unavailableReason
        if (reason == null) {
            logger.info("NBT support: ${NbtSupport.provider}.")
        } else {
            logger.info("NBT support: unavailable. $reason.")
        }
    }

    /**
     * Reports whether the Skript that is currently running is new enough to be linked against.
     *
     * On failure the plugin disables itself instead of throwing: a plugin that fails to enable is
     * still "installed" as far as the server is concerned, and every database statement in the
     * user's scripts would then fail with a confusing "no such expression" error rather than the
     * single line below.
     */
    private fun skriptIsSupported(): Boolean {
        val running = Skript.getVersion()
        if (!running.isSmallerThan(MINIMUM_SKRIPT_VERSION)) return true

        logger.severe(
            "xiaojie-orm requires Skript $MINIMUM_SKRIPT_VERSION or newer, but this server runs " +
                "Skript $running. Disabling xiaojie-orm: upgrade Skript and restart the server."
        )
        return false
    }

    /**
     * Returns the addon Skript knows this plugin by.
     *
     * `Skript.registerAddon(JavaPlugin)` is deprecated for removal. Its replacement registers an addon
     * with a Skript instance, and that instance has to be the server's own: `Skript.instance()`, on the
     * class this plugin already reads the version from, is that one, while a Skript built with
     * `Skript.of(...)` keeps a syntax registry of its own, whose elements no script would ever see. An
     * addon registered here gets a view of the server's registry carrying its own origin, and that view
     * is where the syntax below ends up.
     *
     * The name comes from `plugin.yml`, so renaming the plugin renames the addon with it. It may not
     * collide with the name of the Skript instance itself, which is the only name ruled out.
     */
    private fun registerSkriptAddon(): SkriptAddon =
        Skript.instance().registerAddon(XiaojieOrm::class.java, pluginMeta.name)

    override fun onDisable() {
        try {
            runBlocking(Dispatchers.IO) {
                Database.shutdown()
            }
        } catch (error: Throwable) {
            logger.severe("Failed to disconnect the database during plugin shutdown: ${error.message}")
        } finally {
            ioScope.cancel("Plugin disabled")
            LogoPrinter.printFarewell(pluginMeta.version)
        }
    }
}
