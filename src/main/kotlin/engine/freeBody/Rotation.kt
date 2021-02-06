package engine.freeBody

class Rotation(var y: Float = 0f, var dy: Float = 0f) {

    fun update() {
        y += dy
    }

}
