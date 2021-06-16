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
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryUtil
import utility.Common.Pi
import utility.Common.Pi2
import utility.Common.degreeToRadian
import utility.Common.makeVec2Circle
import utility.toVector3f
import java.nio.FloatBuffer

class NewRenderer {

    private lateinit var program: ShaderProgram
    private lateinit var verticesBufferHandler: VerticesBufferHandler
    private val cameraView = dI.cameraView

    lateinit var projectionGameWorld: Matrix4f

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

        specialTexture = Texture(TextureEnum.mercator_color, TextureEnum.pavement, TextureEnum.steel_plate)
    }

    lateinit var specialTexture: Texture

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
            .rotateZ(rotateZ)
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

//        program.setUniform()
//        GLint sampler = glGetUniformLocation(program.getID(), "densityVol");
//        glUniform1i(sampler, GL_TEXTURE0);
        program.setUniform("textureImage", GL_TEXTURE0)
    }

    private fun specifyVertexAttributes() {
        VertexAttribPointerSetup(listOf(3, 2, 3, 4, 1))
            .also { verticesBufferHandler.vertexDimensionCount = it.vertexDimensionCount }
    }

    fun drawModel(model: Model, worldBody: Body, rotateY: Float) {
        // todo: add rotateY to freebodies
        verticesBufferHandler.drawVertices(model.gpuData, program) {
            setUniformInputs(model.scale, worldBody.position, worldBody.angle, rotateY, model.cameraType)
//            dI.textures.getTexture(model.textures[0]).bind()
            specialTexture.bind()
        }
    }

    private fun setUniformInputs(scale: Float, offset: Vec2, rotateZ: Float, rotateY: Float, cameraType: CameraType) =
        setUniformInputs(Vector3f(scale), offset.toVector3f(), rotateZ, rotateY, cameraType)

}


