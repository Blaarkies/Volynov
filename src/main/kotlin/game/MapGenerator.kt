package game

import display.draw.TextureEnum
import display.graphic.Color
import engine.GameState
import engine.freeBody.MapBorder
import engine.freeBody.Planet
import engine.freeBody.Vehicle
import engine.motion.Director
import engine.physics.Gravity
import engine.physics.LocationVelocity
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World
import utility.Common.getRandomDirection
import utility.Common.getRandomMixed
import utility.Common.getRandomSign
import utility.Common.makeVec2Circle
import java.lang.Math.random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

object MapGenerator {

    fun populateNewGameMap(gameState: GameState) {
        val planet = Planet("terra", gameState.planets, gameState.world, 0f, 0f, 0f, 0f, 0f, .1f * getRandomSign(),
            1600f, 4.5f, texture = TextureEnum.marble_earth)

        val orbitalLevels = (1 until gameState.gamePlayers.size).map { OrbitalLevel(it, it) }
        val moons = gameState.gamePlayers.map {
            val (x, y, h, dx, dy, dh) = getSafeOrbitLocationVelocity(orbitalLevels, planet)
            Planet("luna of ${it.name}", gameState.planets, gameState.world, x, y, h, dx, dy, dh, 50f, 1.25f,
                texture = TextureEnum.full_moon)
        }

        gameState.gamePlayers
            .zip(Color.PALETTE_TINT10.shuffled())
            .zip(moons.shuffled())
            .map { (playerColor, moon) ->
                val (player, color) = playerColor
                val location = moon.worldBody.position.add(makeVec2Circle(getRandomDirection()).mul(moon.radius + 1f))
                val velocity = moon.worldBody.linearVelocity

                Vehicle(gameState.vehicles, gameState.world, player,
                    location.x, location.y, 0f,
                    velocity.x, velocity.y, getRandomMixed(),
                    3f, texture = TextureEnum.metal, color = color)
            }

        createAsteroids(gameState.planets, gameState.world, 20, planet)

        val largestOrbitRadius =
            gameState.planets.map { Director.getDistance(planet.worldBody, it.worldBody) }.max() ?: planet.radius
        gameState.mapBorder = MapBorder(planet.worldBody, gameState.world, largestOrbitRadius)
    }

    private fun getSafeOrbitLocationVelocity(orbitalLevels: List<OrbitalLevel>, parent: Planet): LocationVelocity {
        val level = orbitalLevels.first { it.hasSpace }.also { it.addBody() }

        val distance = (1.25f + parent.radius).times(level.index).pow(1.6f).plus(5f)
        val meanLongitude = level.nowDirection
        val location = makeVec2Circle(meanLongitude).mul(distance)

        val direction = meanLongitude + PI.toFloat() / 2f
        val velocity = Gravity.getVelocityToOrbitParent(location, direction, parent)

        return LocationVelocity(location.x, location.y, getRandomMixed(),
            velocity.x, velocity.y, getRandomMixed())
    }

    private fun createAsteroids(planets: MutableList<Planet>,
                                world: World,
                                count: Int,
                                parent: Planet): List<Planet> {
        return (1..count)
            .withIndex()
            .map { (i, _) ->
                val meanLongitude = (2 * PI * .07 * i).toFloat()
                val beltRadius = 50f
                val dispersion = .8f
                floatArrayOf(
                    (i * dispersion + beltRadius) * cos(meanLongitude),
                    (i * dispersion + beltRadius) * sin(meanLongitude),
                    i.toFloat()
                )
            }
            .map {
                val (x, y, index) = it
                val parentLocation = parent.worldBody.position
                val direction = Director.getDirection(x, y, parentLocation.x, parentLocation.y) + PI.toFloat() / 2f
                val velocity = Gravity.getVelocityToOrbitParent(Vec2(x, y), direction, parent)
                Planet("asteroid ${index.toInt()}", planets, world, x, y, 0f,
                    velocity.x, velocity.y, getRandomMixed(),
                    .2f * index.rem(6f), .2f + index.rem(6f) * .05f,
                    .5f, .6f, TextureEnum.pavement, true
                )
            }
    }

}
