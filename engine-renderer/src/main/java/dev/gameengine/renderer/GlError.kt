package dev.gameengine.renderer

import android.opengl.GLES31

internal object GlError {
    fun check(operation: String) {
        val error = GLES31.glGetError()
        if (error != GLES31.GL_NO_ERROR) {
            throw GlException("$operation failed with GL error 0x${error.toString(16)}")
        }
    }
}
