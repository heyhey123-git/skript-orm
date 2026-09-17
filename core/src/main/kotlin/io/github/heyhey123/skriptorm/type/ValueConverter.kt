package io.github.heyhey123.skriptorm.type

/**
 * Abstract class for converting between domain type and storage type.
 *
 * @param D The domain type.
 * @param S The storage type, typically a type supported by the database.
 * @property domainType The Class object representing the domain type.
 * @property storageType The Class object representing the storage type.
 */
abstract class ValueConverter<D : Any, S : Any>(
    val domainType: Class<D>,
    val storageType: Class<S>
) {

    /**
     * Converts a value from the domain type to the storage type.
     *
     * @param value The value in the domain type.
     * @return The value converted to the storage type.
     */
    abstract fun toStorage(value: D): S

    /**
     * Converts a value from the storage type to the domain type.
     *
     * @param value The value in the storage type.
     * @return The value converted to the domain type.
     */
    abstract fun fromStorage(value: S): D
}
