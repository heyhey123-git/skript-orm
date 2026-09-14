package io.github.heyhey123.xiaojieorm.skript.utils;

import ch.njol.skript.variables.Variables;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Java bridge for Skript APIs whose public methods expose package-private return types.
 * In order to avoid exposing package-private types in the public API, this class provides a bridge to access those methods.
 */
public final class SkriptLocalVariables {
    private SkriptLocalVariables() {
    }

    public static @Nullable Object remove(Event event) {
        return Variables.removeLocals(event);
    }

    public static void restore(Event event, @Nullable Object localVariables) {
        Variables.setLocalVariables(event, localVariables);
    }

    public static void clear(Event event) {
        Variables.removeLocals(event);
    }
}
