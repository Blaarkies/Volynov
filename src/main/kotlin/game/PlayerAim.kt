package game

import utility.Common.Pi2
import utility.Common.radianToDegree

class PlayerAim(var angle: Float = 0f, power: Float = 100f) {

    var power = power
        set(value) {
            field = value.coerceIn(0f, 100f)
        }

    fun getDegreesAngle() = (angle + Pi2) % Pi2 * radianToDegree

}
