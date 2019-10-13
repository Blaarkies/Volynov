package display

import utils.Utils
import display.graph.ShaderProgram
import engine.GameState
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

class Renderer {

    private var vboId = 0
    private var vaoId = 0
    private var shaderProgram: ShaderProgram? = null

    @Throws(Exception::class)
    fun init() {
        renderStart()
        val vertices = floatArrayOf(
            0.0f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f
        )
        renderEnd(vertices)
    }

    private fun renderStart() {
        shaderProgram = ShaderProgram()
        shaderProgram!!.createVertexShader(Utils.loadResource("/vertex.vs"))
        shaderProgram!!.createFragmentShader(Utils.loadResource("/fragment.fs"))
        shaderProgram!!.link()
    }

    private fun renderEnd(vertices: FloatArray) {
        val verticesBuffer: FloatBuffer
        try {
            verticesBuffer = MemoryUtil.memAllocFloat(vertices.size)
            verticesBuffer.put(vertices).flip()
            // Create the VAO and bind to it
            vaoId = GL30.glGenVertexArrays()
            GL30.glBindVertexArray(vaoId)
            // Create the VBO and bind to it
            vboId = GL15.glGenBuffers()
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW)
            // Define structure of the data
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0)
            // Unbind the VBO
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
            // Unbind the VAO
            GL30.glBindVertexArray(0)
        } finally {
//            if (verticesBuffer != null) {
//                MemoryUtil.memFree(verticesBuffer)
//            }
        }

        shaderProgram!!.bind()
        // Bind to the VAO
        GL30.glBindVertexArray(vaoId)
        GL20.glEnableVertexAttribArray(0)
        // Draw the vertices
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3)
        // Restore state
        GL20.glDisableVertexAttribArray(0)
        GL30.glBindVertexArray(0)
        shaderProgram!!.unbind()
    }

    fun render(window: Window, gameState: GameState) {
        clear()

        if (window.isResized) {
            GL11.glViewport(0, 0, window.width, window.height)
            window.isResized = false
        }

        processNewState(gameState)

/*        shaderProgram!!.bind()
        // Bind to the VAO
        GL30.glBindVertexArray(vaoId)
        GL20.glEnableVertexAttribArray(0)
        // Draw the vertices
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3)
        // Restore state
        GL20.glDisableVertexAttribArray(0)
        GL30.glBindVertexArray(0)
        shaderProgram!!.unbind()*/
    }

    private fun processNewState(gameState: GameState) {
        gameState.planets.forEach {
            val location = it.motion.location
            val x = location.x.toFloat() / 1000
            val y = location.y.toFloat() / 1000

            renderStart()
            val vertices = floatArrayOf(
                0.0f + x, 0.1f + y, 0.0f,
                -0.1f + x, -0.1f + y, 0.0f,
                0.1f + x, -0.1f + y, 0.0f
            )
            renderEnd(vertices)
        }
    }

    fun cleanup() {
        if (shaderProgram != null) {
            shaderProgram!!.cleanup()
        }
        GL20.glDisableVertexAttribArray(0)
        // Delete the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL15.glDeleteBuffers(vboId)
        // Delete the VAO
        GL30.glBindVertexArray(0)
        GL30.glDeleteVertexArrays(vaoId)
    }

    private fun clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }
}
