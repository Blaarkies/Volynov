package engine.motion

class Location(var x: Double = .0, var y: Double = .0, var h: Double = .0) {

    fun getDistance(client: Location): Double = Director.getDistance(x, y, client.x, client.y)

    fun getDirection(client: Location): Double = Director.getDirection(x, y, client.x, client.y)

    fun add(x: Double, y: Double, heading: Double = .0) {
        this.x += x
        this.y += y
        this.h += heading
    }

}
