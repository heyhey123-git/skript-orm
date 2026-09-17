package io.github.heyhey123.xiaojieorm.skript

import io.github.heyhey123.xiaojieorm.skript.elements.effects.EffDeleteById
import io.github.heyhey123.xiaojieorm.skript.elements.effects.EffDeleteUnfiltered
import io.github.heyhey123.xiaojieorm.skript.elements.effects.EffDisconnect
import io.github.heyhey123.xiaojieorm.skript.elements.effects.EffInsertIfAbsentFromVariable
import io.github.heyhey123.xiaojieorm.skript.elements.effects.EffInsertManyFromVariable
import io.github.heyhey123.xiaojieorm.skript.elements.effects.EffInsertOneFromVariable
import io.github.heyhey123.xiaojieorm.skript.elements.effects.EffSelectById
import io.github.heyhey123.xiaojieorm.skript.elements.effects.EffSelectManyUnfiltered
import io.github.heyhey123.xiaojieorm.skript.elements.effects.EffSelectOneUnfiltered
import io.github.heyhey123.xiaojieorm.skript.elements.effects.EffSelectPageUnfiltered
import io.github.heyhey123.xiaojieorm.skript.elements.effects.EffSetDefaultConnection
import io.github.heyhey123.xiaojieorm.skript.elements.effects.EffUpdateByIdFromVariable
import io.github.heyhey123.xiaojieorm.skript.elements.effects.EffUpsertByIdFromVariable
import io.github.heyhey123.xiaojieorm.skript.elements.effects.EffUseConnection
import io.github.heyhey123.xiaojieorm.skript.elements.expressions.ExprLastDatabaseError
import io.github.heyhey123.xiaojieorm.skript.elements.sections.SecCreateConnection
import io.github.heyhey123.xiaojieorm.skript.elements.sections.SecDelete
import io.github.heyhey123.xiaojieorm.skript.elements.sections.SecDeleteById
import io.github.heyhey123.xiaojieorm.skript.elements.sections.SecInConnection
import io.github.heyhey123.xiaojieorm.skript.elements.sections.SecInsertIfAbsent
import io.github.heyhey123.xiaojieorm.skript.elements.sections.SecInsertMany
import io.github.heyhey123.xiaojieorm.skript.elements.sections.SecInsertOne
import io.github.heyhey123.xiaojieorm.skript.elements.sections.SecRegisterTable
import io.github.heyhey123.xiaojieorm.skript.elements.sections.SecSelectById
import io.github.heyhey123.xiaojieorm.skript.elements.sections.SecSelectMany
import io.github.heyhey123.xiaojieorm.skript.elements.sections.SecSelectOne
import io.github.heyhey123.xiaojieorm.skript.elements.sections.SecSelectPage
import io.github.heyhey123.xiaojieorm.skript.elements.sections.SecUpdate
import io.github.heyhey123.xiaojieorm.skript.elements.sections.SecUpdateById
import io.github.heyhey123.xiaojieorm.skript.elements.sections.SecUpsertById
import org.skriptlang.skript.addon.SkriptAddon

/**
 * Registers every element this addon provides.
 *
 * Skript reads the syntax registry while it loads scripts, so this runs exactly once, while the
 * plugin enables. Listing the elements instead of loading their package is what the registered syntax
 * API asks for, and it has the side effect that an element written but never added here is visible:
 * the server test drives every element it expects, so one that is missing fails there rather than
 * quietly not existing.
 */
internal fun registerElements(addon: SkriptAddon) {
    EffDeleteById.register(addon)
    EffDeleteUnfiltered.register(addon)
    EffDisconnect.register(addon)
    EffInsertIfAbsentFromVariable.register(addon)
    EffInsertManyFromVariable.register(addon)
    EffInsertOneFromVariable.register(addon)
    EffSelectById.register(addon)
    EffSelectManyUnfiltered.register(addon)
    EffSelectOneUnfiltered.register(addon)
    EffSelectPageUnfiltered.register(addon)
    EffSetDefaultConnection.register(addon)
    EffUpdateByIdFromVariable.register(addon)
    EffUpsertByIdFromVariable.register(addon)
    EffUseConnection.register(addon)
    ExprLastDatabaseError.register(addon)

    SecCreateConnection.register(addon)
    SecDelete.register(addon)
    SecDeleteById.register(addon)
    SecInConnection.register(addon)
    SecInsertIfAbsent.register(addon)
    SecInsertMany.register(addon)
    SecInsertOne.register(addon)
    SecRegisterTable.register(addon)
    SecSelectById.register(addon)
    SecSelectMany.register(addon)
    SecSelectOne.register(addon)
    SecSelectPage.register(addon)
    SecUpdate.register(addon)
    SecUpdateById.register(addon)
    SecUpsertById.register(addon)
}
