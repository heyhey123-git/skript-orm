package io.github.heyhey123.xiaojieorm.logging

import io.github.heyhey123.xiaojieorm.XiaojieOrm
import kotlin.math.roundToInt

/**
 * Prints the banner the plugin greets a server with, and the line it says goodbye with.
 *
 * The colours are written into the text rather than offered to Paper. Paper decides for itself whether a
 * console can show colour and records that decision in the `net.kyori.ansi.colorLevel` system property,
 * so on a platform it reads wrong the banner arrives grey while the terminal could have shown it in full
 * colour. The banner is the one thing here that exists to be looked at, so it carries its own colours and
 * asks nobody. Paper's file appender drops them again on the way into `logs/latest.log`, which stays
 * readable as plain text.
 */
internal object LogoPrinter {

    /**
     * Where the gradient runs, from the left of the wordmark to its right: deep water, the shallows, and
     * the sand at the end of it. The middle stop earns its place, because a straight blue-to-yellow
     * interpolation passes through grey on the way, and that is not a colour a beach has.
     */
    private val gradient = intArrayOf(0x2A7FD4, 0x4FD1C5, 0xF7E8A4)

    private const val ESCAPE = "\u001B["

    private const val RESET = "\u001B[0m"

    private const val DARK_GRAY = 0x555555

    private const val GRAY = 0xAAAAAA

    /**
     * The wordmark, in the figlet face this author's plugins share.
     *
     * The raw string opens with a newline and the source indents every line, so both are taken off: the
     * empty check drops the former, and `trimIndent` the latter. Every artwork line has content, so
     * nothing else is lost, and the art keeps the spacing it was drawn with.
     */
    private val wordmark: List<String> = """
        ____  ______             _____________     _______                   
        __  |/ /__(_)_____ ____________(_)__(_)______  __ \_____________ ___ 
        __    /__  /_  __ `/  __ \____  /__  /_  _ \  / / /_  ___/_  __ `__ \
        _    | _  / / /_/ // /_/ /___  / _  / /  __/ /_/ /_  /   _  / / / / /
        /_/|_| /_/  \__,_/ \____/___  /  /_/  \___/\____/ /_/    /_/ /_/ /_/ 
                                 /___/                                       
    """.trimIndent().lines().filter(String::isNotEmpty)

    /** Prints the banner, with [version] centred on the line under the wordmark. */
    fun print(version: String) {
        // Padding here rather than in the art keeps one gradient running across the whole wordmark
        // instead of restarting on every line, and the border is drawn to match what comes out.
        val width = wordmark.maxOf(String::length)
        val borderWidth = width + 4
        val border = "-".repeat(borderWidth)
        val credit = "xiaojie-orm $version"
        val margin = " ".repeat(((borderWidth - credit.length) / 2).coerceAtLeast(0))

        send(listOf(Span(border, DARK_GRAY)))
        wordmark.forEach { line -> send(gradientSpans(line.padEnd(width))) }
        send(listOf(Span(margin + "xiaojie-orm ", GRAY)) + gradientSpans(version))
        send(listOf(Span(border, DARK_GRAY)))
    }

    /** Prints the line a server sees when the plugin is disabled. */
    fun printFarewell(version: String) {
        send(listOf(Span("xiaojie-orm $version ", GRAY), Span("disabled.", DARK_GRAY)))
    }

    /** A run of text in one colour, so a line can be written as escape sequences without losing it. */
    private data class Span(val text: String, val colour: Int)

    /**
     * Sends one line to the console, colours and all.
     *
     * The logger is what gets the escape sequences past Paper's own rendering, and past its file
     * appender, which drops them again for the log.
     */
    private fun send(spans: List<Span>) {
        XiaojieOrm.instance.logger.info(ansi(spans))
    }

    /**
     * The spans as true-colour escape sequences.
     *
     * `38;2;r;g;b` picks a foreground colour, and the reset at the end keeps it out of everything that
     * follows the line. Doing this by hand is what makes the colours survive a console Paper has decided
     * cannot show them.
     */
    private fun ansi(spans: List<Span>): String = buildString {
        spans.forEach { span ->
            append(ESCAPE)
                .append("38;2;")
                .append(span.colour shr 16 and 0xFF).append(';')
                .append(span.colour shr 8 and 0xFF).append(';')
                .append(span.colour and 0xFF).append('m')
                .append(span.text)
        }
        append(RESET)
    }

    /** [text] painted one character at a time, so that the gradient is not lost on any of it. */
    private fun gradientSpans(text: String): List<Span> {
        val last = (text.length - 1).coerceAtLeast(1)
        return text.mapIndexed { index, character ->
            Span(character.toString(), colourAt(index.toDouble() / last))
        }
    }

    /** The colour [progress] of the way along the gradient, interpolated the way Adventure does. */
    private fun colourAt(progress: Double): Int {
        val scaled = progress.coerceIn(0.0, 1.0) * (gradient.size - 1)
        val stop = scaled.toInt().coerceAtMost(gradient.size - 2)
        val part = scaled - stop
        val from = gradient[stop]
        val to = gradient[stop + 1]
        val red = blend(from shr 16 and 0xFF, to shr 16 and 0xFF, part)
        val green = blend(from shr 8 and 0xFF, to shr 8 and 0xFF, part)
        val blue = blend(from and 0xFF, to and 0xFF, part)
        return red shl 16 or (green shl 8) or blue
    }

    private fun blend(from: Int, to: Int, part: Double): Int = (from + (to - from) * part).roundToInt()
}
