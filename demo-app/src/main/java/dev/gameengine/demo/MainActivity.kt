package dev.gameengine.demo

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import dev.gameengine.android.EngineSurfaceView
import dev.gameengine.renderer.GlCapabilities
import dev.gameengine.renderer.TriangleRenderer

@SuppressLint("SetTextI18n") // GPU diagnostics are intentionally shown verbatim in this demo.
class MainActivity : Activity() {
    private var engineView: EngineSurfaceView? = null
    private lateinit var statusView: TextView
    private lateinit var fallbackView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(7, 17, 23))
        }
        statusView = createLabel(
            text = "M1 · Initializing OpenGL ES 3.1…",
            textSizeSp = 14f,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            setTextColor(Color.rgb(190, 209, 218))
        }
        fallbackView = createLabel("", 18f).apply {
            visibility = View.GONE
            gravity = Gravity.CENTER
        }

        if (declaredGlesVersion() < REQUIRED_GLES_VERSION) {
            showFallback(
                "This device does not declare OpenGL ES 3.1 support.\n\n" +
                    "The demo requires GLES 3.1 or newer; no rendering was started.",
            )
        } else {
            try {
                val scene = TriangleRenderer.fromAssets(applicationContext, BuildConfig.DEBUG)
                engineView = EngineSurfaceView(this, scene, RendererCallback())
                content.addView(
                    engineView,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )
            } catch (error: Throwable) {
                Log.e(TAG, "Unable to initialize engine resources", error)
                showFallback("Engine initialization failed:\n${error.message ?: error.javaClass.simpleName}")
            }
        }

        content.addView(
            fallbackView,
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        content.addView(statusView)
        setContentView(content)
    }

    override fun onResume() {
        super.onResume()
        engineView?.onResume()
    }

    override fun onPause() {
        engineView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        engineView?.shutdown()
        engineView = null
        super.onDestroy()
    }

    private fun declaredGlesVersion(): Int {
        val activityManager = getSystemService(ActivityManager::class.java)
        return activityManager?.deviceConfigurationInfo?.reqGlEsVersion ?: 0
    }

    private fun showFallback(message: String) {
        fallbackView.text = message
        fallbackView.visibility = View.VISIBLE
        engineView?.visibility = View.GONE
        statusView.text = "M1 · Renderer unavailable"
        statusView.setTextColor(Color.rgb(255, 176, 148))
    }

    private fun createLabel(text: String, textSizeSp: Float): TextView = TextView(this).apply {
        this.text = text
        setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp)
        setTextColor(Color.WHITE)
        setPadding(dp(20), dp(20), dp(20), dp(20))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private inner class RendererCallback : EngineSurfaceView.Callback {
        override fun onReady(capabilities: GlCapabilities) {
            statusView.text = "M1 · ${capabilities.version} · fixed 60 Hz / interpolated render"
        }

        override fun onUnsupported(capabilities: GlCapabilities) {
            showFallback("Unsupported graphics device.\n\n${capabilities.summary()}")
        }

        override fun onRendererError(error: Throwable) {
            showFallback("Rendering stopped safely.\n\n${error.message ?: error.javaClass.simpleName}")
        }
    }

    private companion object {
        const val REQUIRED_GLES_VERSION = 0x00030001
        const val TAG = "MainActivity"
    }
}
