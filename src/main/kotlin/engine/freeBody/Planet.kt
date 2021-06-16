package engine.freeBody

import display.draw.Model
import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.CameraType
import display.graphic.vertex.BasicShapes
import display.graphic.vertex.BasicSurfaces
import display.graphic.vertex.MapProjectionType
import engine.physics.CollisionBits
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World
import org.joml.Vector3f
import utility.Common.PiH
import utility.Common.getTimingFunctionEaseIn
import utility.Common.getTimingFunctionEaseOut
import utility.Common.getTimingFunctionFullSine
import utility.Common.makeVec2
import java.lang.Math.random

class Planet(
    id: String,
    planets: MutableList<Planet>,
    world: World,
    x: Float,
    y: Float,
    h: Float,
    dx: Float,
    dy: Float,
    dh: Float,
    mass: Float,
    radius: Float,
    restitution: Float = .5f,
    friction: Float = .3f,
    texture: TextureEnum,
    isAsteroid: Boolean = false
) : FreeBody(id, radius) {

    init {
        val shapeBox = CircleShape()
        shapeBox.radius = radius

        val bodyDef = createBodyDef(BodyType.DYNAMIC, x, y, h, dx, dy, dh)
        worldBody = createWorldBody(
            shapeBox, mass, radius, friction, restitution,
            world, bodyDef,
            CollisionBits.planet,
            CollisionBits.planetVehicleWarhead
        )

        textureConfig =
            when (isAsteroid) {
                true -> TextureConfig(
                    texture, makeVec2(.3f), Vec2(random().toFloat(), random().toFloat()),
                    BasicShapes.polygon15
                )
                false -> TextureConfig(texture, chunkedVertices = BasicShapes.polygon30)
            }
                .updateGpuBufferData()

        planets.add(this)


        model = Model(
            BasicSurfaces.icosahedron.let {
                BasicSurfaces.subdivide(
                    it,
                    when {
                        radius > 5 -> 2
                        radius > .3 -> 1
                        else -> 0
                    }
                )
                    .also {
                        BasicSurfaces.setVertexTexture(it.flatMap { it.vertices },
                            MapProjectionType.Mercator)
                    }
                    .also {
                        // todo: don't deform planets
                        val centerVertex = it.flatMap { it.vertices }
                            .distinct()
                            .minByOrNull { it.location.distance(5f, -2f, 3f) }!!

                        it.flatMap { it.vertices }
                            .map { Pair(it, it.location.distance(centerVertex.location)) }
                            .forEach { (vertex, distance) ->
                                val ratio = (distance / 2f)
                                vertex.location.mul(
                                    getTimingFunctionEaseOut(ratio).coerceIn(.3f, 1f)
                                )
                                vertex.textureDepth = vertex.location.length()
                            }
                    }
            },
            listOf(
                TextureEnum.mercator_color,
                TextureEnum.metal,
                TextureEnum.danger,
            ),
            radius,
            CameraType.UNIVERSE,
        )
    }

    fun clone(planets: MutableList<Planet>, world: World): Planet {
        val body = worldBody
        return Planet(
            id, planets, world,
            body.position.x, body.position.y, body.angle,
            body.linearVelocity.x, body.linearVelocity.y, body.angularVelocity,
            body.mass, model.scale,
            body.fixtureList.restitution, body.fixtureList.friction,
            textureConfig.texture
        )
            .also { newPlanet -> newPlanet.textureConfig = textureConfig.clone() }
    }

}
