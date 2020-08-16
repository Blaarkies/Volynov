package engine.freeBody

import display.draw.TextureConfig
import display.draw.TextureEnum
import display.graphic.vertex.BasicShapes
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World
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
        worldBody = createWorldBody(shapeBox, mass, radius, friction, restitution, world, bodyDef)

        textureConfig =
            when (isAsteroid) {
                true -> TextureConfig(texture, makeVec2(.3f), Vec2(random().toFloat(), random().toFloat()),
                    BasicShapes.polygon15.chunked(2))
                false -> TextureConfig(texture, chunkedVertices = BasicShapes.polygon30.chunked(2))
            }
                .updateGpuBufferData()

        planets.add(this)
    }

    fun clone(planets: MutableList<Planet>, world: World): Planet {
        val body = worldBody
        return Planet(id, planets, world,
            body.position.x, body.position.y, body.angle,
            body.linearVelocity.x, body.linearVelocity.y, body.angularVelocity,
            body.mass, radius,
            body.fixtureList.restitution, body.fixtureList.friction,
            textureConfig.texture)
            .also { newPlanet -> newPlanet.textureConfig = textureConfig.clone() }
    }

}
