package game.fuel

import dI
import display.draw.TextureEnum
import display.graphic.Color
import engine.freeBody.FreeBody
import engine.freeBody.Particle
import engine.freeBody.Vehicle
import engine.motion.Director
import game.PlayerAim
import org.jbox2d.common.Vec2
import utility.Common

interface Fuel {

    var lastUpdatedAt: Float
    val attachedTo: FreeBody

    var amount: Float
    var thrustTarget: Vec2
    var isThrusting: Boolean
    var lastThrustStartedAt: Float
    val thrustRampUpTime: Float
    val thrustMax: Float
    val engineEfficiency: Float
    val jumpStrength: Float

    val updateInterval: Float
    val timingFunction: (value: Float) -> Float

    private fun getRestTime(tickTime: Float) = tickTime - lastUpdatedAt

    fun burn(tickTime: Float) {
        if (isThrusting && amount > 0f && getRestTime(tickTime) > updateInterval) {
            lastUpdatedAt = tickTime

            thrustTarget = dI.cameraView.getWorldLocation(dI.window.getCursorPosition())
            val directionToMouse = Director.getDirection(
                thrustTarget.x, thrustTarget.y, attachedTo.worldBody.position.x, attachedTo.worldBody.position.y)
            val exhaustDirection = Common.makeVec2Circle(directionToMouse + Common.Pi)

            val thrusterLocation = attachedTo.worldBody.position
            val throttleAmplitude = (tickTime - lastThrustStartedAt).div(thrustRampUpTime)
                .coerceIn(0f, 1f).let { timingFunction(it) }

            val momentumGained = throttleAmplitude * thrustMax * updateInterval / 50
            attachedTo.knock(attachedTo.worldBody.mass * momentumGained, directionToMouse)

            val fuelUsed = momentumGained / engineEfficiency
            amount = (amount - fuelUsed).coerceAtLeast(0f)

            onBurn(thrusterLocation, throttleAmplitude, exhaustDirection)
        }
    }

    fun onBurn(thrusterLocation: Vec2, throttleAmplitude: Float, exhaustDirection: Vec2) {
    }

    fun startThrust() {
        isThrusting = true
        lastThrustStartedAt = dI.gameState.tickTime
    }

    fun endThrust() {
        isThrusting = false
    }

    fun startJump(playerAim: PlayerAim) {
        val knockStrength = playerAim.power * jumpStrength

        val footPrintLocation = attachedTo.worldBody.position.add(
            Common.makeVec2Circle(playerAim.angle + Common.Pi).mul(attachedTo.radius))
        attachedTo.knock(knockStrength * attachedTo.worldBody.mass, playerAim.angle)

        onJump(footPrintLocation, knockStrength)
    }

    private fun onJump(footPrintLocation: Vec2, knockStrength: Float) {
        Particle("jump_launch_${attachedTo.id}", dI.gameState.particles, dI.gameState.world, attachedTo.worldBody,
            footPrintLocation, Vec2(), knockStrength * .01f, 150f,
            TextureEnum.white_pixel, jumpColor, dI.gameState.tickTime)
    }

    companion object {

        const val defaultAmount = 100f
        val thrustColor = Color.WHITE
        val jumpColor = Color.WHITE

        val descriptor = HashMap<FuelType, Descriptor>(mutableMapOf(
            Pair(FuelType.Hydrazine, Descriptor(
                "Hydrazine",
                "Free fuel with low thrust and bad efficiency")
            { a, b -> Hydrazine(a, b) }),
            Pair(FuelType.RP1, Descriptor(
                "RP - 1",
                "High thrust and medium efficiency, can launch into planetary orbit")
            { a, b -> RP1(a, b) }),
            Pair(FuelType.Xenon, Descriptor(
                "Xenon",
                "Terrible thrust and excellent efficiency, can cruise to any destination so long as gravity " +
                        "isn't in the way")
            { a, b -> Xenon(a, b) }),
            Pair(FuelType.NitrogenTetroxide, Descriptor(
                "Nitrogen Tetroxide",
                "Medium thrust and medium efficiency, can handle reliably move around moons")
            { a, b -> NitrogenTetroxide(a, b) })
        ))

        class Descriptor(val name: String, val description: String, val factory: (Float, FreeBody) -> Fuel)

        fun create(selectedFuel: FuelType?, lastUpdatedAt: Float, vehicle: Vehicle): Fuel =
            descriptor[selectedFuel]?.factory?.invoke(lastUpdatedAt, vehicle) ?: Hydrazine(lastUpdatedAt, vehicle)

    }

}

