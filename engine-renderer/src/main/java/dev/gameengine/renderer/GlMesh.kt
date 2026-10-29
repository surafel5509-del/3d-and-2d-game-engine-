package dev.gameengine.renderer

import android.opengl.GLES31
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/** Indexed interleaved position/color mesh used by the M1 renderer. GL-thread-only. */
class GlMesh(
    vertices: FloatArray,
    indices: ShortArray,
    private val diagnostics: Boolean,
) {
    private var vaoId = 0
    private var vertexBufferId = 0
    private var indexBufferId = 0
    private val indexCount = indices.size
    private val vertexData: FloatBuffer = ByteBuffer
        .allocateDirect(vertices.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(vertices)
            position(0)
        }
    private val indexData: ShortBuffer = ByteBuffer
        .allocateDirect(indices.size * Short.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
        .apply {
            put(indices)
            position(0)
        }

    init {
        require(vertices.size % COMPONENTS_PER_VERTEX == 0) {
            "Vertex data must contain position and color components"
        }
        require(indices.isNotEmpty()) { "Index data must not be empty" }

        val handles = IntArray(1)
        GLES31.glGenVertexArrays(1, handles, 0)
        vaoId = handles[0]
        GLES31.glBindVertexArray(vaoId)

        GLES31.glGenBuffers(1, handles, 0)
        vertexBufferId = handles[0]
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vertexBufferId)
        GLES31.glBufferData(
            GLES31.GL_ARRAY_BUFFER,
            vertices.size * Float.SIZE_BYTES,
            vertexData,
            GLES31.GL_STATIC_DRAW,
        )

        GLES31.glGenBuffers(1, handles, 0)
        indexBufferId = handles[0]
        GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, indexBufferId)
        GLES31.glBufferData(
            GLES31.GL_ELEMENT_ARRAY_BUFFER,
            indices.size * Short.SIZE_BYTES,
            indexData,
            GLES31.GL_STATIC_DRAW,
        )

        GLES31.glEnableVertexAttribArray(POSITION_LOCATION)
        GLES31.glVertexAttribPointer(
            POSITION_LOCATION,
            POSITION_COMPONENTS,
            GLES31.GL_FLOAT,
            false,
            STRIDE_BYTES,
            0,
        )
        GLES31.glEnableVertexAttribArray(COLOR_LOCATION)
        GLES31.glVertexAttribPointer(
            COLOR_LOCATION,
            COLOR_COMPONENTS,
            GLES31.GL_FLOAT,
            false,
            STRIDE_BYTES,
            POSITION_COMPONENTS * Float.SIZE_BYTES,
        )

        GLES31.glBindVertexArray(0)
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, 0)
        GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, 0)
        if (diagnostics) GlError.check("create indexed mesh")
    }

    fun draw() {
        check(vaoId != 0) { "Mesh has been released" }
        GLES31.glBindVertexArray(vaoId)
        GLES31.glDrawElements(GLES31.GL_TRIANGLES, indexCount, GLES31.GL_UNSIGNED_SHORT, 0)
        GLES31.glBindVertexArray(0)
    }

    fun close() {
        if (indexBufferId != 0) {
            GLES31.glDeleteBuffers(1, intArrayOf(indexBufferId), 0)
            indexBufferId = 0
        }
        if (vertexBufferId != 0) {
            GLES31.glDeleteBuffers(1, intArrayOf(vertexBufferId), 0)
            vertexBufferId = 0
        }
        if (vaoId != 0) {
            GLES31.glDeleteVertexArrays(1, intArrayOf(vaoId), 0)
            vaoId = 0
        }
    }

    private companion object {
        const val POSITION_LOCATION = 0
        const val COLOR_LOCATION = 1
        const val POSITION_COMPONENTS = 3
        const val COLOR_COMPONENTS = 3
        const val COMPONENTS_PER_VERTEX = POSITION_COMPONENTS + COLOR_COMPONENTS
        const val STRIDE_BYTES = COMPONENTS_PER_VERTEX * Float.SIZE_BYTES
    }
}
