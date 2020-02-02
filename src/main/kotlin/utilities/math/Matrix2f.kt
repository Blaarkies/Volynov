import java.nio.FloatBuffer

class Matrix2f {

    private var m00 = 0f
    private var m01 = 0f
    private var m10 = 0f
    private var m11 = 0f

    constructor() {
        setIdentity()
    }

    constructor(col1: Vector2f, col2: Vector2f) {
        m00 = col1.x
        m10 = col1.y
        m01 = col2.x
        m11 = col2.y
    }

    fun setIdentity() {
        m00 = 1f
        m11 = 1f
        m01 = 0f
        m10 = 0f
    }

    fun add(other: Matrix2f): Matrix2f {
        val result = Matrix2f()
        result.m00 = m00 + other.m00
        result.m10 = m10 + other.m10
        result.m01 = m01 + other.m01
        result.m11 = m11 + other.m11
        return result
    }

    fun negate(): Matrix2f {
        return multiply(-1f)
    }

    fun subtract(other: Matrix2f): Matrix2f {
        return add(other.negate())
    }

    fun multiply(scalar: Float): Matrix2f {
        val result = Matrix2f()
        result.m00 = m00 * scalar
        result.m10 = m10 * scalar
        result.m01 = m01 * scalar
        result.m11 = m11 * scalar
        return result
    }

    fun multiply(vector: Vector2f): Vector2f {
        val x = m00 * vector.x + m01 * vector.y
        val y = m10 * vector.x + m11 * vector.y
        return Vector2f(x, y)
    }

    fun multiply(other: Matrix2f): Matrix2f {
        val result = Matrix2f()
        result.m00 = m00 * other.m00 + m01 * other.m10
        result.m10 = m10 * other.m00 + m11 * other.m10
        result.m01 = m00 * other.m01 + m01 * other.m11
        result.m11 = m10 * other.m01 + m11 * other.m11
        return result
    }

    fun transpose(): Matrix2f {
        val result = Matrix2f()
        result.m00 = m00
        result.m10 = m01
        result.m01 = m10
        result.m11 = m11
        return result
    }

    fun toBuffer(buffer: FloatBuffer) {
        buffer.put(m00).put(m10)
        buffer.put(m01).put(m11)
        buffer.flip()
    }

}
