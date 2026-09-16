package io.github.heyhey123.xiaojieorm.skript.utils

import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Section
import org.skriptlang.skript.addon.SkriptAddon
import org.skriptlang.skript.registration.DefaultSyntaxInfos
import org.skriptlang.skript.registration.SyntaxInfo
import org.skriptlang.skript.registration.SyntaxRegistry

/**
 * Registers this addon's syntax with Skript.
 *
 * The `Skript.registerSection`, `registerEffect` and `registerExpression` shortcuts are deprecated in
 * favour of registering a [SyntaxInfo] with the addon's own [SyntaxRegistry], which is what these do.
 * Registering through the addon's registry also attributes the syntax to this addon, so the `@Name`,
 * `@Description` and `@Since` annotations do not have to be read back by hand the way the deprecated
 * shortcuts did it.
 *
 * Each element keeps its own patterns and calls these from a `register` function, which
 * [io.github.heyhey123.xiaojieorm.XiaojieOrm] calls for every element while it enables. Registering
 * is not something to spread out: Skript reads the registry while it loads scripts, and the addon it
 * belongs to only exists once the plugin is enabling.
 */
internal object SkriptSyntax {

    fun section(addon: SkriptAddon, type: Class<out Section>, vararg patterns: String) {
        addon.syntaxRegistry().register(
            SyntaxRegistry.SECTION,
            SyntaxInfo.builder(type).addPatterns(*patterns).build()
        )
    }

    fun effect(addon: SkriptAddon, type: Class<out Effect>, vararg patterns: String) {
        addon.syntaxRegistry().register(
            SyntaxRegistry.EFFECT,
            SyntaxInfo.builder(type).addPatterns(*patterns).build()
        )
    }

    /**
     * Registers an expression.
     *
     * The priority is stated because the deprecated `registerExpression` took an `ExpressionType` and
     * translated it into one; sorting is [SyntaxInfo.SIMPLE] unless an element needs to be found
     * before or after the others. The builder lives on [DefaultSyntaxInfos], which declares the
     * expression shape that `SyntaxRegistry.EXPRESSION` holds; Kotlin does not reach it through
     * [SyntaxInfo].
     */
    fun <E : Expression<R>, R> expression(addon: SkriptAddon, type: Class<E>, returnType: Class<R>, vararg patterns: String) {
        addon.syntaxRegistry().register(
            SyntaxRegistry.EXPRESSION,
            DefaultSyntaxInfos.Expression.builder(type, returnType)
                .priority(SyntaxInfo.SIMPLE)
                .addPatterns(*patterns)
                .build()
        )
    }
}
