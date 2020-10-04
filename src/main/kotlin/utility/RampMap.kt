package utility

import kotlin.math.pow

object RampMap {

    fun parabola(x: Float, weight: Int = 1) = 1 - (x - 1).pow(weight * 2)

}
