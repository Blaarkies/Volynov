package game

import display.draw.TextureEnum
import display.graphic.Color
import engine.GameState
import engine.freeBody.Planet
import engine.freeBody.Vehicle
import engine.motion.Director
import org.jbox2d.dynamics.World
import java.lang.Math.random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object MapGenerator {

    fun populateNewGameMap(gameState: GameState) {
        Planet("terra", gameState.planets, gameState.world, 0f, 0f, 0f, 0f, 0f, .1f, 1600f, 4.5f,
            texture = TextureEnum.marble_earth)
        Planet("luna", gameState.planets, gameState.world, -30f, 0f, 0f, 0f, 4.5f, -.4f, 50f, 1.25f,
            texture = TextureEnum.full_moon)

        gameState.gamePlayers
            .zip(Color.PALETTE_TINT10.shuffled())
            .withIndex()
            .map { (index, playerColor) ->
                val (player, color) = playerColor
                Vehicle(gameState.vehicles, gameState.world, player, -10f * index + .5f * gameState.gamePlayers.size,
                    10f, random().toFloat(), -2f, 3f, 1f, 3f, texture = TextureEnum.metal, color = color)
            }

        createAsteroids(gameState.planets, gameState.world, 20)
    }

    private fun createAsteroids(planets: MutableList<Planet>, world: World, count: Int): List<Planet> {
        return (1..count)
            .withIndex()
            .map { (i, _) ->
                val ratio = (2 * PI * 0.07 * i).toFloat()
                val radius = 30f
                floatArrayOf(
                    (i * .04f + radius) * cos(ratio),
                    (i * .04f + radius) * sin(ratio),
                    i.toFloat()
                )
            }
            .map {
                val (x, y, index) = it
                val direction = Director.getDirection(-x, -y) + PI * .5f
                val speed = 4.8f
                Planet("${index.toInt()}", planets, world, x, y, 0f,
                    cos(direction).toFloat() * speed, sin(direction).toFloat() * speed,
                    random().toFloat(), .2f * index.rem(6f), .2f + index.rem(6f) * .05f,
                    .5f, .6f, TextureEnum.pavement, true
                )
            }
    }

}
