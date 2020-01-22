package display.graph

import org.lwjgl.opengl.GL20

class ShaderProgram {

    private val _programId: Int = GL20.glCreateProgram()
    val programId
        get() = _programId
    private var vertexShaderId = 0
    private var fragmentShaderId = 0

    init {
        if (_programId == 0) {
            throw Exception("Could not create Shader")
        }
    }

    @Throws(Exception::class)
    fun createVertexShader(shaderCode: String) {
        vertexShaderId = createShader(shaderCode, GL20.GL_VERTEX_SHADER)
    }

    @Throws(Exception::class)
    fun createFragmentShader(shaderCode: String) {
        fragmentShaderId = createShader(shaderCode, GL20.GL_FRAGMENT_SHADER)
    }

    @Throws(Exception::class)
    private fun createShader(shaderCode: String, shaderType: Int): Int {
        val shaderId = GL20.glCreateShader(shaderType)
        if (shaderId == 0) {
            throw Exception("Error creating shader. Type: $shaderType")
        }
        GL20.glShaderSource(shaderId, shaderCode)
        GL20.glCompileShader(shaderId)
        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            throw Exception("Error compiling Shader code: " + GL20.glGetShaderInfoLog(shaderId, 1024))
        }
        GL20.glAttachShader(_programId, shaderId)
        return shaderId
    }

    @Throws(Exception::class)
    fun link() {
        GL20.glLinkProgram(_programId)
        if (GL20.glGetProgrami(_programId, GL20.GL_LINK_STATUS) == 0) {
            throw Exception("Error linking Shader code: " + GL20.glGetProgramInfoLog(_programId, 1024))
        }
        if (vertexShaderId != 0) {
            GL20.glDetachShader(_programId, vertexShaderId)
        }
        if (fragmentShaderId != 0) {
            GL20.glDetachShader(_programId, fragmentShaderId)
        }
        GL20.glValidateProgram(_programId)
        if (GL20.glGetProgrami(_programId, GL20.GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + GL20.glGetProgramInfoLog(_programId, 1024))
        }
    }

    fun bind() {
        GL20.glUseProgram(_programId)
    }

    fun unbind() {
        GL20.glUseProgram(0)
    }

    fun cleanup() {
        unbind()
        if (_programId != 0) {
            GL20.glDeleteProgram(_programId)
        }
    }

}
