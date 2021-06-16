package display.graphic

import org.joml.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glBindFragDataLocation
import org.lwjgl.system.MemoryStack
import utility.math.toBuffer

class ShaderProgram {

    private val id: Int = glCreateProgram()

    fun attachShader(shader: Shader): ShaderProgram {
        glAttachShader(id, shader.id)
        return this
    }

    fun bindFragmentDataLocation(number: Int, name: CharSequence): ShaderProgram {
        glBindFragDataLocation(id, number, name)
        return this
    }

    fun link(): ShaderProgram {
        glLinkProgram(id)
        checkStatus()
        return this
    }

    fun getAttributeLocation(name: CharSequence): Int {
        return glGetAttribLocation(id, name)
    }

    fun enableVertexAttribute(location: Int): ShaderProgram {
        glEnableVertexAttribArray(location)
        return this
    }

    fun disableVertexAttribute(location: Int): ShaderProgram {
        glDisableVertexAttribArray(location)
        return this
    }

    fun pointVertexAttribute(location: Int, size: Int, stride: Int, offset: Int): ShaderProgram {
        glVertexAttribPointer(location, size, GL_FLOAT, false, stride, offset.toLong())
        return this
    }

    fun getUniformLocation(name: CharSequence): Int {
        return glGetUniformLocation(id, name)
    }

    fun setUniform(name: CharSequence, value: Int): ShaderProgram {
        val location = getUniformLocation(name)
        glUniform1i(location, value)
        return this
    }

    fun setUniform(name: CharSequence, value: Vector2f): ShaderProgram {
        val location = getUniformLocation(name)

        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(2)
            value.toBuffer(buffer)
            glUniform2fv(location, buffer)
        }
        return this
    }

    fun setUniform(name: CharSequence, value: Vector3f): ShaderProgram {
        val location = getUniformLocation(name)

        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(3)
            value.toBuffer(buffer)
            glUniform3fv(location, buffer)
        }
        return this
    }

    fun setUniform(name: CharSequence, value: Vector4f): ShaderProgram {
        val location = getUniformLocation(name)

        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(4)
            value.toBuffer(buffer)
            glUniform4fv(location, buffer)
        }
        return this
    }

    fun setUniform(name: CharSequence, value: Matrix2f): ShaderProgram {
        val location = getUniformLocation(name)

        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(2 * 2)
            value.toBuffer(buffer)
            glUniformMatrix2fv(location, false, buffer)
        }
        return this
    }

    fun setUniform(name: CharSequence, value: Matrix3f): ShaderProgram {
        val location = getUniformLocation(name)

        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(3 * 3)
            value.toBuffer(buffer)
            glUniformMatrix3fv(location, false, buffer)
        }
        return this
    }

    fun setUniform(name: CharSequence, value: Matrix4f): ShaderProgram {
        val location = getUniformLocation(name)

        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(4 * 4)
            value.toBuffer(buffer)
            glUniformMatrix4fv(location, false, buffer)
        }
        return this
    }

    fun use(): ShaderProgram {
        glUseProgram(id)
        return this
    }

    fun checkStatus() {
        val status = glGetProgrami(id, GL_LINK_STATUS)
        if (status != GL_TRUE) {
            throw RuntimeException(glGetProgramInfoLog(id))
        }
    }

    fun delete() {
        glDeleteProgram(id)
    }

}
