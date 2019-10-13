package engine.motion

class Force(var x: Double = .0, var y: Double = .0) {

    val magnitude
        get() = Director.getMagnitude(x, y)

    val direction: Double
        get() = Director.getDirection(x, y)

    fun sqr(number: Double): Double {
        return number * number
    }

    fun addForce(force: Force) {
        x += force.x
        y += force.y
    }

}
