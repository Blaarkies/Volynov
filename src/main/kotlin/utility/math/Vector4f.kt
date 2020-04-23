package utility.math

import java.nio.FloatBuffer

class Vector4f {

    var x: Float
    var y: Float
    var z: Float
    var w: Float

    constructor() {
        x = 0f
        y = 0f
        z = 0f
        w = 0f
    }

    constructor(x: Float, y: Float, z: Float, w: Float) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    fun lengthSquared(): Float {
        return x * x + y * y + z * z + w * w
    }

    fun length(): Float {
        return Math.sqrt(lengthSquared().toDouble()).toFloat()
    }

    fun normalize(): Vector4f {
        val length = length()
        return divide(length)
    }

    fun add(other: Vector4f): Vector4f {
        val x = x + other.x
        val y = y + other.y
        val z = z + other.z
        val w = w + other.w
        return Vector4f(x, y, z, w)
    }

    fun negate(): Vector4f {
        return scale(-1f)
    }

    fun subtract(other: Vector4f): Vector4f {
        return add(other.negate())
    }

    fun scale(scalar: Float): Vector4f {
        val x = x * scalar
        val y = y * scalar
        val z = z * scalar
        val w = w * scalar
        return Vector4f(x, y, z, w)
    }

    fun divide(scalar: Float): Vector4f {
        return scale(1f / scalar)
    }

    fun dot(other: Vector4f): Float {
        return x * other.x + y * other.y + z * other.z + w * other.w
    }

    fun lerp(other: Vector4f, alpha: Float): Vector4f {
        return scale(1f - alpha).add(other.scale(alpha))
    }

    fun toBuffer(buffer: FloatBuffer) {
        buffer.put(x).put(y).put(z).put(w)
        buffer.flip()
    }
}
