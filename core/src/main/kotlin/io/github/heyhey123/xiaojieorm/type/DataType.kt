package io.github.heyhey123.xiaojieorm.type


/**
 * Abstract class for types can be stored in database.
 *
 * @param D the domain type
 */
@Suppress("UNCHECKED_CAST")
interface DataType<D: Any>{

    /*
     * The domain type class.
     */
    val domainType: Class<D>

    /**
     * The unique type code.
     */
    val typeCode: String

    /**
     * The value converter for this data type.
     */
    val converter: ValueConverter<D, *>
        get() = DefaultValueConverter(domainType) as ValueConverter<D, *>
}
