package display.graphic

import Vector3f
import Vector4f

class Color(val red: Float = 0f, val green: Float = 0f, val blue: Float = 0f, val alpha: Float = 0f) {

    operator fun times(value: Float) =
        Color(this.red * value, this.green * value, this.blue * value, this.alpha * value)

    operator fun plus(color: Color) =
        Color(this.red + color.red, this.green + color.green, this.blue + color.blue, this.alpha + color.alpha)

    constructor(red: Int, green: Int, blue: Int, alpha: Int) : this(
        getIntToFloat(red),
        getIntToFloat(green),
        getIntToFloat(blue),
        getIntToFloat(alpha)
    )

    fun toVector3f(): Vector3f {
        return Vector3f(red, green, blue)
    }

    fun toVector4f(): Vector4f {
        return Vector4f(red, green, blue, alpha)
    }

    companion object {
        val WHITE = Color(1f, 1f, 1f, 1f)
        val BLACK = Color(0f, 0f, 0f, 1f)
        val RED = Color(1f, 0f, 0f, 1f)
        val GREEN = Color(0f, 1f, 0f, 1f)
        val BLUE = Color(0f, 0f, 1f, 1f)
        val TRANSPARENT = Color(0f, 0f, 0f, 0f)

        private fun getSafeValue(value: Float) = value.coerceIn(0f, 1f)

        private fun getIntToFloat(value: Int) = getSafeValue(value / 255f)
    }
}
