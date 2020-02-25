package display.graphic

import Matrix2f
import Matrix3f
import Matrix4f
import Vector2f
import Vector3f
import Vector4f
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glBindFragDataLocation
import org.lwjgl.system.MemoryStack

class ShaderProgram {

    private val id: Int = glCreateProgram()

    fun attachShader(shader: Shader) {
        glAttachShader(id, shader.id)
    }

    fun bindFragmentDataLocation(number: Int, name: CharSequence) {
        glBindFragDataLocation(id, number, name)
    }

    fun link() {
        glLinkProgram(id)
        checkStatus()
    }

    fun getAttributeLocation(name: CharSequence): Int {
        return glGetAttribLocation(id, name)
    }

    fun enableVertexAttribute(location: Int) {
        glEnableVertexAttribArray(location)
    }

    fun disableVertexAttribute(location: Int) {
        glDisableVertexAttribArray(location)
    }

    fun pointVertexAttribute(location: Int, size: Int, stride: Int, offset: Int) {
        glVertexAttribPointer(location, size, GL_FLOAT, false, stride, offset.toLong())
    }

    fun getUniformLocation(name: CharSequence): Int {
        return glGetUniformLocation(id, name)
    }

    fun setUniform(location: Int, value: Int) {
        glUniform1i(location, value)
    }

    fun setUniform(location: Int, value: Vector2f) {
        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(2)
            value.toBuffer(buffer)
            glUniform2fv(location, buffer)
        }
    }

    fun setUniform(location: Int, value: Vector3f) {
        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(3)
            value.toBuffer(buffer)
            glUniform3fv(location, buffer)
        }
    }

    fun setUniform(location: Int, value: Vector4f) {
        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(4)
            value.toBuffer(buffer)
            glUniform4fv(location, buffer)
        }
    }

    fun setUniform(location: Int, value: Matrix2f) {
        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(2 * 2)
            value.toBuffer(buffer)
            glUniformMatrix2fv(location, false, buffer)
        }
    }

    fun setUniform(location: Int, value: Matrix3f) {
        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(3 * 3)
            value.toBuffer(buffer)
            glUniformMatrix3fv(location, false, buffer)
        }
    }

    fun setUniform(location: Int, value: Matrix4f) {
        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(4 * 4)
            value.toBuffer(buffer)
            glUniformMatrix4fv(location, false, buffer)
        }
    }

    fun use() {
        glUseProgram(id)
    }

    fun checkStatus() {
        val status: Int = glGetProgrami(id, GL_LINK_STATUS)
        if (status != GL_TRUE) {
            throw RuntimeException(glGetProgramInfoLog(id))
        }
    }

    fun delete() {
        glDeleteProgram(id)
    }

}
