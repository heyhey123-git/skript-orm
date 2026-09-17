package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.config.SectionNode
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Section
import ch.njol.skript.lang.SectionExitHandler
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import ch.njol.util.Kleenean
import org.bukkit.event.Event

/**
 * A section whose body runs while something is in effect, and which is told when that body ends.
 *
 * Two things make this worth sharing rather than writing twice. The first is that Skript has no
 * end-of-body callback: the loader points the body's last statement at whatever follows the section,
 * so a section that has to act at the end of its own body places a node there itself. The second is
 * that `exit`, `stop` and `return` leave a body early through `SectionExitHandler`, which a section has
 * to implement or it will not hear about them.
 *
 * [onBodyEnd] and [leaveBody] are the two moments a subclass acts on, and they are deliberately
 * different: a body that ran to its end is the normal case, while a body that was left early has to be
 * undone.
 */
abstract class ScopedBodySection : Section(), SectionExitHandler {

    @Suppress("UNCHECKED_CAST")
    final override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult,
        sectionNode: SectionNode,
        triggerItems: List<TriggerItem?>
    ): Boolean {
        if (!prepare(expressions, matchedPattern, parseResult)) return false
        loadCode(sectionNode)
        if (!validateBody()) return false
        spliceBodyEnd()
        return true
    }

    /** Reads this section's own expressions. Returning false rejects the line, exactly as `init` does. */
    protected abstract fun prepare(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        parseResult: SkriptParser.ParseResult
    ): Boolean

    /**
     * Called once the body has been parsed, so a subclass can refuse a body it cannot work with.
     * `first` and `last` are set at this point.
     */
    protected open fun validateBody(): Boolean = true

    /**
     * Runs when the body reached its end.
     *
     * @param continuation the statement after the section, which [TriggerItem.walk] continues from, or
     * null when the trigger has been parked and will be walked on later.
     */
    protected abstract fun onBodyEnd(event: Event, continuation: TriggerItem?): TriggerItem?

    /** Runs when `exit`, `stop` or `return` left the body early. */
    abstract fun leaveBody(event: Event)

    final override fun exit(event: Event) = leaveBody(event)

    /**
     * Puts the end-of-body node after the body's last statement, and moves [last] onto it.
     *
     * Doing this at parse time rather than when the loader links the section covers both cases. When
     * the section is followed by another statement, the loader's `setNext` reaches the node through
     * [last] and keeps the chain in order; when the section is the last statement of a trigger, that
     * call never happens at all (`ScriptLoader.loadItems` links every item but the last), and the node
     * placed here is the only thing that ends the body.
     */
    private fun spliceBodyEnd() {
        val bodyLast = last
        val node = BodyEnd(this)
        if (bodyLast != null) {
            bodyLast.setNext(node)
            last = node
        }
    }

    /** The statement that is the end of a body, so that the end is something a trigger can walk into. */
    private class BodyEnd(private val section: ScopedBodySection) : TriggerItem(section) {

        /** Never called: this item overrides [walk], which is what the loader walks. */
        override fun run(event: Event): Boolean = throw UnsupportedOperationException()

        override fun walk(event: Event?): TriggerItem? {
            if (event == null) return next
            debug(event, true)
            return section.onBodyEnd(event, next)
        }

        override fun toString(event: Event?, debug: Boolean) = "end of ${section.javaClass.simpleName}"
    }
}
