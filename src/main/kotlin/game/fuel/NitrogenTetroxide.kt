package game.fuel

import dI
import display.draw.TextureEnum
import display.graphic.Color
import engine.freeBody.FreeBody
import engine.freeBody.Particle
import org.jbox2d.common.Vec2
import utility.Common

class NitrogenTetroxide(override var lastUpdatedAt: Float, override val attachedTo: FreeBody) : Fuel {

    override var amount = Fuel.defaultAmount
    override var thrustTarget: Vec2 = attachedTo.worldBody.position
    override var isThrusting = false
    override var lastThrustStartedAt = lastUpdatedAt
    override val thrustRampUpTime = 700f
    override val updateInterval = 100f

    override val timingFunction = { value: Float -> Common.getTimingFunctionEaseIn(value) }
    override val engineEfficiency = 1.2f
    override val thrustMax = 2.0f
    override val jumpStrength = .80f

    private val thrustColor = Color("#F6BD92E0")

    override fun onBurn(thrusterLocation: Vec2, throttleAmplitude: Float, exhaustDirection: Vec2) {
        val visualExhaustVelocity = exhaustDirection.mul(throttleAmplitude * engineEfficiency * 7f)
        Particle("jump_thrust_${attachedTo.id}",
            dI.gameState.particles, dI.gameState.world, attachedTo.worldBody, thrusterLocation,
            visualExhaustVelocity, throttleAmplitude * .8f, 400f,
            TextureEnum.rcs_puff, thrustColor, dI.gameState.tickTime)
    }

}
