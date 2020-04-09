package game

import Vector2f
import display.draw.TextureConfig
import display.draw.TextureEnum
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

    fun populateNewGameMap(gameState: GameState) {
        val terra = Planet.create(
            gameState.world, "terra", 0f, 0f, 0f, 0f, 0f, .1f, 1800f, 4.5f, .3f,
            textureConfig = TextureConfig(TextureEnum.marble_earth, chunkedVertices = BasicShapes.polygon30.chunked(2))
        )
        val luna = Planet.create(
            gameState.world, "luna", -20f, 0f, 0f, 0f, 4.8f, -.4f, 100f, 1.25f, .5f,
            textureConfig = TextureConfig(TextureEnum.full_moon, chunkedVertices = BasicShapes.polygon30.chunked(2))
        )

        val vehicles = gameState.gamePlayers.withIndex().map { (index, player) ->
            Vehicle.create(
                gameState.world, player,-10f * index + .5f * gameState.gamePlayers.size,
                10f, index * 1f, 0f, 0f, 1f, 3f, radius = .75f,
                textureConfig = TextureConfig(TextureEnum.metal, Vector2f(.7f, .7f), Vector2f(0f, 0f))
            )
        }

        gameState.vehicles.addAll(vehicles)
        gameState.planets.addAll(listOf(terra, luna))
        gameState.planets.addAll(createPlanets(gameState.world, 5))

        gameState.gravityBodies.forEach { it.textureConfig.updateGpuBufferData() }
    }

    fun populateTestMap(gameState: GameState) {
        val terra = Planet.create(
            gameState.world, "terra", 0f, 0f, 0f, 0f, 0f, .1f, 1800f, 4.5f, .3f,
            textureConfig = TextureConfig(
                TextureEnum.marble_earth,
                Vector2f(1f, 1f),
                Vector2f(0f, 0f),
                BasicShapes.polygon30.chunked(2)
            )
        )
        val luna = Planet.create(
            gameState.world, "luna", -20f, 0f, 0f, 0f, 4.4f, -.4f, 100f, 1.25f, .5f,
            textureConfig = TextureConfig(
                TextureEnum.full_moon,
                Vector2f(1f, 1f),
                Vector2f(0f, 0f),
                BasicShapes.polygon30.chunked(2)
            )
        )
        val alice = Vehicle.create(
            gameState.world, GamePlayer("alice"), -30f, 5f, 0f, -2f, 2.7f, 1f, 3f, radius = .75f,
            textureConfig = TextureConfig(TextureEnum.metal, Vector2f(.7f, .7f), Vector2f(0f, 0f), listOf())
        )
        val bob = Vehicle.create(
            gameState.world, GamePlayer("bob"), 25f, 0f, 0f, 2f, -3f, 0f, 3f, radius = .75f,
            textureConfig = TextureConfig(TextureEnum.metal, Vector2f(.7f, .7f), Vector2f(0f, 0f), listOf())
        )

        gameState.vehicles.addAll(listOf(alice, bob))
        gameState.planets.addAll(listOf(terra, luna))
        gameState.planets.addAll(createPlanets(gameState.world, 20))

        gameState.gravityBodies.forEach { it.textureConfig.updateGpuBufferData() }
    }

    private fun createPlanets(world: World, count: Int): List<Planet> {
        return (1..count)
            .withIndex()
            .map { (i, _) ->
                val ratio = (2 * PI * 0.07 * i).toFloat()
                val radius = 15f
                floatArrayOf(
                    (i * .04f + radius) * cos(ratio),
                    (i * .04f + radius) * sin(ratio),
                    i.toFloat()
                )
            }
            .map {
                val direction = Director.getDirection(-it[0], -it[1]) + PI * .5f
                val speed = 7f
                Planet.create(
                    world, "${it[2].toInt()}", it[0], it[1], 0f,
                    cos(direction).toFloat() * speed,
                    sin(direction).toFloat() * speed,
                    .5f, 0.3f * it[2].rem(6f), .2f + it[2].rem(6f) * .05f,
                    friction = .6f,
                    textureConfig = TextureConfig(
                        TextureEnum.pavement,
                        Vector2f(.3f, .3f),
                        Vector2f(it[0].rem(20f) / 10 - 1, it[1].rem(20f) / 10 - 1),
                        BasicShapes.polygon9.chunked(2)
                    )
                )
            }
    }

}
