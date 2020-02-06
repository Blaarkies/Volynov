import app.IGameLogic
import display.graphic.Window
import display.graphic.BasicShapes
import display.graphic.Color
import display.graphic.Renderer
import display.graphic.Texture
import engine.FreeBody
import engine.GameState
import engine.Planet
import engine.Vehicle
import org.lwjgl.glfw.GLFW
import kotlin.streams.toList

class AppLogic : IGameLogic {

    private var paused = false
    private val renderer = Renderer()

    private lateinit var marble_earth: Texture
    private lateinit var metal: Texture
    private lateinit var pavement: Texture
    private lateinit var white_pixel: Texture

    val gameState = GameState()

    init {
        gameState.addPlayer(-350.0, 0.0, 0.0, 0.0, -0.3, .5, "You")
        gameState.addPlanet(.0, .0, .0, 0.0, -0.135, -.3, "Earth", 325.0, 40.0, 2000.0)
        gameState.addPlanet(-300.0, 0.0, 0.0, 0.0, 0.9, -.5, "Moon", 325.0, 20.0, 300.0)
    }

    @Throws(Exception::class)
    override fun init() {
        renderer.init()

        marble_earth = Texture.loadTexture("src\\main\\resources\\textures\\marble_earth.png")
        metal = Texture.loadTexture("src\\main\\resources\\textures\\metal.png")
        pavement = Texture.loadTexture("src\\main\\resources\\textures\\pavement.png")
        white_pixel = Texture.loadTexture("src\\main\\resources\\textures\\white_pixel.png")
    }

    override fun input(window: Window) {
        if (window.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            paused = !paused
        }
    }

    override fun update(interval: Float) {
        if (!paused) {
            gameState.tickClock()
        }
    }

    override fun render(window: Window) {
        renderer.clear()

        val allFreeBodies = gameState.planets.union(gameState.vehicles)
        allFreeBodies.forEach { drawTrail(it) }
        allFreeBodies.forEach { drawFreeBody(it) }
        allFreeBodies.forEach { drawDebugForces(it) }
    }

    private fun drawDebugForces(freeBody: FreeBody) {
        val location = freeBody.motion.location
        val x = location.x.toFloat()
        val y = location.y.toFloat()
        val accelerationX = freeBody.motion.debugLastAcceleration.ddx.toFloat()
        val accelerationY = freeBody.motion.debugLastAcceleration.ddy.toFloat()

        val multiplier = 2000f
        val linePoints = listOf(
            x,
            y,
            x + multiplier * accelerationX,
            y + multiplier * accelerationY
        )
        val triangleStripPoints = BasicShapes.getTriangleStripPoints(linePoints, 2f)
        val arrowHeadPoints = BasicShapes.getArrowHeadPoints(linePoints)
        val data = renderer.getColoredData(
            triangleStripPoints + arrowHeadPoints,
            Color(0f, 1f, 1f, 1f), Color(0f, 1f, 1f, 0.0f)
        ).toFloatArray()

        white_pixel.bind()
        renderer.drawStrip(data)

        renderer.drawText(
            freeBody.id,
            freeBody.motion.location.x.toFloat(),
            freeBody.motion.location.y.toFloat(),
            Color.WHITE
        )
    }

    private fun drawTrail(freeBody: FreeBody) {
        val linePoints = freeBody.motion.trailers.stream().toList()
            .chunked(2)
            .filter { it.size > 1 }
            .flatMap {
                val a = it[0].location
                val b = it[1].location
                listOf(a.x.toFloat(), a.y.toFloat(), b.x.toFloat(), b.y.toFloat())
            }
        val data = getLineFromPoints(linePoints, Color(0.4f, 0.7f, 1f, 0.5f), Color.TRANSPARENT, 2f, 0f)

        white_pixel.bind()
        renderer.drawStrip(data)
    }

    private fun getLineFromPoints(
        points: List<Float>,
        startColor: Color = Color.WHITE,
        endColor: Color = startColor,
        startWidth: Float = 1f,
        endWidth: Float = startWidth
    ): FloatArray {
        val triangleStripPoints = BasicShapes.getTriangleStripPoints(points, startWidth, endWidth)
        val coloredData = renderer.getColoredData(triangleStripPoints, startColor, endColor)
        return coloredData.toFloatArray()
    }

    private fun drawFreeBody(freeBody: FreeBody) {
        when {
            freeBody is Vehicle -> metal.bind()
            freeBody is Planet && freeBody.radius <= 20.0 -> pavement.bind()
            freeBody is Planet -> marble_earth.bind()
            else -> white_pixel.bind()
        }
        val data = BasicShapes.polygon30.chunked(2)
            .flatMap {
                listOf(
                    it[0], it[1], 0f,
                    1f, 1f, 1f, 1f,
                    it[0] / 2 - 0.5f, it[1] / 2 - 0.5f
                )
            }
            .toFloatArray()

        renderer.drawShape(
            data,
            freeBody.motion.location.x,
            freeBody.motion.location.y,
            freeBody.motion.location.h,
            freeBody.radius,
            freeBody.radius
        )
    }

    override fun cleanup() {
        renderer.dispose()
    }
}
