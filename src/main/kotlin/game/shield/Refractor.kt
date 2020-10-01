package game.shield

import dI
import display.draw.TextureConfig
import display.graphic.Color
import engine.freeBody.Vehicle
import engine.motion.Director
import game.GamePlayer
import io.reactivex.Observable.just
import org.jbox2d.dynamics.Body
import utility.Common
import utility.Common.Pi
import utility.Common.PiE
import utility.Common.PiQ
import utility.Common.getRandom
import utility.Common.getRandomMixed
import utility.Common.getRandomSign
import utility.Common.makeVec2Circle
import utility.RampMap
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class Refractor(override val attachedTo: Vehicle) : VehicleShield {

    override val key = ShieldType.Refractor.toString()
    override lateinit var worldBody: Body
    override var energy = VehicleShield.defaultEnergyAmount
        set(value) {
            field = value
            if (field <= 0f) dispose()
        }
    override val radius = VehicleShield.defaultSize
    override lateinit var textureConfig: TextureConfig
    override val lastHits = mutableListOf<ShieldHit>()
    override val color = Color.WHITE

    init {
        just(0)
            .delay(1000, TimeUnit.MILLISECONDS)
            .subscribe {
                val body = attachedTo.worldBody

                val maxGravityInfluence = dI.gameState
                    .gravityBodies
                    .filter { it != attachedTo }
                    .map {
                        it.worldBody.position
                            .sub(body.position)
                            .let { difference -> difference.mul(it.worldBody.mass / difference.length().pow(2f)) }
                    }
                    .let { list -> list.maxBy { it.length() } }
                    ?: makeVec2Circle(Common.getRandomDirection())

                attachedTo.knock(
                    body.mass * maxGravityInfluence.length() * .36f
                            * (1f + getRandomMixed() * .05f), // 10% power deviation
                    Director.getDirection(maxGravityInfluence)
                            + Pi // reverse direction
                            + RampMap.parabola(getRandom()) * getRandomSign() * PiQ) // +- 45º
            }
    }

    override fun render() {
    }

    override fun setShieldOnTurn() {
    }

    override fun setShieldEndTurn() {
    }

    override fun blockDamage(amount: Float, firedBy: GamePlayer): Float {
        attachedTo.shield = null
        return super.blockDamage(amount, firedBy);
    }

}

