package engine.motion

class Velocity(var dx: Double = .0, var dy: Double = .0, var dh: Double = .0) {

    val magnitude
        get() = Director.getMagnitude(dx, dy)

    val direction: Double
        get() = Director.getDirection(dx, dy)

    /**
     * Inertial frame of reference ON the slow moving planet. How fast is the satellite moving towards the planet?
     * As fast as the planet, in the opposite direction as the planet (-dx) + the satellite's velocity
     */
    fun getRelativeVelocity(client: Velocity): Velocity =
        Velocity(-dx + client.dx, -dy + client.dy, -dh + client.dh)

    fun addVelocity(dx: Double, dy: Double, dh: Double = .0) {
        this.dx += dx
        this.dy += dy
        this.dh += dh
    }
}
