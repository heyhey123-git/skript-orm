package io.github.heyhey123.xiaojieorm.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.util.AsyncEffect
import ch.njol.util.Kleenean
import io.github.heyhey123.xiaojieorm.database.Database
import kotlinx.coroutines.runBlocking
import org.bukkit.event.Event

@Name("Disconnect Database")
@Description("Disconnects the current database asynchronously. The following trigger item runs after disconnection finishes.")
@Example("disconnect from the current database")
@Since("1.0")
class EffDisconnect : AsyncEffect() {
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
    ): Boolean {
        parser.hasDelayBefore = Kleenean.TRUE
        return true
    }

    override fun execute(event: Event?) {
        runBlocking {
            Database.current?.disconnect()
        }
    }

    override fun toString(event: Event?, debug: Boolean) =
        "disconnect from the current database connection"
}
