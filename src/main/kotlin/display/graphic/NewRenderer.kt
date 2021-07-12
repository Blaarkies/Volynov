package display.graphic

import dI
import display.draw.Model
import display.draw.TextureEnum
import display.graphic.vertex.VertexAttribPointerSetup
import org.jbox2d.common.MathUtils.sin
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.ARBFramebufferObject.glGenerateMipmap
import org.lwjgl.opengl.ARBInternalformatQuery2.GL_TEXTURE_2D_ARRAY
import org.lwjgl.opengl.ARBTextureStorage.glTexStorage3D
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL43.glDebugMessageCallback
import org.lwjgl.opengl.GLDebugMessageCallbackI
import utility.Common.Pi
import utility.Common.Pi2
import utility.Common.degreeToRadian
import utility.Common.makeVec2Circle
import utility.toVector3f
import java.nio.ByteBuffer
import java.nio.FloatBuffer

class NewRenderer {

    private lateinit var program: ShaderProgram
    private lateinit var verticesBufferHandler: VerticesBufferHandler
    private val cameraView = dI.cameraView

    lateinit var projectionGameWorld: Matrix4f

//    lateinit var specialTexture: Texture

    // Coordinate system is expected cartesian.
    // y-value increases to top of screen, x-value increases to right of screen
    fun init() {
        verticesBufferHandler = VerticesBufferHandler()
        setupShaderProgram()

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        //        glEnable(GL_CULL_FACE)
        //        glDepthFunc(GL_LESS)

        val widthSide = cameraView.windowWidth * .5f
        val heightSide = cameraView.windowHeight * .5f
        val aspectRatio = widthSide / heightSide
        projectionGameWorld = Matrix4f()
            .perspective(degreeToRadian * 30f, aspectRatio, .001f, 100f)
            .transpose()

        glDebugMessageCallback({source, type, id, severity, length, message, userParam ->
            println(message)
        }, 0)
//        setup2dTexture()
        setup2dArrayTexture()
    }

    fun setup2dArrayTexture() {
        var texture = 0;

        val width = 2;
        val height = 2;
        val layerCount = 2;
        val mipLevelCount = 1;

// Read you texels here. In the current example, we have 2*2*2 = 8 texels, with each texel being 4 GLubytes.
        val texels1 =
            intArrayOf(
                255,    0,      0,      255,
                255,    255,    0,      255,
                0,      255,    0,      255,
                0,      255,    255,    255,
            ).map { it.toFloat() }.toFloatArray();

        val texels2 =
            intArrayOf(
                0,      0,      255,    255,
                255,    0,      255,    255,
                255,    255,    255,    255,
                128,    128,    128,    255,
            ).map { it.toFloat() }.toFloatArray();

//        glActiveTexture(GL_TEXTURE0)
        val imageBuffer = Texture.getImageBufferData(TextureEnum.mercator_color.name)
        val texel = imageBuffer.buffer.array().map { it.toInt() }.toIntArray()

        texture = glGenTextures();
//        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D_ARRAY,texture);

        glTexStorage3D(GL_TEXTURE_2D_ARRAY, mipLevelCount, GL_RGBA8, imageBuffer.width, imageBuffer.height, layerCount);

        glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, 0, imageBuffer.width, imageBuffer.height, 1, GL_RGBA, GL_INT, texel);
        glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, 1, imageBuffer.width, imageBuffer.height, 1, GL_RGBA, GL_INT, texel);

        glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_WRAP_T, GL_REPEAT);
    }

    fun setup2dTexture() {
        val imageBuffer = Texture.getImageBufferData(TextureEnum.mercator_color.name)

        val texture = glGenTextures()

        glBindTexture(GL_TEXTURE_2D, texture);
// set the texture wrapping/filtering options (on the currently bound
// texture object)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
// load and generate the texture

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8,
            imageBuffer.width, imageBuffer.height,
            0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer.buffer);
        glGenerateMipmap(GL_TEXTURE_2D);

        program.setUniform("textureImage", GL_TEXTURE0)
    }

    fun clear() = glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

    fun dispose() {
//        verticesBufferHandler.dispose()
//        program.delete()
    }

    private fun setupShaderProgram() {
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
        scale: Vector3f,
        offset: Vector3f,
        rotateZ: Float,
        rotateY: Float,
        cameraType: CameraType,
    ) {
        val model = Matrix4f()
            .translate(offset)
            .rotateZ(rotateZ + System.nanoTime()*.0000000001f)
            .rotateY(rotateY)
            .scale(scale)
            .transpose()
        program.setUniform("model", model)

        glEnable(GL_DEPTH_TEST)
        val view = cameraView.renderCamera
        program.setUniform("view", view)

        val projection = projectionGameWorld
        program.setUniform("projection", projection)

        program.setUniform("ambientStrength", 15)

        val lampPosition = dI.gameState.tickTime
            .let {
                val duration = 30f * 1000f
                val txy = it.rem(duration).div(duration)
                val tz = it.rem(duration * .9f).div(duration * .9f)
                makeVec2Circle(Pi2 * -txy).mul(20f)
                    .add(dI.gameState.mapBorder?.worldBody?.position ?: Vec2())
                    .toVector3f(40f * sin(Pi * tz) - 80f)
            }
        program.setUniform("lightPosition", lampPosition)
        program.setUniform("lightColor", Vector3f(1f, 1f, 1f).mul(.5f))
        program.setUniform("viewPos", cameraView.location.toVector3f(cameraView.z))

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D_ARRAY,1);
        program.setUniform("textureImage", GL_TEXTURE0)
    }

    private fun specifyVertexAttributes() {
        VertexAttribPointerSetup(listOf(3, 2, 3, 4, 1))
            .also { verticesBufferHandler.vertexDimensionCount = it.vertexDimensionCount }
    }

    fun drawModel(model: Model, worldBody: Body, rotateY: Float) {
        // todo: add rotateY to free bodies
        verticesBufferHandler.drawVertices(model.gpuData, program) {
            setUniformInputs(model.scale, worldBody.position, worldBody.angle, rotateY,
                model.cameraType)
//            dI.textures.getTexture(model.textures[0]).bind()
//            glActiveTexture(GL_TEXTURE0)
//            specialTexture.bind()
        }
    }

    private fun setUniformInputs(scale: Float,
                                 offset: Vec2,
                                 rotateZ: Float,
                                 rotateY: Float,
                                 cameraType: CameraType) =
        setUniformInputs(Vector3f(scale), offset.toVector3f(), rotateZ, rotateY, cameraType)

}


