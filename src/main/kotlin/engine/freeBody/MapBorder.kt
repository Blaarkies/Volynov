package engine.freeBody

import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.BasicShapes
import display.graphic.Color
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World
import utility.Common.makeVec2
import utility.PidControllerVec2

class MapBorder(private val mapCenterBody: Body, world: World, radius: Float) {

    var worldBody: Body
    var textureConfig: TextureConfig
    private val movementController = PidControllerVec2(.3f, .001f, 2f)

    init {
        val bodyDef = FreeBody.createBodyDef(BodyType.DYNAMIC,
            mapCenterBody.position.x, mapCenterBody.position.y, 0f, 0f, 0f, 0f)
        worldBody = world.createBody(bodyDef)
        worldBody.userData = this

        val squareChunks = BasicShapes.polygon6.chunked(2)
        val fixtures = (squareChunks + listOf(squareChunks.first()))
            .windowed(2)
            .map {
                val (last, now) = it
                val distanceFromCenter = radius * 1.5f
                val borderWidth = 10f
                val lastLocation = makeVec2(last).mul(distanceFromCenter)
                val nowLocation = makeVec2(now).mul(distanceFromCenter)

                listOf(lastLocation, lastLocation.add(lastLocation.mul(borderWidth)),
                    nowLocation.add(nowLocation.mul(borderWidth)), nowLocation)
                    .toTypedArray()
            }
        fixtures.forEach { vertices ->
            val shapeBox = PolygonShape()
            shapeBox.set(vertices, vertices.size)
            FixtureDef().also {
                it.shape = shapeBox
                it.density = .0001f
                it.friction = .1f
                it.restitution = .5f
                worldBody.createFixture(it)
            }
        }

        textureConfig = TextureConfig(TextureEnum.danger, makeVec2(.7f),
            chunkedVertices = (fixtures + listOf(fixtures.first())).flatMap { fixture ->
                fixture.map { vertex -> listOf(vertex.x, vertex.y) }.subList(0, 2)
            },
            color = Color("#9010101A"))
            .updateGpuBufferData()
    }

    fun update() {
        movementController.getReaction(mapCenterBody.position, worldBody.position)
            .also { worldBody.applyLinearImpulse(it.mul(worldBody.mass), worldBody.position) }
    }
}
