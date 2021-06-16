package display.graphic

import org.lwjgl.opengl.GL20.*
import utility.Common

class Shader(type: Int, path: String) {

    val id: Int = glCreateShader(type)

    init {
        val source = Common.loadResource(path)
        glShaderSource(id, source)

        glCompileShader(id)
        checkStatus()
    }

    private fun checkStatus() {
        val status = glGetShaderi(id, GL_COMPILE_STATUS)
        if (status != GL_TRUE) {
            throw RuntimeException(glGetShaderInfoLog(id))
        }
    }

    fun delete() {
        glDeleteShader(id)
    }

}
