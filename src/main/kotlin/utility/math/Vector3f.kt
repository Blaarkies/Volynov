package utility.math

import java.nio.FloatBuffer

class Vector3f {

    var x: Float
    var y: Float
    var z: Float

    constructor() {
        x = 0f
        y = 0f
        z = 0f
    }

    constructor(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun lengthSquared(): Float {
        return x * x + y * y + z * z
    }

    fun length(): Float {
        return Math.sqrt(lengthSquared().toDouble()).toFloat()
    }

    fun normalize(): Vector3f {
        val length = length()
        return divide(length)
    }

    fun add(other: Vector3f): Vector3f {
        val x = x + other.x
        val y = y + other.y
        val z = z + other.z
        return Vector3f(x, y, z)
    }

    fun negate(): Vector3f {
        return scale(-1f)
    }

    fun subtract(other: Vector3f): Vector3f {
        return add(other.negate())
    }

    fun scale(scalar: Float): Vector3f {
        val x = x * scalar
        val y = y * scalar
        val z = z * scalar
        return Vector3f(x, y, z)
    }

    fun divide(scalar: Float): Vector3f {
        return scale(1f / scalar)
    }

    fun dot(other: Vector3f): Float {
        return x * other.x + y * other.y + z * other.z
    }

    fun cross(other: Vector3f): Vector3f {
        val x = y * other.z - z * other.y
        val y = z * other.x - this.x * other.z
        val z = this.x * other.y - this.y * other.x
        return Vector3f(x, y, z)
    }

    fun lerp(other: Vector3f, alpha: Float): Vector3f {
        return scale(1f - alpha).add(other.scale(alpha))
    }

    fun toBuffer(buffer: FloatBuffer) {
        buffer.put(x).put(y).put(z)
        buffer.flip()
    }

}
