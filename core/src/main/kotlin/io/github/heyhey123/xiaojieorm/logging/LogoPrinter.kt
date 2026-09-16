package io.github.heyhey123.xiaojieorm.logging

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import kotlin.math.roundToInt

/**
 * Prints the banner the plugin greets a server with, and the line it says goodbye with.
 *
 * Adventure goes straight to the console sender: Paper colours it when the terminal can show colour,
 * and writes plain text into `logs/latest.log`. Serialising ANSI here by hand, as some plugins do,
 * would put escape sequences into that file and bury the one thing a banner is for.
 */
internal object LogoPrinter {

    /** Where the gradient runs, from the left of the wordmark to its right. */
    private val gradient = intArrayOf(0x2E9BE6, 0x7C4DFF, 0xC242F5)

    /**
     * The wordmark, in the figlet face this author's plugins share.
     *
     * The raw string opens with a newline and the source indents every line, so both are taken off: the
     * empty check drops the former, and `trimIndent` the latter. Every artwork line has content, so
     * nothing else is lost, and the art keeps the spacing it was drawn with.
     */
    private val wordmark: List<String> = """
         __   ___             _ _   
         \ \ / (_)           (_|_)  
          \ V / _  __ _  ___  _ _   
           > < | |/ _` |/ _ \| | |/ 
          / . \| | (_| | (_) | | |  
         /_/ \_\_|\__,_|\___/| |_|\ 
                            _/ |    
                           |__/     
    """.trimIndent().lines().filter(String::isNotEmpty)

    /** Prints the banner, with [version] centred on the line under the wordmark. */
    fun print(version: String) {
        val console = Bukkit.getConsoleSender()
        // Padding here rather than in the art keeps one gradient running across the whole wordmark
        // instead of restarting on every line, and the border is drawn to match what comes out.
        val width = wordmark.maxOf(String::length)
        val borderWidth = width + 4
        val border = Component.text("-".repeat(borderWidth), NamedTextColor.DARK_GRAY)
        val credit = "xiaojie-orm $version"
        val margin = " ".repeat(((borderWidth - credit.length) / 2).coerceAtLeast(0))

        console.sendMessage(border)
        wordmark.forEach { line -> console.sendMessage(paint(line.padEnd(width))) }
        console.sendMessage(Component.text(margin + "xiaojie-orm ", NamedTextColor.GRAY).append(paint(version)))
        console.sendMessage(border)
    }

    /** Prints the line a server sees when the plugin is disabled. */
    fun printFarewell(version: String) {
        Bukkit.getConsoleSender().sendMessage(
            Component.text("xiaojie-orm $version ", NamedTextColor.GRAY)
                .append(Component.text("disabled.", NamedTextColor.DARK_GRAY))
        )
    }

    /**
     * Paints [text] across the gradient, one character at a time.
     *
     * The art is full of angle brackets and backslashes, so it is coloured here rather than handed to
     * MiniMessage, where those characters would be read as tags.
     */
    private fun paint(text: String): Component {
        val builder = Component.text()
        val last = (text.length - 1).coerceAtLeast(1)
        text.forEachIndexed { index, character ->
            builder.append(Component.text(character).color(colourAt(index.toDouble() / last)))
        }
        return builder.build()
    }

    /** The colour [progress] of the way along the gradient, interpolated the way Adventure does. */
    private fun colourAt(progress: Double): TextColor {
        val scaled = progress.coerceIn(0.0, 1.0) * (gradient.size - 1)
        val stop = scaled.toInt().coerceAtMost(gradient.size - 2)
        val part = scaled - stop
        val from = gradient[stop]
        val to = gradient[stop + 1]
        return TextColor.color(
            blend(from shr 16 and 0xFF, to shr 16 and 0xFF, part),
            blend(from shr 8 and 0xFF, to shr 8 and 0xFF, part),
            blend(from and 0xFF, to and 0xFF, part)
        )
    }

    private fun blend(from: Int, to: Int, part: Double): Int = (from + (to - from) * part).roundToInt()
}
