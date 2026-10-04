package io.github.heyhey123.skriptorm.utils

import kotlinx.coroutines.asContextElement
import org.bukkit.Bukkit
import java.util.concurrent.atomic.AtomicLong

/** Optional operation timings. Enabled only in benchmark servers with a JVM property. */
object BenchmarkTimings {
    val enabled: Boolean = java.lang.Boolean.getBoolean("skriptorm.benchmark.timings")
    private val ids = AtomicLong()
    private val current = ThreadLocal<Trace?>()

    @Volatile
    private var completed = Snapshot()

    data class Snapshot(
        val operationId: Long = 0,
        val mainNs: Long = 0,
        val mainTickMaxNs: Long = 0,
        val prepareMainNs: Long = 0,
        val prepareAsyncNs: Long = 0,
        val conversionMainNs: Long = 0,
        val resultMainNs: Long = 0,
        val resultAsyncNs: Long = 0,
        val executionNs: Long = 0,
        val queueWaitNs: Long = 0,
        val syncConversions: Long = 0,
        val largestConversionNs: Long = 0
    )

    enum class Stage { PREPARE, CONVERSION, RESULT }

    class Trace internal constructor(val id: Long) {
        private val mainTicks = HashMap<Int, Long>()
        private var main = 0L
        private var prepareMain = 0L
        private var prepareAsync = 0L
        private var conversionMain = 0L
        private var resultMain = 0L
        private var resultAsync = 0L
        var executionNs = 0L
        var queueWaitNs = 0L
        private var conversions = 0L
        private var largestConversion = 0L

        fun <T> measureMain(stage: Stage, action: () -> T): T {
            val start = System.nanoTime()
            try {
                return action()
            } finally {
                val elapsed = System.nanoTime() - start
                main += elapsed
                val tick = Bukkit.getCurrentTick()
                mainTicks[tick] = (mainTicks[tick] ?: 0) + elapsed
                when (stage) {
                    Stage.PREPARE -> prepareMain += elapsed
                    Stage.CONVERSION -> {
                        conversionMain += elapsed
                        conversions++
                        largestConversion = maxOf(largestConversion, elapsed)
                    }
                    Stage.RESULT -> resultMain += elapsed
                }
            }
        }

        fun <T> measureAsync(stage: Stage, action: () -> T): T {
            val start = System.nanoTime()
            try {
                return action()
            } finally {
                val elapsed = System.nanoTime() - start
                when (stage) {
                    Stage.PREPARE -> prepareAsync += elapsed
                    Stage.RESULT -> resultAsync += elapsed
                    Stage.CONVERSION -> Unit
                }
            }
        }

        fun finish() {
            completed = Snapshot(
                id, main, mainTicks.values.maxOrNull() ?: 0,
                prepareMain, prepareAsync, conversionMain, resultMain, resultAsync,
                executionNs, queueWaitNs, conversions, largestConversion
            )
        }
    }

    fun begin(): Trace? = if (enabled) Trace(ids.incrementAndGet()) else null
    fun trace(): Trace? = current.get()
    fun context(trace: Trace?) = current.asContextElement(trace)

    @JvmStatic
    fun latest(): Snapshot = completed

    fun <T> main(trace: Trace?, stage: Stage, action: () -> T): T =
        trace?.measureMain(stage, action) ?: action()

    fun <T> async(stage: Stage, action: () -> T): T =
        current.get()?.measureAsync(stage, action) ?: action()
}
