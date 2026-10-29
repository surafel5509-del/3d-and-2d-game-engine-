package dev.gameengine.renderer

import android.content.Context
import android.opengl.GLES31
import dev.gameengine.core.FixedStepLoop

/** M1 vertical slice: a rotating, vertex-colored indexed triangle. GL callbacks are GL-thread-only. */
class TriangleRenderer(
    private val vertexShaderSource: String,
    private val fragmentShaderSource: String,
    private val diagnostics: Boolean,
) : GlScene {
    private val fixedStepLoop = FixedStepLoop()
    private var program: GlShaderProgram? = null
    private var mesh: GlMesh? = null
    private var angleUniform = -1
    private var aspectUniform = -1
    private var supported = false
    private var aspect = 1.0f
    private var angle = 0.0
    private var previousAngle = 0.0
    private var previousFrameNanos = 0L

    override fun onSurfaceCreated(): GlCapabilities {
        release()
        val capabilities = GlCapabilities.detect()
        supported = capabilities.supportsGles31
        previousFrameNanos = 0L
        fixedStepLoop.reset()
        angle = 0.0
        previousAngle = 0.0

        if (!supported) return capabilities

        program = GlShaderProgram.create(vertexShaderSource, fragmentShaderSource, diagnostics)
        mesh = GlMesh(VERTICES, INDICES, diagnostics)
        angleUniform = program!!.uniformLocation("uAngle")
        aspectUniform = program!!.uniformLocation("uAspect")

        GLES31.glDisable(GLES31.GL_DEPTH_TEST)
        GLES31.glDisable(GLES31.GL_CULL_FACE)
        GLES31.glClearColor(0.027f, 0.067f, 0.090f, 1.0f)
        if (diagnostics) GlError.check("initialize triangle renderer")
        return capabilities
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        aspect = if (height == 0) 1.0f else width.toFloat() / height.toFloat()
        if (supported) GLES31.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame() {
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT)
        if (!supported) return

        val now = System.nanoTime()
        val frameSeconds = if (previousFrameNanos == 0L) {
            0.0
        } else {
            (now - previousFrameNanos) / 1_000_000_000.0
        }
        previousFrameNanos = now
        previousAngle = angle
        fixedStepLoop.advance(frameSeconds) { stepSeconds ->
            angle += stepSeconds * RADIANS_PER_SECOND
        }
        val renderAngle = previousAngle + (angle - previousAngle) * fixedStepLoop.interpolation

        program!!.use()
        GLES31.glUniform1f(angleUniform, renderAngle.toFloat())
        GLES31.glUniform1f(aspectUniform, aspect)
        mesh!!.draw()
        if (diagnostics) GlError.check("draw triangle")
    }

    override fun release() {
        mesh?.close()
        program?.close()
        mesh = null
        program = null
        angleUniform = -1
        aspectUniform = -1
        supported = false
        previousFrameNanos = 0L
    }

    companion object {
        private const val RADIANS_PER_SECOND = 0.9
        private val VERTICES = floatArrayOf(
            // Position XYZ       Color RGB
            0.0f, 0.62f, 0.0f,   0.39f, 0.90f, 0.75f,
            -0.58f, -0.42f, 0.0f, 0.25f, 0.55f, 0.95f,
            0.58f, -0.42f, 0.0f,  0.98f, 0.55f, 0.30f,
        )
        private val INDICES = shortArrayOf(0, 1, 2)

        /** Loads built-in shader text on the caller's thread, before GL rendering begins. */
        fun fromAssets(context: Context, diagnostics: Boolean): TriangleRenderer {
            val vertex = context.assets.open("shaders/triangle.vert")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val fragment = context.assets.open("shaders/triangle.frag")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            return TriangleRenderer(vertex, fragment, diagnostics)
        }
    }
}
