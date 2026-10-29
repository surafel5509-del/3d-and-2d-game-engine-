package dev.gameengine.renderer

/**
 * A renderable scene whose methods are invoked only on the GL thread.
 * Implementations must recreate GPU objects in [onSurfaceCreated] after context loss.
 */
interface GlScene {
    fun onSurfaceCreated(): GlCapabilities
    fun onSurfaceChanged(width: Int, height: Int)
    fun onDrawFrame()
    fun release()
}
