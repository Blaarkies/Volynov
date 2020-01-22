package display

import display.graph.ShaderProgram
import engine.FreeBody
import engine.GameState
import engine.Planet
import engine.Vehicle
import engine.motion.Location
import engine.utilities.Matrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack
import utils.Utils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.streams.toList


class Renderer {

    private var vboId = 0
    private var vaoId = 0
    private var shaderProgram: ShaderProgram? = null
    val screenMultiplier = 0.002f

    private val heptagon = (0 until 9)
        .flatMap {
            val t = 2.0 * PI * (it.toFloat() / 9)
            listOf(cos(t).toFloat(), sin(t).toFloat())
        }

    @Throws(Exception::class)
    fun init() {
        shaderProgram = ShaderProgram()
        shaderProgram!!.createVertexShader(Utils.loadResource("/shaders/vertexBasicPosition.glsl"))
        shaderProgram!!.createFragmentShader(Utils.loadResource("/shaders/fragmentBasicColor.glsl"))
        shaderProgram!!.link()

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
    }

    private fun renderEnd(vertices: FloatArray, type: Int = GL_TRIANGLE_FAN, location: Location, scale: Double) {
        val vertexDimensions = setupVerticesMemory(vertices)
        setShaderProgramUniformMatrices(location, scale)

        glDrawArrays(type, 0, vertices.size / vertexDimensions)

        glDisableVertexAttribArray(0)
        GL30.glBindVertexArray(0)
        shaderProgram!!.unbind()
    }

    private fun setupVerticesMemory(vertices: FloatArray): Int {
        val vertexDimensions = 7
        vaoId = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vaoId)

        MemoryStack.stackPush().use { stack ->
            val memoryVertices = stack.mallocFloat(vertexDimensions * vertices.size)
                .put(vertices)
                .flip()
            val vboId = glGenBuffers()
            glBindBuffer(GL_ARRAY_BUFFER, vboId)
            glBufferData(GL_ARRAY_BUFFER, memoryVertices, GL_STATIC_DRAW)
        } //        glVertexAttribPointer(0, vertexDimensions, GL_FLOAT, false, 0, 0)

        val positionInputs = 3
        val colorInputs = 4
        val strideSize = positionInputs + colorInputs
        val floatSize = 4

        val posAttrib: Int = glGetAttribLocation(shaderProgram!!.programId, "position")
        glEnableVertexAttribArray(posAttrib)
        glVertexAttribPointer(posAttrib, positionInputs, GL_FLOAT, false, strideSize * floatSize, 0)

        val colorAttrib: Int = glGetAttribLocation(shaderProgram!!.programId, "color")
        glEnableVertexAttribArray(colorAttrib)
        glVertexAttribPointer(
            colorAttrib, colorInputs, GL_FLOAT, false, strideSize * floatSize,
            (positionInputs * floatSize).toLong()
        ) //        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0) GL30.glBindVertexArray(0)

        shaderProgram!!.bind() //        GL30.glBindVertexArray(vaoId)
        glEnableVertexAttribArray(0)
        return vertexDimensions
    }

    private fun setShaderProgramUniformMatrices(location: Location, scale: Double) {
        val uniModel: Int = glGetUniformLocation(shaderProgram!!.programId, "model")
        val model =
            Matrix4f.translate(location.x.toFloat() * screenMultiplier, location.y.toFloat() * screenMultiplier, 0f)
                .multiply(Matrix4f.scale(scale.toFloat() * screenMultiplier, scale.toFloat() * screenMultiplier, 0.1f))
                .multiply(Matrix4f.rotate(location.h.toFloat(), 0f, 0f, 1f))

        val uniModelBuffer = BufferUtils.createFloatBuffer(16)
        model.toBuffer(uniModelBuffer)
        glUniformMatrix4fv(uniModel, false, uniModelBuffer)

        val uniView: Int = glGetUniformLocation(shaderProgram!!.programId, "view")
        val view = Matrix4f()
        val uniViewBuffer = BufferUtils.createFloatBuffer(16)
        view.toBuffer(uniViewBuffer)
        glUniformMatrix4fv(uniView, false, uniViewBuffer)

        val uniProjection: Int = glGetUniformLocation(shaderProgram!!.programId, "projection")
        val ratio = 1f //640f / 480f
        val projection = Matrix4f.orthographic(-ratio, ratio, -1f, 1f, -1f, 1f)
        val uniProjectionBuffer = BufferUtils.createFloatBuffer(16)
        projection.toBuffer(uniProjectionBuffer)
        glUniformMatrix4fv(uniProjection, false, uniProjectionBuffer)
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
        val allFreeBodies = gameState.planets.union(gameState.vehicles)

        allFreeBodies.forEach { drawTrail(it) }
        allFreeBodies.forEach { drawFreeBody(it) }
    }

    private fun drawFreeBody(freeBody: FreeBody) {
        val color = when (freeBody) {
            is Vehicle -> listOf(0.1f, 0.5f, 0.9f)
            is Planet -> listOf(0.20f, 0.15f, 0.11f)
            else -> listOf(1f, 1f, 1f)
        }
        val shapeVertices = heptagon.chunked(2).flatMap {
            listOf(it[0], it[1], 0f) + color + listOf(1f)
        }.toFloatArray()

        renderEnd(shapeVertices, location = freeBody.motion.location, scale = freeBody.radius)
    }

    private fun drawTrail(freeBody: FreeBody) {
        val color = when (freeBody) {
            is Vehicle -> listOf(0.1f, 0.5f, 0.9f)
            is Planet -> listOf(0.20f, 0.15f, 0.11f)
            else -> listOf(1f, 1f, 1f)
        }
        val vertices = freeBody.motion.trailers.stream()
            .toList().withIndex()
            .flatMap { (index, trailer) ->
                listOf(
                    trailer.location.x.toFloat(), trailer.location.y.toFloat(), 0f,
                    (0.7f * 2 + color[0]) / 3,
                    (0.3f * 2 + color[1]) / 3,
                    (1.0f * 2 + color[2]) / 3,
                    index.toFloat() / freeBody.motion.trailerQuantity - 0.3f
                )
            }.toFloatArray()

        val vertexDimensions = setupVerticesMemory(vertices)
        setShaderProgramUniformMatrices(Location(.0, .0), 1.0)

        glDrawArrays(GL_LINE_STRIP, 0, vertices.size / vertexDimensions)

        glDisableVertexAttribArray(0)
        GL30.glBindVertexArray(0)
        shaderProgram!!.unbind()
    }

    fun cleanup() {
        shaderProgram!!.cleanup()
        glDisableVertexAttribArray(0)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL15.glDeleteBuffers(vboId)
        GL30.glBindVertexArray(0)
        GL30.glDeleteVertexArrays(vaoId)
    }

    private fun clear() {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    }
}
