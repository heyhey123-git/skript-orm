package io.github.heyhey123.skriptorm.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.Trigger
import ch.njol.skript.lang.TriggerItem
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.skript.elements.sections.SecTransaction
import io.github.heyhey123.skriptorm.skript.utils.ConnectionScope
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.ErrorPrinter
import io.github.heyhey123.skriptorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

/**
 * Undoes the transaction the script is inside, and leaves its section.
 *
 * It is the only way a script ends a transaction other than reaching the end of the body, and it is
 * why `commit` is not offered: a commit that carried on with the body would leave the statements after
 * it in a transaction that no longer exists, and every answer to "which transaction are they in now"
 * is a surprise.
 *
 * Where it goes is the statement after the transaction section, which is found while the script is
 * parsed, the same way `exit` finds the sections it leaves.
 */
@Name("Rollback Database Transaction")
@Description("Rolls back the database transaction the script is inside and leaves its section, so the statements after the section run on the connection again. Can only be written inside a 'database transaction' section. Failures are logged and exposed as the last database error.")
@Example(
    """
database transaction:
    update one entity in table "accounts" by id {_from} and wait:
        values:
            balance: {_from::balance} - {_amount}
    if {_from::balance} < {_amount}:
        rollback database transaction
"""
)
@Since("1.0.0")
class EffRollbackTransaction : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffRollbackTransaction::class.java,
                "roll[ ]back [the] [current] [database] transaction"
            )
        }
    }

    /**
     * The transaction section this effect is written inside, resolved while parsing.
     *
     * It is what the effect has to walk on from, and it is also what proves at parse time that the
     * effect is inside a transaction at all.
     */
    private var section: SecTransaction? = null

    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        val enclosing = parser.currentSections.filterIsInstance<SecTransaction>().lastOrNull()
        if (enclosing == null) {
            Skript.error("A transaction can only be rolled back inside a 'database transaction' section.")
            return false
        }
        section = enclosing
        parser.hasDelayBefore = Kleenean.TRUE
        return true
    }

    /** Never called: the work is in [walk], the only place that runs on the server thread. */
    override fun execute(event: Event?) = Unit

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return next
        val target = section ?: return next
        val trigger = this.trigger

        val transaction = ConnectionScope.transaction(actualEvent)
        if (transaction == null) {
            report(actualEvent, trigger, "There is no database transaction to roll back.")
            return target.next
        }

        // The frame goes first: the statements after the section must not resolve into a transaction
        // that is on its way out.
        ConnectionScope.popOwned(actualEvent, target)

        return DatabaseWork.run(
            event = actualEvent,
            continuation = target.next,
            wait = true,
            query = { transaction.rollback() },
            onFailure = { error ->
                report(actualEvent, trigger, "The database transaction could not be rolled back: ${error.message}")
            }
        )
    }

    private fun report(event: Event, trigger: Trigger?, message: String) {
        SkriptDatabaseErrors.set(event, message)
        trigger?.let { ErrorPrinter.printErrorMessageWithDetail(it, message) }
    }

    override fun toString(event: Event?, debug: Boolean) = "rollback database transaction"
}
