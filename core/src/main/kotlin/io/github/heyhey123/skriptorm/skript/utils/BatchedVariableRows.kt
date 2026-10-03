package io.github.heyhey123.skriptorm.skript.utils

import java.util.ConcurrentModificationException

/**
 * Reads one Skript variable tree in bounded main-thread steps. The caller must not change the source
 * until reading finishes; structural changes are detected, but replacing an existing value is not.
 */
internal class BatchedVariableRows(
    private val source: Map<*, *>,
    private val description: String,
    private val convert: (String, Any?) -> Any?
) {
    private enum class Phase { SCAN, FILL, DONE, FAILED }

    private val sourceSize = source.size
    private val entries = source.entries.iterator()
    private val columns = linkedSetOf<String>()
    private val converted = ArrayList<Map<String, Any?>>()
    private val flatRow = linkedMapOf<String, Any?>()

    private var phase = Phase.SCAN
    private var failure: Throwable? = null
    private var nested: Boolean? = null
    private var currentKey: String? = null
    private var currentSource: Map<*, *>? = null
    private var currentSize = 0
    private var currentEntries: Iterator<Map.Entry<*, *>>? = null
    private var currentValues: LinkedHashMap<String, Any?>? = null
    private var fillIndex = 0
    private var fillColumns: Iterator<String>? = null
    private var filledRow: LinkedHashMap<String, Any?>? = null

    /** Available only after [advance] returns true. */
    val rows: List<Map<String, Any?>>
        get() {
            check(phase == Phase.DONE) { "$description has not finished reading." }
            return converted
        }

    /** Returns true when every row has been read and filled to the batch's column set. */
    fun advance(maxEntries: Int = 4096, timeBudgetNanos: Long = 2_000_000): Boolean {
        require(maxEntries > 0) { "maxEntries must be positive." }
        require(timeBudgetNanos > 0) { "timeBudgetNanos must be positive." }
        if (phase == Phase.DONE) return true
        failure?.let { throw it }

        val started = System.nanoTime()
        var steps = 0
        try {
            checkSource()
            while (phase != Phase.DONE && steps < maxEntries) {
                if (phase == Phase.SCAN) scanStep() else fillStep()
                steps++
                if (steps % 64 == 0) {
                    checkSource()
                    if (System.nanoTime() - started >= timeBudgetNanos) break
                }
            }
        } catch (error: Exception) {
            val reported = if (error is ConcurrentModificationException) {
                IllegalArgumentException("$description changed while its rows were being read.", error)
            } else {
                error
            }
            phase = Phase.FAILED
            failure = reported
            throw reported
        }
        return phase == Phase.DONE
    }

    private fun checkSource() {
        require(source.size == sourceSize) { "$description changed while its rows were being read." }
        val row = currentSource ?: return
        require(row.size == currentSize && source[currentKey] === row) {
            "$description changed while its rows were being read."
        }
    }

    private fun scanStep() {
        val rowEntries = currentEntries
        if (rowEntries != null) {
            if (rowEntries.hasNext()) {
                val (key, value) = rowEntries.next()
                val column = requireKey(key)
                columns += column
                currentValues!![column] = convert(column, value)
            } else {
                converted += currentValues!!
                currentKey = null
                currentSource = null
                currentEntries = null
                currentValues = null
            }
            return
        }

        if (!entries.hasNext()) {
            require(nested != null) { "$description does not hold any values." }
            if (nested == true) {
                phase = Phase.FILL
            } else {
                converted += flatRow
                phase = Phase.DONE
            }
            return
        }

        val (key, value) = entries.next()
        val name = requireKey(key)
        val isRow = value is Map<*, *>
        if (nested == null) nested = isRow
        require(nested == isRow) { "$description mixes rows with plain column values." }
        if (isRow) {
            val row = value
            currentKey = name
            currentSource = row
            currentSize = row.size
            currentEntries = row.entries.iterator()
            currentValues = linkedMapOf()
        } else {
            flatRow[name] = convert(name, value)
        }
    }

    private fun fillStep() {
        if (fillIndex == converted.size) {
            phase = Phase.DONE
            return
        }
        val target = filledRow ?: linkedMapOf<String, Any?>().also {
            filledRow = it
            fillColumns = columns.iterator()
        }
        val nextColumn = fillColumns!!
        if (nextColumn.hasNext()) {
            val column = nextColumn.next()
            target[column] = converted[fillIndex][column]
        } else {
            converted[fillIndex] = target
            fillIndex++
            filledRow = null
            fillColumns = null
        }
    }

    private fun requireKey(key: Any?): String {
        require(key != null) { "$description holds a plain value alongside its keys. Only keys are allowed." }
        require(key is String) { "$description has a non-text row or column key." }
        return key
    }
}
