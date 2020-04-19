package display.graphic

import Matrix4f
import display.text.Font
import display.text.TextJustify
import input.CameraView
import org.jbox2d.common.Vec2
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER
import org.lwjgl.system.MemoryUtil
import utility.Common
import utility.Common.getSafePath
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
    private lateinit var cameraView: CameraView

    fun init(gameStateCamera: CameraView) {
        cameraView = gameStateCamera
        setupShaderProgram()

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        font = try {
            val fontPath = getSafePath("./fonts/ALBMT___.TTF")
            Font(FileInputStream(fontPath), 80)
        } catch (ex: FontFormatException) {
            Logger.getLogger(Renderer::class.java.name).log(Level.CONFIG, null, ex)
            Font()
        } catch (ex: IOException) {
            Logger.getLogger(Renderer::class.java.name).log(Level.CONFIG, null, ex)
            Font()
        }
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
                 snipRegion: SnipRegion? = null
    ) = font.drawText(this, text, offset, scale, color, justify, useCamera, snipRegion)

    fun drawShape(
        data: FloatArray,
        offset: Vec2 = Vec2(),
        h: Float = 0f,
        scale: Vec2 = vectorUnit,
        useCamera: Boolean = true,
        snipRegion: SnipRegion? = null
    ) = drawEntity(data, offset, h, scale, GL_TRIANGLE_FAN, useCamera, snipRegion)

    fun drawStrip(
        data: FloatArray,
        offset: Vec2 = Vec2(),
        h: Float = 0f,
        scale: Vec2 = vectorUnit,
        useCamera: Boolean = true,
        snipRegion: SnipRegion? = null
    ) =
        drawEntity(data, offset, h, scale, GL_TRIANGLE_STRIP, useCamera, snipRegion)

    private fun drawEntity(
        data: FloatArray,
        offset: Vec2,
        h: Float,
        scale: Vec2,
        drawType: Int,
        useCamera: Boolean,
        snipRegion: SnipRegion?
    ) {
        begin()
        if (vertices.remaining() < data.size) {
            flush(GL_TRIANGLES)
            println("👉  (vertices.remaining() < data.size) was true ✔ ")
        }

        vertices.put(data)
        numVertices += data.size / vertexDimensionCount

        setUniformInputs(offset, 0f, h, scale, useCamera, snipRegion)
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
        vertices = MemoryUtil.memAllocFloat(4096)
        /* Upload null data to allocate storage for the VBO */
        val size = (vertices.capacity() * java.lang.Float.BYTES).toLong()
        vbo.uploadData(GL_ARRAY_BUFFER, size, GL_DYNAMIC_DRAW)

        val vertexShader = Shader.loadShader(GL_VERTEX_SHADER, "/shaders/vertexBasicPosition.glsl")
        val fragmentShader = Shader.loadShader(GL_FRAGMENT_SHADER, "/shaders/fragmentBasicColor.glsl")
        program = ShaderProgram()
        program.attachShader(vertexShader)
        program.attachShader(fragmentShader)
        program.bindFragmentDataLocation(0, "fragColor")
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
        //        val uniTex = program!!.getUniformLocation("texImage")
        //        program!!.setUniform(uniTex, 0)

        val gameCamera = cameraView.getRenderCamera()
        val guiCamera = Matrix4f()
        glDisable(GL_SCISSOR_TEST)


        val model = Matrix4f.translate(offset.x, offset.y, z)
            .multiply(Matrix4f.rotate(h * Common.radianToDegree, 0f, 0f, 1f))
            .multiply(Matrix4f.scale(scale.x, scale.y, 1f))
        val uniModel = program.getUniformLocation("model")
        program.setUniform(uniModel, model)

        val view = when (useCamera) {
            true -> gameCamera
            false -> {
                if (snipRegion != null) {
                    glScissor(cameraView.windowWidth.div(2).toInt() + snipRegion.offset.x.toInt(),
                        cameraView.windowHeight.div(2).toInt() + snipRegion.offset.y.toInt(),
                        snipRegion.scale.x.toInt(), snipRegion.scale.y.toInt())
                    glEnable(GL_SCISSOR_TEST)
                }
                guiCamera
            }
        }
        val uniView = program.getUniformLocation("view")
        program.setUniform(uniView, view)

        val widthSide = cameraView.windowWidth * .5f
        val heightSide = cameraView.windowHeight * .5f
        val depthSide = 100f
        val projection = Matrix4f.orthographic(
            -widthSide, widthSide,
            -heightSide, heightSide,
            -depthSide, depthSide
        )
        val uniProjection = program.getUniformLocation("projection")
        program.setUniform(uniProjection, projection)
    }

    private fun specifyVertexAttributes() {
        val posAttribute = program.getAttributeLocation("position")
        program.enableVertexAttribute(posAttribute)
        program.pointVertexAttribute(posAttribute, 3, 9 * java.lang.Float.BYTES, 0)

        val colAttribute = program.getAttributeLocation("color")
        program.enableVertexAttribute(colAttribute)
        program.pointVertexAttribute(colAttribute, 4, 9 * java.lang.Float.BYTES, 3 * java.lang.Float.BYTES)

        val texAttribute = program.getAttributeLocation("texcoord")
        program.enableVertexAttribute(texAttribute)
        program.pointVertexAttribute(texAttribute, 2, 9 * java.lang.Float.BYTES, 7 * java.lang.Float.BYTES)
    }
}

