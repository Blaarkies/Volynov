package engine.physics

import engine.freeBody.FreeBody

class GravityCell(var totalMass: Float = 0f, val freeBodies: MutableList<FreeBody> = mutableListOf())

data class CellLocation(val x: Int, val y: Int) {
    operator fun times(value: CellLocation) = CellLocation(x * value.x, y * value.y)
}
