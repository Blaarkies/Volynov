package display.graphic

import Matrix4f
import display.text.Font
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.FontFormatException
import java.io.FileInputStream
import java.io.IOException
import java.nio.FloatBuffer
import java.util.logging.Level
import java.util.logging.Logger

class Renderer {

    private var vao: VertexArrayObject? = null
    private var vbo: VertexBufferObject? = null
    private var program: ShaderProgram? = null
    private var vertices: FloatBuffer? = null
    private var numVertices = 0
    private var drawing = false
    private var font: Font? = null
    private var debugFont: Font? = null
    private var vertexDimensionCount = 9

    fun init() {
        setupShaderProgram()

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        font = try {
            Font(FileInputStream("src\\main\\resources\\fonts\\ALBMT___.TTF"), 16)
        } catch (ex: FontFormatException) {
            Logger.getLogger(Renderer::class.java.name).log(Level.CONFIG, null, ex)
            Font()
        } catch (ex: IOException) {
            Logger.getLogger(Renderer::class.java.name).log(Level.CONFIG, null, ex)
            Font()
        }
        debugFont = Font(12, false)
    }

    fun clear() {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    }

    fun begin() {
        check(!drawing) { "Renderer is already drawing!" }
        drawing = true
        numVertices = 0
    }

    fun end() {
        check(drawing) { "Renderer isn't drawing!" }
        drawing = false
        flush(GL_TRIANGLES)
    }

    private fun flush(drawType: Int) {
        if (numVertices <= 0) return

        vertices!!.flip()
        if (vao != null) {
            vao!!.bind()
        } else {
            vbo!!.bind(GL_ARRAY_BUFFER)
            specifyVertexAttributes()
        }
        program!!.use()

        vbo!!.bind(GL_ARRAY_BUFFER) // TODO: not needed?
        vbo!!.uploadSubData(GL_ARRAY_BUFFER, 0, vertices!!)

        glDrawArrays(drawType, 0, numVertices)

        vertices!!.clear()
        numVertices = 0
    }

    fun getTextWidth(text: CharSequence): Int {
        return font!!.getWidth(text)
    }

    fun getTextHeight(text: CharSequence): Int {
        return font!!.getHeight(text)
    }

    fun getDebugTextWidth(text: CharSequence): Int {
        return debugFont!!.getWidth(text)
    }

    fun getDebugTextHeight(text: CharSequence): Int {
        return debugFont!!.getHeight(text)
    }

    fun drawText(text: CharSequence, x: Float, y: Float) {
        font!!.drawText(this, text, x, y)
    }

    fun drawDebugText(text: CharSequence, x: Float, y: Float) {
        debugFont!!.drawText(this, text, x, y)
    }

    fun drawText(text: CharSequence, x: Float, y: Float, c: Color) {
        font!!.drawText(this, text, x, y, c)
    }

    fun drawDebugText(text: CharSequence, x: Float, y: Float, c: Color) {
        debugFont!!.drawText(this, text, x, y, c)
    }

    fun drawTexture(texture: Texture, x: Float, y: Float, c: Color = Color.WHITE) {
        val x2 = x + texture.width
        val y2 = y + texture.height

        val s1 = 0f
        val t1 = 0f
        val s2 = 1f
        val t2 = 1f
        drawTextureRegion(x, y, x2, y2, s1, t1, s2, t2, c)
    }

    fun drawTextureRegion(
        texture: Texture,
        x: Float, y: Float,
        regX: Float, regY: Float,
        regWidth: Float, regHeight: Float,
        c: Color? = Color.WHITE
    ) {
        val x2 = x + regWidth
        val y2 = y + regHeight

        val s1 = regX / texture.width
        val t1 = regY / texture.height
        val s2 = (regX + regWidth) / texture.width
        val t2 = (regY + regHeight) / texture.height

        setUniformInputs()
        drawTextureRegion(x, y, x2, y2, s1, t1, s2, t2, c)
    }

    fun drawTextureRegion(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        s1: Float,
        t1: Float,
        s2: Float,
        t2: Float,
        c: Color? = Color.WHITE
    ) {
        if (vertices!!.remaining() < 8 * 6) {
            flush(GL_TRIANGLES)
        }
        val r = c!!.red
        val g = c.green
        val b = c.blue
        val a = c.alpha

        vertices!!.put(
            floatArrayOf(
                x1, y1, 0f, r, g, b, a, s1, t1,
                x1, y2, 0f, r, g, b, a, s1, t2,
                x2, y2, 0f, r, g, b, a, s2, t2,
                x1, y1, 0f, r, g, b, a, s1, t1,
                x2, y2, 0f, r, g, b, a, s2, t2,
                x2, y1, 0f, r, g, b, a, s2, t1
            )
        )
        numVertices += 6
    }

    fun drawShape(
        data: FloatArray,
        x: Double = .0,
        y: Double = .0,
        h: Double = .0,
        scaleX: Double = 1.0,
        scaleY: Double = 1.0
    ) {
        drawEntity(data, x, y, h, scaleX, scaleY, GL_TRIANGLE_FAN)
    }

    fun drawStrip(
        data: FloatArray,
        x: Double = .0,
        y: Double = .0,
        h: Double = .0,
        scaleX: Double = 1.0,
        scaleY: Double = 1.0
    ) {
        drawEntity(data, x, y, h, scaleX, scaleY, GL_TRIANGLE_STRIP)
    }

