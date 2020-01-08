package display

import utils.Utils
import display.graph.ShaderProgram
import engine.GameState
import engine.Planet
import engine.Vehicle
import engine.utilities.Utilities
import org.apache.commons.math3.geometry.euclidean.threed.Plane
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
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
        glDrawArrays(GL_TRIANGLES, 0, 3)
        // Restore state
        GL20.glDisableVertexAttribArray(0)
        GL30.glBindVertexArray(0)
        shaderProgram!!.unbind()
    }

    fun render(window: Window, gameState: GameState) {
        clear()

        if (window.isResized) {
            glViewport(0, 0, window.width, window.height)
            window.isResized = false
        }

        processNewState(gameState)
    }

    private fun processNewState(gameState: GameState) {
        gameState.planets
            .union(gameState.vehicles)
            .forEach {
                val location = it.motion.location
                val multiplier = 0.002f
                val x = location.x.toFloat() * multiplier
                val y = location.y.toFloat() * multiplier

                val tri = when (it) {
                    is Vehicle -> floatArrayOf(
                        0.0f, 0.05f, 0.0f,
                        -0.05f, -0.05f, 0.0f,
                        0.05f, -0.05f, 0.0f
                    )
                    is Planet -> floatArrayOf(
                        0.0f, 0.1f, 0.0f,
                        -0.1f, -0.1f, 0.0f,
                        0.1f, -0.1f, 0.0f
                    )
                    else -> floatArrayOf(
                        0.0f, 0.1f, 0.0f,
                        -0.1f, -0.1f, 0.0f,
                        0.1f, -0.1f, 0.0f
                    )
                }


                renderStart()
                val vertices = floatArrayOf(
                    tri[0] + x, tri[1] + y, tri[2],
                    tri[3] + x, tri[4] + y, tri[5],
                    tri[6] + x, tri[7] + y, tri[8]
                )
                renderEnd(vertices)

                it.motion.trailers.stream()
                    .forEach{trailer ->
                    val tx = trailer.location.x.toFloat() * multiplier
                    val ty = trailer.location.y.toFloat() * multiplier

                    renderStart()
                    val vertices = floatArrayOf(
                        tri[0]*0.2f + tx, tri[1]*0.2f + ty, tri[2],
                        tri[3]*0.2f + tx, tri[4]*0.2f + ty, tri[5],
                        tri[6]*0.2f + tx, tri[7]*0.2f + ty, tri[8]
                    )
                    renderEnd(vertices)
                }
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
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    }
}
