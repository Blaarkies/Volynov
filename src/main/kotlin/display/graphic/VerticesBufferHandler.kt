package display.graphic

import org.lwjgl.opengl.GL20
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

class VerticesBufferHandler {

    private var vao: VertexArrayObject
    private var vbo: VertexBufferObject
    private var vertices: FloatBuffer
    private var numVertices = 0
    private var isDrawing = false
    var vertexDimensionCount = 0

    init {
        vao = VertexArrayObject().bind()

        vertices = MemoryUtil.memAllocFloat(4096 * 2 * 64) // Upload null data to allocate storage for the VBO
        val size = vertices.capacity() * java.lang.Float.BYTES
        vbo = VertexBufferObject().bind(GL20.GL_ARRAY_BUFFER)
            .uploadData(GL20.GL_ARRAY_BUFFER, size.toLong(), GL20.GL_STATIC_DRAW)
    }

    fun begin() {
        check(!isDrawing) { "Renderer is already drawing!" }
        isDrawing = true
        numVertices = 0
    }

    fun end(drawType: Int = GL20.GL_TRIANGLES, program: ShaderProgram) {
        check(isDrawing) { "Renderer isn't drawing!" }
        isDrawing = false
        flush(drawType, program)
    }

    private fun flush(drawType: Int, program: ShaderProgram) {
        if (numVertices <= 0) return

        vertices.flip()
        vao.bind()

        program.use()

        vbo.uploadSubData(GL20.GL_ARRAY_BUFFER, 0, vertices)

        GL20.glDrawArrays(drawType, 0, numVertices)

        vertices.clear()
        numVertices = 0
    }

    fun dispose() {
        MemoryUtil.memFree(vertices)
        vao.delete()
        vbo.delete()
    }

    fun drawVertices(data: FloatArray, program: ShaderProgram, preDrawCallback: () -> Unit) {
        begin()

        vertices.put(data)
        numVertices += data.size / vertexDimensionCount

        preDrawCallback()

        end(GL20.GL_TRIANGLES, program)
    }

}
