package display.graphic

import dI
import display.text.Font
import display.text.TextJustify
import org.jbox2d.common.MathUtils.sin
import org.jbox2d.common.Vec2
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryUtil
import utility.Common.Pi
import utility.Common.Pi2
import utility.Common.degreeToRadian
import utility.Common.getSafePath
import utility.Common.makeVec2Circle
import utility.Common.vectorUnit
import utility.toVector3f
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
    private val vertexDimensionCount = 12
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
            .perspective(degreeToRadian * 30f, aspectRatio, .001f, 100f)
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
    ) = drawEntity(
        data, offset.toVector3f(z), h, scale.toVector3f(), GL_TRIANGLE_FAN,
        if (useCamera) CameraType.UNIVERSE_SPECTRAL else CameraType.GUI,
        snipRegion,
        0f
    )

    fun drawStrip(
        data: FloatArray,
        offset: Vec2 = Vec2(),
        h: Float = 0f,
        scale: Vec2 = vectorUnit,
        useCamera: Boolean = true,
        snipRegion: SnipRegion? = null
    ) = drawEntity(
        data, offset.toVector3f(), h, scale.toVector3f(), GL_TRIANGLE_STRIP,
        if (useCamera) CameraType.UNIVERSE_SPECTRAL else CameraType.GUI,
        snipRegion,
        0f
    )

    fun drawMesh(
        data: FloatArray,
        offset: Vector3f = Vector3f(),
        h: Float = 0f,
        scale: Vector3f = Vector3f(1f),
        cameraType: CameraType = CameraType.UNIVERSE,
        snipRegion: SnipRegion? = null,
        rotateY: Float = 0f
    ) = drawEntity(data, offset, h, scale, GL_TRIANGLES, cameraType, snipRegion, rotateY)

    private fun drawEntity(
        data: FloatArray,
        offset: Vector3f,
        h: Float,
        scale: Vector3f,
        drawType: Int,
        cameraType: CameraType,
        snipRegion: SnipRegion?,
        rotateY: Float
    ) {
        if (snipRegion != null && snipRegion.sizeX * snipRegion.sizeY == 0) return

        begin()
        if (vertices.remaining() < data.size) {
            flush(GL_TRIANGLES)
            println("👉  (vertices.remaining() < data.size) was true ✔ ")
        }

        vertices.put(data)
        numVertices += data.size / vertexDimensionCount

        setUniformInputs(offset, h, scale, cameraType, snipRegion, rotateY)
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

        vertices = MemoryUtil.memAllocFloat(4096 * 2 * 64) // Upload null data to allocate storage for the VBO
        val size = vertices.capacity() * java.lang.Float.BYTES
        vbo = VertexBufferObject().bind(GL_ARRAY_BUFFER)
            .uploadData(GL_ARRAY_BUFFER, size.toLong(), GL_STATIC_DRAW)

        val vertexShader = Shader(GL_VERTEX_SHADER, "/shaders/vertexBasicPosition.glsl")
        val fragmentShader = Shader(GL_FRAGMENT_SHADER, "/shaders/fragmentBasicColor.glsl")
        program = ShaderProgram()
            .attachShader(vertexShader)
            .attachShader(fragmentShader)
            .bindFragmentDataLocation(0, "fragmentColor")
            .link()
            .use()
        vertexShader.delete()
        fragmentShader.delete()

        specifyVertexAttributes()
    }

    private fun setUniformInputs(
        offset: Vector3f = Vector3f(),
        h: Float,
        scale: Vector3f = Vector3f(1f),
        cameraType: CameraType,
        snipRegion: SnipRegion?,
        rotateY: Float = 0f
    ) {
        val model = Matrix4f()
            .translate(offset)
            .rotateZ(h)
            .rotateY(rotateY)
            .scale(scale)
            .transpose()
        program.setUniform("model", model)

        val gameCamera = cameraView.renderCamera
        val guiCamera = Matrix4f()

        glDisable(GL_SCISSOR_TEST)

        val view = when (cameraType) {
            CameraType.UNIVERSE -> {
                glEnable(GL_DEPTH_TEST)
                gameCamera
            }
            CameraType.UNIVERSE_SPECTRAL -> {
                glEnable(GL_DEPTH_TEST)
                gameCamera
            }
            CameraType.GUI -> {
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
            else -> Matrix4f()
        }
        program.setUniform("view", view)

        val widthSide = cameraView.windowWidth * .5f
        val heightSide = cameraView.windowHeight * .5f
        val depthSide = 10f
        val projection = if (cameraType == CameraType.UNIVERSE
            || cameraType == CameraType.UNIVERSE_SPECTRAL) projectionGameWorld
        else Matrix4f().ortho(-widthSide, widthSide, -heightSide, heightSide, depthSide, -depthSide)

        program.setUniform("projection", projection)

        //        if (dI.window.isResized() ) {
        //            glViewport(0, 0, window.getWidth(), window.getHeight());
        //            window.setResized(false);
        //        }

        program.setUniform("ambientStrength", if (cameraType == CameraType.UNIVERSE) 15 else 100)

        val lampPosition = dI.gameState.tickTime
            .let {
                val duration = 30f * 1000f
                val txy = it.rem(duration).div(duration)
                val tz = it.rem(duration * .9f).div(duration * .9f)
                makeVec2Circle(Pi2 * -txy).mul(20f)
                    .add(dI.gameState.mapBorder?.worldBody?.position ?: Vec2())
                    .toVector3f(40f * sin(Pi * tz) - 20f)
            }
        program.setUniform("lightPosition", lampPosition)
        program.setUniform("lightColor", Vector3f(1f, 1f, 1f))
        program.setUniform("viewPos", cameraView.location.toVector3f(cameraView.z))
    }

    private fun specifyVertexAttributes() {
        val strideSize = 12 * java.lang.Float.BYTES
        val bytesInt = java.lang.Float.BYTES.toLong()

        // in_position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, strideSize, 0 * bytesInt)
        glEnableVertexAttribArray(0)

        // in_normal attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, strideSize, 3 * bytesInt)
        glEnableVertexAttribArray(1)

        // in_color attribute
        glVertexAttribPointer(2, 4, GL_FLOAT, false, strideSize, 6 * bytesInt)
        glEnableVertexAttribArray(2)

        // in_textureCoordinate attribute
        glVertexAttribPointer(3, 2, GL_FLOAT, false, strideSize, 10 * bytesInt)
        glEnableVertexAttribArray(3)
    }
}
