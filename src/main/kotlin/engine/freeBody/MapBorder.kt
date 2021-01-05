package engine.freeBody

import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.vertex.BasicShapes
import display.graphic.Color
import engine.physics.CollisionBits
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.*
import org.jbox2d.dynamics.joints.MouseJoint
import org.jbox2d.dynamics.joints.MouseJointDef
import utility.Common.makeVec2
import utility.PidControllerVec2

class MapBorder(val mapCenterBody: FreeBody, world: World, val radius: Float) {

    var worldBody: Body
    var textureConfig: TextureConfig

    init {
        val center = mapCenterBody.worldBody.position
        val bodyDef = FreeBody.createBodyDef(BodyType.DYNAMIC,
            center.x, center.y, 0f, 0f, 0f, 0f)
        worldBody = world.createBody(bodyDef)
        worldBody.userData = this

        val squareChunks = BasicShapes.polygon6
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
                it.density = .00001f
                it.friction = .1f
                it.restitution = .5f
                worldBody.angularDamping = .5f

                it.filter.categoryBits = CollisionBits.border
                it.filter.maskBits = CollisionBits.planetVehicleWarhead
                worldBody.createFixture(it)
            }
        }

        MouseJointDef().also {
            it.target.set(worldBody.position)
            it.bodyA = worldBody
            it.bodyB = worldBody
            it.maxForce = worldBody.mass * 100f
            it.dampingRatio = .9f

            world.createJoint(it)
        }

        textureConfig = TextureConfig(TextureEnum.danger, makeVec2(.7f),
            chunkedVertices = (fixtures + listOf(fixtures.first())).flatMap { fixture ->
                fixture.map { vertex -> listOf(vertex.x, vertex.y) }.subList(0, 2)
            },
            color = Color("#9010101A"))
            .updateGpuBufferData()
    }

    fun update() {
        (worldBody.jointList.joint as MouseJoint).target = mapCenterBody.worldBody.position
    }

    fun clone(mapCenterBody: FreeBody, world: World): MapBorder = MapBorder(mapCenterBody, world, radius)

}