    private fun drawEntity(
        data: FloatArray,
        x: Double,
        y: Double,
        h: Double,
        scaleX: Double,
        scaleY: Double,
        drawType: Int
    ) {
        begin()
        if (vertices!!.remaining() < data.size) {
            flush(GL_TRIANGLES)
        }

        vertices!!.put(data)
        numVertices += data.size / vertexDimensionCount

        setUniformInputs(
            0f, 0f,
            x.toFloat(), y.toFloat(), 0f, h.toFloat(),
            scaleX.toFloat(), scaleY.toFloat()
        )
        flush(drawType)
        end()
    }

    fun getColoredData(
        points: List<Float>,
        startColor: Color = Color.WHITE,
        endColor: Color = startColor
    ): List<Float> {
        val pointsLastIndex = points.lastIndex.toFloat() / 2f

        return points
            .chunked(2)
            .withIndex()
            .flatMap { (index, chunk) ->
                val interpolationDistance = index.toFloat() / pointsLastIndex
                val color = startColor * interpolationDistance + endColor * (1f - interpolationDistance)
                listOf(
                    chunk[0],
                    chunk[1],
                    0f, /* pos*/
                    color.red,
                    color.green,
                    color.blue,
                    color.alpha, /* color*/
                    0f,
                    0f /* texture*/
                )
            }
    }

    fun dispose() {
        MemoryUtil.memFree(vertices)
        if (vao != null) {
            vao?.delete()
        }
        vbo?.delete()
        program?.delete()
        font?.dispose()
        debugFont?.dispose()
    }

    private fun setupShaderProgram() {
        vao = VertexArrayObject()
        vao!!.bind()

        vbo = VertexBufferObject()
        vbo!!.bind(GL_ARRAY_BUFFER)

        vertices = MemoryUtil.memAllocFloat(4096)
        /* Upload null data to allocate storage for the VBO */
        val size = (vertices!!.capacity() * java.lang.Float.BYTES).toLong()
        vbo!!.uploadData(GL_ARRAY_BUFFER, size, GL_DYNAMIC_DRAW)

        numVertices = 0
        drawing = false

        val vertexShader = Shader.loadShader(GL_VERTEX_SHADER, "/shaders/vertexBasicPosition.glsl")
        val fragmentShader = Shader.loadShader(GL_FRAGMENT_SHADER, "/shaders/fragmentBasicColor.glsl")

        program = ShaderProgram()
        program!!.attachShader(vertexShader)
        program!!.attachShader(fragmentShader)
        program!!.bindFragmentDataLocation(0, "fragColor")
        program!!.link()
        program!!.use()
        vertexShader.delete()
        fragmentShader.delete()

        val window: Long = GLFW.glfwGetCurrentContext()
        var width = 0f
        var height = 0f
        MemoryStack.stackPush().use { stack ->
            val widthBuffer = stack.mallocInt(1)
            val heightBuffer = stack.mallocInt(1)
            GLFW.glfwGetFramebufferSize(window, widthBuffer, heightBuffer)
            width = widthBuffer.get().toFloat()
            height = heightBuffer.get().toFloat()
        }

        specifyVertexAttributes()

        setUniformInputs(width, height)
    }

    private fun setUniformInputs(
        width: Float = 0f, height: Float = 0f,
        x: Float = 0f,
        y: Float = 0f,
        z: Float = 0f,
        h: Float = 0f,
        scaleX: Float = 1f,
        scaleY: Float = 1f
    ) {
        val uniTex = program!!.getUniformLocation("texImage")
        program!!.setUniform(uniTex, 0)

        val model = Matrix4f.translate(x, y, z)
            .multiply(Matrix4f.rotate(h, 0f, 0f, 1f))
            .multiply(Matrix4f.scale(scaleX, scaleY, 1f))
        val uniModel = program!!.getUniformLocation("model")
        program!!.setUniform(uniModel, model)

        val view = Matrix4f()
        val uniView = program!!.getUniformLocation("view")
        program!!.setUniform(uniView, view)

        if (width * height == 0f) return
        val projection = Matrix4f.orthographic(-width / 2, width / 2, -height / 2, height / 2, -1f, 1f)
        val uniProjection = program!!.getUniformLocation("projection")
        program!!.setUniform(uniProjection, projection)
    }

    private fun specifyVertexAttributes() {
        val posAttribute = program!!.getAttributeLocation("position")
        program!!.enableVertexAttribute(posAttribute)
        program!!.pointVertexAttribute(posAttribute, 3, 9 * java.lang.Float.BYTES, 0)

        val colAttribute = program!!.getAttributeLocation("color")
        program!!.enableVertexAttribute(colAttribute)
        program!!.pointVertexAttribute(colAttribute, 4, 9 * java.lang.Float.BYTES, 3 * java.lang.Float.BYTES)

        val texAttribute = program!!.getAttributeLocation("texcoord")
        program!!.enableVertexAttribute(texAttribute)
        program!!.pointVertexAttribute(texAttribute, 2, 9 * java.lang.Float.BYTES, 7 * java.lang.Float.BYTES)
    }
}
