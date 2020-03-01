import app.IGameLogic
import display.graphic.*
import engine.freeBody.FreeBody
import engine.GameState
import engine.freeBody.Planet
import engine.freeBody.Vehicle
import engine.motion.Director
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World
import org.lwjgl.glfw.GLFW
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class AppLogic : IGameLogic {

    private var paused = false
    private val renderer = Renderer()

    private lateinit var marble_earth: Texture
    private lateinit var metal: Texture
    private lateinit var pavement: Texture
    private lateinit var white_pixel: Texture

    private val gameState = GameState()

    private val world: World = World(Vec2(0f, 0f))
    private val timeStep = 1f / 60f
    private val velocityIterations = 8
    private val positionIterations = 3
//    var lastTime = System.currentTimeMillis()

    init {
        val terra = Planet.create(world, "terra", 0f, 0f, 0f, -10f, -10f, -.1f, 1800f, 90f, .3f)
        val luna = Planet.create(world, "luna", 500f, 0f, 0f, 0f, 230f, -2f, 100f, 25f, .5f)
        val alice = Vehicle.create(world, "alice", 500f, -50f, 0f, -180f, 100f, 0f, 3f)
        gameState.vehicles.add(alice)
        gameState.planets.addAll(listOf(terra, luna))
        gameState.planets.addAll((0..50)
            .withIndex()
            .map { (i, _) ->
                val ratio = (2 * PI * 0.92 * i).toFloat()
                val radius = 250
                floatArrayOf(
                    (i + radius) * cos(ratio),
                    (i + radius) * sin(ratio),
                    i.toFloat()
                )
            }
            .map {
                val direction = Director.getDirection(-it[0], -it[1]) + PI * .5f
                val speed = 400f
                Planet.create(
                    world, "${it[2].toInt()}", it[0], it[1], 0f,
                    cos(direction).toFloat() * speed,
                    sin(direction).toFloat() * speed,
                    .5f, 0.3f * it[2].rem(6f), 4f + it[2].rem(6f)
                )
            })
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
            gameState.tickClock(world, timeStep, velocityIterations, positionIterations)
        }

    }

    override fun render(window: Window) {
        renderer.clear()

        val allFreeBodies = gameState.planets.union(gameState.vehicles)
        allFreeBodies.forEach { drawTrail(it) }
        allFreeBodies.forEach { drawFreeBody(it) }
//        allFreeBodies.forEach { drawDebugForces(it) }

//        val end = System.currentTimeMillis()
//        renderer.drawText("${(1000f / (end - lastTime + 0.1)).roundToInt().toString().padStart(2, '0')}fps", 450f, 450f)
//        lastTime = end
    }

    private fun drawDebugForces(freeBody: FreeBody) {
        val x = freeBody.worldBody.position.x
        val y = freeBody.worldBody.position.y
        val accelerationX = freeBody.worldBody.m_force.x
        val accelerationY = freeBody.worldBody.m_force.y

        val multiplier = 2000f
        val linePoints = listOf(
            x,
            y,
            x + accelerationX * multiplier,
            y + accelerationY * multiplier
        )
        val triangleStripPoints = BasicShapes.getTriangleStripPoints(linePoints, 2f)
        val arrowHeadPoints = BasicShapes.getArrowHeadPoints(linePoints)
        val data = renderer.getColoredData(
            triangleStripPoints + arrowHeadPoints,
            Color(0f, 1f, 1f, 1f), Color(0f, 1f, 1f, 0.0f)
        ).toFloatArray()

        white_pixel.bind()
//        renderer.drawStrip(data)

        renderer.drawText(freeBody.id, x, y, Color.WHITE)
    }

    private fun drawTrail(freeBody: FreeBody) {
        val linePoints = freeBody.motion.trailers
            .chunked(2)
            .chunked(2)
            .filter { it.size > 1 }
            .flatMap {
                val (a, b) = it
                listOf(a[0], a[1], b[0], b[1])
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
            freeBody is Planet && freeBody.radius <= 30.0 -> pavement.bind()
            freeBody is Planet -> marble_earth.bind()
            else -> white_pixel.bind()
        }

        val textureScale = when {
            freeBody.shapeBox is CircleShape && freeBody.radius <= 30f -> 1.2f
            freeBody.shapeBox is CircleShape -> 1f
            freeBody.shapeBox is PolygonShape -> .02f
            else -> 1f
        }

        val data3 = when (freeBody.shapeBox) {
            is CircleShape -> BasicShapes.polygon30.chunked(2)
            is PolygonShape -> (freeBody.shapeBox as PolygonShape).vertices
                .flatMap { listOf(it.x, it.y) }.chunked(2)
            else -> listOf()
        }.flatMap {
            listOf(
                it[0], it[1], 0f,
                1f, 1f, 1f, 1f,
                (it[0] / 2 - 0.5f) * textureScale, (it[1] / 2 - 0.5f) * textureScale
            )
        }.toFloatArray()

        val scale = when {
            freeBody.shapeBox is CircleShape -> freeBody.shapeBox.radius
            freeBody.shapeBox is PolygonShape -> 1f
            else -> 1f
        }

        renderer.drawShape(
            data3,
            freeBody.worldBody.position.x,
            freeBody.worldBody.position.y,
            freeBody.worldBody.angle,
            scale,
            scale
        )
    }

    override fun cleanup() {
        renderer.dispose()
    }
}
