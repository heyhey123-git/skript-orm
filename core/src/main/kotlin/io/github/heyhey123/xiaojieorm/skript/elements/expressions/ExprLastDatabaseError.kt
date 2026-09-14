package io.github.heyhey123.xiaojieorm.skript.elements.expressions

import ch.njol.skript.Skript
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.util.Kleenean
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptDatabaseErrors
import org.bukkit.event.Event

class ExprLastDatabaseError : SimpleExpression<String>() {
    companion object {
        init {
            Skript.registerExpression(
                ExprLastDatabaseError::class.java,
                String::class.java,
                ExpressionType.SIMPLE,
                "[the] last (database|query) error"
            )
        }
    }

    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ) = true

    override fun get(event: Event): Array<String>? =
        SkriptDatabaseErrors.get(event)?.let { arrayOf(it) }

    override fun isSingle() = true

    override fun getReturnType() = String::class.java

    override fun toString(event: Event?, debug: Boolean) = "last database error"
}
