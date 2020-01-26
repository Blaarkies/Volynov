import java.nio.FloatBuffer

class Vector2f {

    var x: Float
    var y: Float

    constructor() {
        x = 0f
        y = 0f
    }

    constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun lengthSquared(): Float {
        return x * x + y * y
    }

    fun length(): Float {
        return Math.sqrt(lengthSquared().toDouble()).toFloat()
    }

    fun normalize(): Vector2f {
        val length = length()
        return divide(length)
    }

    fun add(other: Vector2f): Vector2f {
        val x = x + other.x
        val y = y + other.y
        return Vector2f(x, y)
    }

    fun negate(): Vector2f {
        return scale(-1f)
    }

    fun subtract(other: Vector2f): Vector2f {
        return add(other.negate())
    }

    fun scale(scalar: Float): Vector2f {
        val x = x * scalar
        val y = y * scalar
        return Vector2f(x, y)
    }

    fun divide(scalar: Float): Vector2f {
        return scale(1f / scalar)
    }

    fun dot(other: Vector2f): Float {
        return x * other.x + y * other.y
    }

    fun lerp(other: Vector2f, alpha: Float): Vector2f {
        return scale(1f - alpha).add(other.scale(alpha))
    }

    fun toBuffer(buffer: FloatBuffer) {
        buffer.put(x).put(y)
        buffer.flip()
    }

}
