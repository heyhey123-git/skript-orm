package io.github.heyhey123.xiaojieorm.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import io.github.heyhey123.xiaojieorm.database.Database
import org.bukkit.event.Event

class EffDisconnect: Effect() {
    companion object {
        init {
            Skript.registerEffect(
                EffDisconnect::class.java,
                "disconnect [from] [the] [current] database [connection]"
            )
        }
    }

    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean = true

    override fun execute(event: Event?) {
        Database.current?.disconnect()
    }

    override fun toString(event: Event?, debug: Boolean) =
        "disconnect from the current database connection"
}
