package engine.physics

object CollisionBits {

    private var nextBit = 1
        get() {
            val preValue = field
            field *= 2
            return preValue
        }

    val shield = nextBit
    val planet = nextBit
    val vehicle = nextBit
    val warhead = nextBit
    val border = nextBit
    val onTurnShield = nextBit
    val onTurnVehicle = nextBit
    val onTurnWarhead = nextBit

    val planetVehicleWarhead = planet or vehicle or warhead

}
