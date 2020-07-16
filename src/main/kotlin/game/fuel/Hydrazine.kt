package game.fuel

import dI
import display.draw.TextureEnum
import engine.freeBody.FreeBody
import engine.freeBody.Particle
import org.jbox2d.common.Vec2
import utility.Common

class Hydrazine(override var lastUpdatedAt: Float, override val attachedTo: FreeBody) : Fuel {

    override var amount = Fuel.defaultAmount
    override var thrustTarget: Vec2 = attachedTo.worldBody.position
    override var isThrusting = false
    override var lastThrustStartedAt = lastUpdatedAt
    override val thrustRampUpTime = 500f
    override val updateInterval = 300f

    override val timingFunction = { value: Float -> Common.getTimingFunctionEaseIn(value) }
    override val engineEfficiency = .55f
    override val thrustMax = 1.1f
    override val jumpStrength = .75f

    override fun onBurn(thrusterLocation: Vec2, throttleAmplitude: Float, exhaustDirection: Vec2) {
        val visualExhaustVelocity = exhaustDirection.mul(throttleAmplitude * engineEfficiency * 5f)
        Particle("jump_thrust_${attachedTo.id}",
            dI.gameState.particles, dI.gameState.world, attachedTo.worldBody, thrusterLocation,
            visualExhaustVelocity, throttleAmplitude * .8f, 500f,
            TextureEnum.rcs_puff, Fuel.thrustColor, dI.gameState.tickTime)
    }

}
