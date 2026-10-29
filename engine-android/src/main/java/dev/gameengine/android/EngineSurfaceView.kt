package dev.gameengine.android

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import dev.gameengine.renderer.GlCapabilities
import dev.gameengine.renderer.GlScene
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * GLES 3.x rendering host with runtime 3.1 verification and a UI-thread failure callback.
 * All [scene] methods, including cleanup, execute on the GL thread.
 */
@SuppressLint("ViewConstructor") // Runtime dependencies make XML inflation intentionally unsupported.
class EngineSurfaceView(
    context: Context,
    private val scene: GlScene,
    private val callback: Callback,
) : GLSurfaceView(context) {
    private val shutdownRequested = AtomicBoolean(false)

    init {
        setEGLContextClientVersion(GLES_MAJOR_VERSION)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setRenderer(SceneRendererAdapter())
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    /** Requests idempotent GL-thread cleanup. The owning Activity calls this from onDestroy. */
    fun shutdown() {
        if (shutdownRequested.compareAndSet(false, true)) {
            queueEvent { scene.release() }
        }
    }

    interface Callback {
        fun onReady(capabilities: GlCapabilities)
        fun onUnsupported(capabilities: GlCapabilities)
        fun onRendererError(error: Throwable)
    }

    private inner class SceneRendererAdapter : Renderer {
        private var failed = false

        override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
            failed = false
            try {
                val capabilities = scene.onSurfaceCreated()
                if (capabilities.supportsGles31) {
                    post { callback.onReady(capabilities) }
                } else {
                    failed = true
                    post { callback.onUnsupported(capabilities) }
                }
            } catch (error: Throwable) {
                reportFailure(error)
            }
        }

        override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
            if (failed) return
            try {
                scene.onSurfaceChanged(width, height)
            } catch (error: Throwable) {
                reportFailure(error)
            }
        }

        override fun onDrawFrame(unused: GL10?) {
            if (failed) return
            try {
                scene.onDrawFrame()
            } catch (error: Throwable) {
                reportFailure(error)
            }
        }

        private fun reportFailure(error: Throwable) {
            if (failed) return
            failed = true
            Log.e(TAG, "Renderer failed", error)
            try {
                scene.release()
            } catch (cleanupError: Throwable) {
                error.addSuppressed(cleanupError)
            }
            post { callback.onRendererError(error) }
        }
    }

    private companion object {
        const val GLES_MAJOR_VERSION = 3
        const val TAG = "EngineSurfaceView"
    }
}
