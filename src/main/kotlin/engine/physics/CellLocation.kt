package engine.physics

data class CellLocation(val x: Int, val y: Int) {

    operator fun times(value: CellLocation) = CellLocation(x * value.x, y * value.y)
    
}
