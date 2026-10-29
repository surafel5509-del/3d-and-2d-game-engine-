package dev.gameengine.renderer

import android.opengl.GLES31

/** Owns one linked program. Creation, use, location queries, and [close] are GL-thread-only. */
class GlShaderProgram private constructor(
    private var programId: Int,
    private val diagnostics: Boolean,
) {
    fun use() {
        check(programId != 0) { "Shader program has been released" }
        GLES31.glUseProgram(programId)
    }

    fun uniformLocation(name: String): Int {
        check(programId != 0) { "Shader program has been released" }
        val location = GLES31.glGetUniformLocation(programId, name)
        if (location < 0) throw GlException("Uniform '$name' is missing or was optimized out")
        return location
    }

    fun close() {
        if (programId != 0) {
            GLES31.glDeleteProgram(programId)
            programId = 0
        }
    }

    companion object {
        fun create(vertexSource: String, fragmentSource: String, diagnostics: Boolean): GlShaderProgram {
            val vertexShader = compile(GLES31.GL_VERTEX_SHADER, vertexSource)
            var fragmentShader = 0
            var program = 0
            try {
                fragmentShader = compile(GLES31.GL_FRAGMENT_SHADER, fragmentSource)
                program = GLES31.glCreateProgram()
                if (program == 0) throw GlException("glCreateProgram returned 0")

                GLES31.glAttachShader(program, vertexShader)
                GLES31.glAttachShader(program, fragmentShader)
                GLES31.glLinkProgram(program)

                val status = IntArray(1)
                GLES31.glGetProgramiv(program, GLES31.GL_LINK_STATUS, status, 0)
                if (status[0] == 0) {
                    val log = GLES31.glGetProgramInfoLog(program).orEmpty()
                    throw GlException("Shader program link failed:\n$log")
                }
                if (diagnostics) GlError.check("link shader program")
                return GlShaderProgram(program, diagnostics).also { program = 0 }
            } finally {
                if (program != 0) GLES31.glDeleteProgram(program)
                GLES31.glDeleteShader(vertexShader)
                if (fragmentShader != 0) GLES31.glDeleteShader(fragmentShader)
            }
        }

        private fun compile(type: Int, source: String): Int {
            val shader = GLES31.glCreateShader(type)
            if (shader == 0) throw GlException("glCreateShader returned 0 for type $type")
            try {
                GLES31.glShaderSource(shader, source)
                GLES31.glCompileShader(shader)
                val status = IntArray(1)
                GLES31.glGetShaderiv(shader, GLES31.GL_COMPILE_STATUS, status, 0)
                if (status[0] == 0) {
                    val log = GLES31.glGetShaderInfoLog(shader).orEmpty()
                    val kind = if (type == GLES31.GL_VERTEX_SHADER) "vertex" else "fragment"
                    throw GlException("$kind shader compilation failed:\n$log")
                }
                return shader
            } catch (error: Throwable) {
                GLES31.glDeleteShader(shader)
                throw error
            }
        }
    }
}
