package display

import display.graph.ShaderProgram
import engine.FreeBody
import engine.GameState
import engine.Planet
import engine.Vehicle
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil
import utils.Utils
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.streams.toList

class Renderer {

    private var vboId = 0
    private var vaoId = 0
    private var shaderProgram: ShaderProgram? = null

    private val heptagon = (0 until 3)
        .flatMap {
            val t = 2.0 * PI * (it.toFloat() / 3.toFloat())
            listOf(cos(t).toFloat(), sin(t).toFloat(), 0f)
        }

    @Throws(Exception::class)
    fun init() {
        shaderProgram = ShaderProgram()
        shaderProgram!!.createVertexShader(Utils.loadResource("/vertex.vs"))
        shaderProgram!!.createFragmentShader(Utils.loadResource("/fragment.fs"))
        shaderProgram!!.link()
    }

    private fun renderEnd(vertices: FloatArray, type: Int = GL_TRIANGLE_FAN) {
        val verticesBuffer: FloatBuffer
        val vertexDimensions = 3
        // try {
        verticesBuffer = MemoryUtil.memAllocFloat(vertices.size)
        verticesBuffer.put(vertices).flip()
        vaoId = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vaoId)

        vboId = GL15.glGenBuffers()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW)

        GL20.glVertexAttribPointer(0, vertexDimensions, GL_FLOAT, false, 0, 0)
//        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0) // TODO: does this clear/empty the buffer?
//        GL30.glBindVertexArray(0) // TODO: does this clear/empty the buffer?
        // } finally { if (verticesBuffer != null) MemoryUtil.memFree(verticesBuffer) }

        shaderProgram!!.bind()
//        GL30.glBindVertexArray(vaoId) // TODO: duplicate?
        GL20.glEnableVertexAttribArray(0)

        glDrawArrays(type, 0, vertices.size / vertexDimensions)

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
        val screenMultiplier = 0.002f

        gameState.planets
            .union(gameState.vehicles)
            .forEach {
                drawFreeBody(it, screenMultiplier)
                drawTrail(it, screenMultiplier)
            }
    }

    private fun drawFreeBody(freeBody: FreeBody, screenMultiplier: Float) {
        val location = freeBody.motion.location
        val x = location.x.toFloat() * screenMultiplier
        val y = location.y.toFloat() * screenMultiplier

        val shapeVertices = when (freeBody) {
            is Vehicle -> heptagon.map { v -> v * freeBody.radius.toFloat() * screenMultiplier }
            is Planet -> heptagon.map { v -> v * freeBody.radius.toFloat() * screenMultiplier }
            else -> heptagon
        }

        val vertices = shapeVertices.mapIndexed { index, fl ->
            when (index.rem(3)) {
                0 -> fl + x
                1 -> fl + y
                else -> fl
            }
        }.toFloatArray()
        renderEnd(vertices)
    }

    private fun drawTrail(freeBody: FreeBody, screenMultiplier: Float) {
        val points = freeBody.motion.trailers.stream().toList()
            .flatMap { trailer ->
                listOf(
                    trailer.location.x.toFloat() * screenMultiplier,
                    trailer.location.y.toFloat() * screenMultiplier,
                    0f
                )
            }
            .toFloatArray()
        renderEnd(points, GL_LINE_STRIP)
    }

    fun cleanup() {
        if (shaderProgram != null) {
            shaderProgram!!.cleanup()
        }
        GL20.glDisableVertexAttribArray(0)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL15.glDeleteBuffers(vboId)
        GL30.glBindVertexArray(0)
        GL30.glDeleteVertexArrays(vaoId)
    }

    private fun clear() {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    }
}
