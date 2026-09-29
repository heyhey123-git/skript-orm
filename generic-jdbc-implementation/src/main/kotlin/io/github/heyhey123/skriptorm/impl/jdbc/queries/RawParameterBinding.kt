package io.github.heyhey123.skriptorm.impl.jdbc.queries

import java.sql.PreparedStatement

/**
 * The values a raw SQL statement accepts, and the refusal for anything else.
 *
 * A declared statement knows each column's type, so it converts a script's value into that column's
 * storage form and can say exactly why a value does not fit. A raw statement knows none of that: the
 * placeholder it binds to could be any column, of any type, in any table, or no column at all. There is
 * nothing to check a value against, and guessing a conversion would be the worst of both — a value stored
 * in a form the script never asked for, discovered later by reading it back.
 *
 * So the rule is the driver's own: a raw parameter is a value the driver already knows how to send. A
 * script that wants a domain object in a raw statement converts it itself, which is what "raw" means
 * everywhere else in this feature.
 */
internal fun PreparedStatement.bindRawParameters(parameters: List<Any?>) {
    // Every value is checked before any is bound. Binding and validating in one pass would leave a
    // statement half-bound when the third of five parameters is refused, and a prepared statement that is
    // never executed still holds those values until it is closed.
    val bindable = parameters.mapIndexed { index, value -> requireBindable(value, index) }
    bindable.forEachIndexed { index, value ->
        // The driver infers the SQL type from the value. No target type is passed because there is none to
        // pass: `setObject(int, Object, int)` needs one, and any answer chosen here would be this addon
        // deciding a column's type for a statement it cannot see.
        setObject(index + 1, value)
    }
}

/**
 * The value as the driver will receive it.
 *
 * The list is the driver's, and the refusal says what the list is rather than what to convert into it: which
 * Skript value becomes which driver value is the script's business here, and naming the domain types would
 * have this layer describing types it does not have a registry for — the advice would go stale in a module
 * that never sees them.
 *
 * @throws IllegalArgumentException when the value is not one the driver can send as it stands
 */
internal fun requireBindable(value: Any?, index: Int): Any? {
    if (value == null) return null
    if (value is String || value is Number || value is Boolean || value is ByteArray) return value
    if (value is java.util.Date) return value
    throw IllegalArgumentException(
        "Raw statement parameter ${index + 1} is a ${value.javaClass.name}, which a raw statement cannot " +
            "bind. Raw parameters must be values the database driver sends as they stand: a string, a " +
            "number, a boolean, a byte array, or a date — null is allowed. A raw statement names no column, " +
            "so there is no type to convert a value for: convert it in the script first."
    )
}
