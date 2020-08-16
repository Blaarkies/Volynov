package display.graphic

import dI
import display.text.Font
import display.text.TextJustify
import org.jbox2d.common.Vec2
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER
import org.lwjgl.system.MemoryUtil
import utility.Common.Pi
import utility.Common.Pi2
import utility.Common.degreeToRadian
import utility.Common.getSafePath
import utility.Common.radianToDegree
import utility.Common.vectorUnit
import java.awt.FontFormatException
import java.io.FileInputStream
import java.io.IOException
import java.nio.FloatBuffer
import java.util.logging.Level
import java.util.logging.Logger

class Renderer {

    var debugOffset: Vec2 = vectorUnit

    private lateinit var vao: VertexArrayObject
    private lateinit var vbo: VertexBufferObject
    private lateinit var program: ShaderProgram
    private lateinit var vertices: FloatBuffer
    private var numVertices = 0
    private var isDrawing = false
    lateinit var font: Font
    private val vertexDimensionCount = 9
    private val cameraView = dI.cameraView

    lateinit var projectionGameWorld: Matrix4f

    // Coordinate system is expected cartesian.
    // y-value increases to top of screen, x-value increases to right of screen
    fun init() {
        setupShaderProgram()

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        //        glEnable(GL_CULL_FACE)
        //        glDepthFunc(GL_LESS)

        font = try {
            val fontPath = getSafePath("./fonts/ALBMT___.TTF")
            Font(FileInputStream(fontPath), 80)
        } catch (ex: FontFormatException) {
            ex.printStackTrace()
            Logger.getLogger(Renderer::class.java.name).log(Level.CONFIG, null, ex)
            Font()
        } catch (ex: IOException) {
            ex.printStackTrace()
            Logger.getLogger(Renderer::class.java.name).log(Level.CONFIG, null, ex)
            Font()
        }

        val widthSide = cameraView.windowWidth * .5f
        val heightSide = cameraView.windowHeight * .5f
        val aspectRatio = widthSide / heightSide
        projectionGameWorld = Matrix4f()
            .perspective(degreeToRadian * 140f, aspectRatio, .001f, 100f)
            .transpose()
    }

    fun clear() = glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

    fun begin() {
        check(!isDrawing) { "Renderer is already drawing!" }
        isDrawing = true
        numVertices = 0
    }

    fun end(drawType: Int = GL_TRIANGLES) {
        check(isDrawing) { "Renderer isn't drawing!" }
        isDrawing = false
        flush(drawType)
    }

    private fun flush(drawType: Int) {
        if (numVertices <= 0) return

        vertices.flip()
        vao.bind()
        program.use()

        vbo.bind(GL_ARRAY_BUFFER) // TODO: not needed?
            .uploadSubData(GL_ARRAY_BUFFER, 0, vertices)

        glDrawArrays(drawType, 0, numVertices)

        vertices.clear()
        numVertices = 0
    }

    fun drawText(text: CharSequence,
                 offset: Vec2,
                 scale: Vec2,
                 color: Color,
                 justify: TextJustify = TextJustify.LEFT,
                 useCamera: Boolean = true,
                 snipRegion: SnipRegion? = null,
                 maxWidth: Float = 0f,
                 angle: Float = 0f
    ) {
        if (text.isEmpty()) return
        font.drawText(this, text, offset, scale, color, justify, useCamera, snipRegion, maxWidth, angle)
    }

    fun drawShape(
        data: FloatArray,
        offset: Vec2 = Vec2(),
        h: Float = 0f,
        scale: Vec2 = vectorUnit,
        useCamera: Boolean = true,
        snipRegion: SnipRegion? = null,
        z: Float = 0f
    ) = drawEntity(data, offset, h, scale, GL_TRIANGLE_FAN, useCamera, snipRegion, z)

    fun drawStrip(
        data: FloatArray,
        offset: Vec2 = Vec2(),
        h: Float = 0f,
        scale: Vec2 = vectorUnit,
        useCamera: Boolean = true,
        snipRegion: SnipRegion? = null
    ) = drawEntity(data, offset, h, scale, GL_TRIANGLE_STRIP, useCamera, snipRegion)

    fun drawMesh(
        data: FloatArray,
        offset: Vec2 = Vec2(),
        h: Float = 0f,
        scale: Vec2 = vectorUnit,
        useCamera: Boolean = true,
        snipRegion: SnipRegion? = null,
        z: Float = 0f
    ) = drawEntity(data, offset, h, scale, GL_TRIANGLES, useCamera, snipRegion, z)

