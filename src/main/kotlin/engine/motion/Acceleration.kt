package engine.motion

class Acceleration(var ddx: Double = .0, var ddy: Double = .0, var ddh: Double = .0) {

    val magnitude: Double
        get() = Director.getMagnitude(ddx, ddy)

    val direction: Double
        get() = Director.getDirection(ddx, ddy)

    fun getRelative(client: Acceleration): Acceleration =
        Acceleration(-ddx + client.ddx, -ddy + client.ddy, -ddh + client.ddh)

    fun add(ddx: Double, ddy: Double, ddh: Double = .0) {
        this.ddx += ddx
        this.ddy += ddy
        this.ddh += ddh
    }

    fun add(acceleration: Acceleration) {
        this.ddx += acceleration.ddx
        this.ddy += acceleration.ddy
        this.ddh += acceleration.ddh
    }

    fun add(force: Force, mass: Double) = add(force.x / mass, force.y / mass)

    fun remove() {
        ddx = .0
        ddy = .0
        ddh = .0
    }
}

