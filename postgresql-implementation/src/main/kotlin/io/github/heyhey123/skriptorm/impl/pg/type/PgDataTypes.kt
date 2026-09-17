package io.github.heyhey123.skriptorm.impl.pg.type

import io.github.heyhey123.skriptorm.impl.jdbc.type.BigIntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.BooleanJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.DoubleJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.FloatJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.LocationJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.SkriptDateJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.SkriptTimeJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.SkriptTimespanJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.TinyIntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.UuidJdbcDataType
import io.github.heyhey123.skriptorm.type.ConfigurationSerializableDataType
import io.github.heyhey123.skriptorm.type.DataType
import io.github.heyhey123.skriptorm.type.DataTypes
import io.github.heyhey123.skriptorm.type.ItemStackDataType
import io.github.heyhey123.skriptorm.type.NbtDataType
import io.github.heyhey123.skriptorm.type.TypeId
import io.github.heyhey123.skriptorm.type.ValueConverter
import io.github.heyhey123.skriptorm.type.nbt.NbtSupport
import io.github.heyhey123.skriptorm.utils.SerializationUtils
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import java.io.ByteArrayInputStream
import java.sql.JDBCType

/** JDBC-compatible logical types with PostgreSQL-supported binary and numeric storage types. */
object PgDataTypes : DataTypes() {

    override val typesRegistry: MutableMap<TypeId, DataType<*>> = mutableMapOf(
        TypeId.BOOLEAN to BooleanJdbcDataType(),
        TypeId.TINYINT to PgTinyIntJdbcDataType(),
        TypeId.INT to IntJdbcDataType(),
        TypeId.BIGINT to BigIntJdbcDataType(),
        TypeId.DOUBLE to PgDoubleJdbcDataType(),
        TypeId.FLOAT to PgFloatJdbcDataType(),
        TypeId.STRING to StringJdbcDataType(),
        TypeId.UUID to PgUuidJdbcDataType(),
        TypeId.ITEMSTACK to PgItemStackJdbcDataType(),
        TypeId.LOCATION to PgLocationJdbcDataType(),
        TypeId.BUKKIT_SERIALIZABLE to PgConfigurationSerializableJdbcDataType(),
        TypeId.NBT_COMPOUND to PgNbtJdbcDataType(),
        TypeId.DATE to SkriptDateJdbcDataType(),
        TypeId.TIME to SkriptTimeJdbcDataType(),
        TypeId.TIMESPAN to SkriptTimespanJdbcDataType()
    )
}

private class PgTinyIntJdbcDataType : TinyIntJdbcDataType() {

    override val storageName: String = "SMALLINT"
}

private class PgDoubleJdbcDataType : DoubleJdbcDataType() {

    override val storageName: String = "DOUBLE PRECISION"
}

private class PgFloatJdbcDataType : FloatJdbcDataType() {

    override val storageName: String = "REAL"
}

private class PgUuidJdbcDataType : UuidJdbcDataType() {

    override val storageName: String = "BYTEA"
    override val defaultSize: Int = -1
    override val supportsSize: Boolean = false
}

private class PgItemStackJdbcDataType : ItemStackDataType(), JdbcDataType<ItemStack> {

    override val jdbcType: JDBCType = JDBCType.VARBINARY
    override val storageName: String = "BYTEA"
    override val converter: ValueConverter<ItemStack, ByteArray> = PgItemStackConverter
}

private class PgLocationJdbcDataType : LocationJdbcDataType() {

    override val storageName: String = "BYTEA"
    override val defaultSize: Int = -1
    override val supportsSize: Boolean = false
}

private class PgConfigurationSerializableJdbcDataType :
    ConfigurationSerializableDataType(), JdbcDataType<ConfigurationSerializable> {

    override val jdbcType: JDBCType = JDBCType.VARBINARY
    override val storageName: String = "BYTEA"
    override val converter: ValueConverter<ConfigurationSerializable, ByteArray> = PgConfigurationSerializableConverter
}

private class PgNbtJdbcDataType : NbtDataType(), JdbcDataType<Any> {

    override val jdbcType: JDBCType = JDBCType.VARBINARY
    override val storageName: String = "BYTEA"
    override val converter: ValueConverter<Any, ByteArray> = PgNbtConverter
}

private object PgItemStackConverter : ValueConverter<ItemStack, ByteArray>(
    ItemStack::class.java,
    ByteArray::class.java
) {

    override fun toStorage(value: ItemStack): ByteArray = value.serializeAsBytes()

    override fun fromStorage(value: ByteArray): ItemStack = try {
        ItemStack.deserializeBytes(value)
    } catch (error: Throwable) {
        throw IllegalArgumentException("Failed to deserialize an ItemStack from PostgreSQL BYTEA data.", error)
    }
}

private object PgConfigurationSerializableConverter : ValueConverter<ConfigurationSerializable, ByteArray>(
    ConfigurationSerializable::class.java,
    ByteArray::class.java
) {

    override fun toStorage(value: ConfigurationSerializable): ByteArray =
        SerializationUtils.BukkitSerialization.serialize(value).use { it.toByteArray() }

    override fun fromStorage(value: ByteArray): ConfigurationSerializable =
        ByteArrayInputStream(value).use { input ->
            val deserialized = SerializationUtils.BukkitSerialization.deserialize(input)
            deserialized as? ConfigurationSerializable
                ?: throw IllegalArgumentException(
                    "Expected a Bukkit ConfigurationSerializable, but found ${deserialized::class.java.name}."
                )
        }
}

private object PgNbtConverter : ValueConverter<Any, ByteArray>(
    NbtSupport.domainType as Class<Any>,
    ByteArray::class.java
) {

    override fun toStorage(value: Any): ByteArray = NbtSupport.toBytes(value)

    override fun fromStorage(value: ByteArray): Any = try {
        NbtSupport.fromBytes(value)
    } catch (error: Throwable) {
        throw IllegalArgumentException("Failed to deserialize NBT from PostgreSQL BYTEA data.", error)
    }
}