    private fun drawEntity(
        data: FloatArray,
        offset: Vec2,
        h: Float,
        scale: Vec2,
        drawType: Int,
        useCamera: Boolean,
        snipRegion: SnipRegion?,
        z: Float = 0f
    ) {
        if (snipRegion != null && snipRegion.sizeX * snipRegion.sizeY == 0) return

        begin()
        if (vertices.remaining() < data.size) {
            flush(GL_TRIANGLES)
            println("👉  (vertices.remaining() < data.size) was true ✔ ")
        }

        vertices.put(data)
        numVertices += data.size / vertexDimensionCount

        setUniformInputs(offset, z, h, scale, useCamera, snipRegion)
        end(drawType)
    }

    fun dispose() {
        MemoryUtil.memFree(vertices)
        vao.delete()
        vbo.delete()
        program.delete()
        font.dispose()
    }

    private fun setupShaderProgram() {
        vao = VertexArrayObject().bind()
        vbo = VertexBufferObject().bind(GL_ARRAY_BUFFER)
        vertices = MemoryUtil.memAllocFloat(4096 * 2 * 64)
        /* Upload null data to allocate storage for the VBO */
        val size = (vertices.capacity() * java.lang.Float.BYTES).toLong()
        vbo.uploadData(GL_ARRAY_BUFFER, size, GL_STATIC_DRAW)

        val vertexShader = Shader.loadShader(GL_VERTEX_SHADER, "/shaders/vertexBasicPosition.glsl")
        val fragmentShader = Shader.loadShader(GL_FRAGMENT_SHADER, "/shaders/fragmentBasicColor.glsl")
        program = ShaderProgram()
        program.attachShader(vertexShader)
        program.attachShader(fragmentShader)
        program.bindFragmentDataLocation(0, "fragmentColor")
        program.link()
        program.use()
        vertexShader.delete()
        fragmentShader.delete()

        specifyVertexAttributes()
    }

    private fun setUniformInputs(
        offset: Vec2 = Vec2(),
        z: Float = 0f,
        h: Float = 0f,
        scale: Vec2 = vectorUnit,
        useCamera: Boolean,
        snipRegion: SnipRegion?
    ) {
        val model = Matrix4f()
            .translate(offset.x, offset.y, z)
            .rotate(h, 0f, 0f, 1f)
            .scale(scale.x, scale.y, 1f)
            .transpose()
        val uniModel = program.getUniformLocation("model")
        program.setUniform(uniModel, model)

        val gameCamera = cameraView.renderCamera
        val guiCamera = Matrix4f()

        glDisable(GL_SCISSOR_TEST)

        val view = when (useCamera) {
            true -> {
                glEnable(GL_DEPTH_TEST)
                gameCamera
            }
            false -> {
                if (snipRegion != null) {
                    glScissor(
                        cameraView.windowWidthInt.div(2) + snipRegion.x,
                        cameraView.windowHeightInt.div(2) + snipRegion.y,
                        snipRegion.sizeX, snipRegion.sizeY)
                    glEnable(GL_SCISSOR_TEST)
                }
                glDisable(GL_DEPTH_TEST)
                guiCamera
            }
        }
        val uniView = program.getUniformLocation("view")
        program.setUniform(uniView, view)

        val widthSide = cameraView.windowWidth * .5f
        val heightSide = cameraView.windowHeight * .5f
        val depthSide = 10f
        val projection = if (useCamera) projectionGameWorld
        else Matrix4f().ortho(-widthSide, widthSide, -heightSide, heightSide, depthSide, -depthSide)

        val uniProjection = program.getUniformLocation("projection")
        program.setUniform(uniProjection, projection)

        //        if (dI.window.isResized() ) {
        //            glViewport(0, 0, window.getWidth(), window.getHeight());
        //            window.setResized(false);
        //        }

    }

    private fun specifyVertexAttributes() {
        val posAttribute = program.getAttributeLocation("inPosition")
        program.enableVertexAttribute(posAttribute)
        program.pointVertexAttribute(posAttribute, 3, 9 * java.lang.Float.BYTES, 0)

        val colAttribute = program.getAttributeLocation("inColor")
        program.enableVertexAttribute(colAttribute)
        program.pointVertexAttribute(colAttribute, 4, 9 * java.lang.Float.BYTES, 3 * java.lang.Float.BYTES)

        val texAttribute = program.getAttributeLocation("inTextureCoordinate")
        program.enableVertexAttribute(texAttribute)
        program.pointVertexAttribute(texAttribute, 2, 9 * java.lang.Float.BYTES, 7 * java.lang.Float.BYTES)
    }
}

