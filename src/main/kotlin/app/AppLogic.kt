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
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import org.lwjgl.glfw.GLFW
import kotlin.math.PI
import kotlin.math.pow
import kotlin.streams.toList

class AppLogic : IGameLogic {

    private var paused = false
    private val renderer = Renderer()

    private lateinit var marble_earth: Texture
    private lateinit var metal: Texture
    private lateinit var pavement: Texture
    private lateinit var white_pixel: Texture

    val gameState = GameState()

    val world: World
    val timeStep = 1f / 60f
    val velocityIterations = 8
    val positionIterations = 3

    init {
        val terra = Planet("terra", .0, .0, .0, 0.0, -40.0, -.1, 800.0, radius = 60.0, restitution = 0.3)
        val luna = Planet("luna", -270.0, .0, .0, -60.0, 260.0, .5, 100.0, radius = 25.0, restitution = 0.5)
        val alice = Vehicle("alice", 270.0, .0, .0, -60.0, 60.0, .5, 3.0)
        gameState.vehicles.add(alice)
        gameState.planets.addAll(listOf(terra, luna))
        gameState.planets.addAll((0..8).map {
            Planet(
                "$it body",
                100.0 * it - 650.0,
                100.0 * it - 250.0,
                .0,
                .0,
                .0,
                15.0,
                0.5,
                radius = 5.0
            )
        })

        world = World(Vec2(0f, 0f))

        val allFreeBodies = gameState.planets.union(gameState.vehicles)
        allFreeBodies.forEach {
            val bodyDef = BodyDef()
            bodyDef.type = BodyType.DYNAMIC
            bodyDef.position.set(it.motion.location.x.toFloat(), it.motion.location.y.toFloat())
            val shapeBox = CircleShape()
            shapeBox.radius = it.radius.toFloat()

            val fixtureDef = FixtureDef()
            fixtureDef.shape = shapeBox
            fixtureDef.density = (it.mass / (PI * it.radius.pow(2))).toFloat()
            fixtureDef.friction = .3f
            fixtureDef.restitution = it.restitution.toFloat()

            val worldBody = world.createBody(bodyDef)
            worldBody.createFixture(fixtureDef)

            worldBody.applyForceToCenter(
                Vec2(
                    it.motion.velocity.dx.toFloat() * worldBody.mass * 9.81f,
                    it.motion.velocity.dy.toFloat() * worldBody.mass * 9.81f
                )
            )

            worldBody.applyAngularImpulse(it.motion.velocity.dh.toFloat() * worldBody.inertia)

            it.shapeBox = shapeBox
            it.worldBody = worldBody
        }

/*        val groundBodyDef = BodyDef()
        groundBodyDef.position.set(0f, -100f)
        groundBox = PolygonShape()
        groundBox.setAsBox(250f, 10f)
        groundBody = world.createBody(groundBodyDef)
        groundBody.createFixture(groundBox, 0f)

        val bodyDef = BodyDef()
        bodyDef.type = BodyType.DYNAMIC
        bodyDef.position.set(0f, 0f)
        dynamicBox = PolygonShape()
        dynamicBox.setAsBox(5f, 50f)
        val fixtureDef1 = FixtureDef()
        fixtureDef1.shape = dynamicBox
        fixtureDef1.density = 1f
        fixtureDef1.friction = .5f
        fixtureDef1.restitution = .7f

        body = world.createBody(bodyDef)
        body.createFixture(fixtureDef1)
        body.applyTorque(11000000f)

        val circleDef = BodyDef()
        circleDef.type = BodyType.DYNAMIC
        circleDef.position.set(0f, 0f)
        circleBox = CircleShape()
        circleBox.radius = 30f

        val fixtureDef = FixtureDef()
        fixtureDef.shape = circleBox
        fixtureDef.density = 1f
        fixtureDef.friction = .9f
        fixtureDef.restitution = .7f

        circle = world.createBody(circleDef)
        circle.createFixture(fixtureDef)
        circle.applyTorque(-100000000f)*/
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
            freeBody is Planet && freeBody.radius <= 30.0 -> pavement.bind()
            freeBody is Planet -> marble_earth.bind()
            else -> white_pixel.bind()
        }
        val data3 = BasicShapes.polygon30.chunked(2)
            .flatMap {
                listOf(
                    it[0], it[1], 0f,
                    1f, 1f, 1f, 1f,
                    it[0] / 2 - 0.5f, it[1] / 2 - 0.5f
                )
            }
            .toFloatArray()

        renderer.drawShape(
            data3,
            freeBody.worldBody!!.position.x.toDouble(),
            freeBody.worldBody!!.position.y.toDouble(),
            freeBody.worldBody!!.angle.toDouble(),
            freeBody.shapeBox!!.radius.toDouble(),
            freeBody.shapeBox!!.radius.toDouble()
        )
    }

    override fun cleanup() {
        renderer.dispose()
    }
}
