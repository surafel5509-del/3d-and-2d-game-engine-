package dev.gameengine.renderer

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlCapabilitiesTest {
    @Test
    fun supportsGles31_accepts31AndNewer() {
        assertTrue(GlCapabilities.fromStrings("OpenGL ES 3.1 v1.r29p0").supportsGles31)
        assertTrue(GlCapabilities.fromStrings("OpenGL ES 3.2 Mesa").supportsGles31)
        assertTrue(GlCapabilities.fromStrings("OpenGL ES 4.0").supportsGles31)
    }

    @Test
    fun supportsGles31_rejectsOlderOrMalformedVersions() {
        assertFalse(GlCapabilities.fromStrings("OpenGL ES 3.0").supportsGles31)
        assertFalse(GlCapabilities.fromStrings("OpenGL ES 2.0").supportsGles31)
        assertFalse(GlCapabilities.fromStrings("").supportsGles31)
        assertFalse(GlCapabilities.fromStrings("unavailable").supportsGles31)
    }

    @Test
    fun summary_includesRendererAndRequirementWhenUnsupported() {
        val summary = GlCapabilities.fromStrings(
            version = "OpenGL ES 3.0",
            renderer = "Test GPU",
        ).summary()

        assertTrue(summary.contains("Test GPU"))
        assertTrue(summary.contains("3.1 or newer"))
    }
}
