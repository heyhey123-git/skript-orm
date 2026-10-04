package io.github.heyhey123.skriptorm.utils

import io.github.heyhey123.skriptorm.type.ConversionWork
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

/** A single conversion budget shared by all pending database requests. */
object MainThreadConversions : ConversionWork.Executor {
    const val BUDGET_NANOS = 2_000_000L
    private val queue = BudgetedWorkQueue()
    private val lifecycle = Any()
    private var task: BukkitTask? = null

    fun start(plugin: JavaPlugin) {
        task = plugin.server.scheduler.runTaskTimer(plugin, Runnable { queue.drain(BUDGET_NANOS) }, 1L, 1L)
        ConversionWork.install(this)
    }

    fun stop() {
        synchronized(lifecycle) {
            task?.cancel()
            task = null
            queue.close(CancellationException("Plugin disabled"))
        }
    }

    override suspend fun <T> executeBatch(actions: List<() -> T>): List<T> {
        if (actions.isEmpty()) return emptyList()
        val trace = BenchmarkTimings.trace()
        return suspendCancellableCoroutine { continuation ->
            val work = Batch(actions, continuation, trace)
            synchronized(lifecycle) {
                if (task == null) {
                    continuation.resumeWith(Result.failure(CancellationException("Plugin disabled")))
                } else {
                    queue.add(work)
                }
            }
            continuation.invokeOnCancellation { queue.remove(work) }
        }
    }

    private class Batch<T>(
        private val actions: List<() -> T>,
        private val continuation: CancellableContinuation<List<T>>,
        private val trace: BenchmarkTimings.Trace?
    ) : BudgetedWorkQueue.Work {
        private val results = ArrayList<T>(actions.size)
        private var index = 0
        private var waitedSince = if (trace != null) System.nanoTime() else 0L

        override fun step(): Boolean {
            if (!continuation.isActive) return true
            if (trace != null) trace.queueWaitNs += System.nanoTime() - waitedSince
            results.add(BenchmarkTimings.main(trace, BenchmarkTimings.Stage.CONVERSION, actions[index++]))
            if (index == actions.size) {
                continuation.resumeWith(Result.success(results))
                return true
            }
            if (trace != null) waitedSince = System.nanoTime()
            return false
        }

        override fun fail(error: Throwable) {
            if (continuation.isActive) continuation.resumeWith(Result.failure(error))
        }
    }
}
