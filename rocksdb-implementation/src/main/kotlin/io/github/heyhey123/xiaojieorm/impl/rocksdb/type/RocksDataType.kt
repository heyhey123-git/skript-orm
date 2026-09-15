package io.github.heyhey123.xiaojieorm.impl.rocksdb.type

import io.github.heyhey123.xiaojieorm.type.DataType
import io.github.heyhey123.xiaojieorm.type.ValueConverter

interface RocksDataType<T : Any> : DataType<T> {

    override val converter: ValueConverter<T, ByteArray>
}
