package io.github.heyhey123.skriptorm.skript.utils

import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Variable
import io.github.heyhey123.skriptorm.result.WriteResult
import org.bukkit.event.Event

/**
 * The optional clause a write statement can end with, and what it does with the count it reports.
 *
 * `store affected rows in {_rows}` turns the count a write already computes into something a script can
 * read: how many rows the statement matched. That is what makes a safe conditional write expressible
 * without transactions — a statement written as
 *
 * ```
 * update entities in table "accounts" with limit 1 and wait:
 *     where all:
 *         id = {_from}
 *         balance = {_balance}
 *     values:
 *         balance: {_balance} - {_amount}
 * ```
 *
 * can then read the count, and a count of `0` means the row changed between the read and the write, so
 * the script re-reads and tries again. Nothing else in this addon reports whether a write matched, which
 * is why the clause exists rather than an expression: a variable the statement names cannot be confused
 * with the result of some other statement.
 *
 * ## When the variable is written, and when it is not
 *
 * The target is cleared when the statement starts, before anything that can refuse it, so a failure
 * leaves it unset rather than holding the value of an earlier statement — the same rule the error slot
 * follows, for the same reason: a stale number that looks like this statement's answer is the one
 * dangerous outcome here.
 *
 * It is then written only when the statement both succeeded and could count exactly. A backend that
 * answers a write without an exact per-row count (a JDBC batch answering `SUCCESS_NO_INFO`) leaves it
 * unset, because a number nobody can check is worse than no number. Every other statement reports an
 * exact count, so the three states are:
 *
 * - **set** — the statement ran and matched this many rows;
 * - **unset** — the statement was not written with a count to take (nothing was written, the statement
 *   failed before it ran, the count is not exact, or the statement was written without `and wait`, in
 *   which case the value is read before the write has happened);
 * - never `0` by accident: `0` is a real answer, "the filter matched nothing".
 */
internal object AffectedRows {

    /**
     * The clause, as a pattern fragment, appended to every write pattern after its own arguments.
     *
     * The expression is nullable (`%-number%`): the optional group around it is what makes the clause
     * optional, and without the `-` Skript would fill an omitted slot with a default expression taken
     * from the event, which is a silent way to write the count somewhere nobody asked for.
     *
     * The `and` is part of the pattern, as it is in `[wait:and wait]`, and it is not decoration: a group
     * that starts with a word after an expression slot makes Skript read that word as part of the
     * preceding expression. `delete ... by id %object% [store ...]` parses `by id 1 and store ...` with
     * the id `1 and`, and then the script is refused for using a variable where Skript expected a
     * literal. Starting the group with the conjunction gives the preceding expression a boundary it can
     * find, so the clause is written `and store affected rows in {_rows}`.
     *
     * It is always the **last** expression of a write pattern, which is what lets [target] find it
     * without every statement having to know its own index.
     */
    const val PATTERN = "[and store affected rows in %-number%]"

    /**
     * The variable [expression] names, or null when the clause was not written.
     *
     * @throws IllegalArgumentException if the clause names something that is not a single variable: a
     *   list variable would hold one number under a key nobody chose, and an expression cannot be
     *   written into at all.
     */
    fun target(expression: Expression<*>?): Variable<*>? {
        if (expression == null) return null
        require(expression is Variable<*> && !expression.isList) {
            "The affected row count must be stored in a single variable, for example {_rows}."
        }
        return expression
    }

    /** Clears [target], which is what a statement does before it can fail. */
    fun clear(target: Variable<*>?, event: Event?) {
        if (target == null) return
        VariableModifier.clear(target, event)
    }

    /** Writes what [result] reports into [target], or leaves it unset when that is not a number. */
    fun write(target: Variable<*>?, event: Event?, result: WriteResult) {
        if (target == null) return
        if (result.countExact) {
            VariableModifier.writeValue(target, event, result.affectedCount)
        } else {
            // Cleared when the statement started, so saying nothing is what leaves it unset.
            VariableModifier.clear(target, event)
        }
    }
}
