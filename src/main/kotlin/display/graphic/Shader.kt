package display.graphic

import org.lwjgl.opengl.GL20.*
import utility.Common

class Shader(type: Int) {

    val id: Int = glCreateShader(type)

    fun source(source: CharSequence) {
        glShaderSource(id, source)
    }

    fun compile() {
        glCompileShader(id)
        checkStatus()
    }

    private fun checkStatus() {
        val status: Int = glGetShaderi(id, GL_COMPILE_STATUS)
        if (status != GL_TRUE) {
            throw RuntimeException(glGetShaderInfoLog(id))
        }
    }

    fun delete() {
        glDeleteShader(id)
    }

    companion object {

        fun createShader(type: Int, source: CharSequence): Shader {
            val shader = Shader(type)
            shader.source(source)
            shader.compile()
            return shader
        }

        fun loadShader(type: Int, path: String): Shader {
            val source = Common.loadResource(path)
            return createShader(type, source)
        }
    }

}
