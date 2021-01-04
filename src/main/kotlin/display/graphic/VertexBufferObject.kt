package display.graphic

import org.lwjgl.opengl.GL15.*
import java.nio.FloatBuffer
import java.nio.IntBuffer

class VertexBufferObject {

    val id: Int = glGenBuffers()

    fun bind(target: Int): VertexBufferObject {
        glBindBuffer(target, id)
        return this
    }

    fun uploadData(target: Int, data: FloatBuffer, usage: Int): VertexBufferObject {
        glBufferData(target, data, usage)
        return this
    }

    fun uploadData(target: Int, size: Long, usage: Int): VertexBufferObject {
        glBufferData(target, size, usage)
        return this
    }

    fun uploadSubData(target: Int, offset: Long, data: FloatBuffer): VertexBufferObject {
        glBufferSubData(target, offset, data)
        return this
    }

    fun uploadData(target: Int, data: IntBuffer, usage: Int): VertexBufferObject {
        glBufferData(target, data, usage)
        return this
    }

    fun delete() {
        glDeleteBuffers(id)
    }

}
