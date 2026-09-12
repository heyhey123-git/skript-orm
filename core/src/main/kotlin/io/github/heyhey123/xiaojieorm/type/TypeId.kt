package io.github.heyhey123.xiaojieorm.type

/**
 * Type id enum class.
 *
 * @property code The code of the type.
 */
enum class TypeId(val code: String) {
    BOOLEAN("boolean"),
    TINYINT("tinyint"),
    INT("int"),
    BIGINT("bigint"),
    DOUBLE("double"),
    FLOAT("float"),
    STRING("string"),
    UUID("uuid"),
    ITEMSTACK("itemstack"),
    LOCATION("location"),
    BUKKIT_SERIALIZABLE("bukkitserializable"),
    NBT_COMPOUND("nbtcompound"),
    DATE("date"),
    TIME("time"),
    TIMESPAN("timespan");

    companion object {
        fun fromCode(code: String): TypeId? = entries.find { it.code == code }
    }
}
