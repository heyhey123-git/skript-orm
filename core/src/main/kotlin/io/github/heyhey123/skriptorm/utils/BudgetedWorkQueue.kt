package io.github.heyhey123.skriptorm.utils

import java.util.concurrent.ConcurrentLinkedQueue

/** Runs one step per request in turn, stopping when the shared time budget has been spent. */
internal class BudgetedWorkQueue(private val clock: () -> Long = System::nanoTime) {
    interface Work {
        /** Returns true once the request is finished. */
        fun step(): Boolean
        fun fail(error: Throwable)
    }

    private val requests = ConcurrentLinkedQueue<Work>()

    fun add(work: Work) {
        requests.add(work)
    }

    fun remove(work: Work) {
        requests.remove(work)
    }

    fun drain(budgetNanos: Long) {
        require(budgetNanos > 0)
        val started = clock()
        do {
            val work = requests.poll() ?: return
            try {
                if (!work.step()) requests.add(work)
            } catch (error: Throwable) {
                work.fail(error)
            }
        } while (clock() - started < budgetNanos)
    }

    fun close(error: Throwable) {
        while (true) (requests.poll() ?: return).fail(error)
    }
}
