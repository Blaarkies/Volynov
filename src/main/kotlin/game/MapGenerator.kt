package game

import Vector2f
import display.draw.TextureConfig
import display.draw.TextureHolder
import display.graphic.BasicShapes
import engine.GameState
import engine.freeBody.Planet
import engine.freeBody.Vehicle
import engine.motion.Director
import org.jbox2d.dynamics.World
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object MapGenerator {

    fun populateTestMap(gameState: GameState, world: World, textures: TextureHolder) {
        val terra = Planet.create(
            world, "terra", 0f, 0f, 0f, 0f, 0f, .0f, 1800f, 90f, .3f,
            textureConfig = TextureConfig(
                textures.marble_earth,
                Vector2f(1f, 1f),
                Vector2f(0f, 0f),
                BasicShapes.polygon30.chunked(2)
            )
        )
        val luna = Planet.create(
            world, "luna", 500f, 0f, 0f, 0f, -250f, -2f, 100f, 25f, .5f,
            textureConfig = TextureConfig(
                textures.pavement,
                Vector2f(2f, 2f),
                Vector2f(0f, 0f),
                BasicShapes.polygon30.chunked(2)
            )
        )
        val alice = Vehicle.create(
            world, "alice", -500f, 330f, 0f, 1500f, 0f, 0f, 3f, radius = 15f,
            textureConfig = TextureConfig(textures.metal, Vector2f(.05f, .05f), Vector2f(0f, 0f), listOf())
        )

        gameState.vehicles.add(alice)
        gameState.planets.addAll(listOf(terra, luna))
        gameState.planets.addAll(createPlanets(world, 7, textures))

        gameState.tickables.forEach { it.textureConfig.updateGpuBufferData() }
    }

    private fun createPlanets(world: World, count: Int, textures: TextureHolder): List<Planet> {
        return (1..count)
            .withIndex()
            .map { (i, _) ->
                val ratio = (2 * PI * 0.07 * i).toFloat()
                val radius = 300
                floatArrayOf(
                    (i * .4f + radius) * cos(ratio),
                    (i * .4f + radius) * sin(ratio),
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
                    .5f, 0.3f * it[2].rem(6f), 4f + it[2].rem(6f),
                    friction = .6f,
                    textureConfig = TextureConfig(
                        textures.pavement,
                        Vector2f(.3f, .3f),
                        Vector2f(it[0].rem(20f) / 10 - 1, it[1].rem(20f) / 10 - 1),
                        BasicShapes.polygon9.chunked(2)
                    )
                )
            }
    }

}
