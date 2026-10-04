package io.github.heyhey123.skriptorm.type

/**
 * Default implementation of [ValueConverter] that performs no conversion.
 *
 * @param T the type of the value
 *
 * @param type the class of the type
 */
class DefaultValueConverter<T : Any>(type: Class<T>) : ValueConverter<T, T>(
    type,
    type
) {

    // Identity conversion never invokes an object method or touches server state.
    override val writeThread: ConversionThread get() = ConversionThread.ANY
    override val readThread: ConversionThread get() = ConversionThread.ANY

    override fun toStorage(value: T): T = value

    override fun fromStorage(value: T): T = value
}
