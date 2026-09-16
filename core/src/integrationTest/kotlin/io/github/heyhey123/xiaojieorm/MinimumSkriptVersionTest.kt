package io.github.heyhey123.xiaojieorm

import ch.njol.skript.util.Version
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pins the comparison [XiaojieOrm] relies on to reject an old Skript.
 *
 * The check is a single `isSmallerThan` call, and it fails in the worst possible direction: if
 * Skript's [Version] ordering ever stopped comparing the third component, an old Skript would pass
 * the floor and the plugin would fail later with a missing-method error, and if it compared in the
 * other direction the plugin would refuse to start on a supported server. Neither is visible from
 * this repository's own code, so the assumption is asserted here against the real class.
 */
class MinimumSkriptVersionTest {

    @Test
    fun `an older release than the floor is smaller`() {
        assertTrue(Version("2.16.1").isSmallerThan(XiaojieOrm.MINIMUM_SKRIPT_VERSION))
        assertTrue(Version("2.16").isSmallerThan(XiaojieOrm.MINIMUM_SKRIPT_VERSION))
        assertTrue(Version("2.13.2").isSmallerThan(XiaojieOrm.MINIMUM_SKRIPT_VERSION))
    }

    @Test
    fun `the floor itself and anything newer is not smaller`() {
        assertFalse(XiaojieOrm.MINIMUM_SKRIPT_VERSION.isSmallerThan(XiaojieOrm.MINIMUM_SKRIPT_VERSION))
        assertFalse(Version("2.16.2").isSmallerThan(XiaojieOrm.MINIMUM_SKRIPT_VERSION))
        assertFalse(Version("2.16.3").isSmallerThan(XiaojieOrm.MINIMUM_SKRIPT_VERSION))
        assertFalse(Version("2.17").isSmallerThan(XiaojieOrm.MINIMUM_SKRIPT_VERSION))
        assertFalse(Version("3.0").isSmallerThan(XiaojieOrm.MINIMUM_SKRIPT_VERSION))
    }

    @Test
    fun `the floor parses the way the log message claims`() {
        // The failure message prints MINIMUM_SKRIPT_VERSION, so a floor that parsed to something
        // other than the version we meant would mislead whoever reads the server log.
        assertTrue(XiaojieOrm.MINIMUM_SKRIPT_VERSION.toString().startsWith("2.16.2"))
    }
}
