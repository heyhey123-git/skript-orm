package io.github.heyhey123.skriptorm.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class BudgetedWorkQueueTest {
    @Test
    fun `requests share a budget and resume in turn on the next tick`() {
        var now = 0L
        val queue = BudgetedWorkQueue { now }
        val order = mutableListOf<String>()
        fun work(name: String) = object : BudgetedWorkQueue.Work {
            var remaining = 2
            override fun step(): Boolean {
                order += name
                now += 3
                return --remaining == 0
            }
            override fun fail(error: Throwable) = throw error
        }
        queue.add(work("a"))
        queue.add(work("b"))
        queue.drain(5)
        assertEquals(listOf("a", "b"), order)
        queue.drain(5)
        assertEquals(listOf("a", "b", "a", "b"), order)
        queue.drain(5)
        assertEquals(4, order.size)
    }

    @Test
    fun `a slow conversion finishes but later work waits for the next tick`() {
        var now = 0L
        val queue = BudgetedWorkQueue { now }
        var steps = 0
        val work = object : BudgetedWorkQueue.Work {
            override fun step(): Boolean {
                now += 20
                steps++
                return false
            }
            override fun fail(error: Throwable) = throw error
        }
        queue.add(work)
        queue.drain(5)
        assertEquals(1, steps)
        queue.remove(work)
        queue.drain(5)
        assertEquals(1, steps)
    }

    @Test
    fun `a failed request does not block others and close fails pending work`() {
        val queue = BudgetedWorkQueue()
        val problem = IllegalStateException("conversion failed")
        var failure: Throwable? = null
        var completed = false
        queue.add(object : BudgetedWorkQueue.Work {
            override fun step(): Boolean = throw problem
            override fun fail(error: Throwable) {
                failure = error
            }
        })
        queue.add(object : BudgetedWorkQueue.Work {
            override fun step(): Boolean {
                completed = true
                return true
            }
            override fun fail(error: Throwable) = throw error
        })
        queue.drain(Long.MAX_VALUE)
        assertSame(problem, failure)
        assertEquals(true, completed)
        queue.add(object : BudgetedWorkQueue.Work {
            override fun step(): Boolean = error("must not run")
            override fun fail(error: Throwable) {
                failure = error
            }
        })
        val stopped = IllegalStateException("stopped")
        queue.close(stopped)
        assertSame(stopped, failure)
    }
}
