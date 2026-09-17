package io.github.heyhey123.skriptorm.skript.utils

import ch.njol.skript.config.Config
import ch.njol.skript.config.Node
import ch.njol.skript.config.SectionNode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockbukkit.mockbukkit.MockBukkit

/**
 * Builds the node trees a Skript section receives at parse time, using Skript's real config parser.
 *
 * MockBukkit is started for every test, but not to host Skript. Skript logs through
 * `Bukkit.getConsoleSender()`, and the config parser reports its problems through it, so without a
 * server the parser NPEs instead of returning nodes. Loading Skript itself is impossible here:
 * MockBukkit loads a plugin as a generated subclass of its main class, and Skript's main class is
 * final.
 *
 * Bodies are parsed in *simple* mode, which is what makes a line such as `name: "Alice"` arrive as
 * one node whose key is the whole line. That is the shape [ValuesParser] and [WhereParser] read;
 * [SkriptConfigProbeTest] pins it.
 */
abstract class SkriptConfigTestBase {

    @BeforeEach
    fun startServer() {
        MockBukkit.mock()
    }

    @AfterEach
    fun stopServer() {
        MockBukkit.unmock()
    }

    /** The top-level nodes of [text]. */
    protected fun nodes(text: String): List<Node> = parse(text).getMainNode().toList()

    /** The first top-level node of [text]. */
    protected fun firstNode(text: String): Node = nodes(text).first()

    /** The first top-level section of [text]. */
    protected fun section(text: String): SectionNode = firstNode(text) as SectionNode

    /** The children of the first top-level section of [text], the way a section body arrives. */
    protected fun body(text: String): List<Node> = section(text).toList()

    private fun parse(text: String): Config =
        Config(text.byteInputStream(), "test.sk", true, false, "\n")
}
