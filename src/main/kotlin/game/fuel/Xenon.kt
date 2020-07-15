package game.fuel

import dI
import display.draw.TextureEnum
import display.graphic.Color
import engine.freeBody.FreeBody
import engine.freeBody.Particle
import engine.motion.Director
import game.PlayerAim
import org.jbox2d.common.Vec2
import utility.Common

class Xenon(override var lastUpdatedAt: Float, override val attachedTo: FreeBody) : Fuel {

    override var amount = 100f
    override var thrustTarget: Vec2 = attachedTo.worldBody.position
    override var isThrusting = false
    override var lastThrustStartedAt = lastUpdatedAt
    override val thrustRampUpTime = 200f
    override val updateInterval = 40f

    override val timingFunction = { value: Float -> Common.getTimingFunctionEaseIn(value) }
    override val engineEfficiency = 4f
    override val thrustMax = .75f
    override val jumpStrength = .6f

    private val thrustColor = Color("00A7D9E0")

    override fun onBurn(thrusterLocation: Vec2, throttleAmplitude: Float, exhaustDirection: Vec2) {
        val visualExhaustVelocity = exhaustDirection.mul(throttleAmplitude * engineEfficiency * 6)
        Particle("jump_thrust_${attachedTo.id}",
            dI.gameState.particles, dI.gameState.world, attachedTo.worldBody, thrusterLocation,
            visualExhaustVelocity, throttleAmplitude * 1.5f, 100f,
            TextureEnum.rcs_puff, thrustColor, dI.gameState.tickTime)
    }

}
