package game

import utility.Common
import kotlin.math.PI

class VehiclePlacement(private val space: Int) {

    private var bodyCount = 0
    private val randomDirection = Common.getRandomDirection()

    val nowDirection: Float
        get() {
            check(bodyCount > 0) { "Must addBody() before using nowDirection" }
            return (bodyCount.toFloat() / space.toFloat()) * 2f * PI.toFloat() + randomDirection
        }

    fun addBody() {
        bodyCount++
    }

}
