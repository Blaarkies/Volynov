package game

import utility.Common
import utility.Common.Pi2

class OrbitalLevel(index: Int, private val space: Int) {

    val index: Float = index.toFloat()
    private var bodyCount = 0
    private val randomDirection = Common.getRandomDirection()
    val hasSpace
        get() = bodyCount < space

    val nowDirection: Float
        get() {
            check(bodyCount > 0) { "Must addBody() before using nowDirection" }
            return (bodyCount.toFloat() / space.toFloat()) * Pi2 + randomDirection
        }

    fun addBody() {
        bodyCount++
    }

}
