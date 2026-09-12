package io.github.heyhey123.xiaojieorm.impl.rocksdb.storage

import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

object EncoderUtils {
    /**
     * A counter to ensure uniqueness within the same second.
     */
    private val counter = AtomicInteger(0)

    const val PRIMARY_KEY_MARKER: Byte = 0x01

    const val ROW_MARKER: Byte = 0x02

    const val KEEP_THREE_BYTES_MASK = 0xFFFFFF

    const val KEEP_TWO_BYTES_MASK = 0xFFFF // 2 bytes

    /**
     * Generate a unique row ID that combines the current timestamp, machine ID, process ID, and a counter.
     *
     * @return A 24 BIT unique row ID as a hexadecimal string like:
     *       "000000005f2d3c4a1b2c3d4e5f6a7b8c9d"
     */
    fun generateRowId(): String {
        val timestamp = System.currentTimeMillis() / 1000 // in seconds
        val machineId = machineId
        val processId = processId
        val counterValue = counter.incrementAndGet() and KEEP_THREE_BYTES_MASK

        return String.format(
            "%08x%06x%04x%06x",
            timestamp,
            machineId,
            processId,
            counterValue
        )
    }

    /**
     * The machine identifier based on the hostname's hash code.
     *
     * @return A 3-byte integer representing the machine ID.
     */
    private val machineId: Int by lazy {
        try {
            InetAddress.getLocalHost().hostName.hashCode() and KEEP_THREE_BYTES_MASK
        } catch (_: Exception) {
            Random.nextInt() and KEEP_THREE_BYTES_MASK
        }
    }


    /**
     * The current process ID.
     *
     * @return A 2-byte integer representing the process ID.
     */
    private val processId: Int by lazy {
        try {
            ManagementFactory.getRuntimeMXBean()
                .name.split("@")[0].toInt() and KEEP_TWO_BYTES_MASK
        } catch (_: Exception) {
            Random.nextInt() and KEEP_TWO_BYTES_MASK
        }
    }
}
