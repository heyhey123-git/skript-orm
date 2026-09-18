package io.github.heyhey123.skriptorm.skript.elements.expressions

import ch.njol.skript.doc.*
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Last Database Error")
@Description("Returns the most recent database error for the current event. A statement that touches the database waits for its work, so a failure is in here once the lines after it run.")
@Example(
    """delete one entity from table "users" by id {_id} and wait
if last database error is set:
    send "Delete failed: %last database error%"
"""
)
@Since("1.0.0")
class ExprLastDatabaseError : SimpleExpression<String>() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.expression(
                addon,
                ExprLastDatabaseError::class.java,
                String::class.java,
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
