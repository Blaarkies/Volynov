package game.fuel

import dI
import display.draw.TextureEnum
import display.graphic.Color
import engine.freeBody.FreeBody
import engine.freeBody.Particle
import org.jbox2d.common.Vec2
import utility.Common

class RP1(override var lastUpdatedAt: Float, override val attachedTo: FreeBody) : Fuel {

    override var amount = Fuel.defaultAmount
    override var thrustTarget: Vec2 = attachedTo.worldBody.position
    override var isThrusting = false
    override var lastThrustStartedAt = lastUpdatedAt
    override val thrustRampUpTime = 500f
    override val updateInterval = 70f

    override val timingFunction = { value: Float -> Common.getTimingFunctionEaseIn(value) }
    override val engineEfficiency = 1.4f
    override val thrustMax = 6f
    override val jumpStrength = .9f

    private val thrustColorA = Color("#FFF8C9A0")
    private val thrustColorB = Color("#FC5819E0")

    override fun onBurn(thrusterLocation: Vec2, throttleAmplitude: Float, exhaustDirection: Vec2) {
        val thrustColor = if (lastUpdatedAt.rem(2f) > 1f) thrustColorA else thrustColorB

        val visualExhaustVelocity = exhaustDirection.mul(throttleAmplitude * engineEfficiency * 5f)
        Particle("jump_thrust_${attachedTo.id}",
            dI.gameState.particles, dI.gameState.world, attachedTo.worldBody.linearVelocity, thrusterLocation,
            visualExhaustVelocity, throttleAmplitude * 1.8f, 700f,
            TextureEnum.rcs_puff, thrustColor, dI.gameState.tickTime)
    }

}
