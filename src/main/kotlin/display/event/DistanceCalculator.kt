package display.event

import org.jbox2d.common.Vec2

class DistanceCalculator {

    private var lastLocation: Vec2? = null

    fun getLastDistance(location: Vec2): Vec2 {
        if (lastLocation == null) {
            lastLocation = location.clone()
        }
        val movement = location.sub(lastLocation)
        lastLocation?.set(location)
        return movement
    }

}
