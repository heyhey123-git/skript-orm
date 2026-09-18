package io.github.heyhey123.skriptorm.impl.pg.type

import io.github.heyhey123.skriptorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.skriptorm.type.TypeId
import java.sql.Date
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins what PostgreSQL is asked to store each type as, and the class each value is read back as.
 *
 * The second half is not cosmetic. Values are read with the typed `getObject(column, class)`, and the
 * driver supports a fixed set of classes for each column type: asked for one it does not support, it
 * throws — for a column that is SQL NULL just the same — so a single wrong pair makes every row of the
 * table unreadable rather than one column of it. `tinyint` was that pair: MySQL's one-byte column maps
 * to a `Byte`, and pgjdbc answers an `int2` with "conversion to class java.lang.Byte from int2 not
 * supported". It is a `SMALLINT` read as a `Short` here for that reason.
 */
class PgDataTypesTest {

    @Test
    fun `every type declares the storage it is written as and read back as`() {
        val expected = linkedMapOf(
            TypeId.BOOLEAN to Storage("BOOLEAN", Boolean::class.javaObjectType),
            // The one type PostgreSQL has no exact column for. See the class comment.
            TypeId.TINYINT to Storage("SMALLINT", Short::class.javaObjectType),
            TypeId.INT to Storage("INT", Int::class.javaObjectType),
            TypeId.BIGINT to Storage("BIGINT", Long::class.javaObjectType),
            TypeId.DOUBLE to Storage("DOUBLE PRECISION", Double::class.javaObjectType),
            TypeId.FLOAT to Storage("REAL", Float::class.javaObjectType),
            TypeId.STRING to Storage("VARCHAR", String::class.java),
            // A UUID is stored as the 16 bytes of its two halves, which is what BYTEA holds.
            TypeId.UUID to Storage("BYTEA", ByteArray::class.java),
            TypeId.ITEMSTACK to Storage("BYTEA", ByteArray::class.java),
            TypeId.LOCATION to Storage("BYTEA", ByteArray::class.java),
            TypeId.BUKKIT_SERIALIZABLE to Storage("BYTEA", ByteArray::class.java),
            TypeId.NBT_COMPOUND to Storage("BYTEA", ByteArray::class.java),
            TypeId.DATE to Storage("DATE", Date::class.java),
            TypeId.TIME to Storage("INT", Int::class.javaObjectType),
            TypeId.TIMESPAN to Storage("BIGINT", Long::class.javaObjectType)
        )

        val actual = expected.keys.associateWith { id ->
            val type = requireNotNull(PgDataTypes.typesRegistry[id]) {
                "The PostgreSQL implementation declares no type for $id."
            }
            val jdbc = type as? JdbcDataType<*> ?: error("$id is not a JDBC type: ${type::class.java.name}")
            Storage(jdbc.storageName, boxed(type.converter.storageType))
        }

        assertEquals(expected, actual)
    }

    /** The class a value is read as; the cursor boxes the same primitives before asking for one. */
    private fun boxed(type: Class<*>): Class<*> = when (type) {
        Boolean::class.java -> Boolean::class.javaObjectType
        Byte::class.java -> Byte::class.javaObjectType
        Short::class.java -> Short::class.javaObjectType
        Int::class.java -> Int::class.javaObjectType
        Long::class.java -> Long::class.javaObjectType
        Float::class.java -> Float::class.javaObjectType
        Double::class.java -> Double::class.javaObjectType
        else -> type
    }

    private data class Storage(val column: String, val readAs: Class<*>)
}
