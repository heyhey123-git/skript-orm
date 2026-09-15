package io.github.heyhey123.xiaojieorm.type

/**
 * Describes a domain type supported by a database implementation.
 *
 * @param D non-null domain value type
 */
interface DataType<D : Any> {

    /** Runtime class accepted for domain values. */
    val domainType: Class<D>

    /** Stable code used to register and resolve this logical type. */
    val typeCode: String

    /**
     * Converts domain values to backend storage values; defaults to identity conversion.
     *
     * Implementations that need a different conversion override this property with their own
     * converter, which is usually a stateless object they can share.
     */
    val converter: ValueConverter<D, *>
        get() = DefaultValueConverter(domainType)
}
