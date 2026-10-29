package dev.gameengine.renderer

import android.opengl.GLES31

/** Immutable description of the current GL context. Query only while that context is current. */
data class GlCapabilities(
    val version: String,
    val shadingLanguageVersion: String,
    val vendor: String,
    val renderer: String,
) {
    private val versionParts: Pair<Int, Int>? = parseVersion(version)

    val supportsGles31: Boolean
        get() = versionParts?.let { (major, minor) ->
            major > 3 || (major == 3 && minor >= 1)
        } == true

    fun summary(): String = buildString {
        append(if (version.isBlank()) "Unknown OpenGL ES version" else version)
        if (renderer.isNotBlank()) {
            append("\nRenderer: ")
            append(renderer)
        }
        if (!supportsGles31) {
            append("\nOpenGL ES 3.1 or newer is required.")
        }
    }

    companion object {
        /** Must be called on the GL thread with a current context. */
        fun detect(): GlCapabilities = fromStrings(
            version = GLES31.glGetString(GLES31.GL_VERSION).orEmpty(),
            shadingLanguageVersion = GLES31.glGetString(GLES31.GL_SHADING_LANGUAGE_VERSION).orEmpty(),
            vendor = GLES31.glGetString(GLES31.GL_VENDOR).orEmpty(),
            renderer = GLES31.glGetString(GLES31.GL_RENDERER).orEmpty(),
        )

        /** String parsing is separate from GL queries so capability logic is host-testable. */
        fun fromStrings(
            version: String,
            shadingLanguageVersion: String = "",
            vendor: String = "",
            renderer: String = "",
        ): GlCapabilities = GlCapabilities(
            version = version,
            shadingLanguageVersion = shadingLanguageVersion,
            vendor = vendor,
            renderer = renderer,
        )

        private fun parseVersion(value: String): Pair<Int, Int>? {
            val match = Regex("""(\d+)\.(\d+)""").find(value) ?: return null
            val major = match.groupValues[1].toIntOrNull() ?: return null
            val minor = match.groupValues[2].toIntOrNull() ?: return null
            return major to minor
        }
    }
}
