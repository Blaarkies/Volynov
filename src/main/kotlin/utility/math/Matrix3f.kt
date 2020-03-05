import java.nio.FloatBuffer

class Matrix3f {

    private var m00 = 0f
    private var m01 = 0f
    private var m02 = 0f
    private var m10 = 0f
    private var m11 = 0f
    private var m12 = 0f
    private var m20 = 0f
    private var m21 = 0f
    private var m22 = 0f

    constructor() {
        setIdentity()
    }

    constructor(col1: Vector3f, col2: Vector3f, col3: Vector3f) {
        m00 = col1.x
        m10 = col1.y
        m20 = col1.z
        m01 = col2.x
        m11 = col2.y
        m21 = col2.z
        m02 = col3.x
        m12 = col3.y
        m22 = col3.z
    }

    fun setIdentity() {
        m00 = 1f
        m11 = 1f
        m22 = 1f
        m01 = 0f
        m02 = 0f
        m10 = 0f
        m12 = 0f
        m20 = 0f
        m21 = 0f
    }

    fun add(other: Matrix3f): Matrix3f {
        val result = Matrix3f()
        result.m00 = m00 + other.m00
        result.m10 = m10 + other.m10
        result.m20 = m20 + other.m20
        result.m01 = m01 + other.m01
        result.m11 = m11 + other.m11
        result.m21 = m21 + other.m21
        result.m02 = m02 + other.m02
        result.m12 = m12 + other.m12
        result.m22 = m22 + other.m22
        return result
    }

    fun negate(): Matrix3f {
        return multiply(-1f)
    }

    fun subtract(other: Matrix3f): Matrix3f {
        return add(other.negate())
    }

    fun multiply(scalar: Float): Matrix3f {
        val result = Matrix3f()
        result.m00 = m00 * scalar
        result.m10 = m10 * scalar
        result.m20 = m20 * scalar
        result.m01 = m01 * scalar
        result.m11 = m11 * scalar
        result.m21 = m21 * scalar
        result.m02 = m02 * scalar
        result.m12 = m12 * scalar
        result.m22 = m22 * scalar
        return result
    }

    fun multiply(vector: Vector3f): Vector3f {
        val x = m00 * vector.x + m01 * vector.y + m02 * vector.z
        val y = m10 * vector.x + m11 * vector.y + m12 * vector.z
        val z = m20 * vector.x + m21 * vector.y + m22 * vector.z
        return Vector3f(x, y, z)
    }

    fun multiply(other: Matrix3f): Matrix3f {
        val result = Matrix3f()
        result.m00 = m00 * other.m00 + m01 * other.m10 + m02 * other.m20
        result.m10 = m10 * other.m00 + m11 * other.m10 + m12 * other.m20
        result.m20 = m20 * other.m00 + m21 * other.m10 + m22 * other.m20
        result.m01 = m00 * other.m01 + m01 * other.m11 + m02 * other.m21
        result.m11 = m10 * other.m01 + m11 * other.m11 + m12 * other.m21
        result.m21 = m20 * other.m01 + m21 * other.m11 + m22 * other.m21
        result.m02 = m00 * other.m02 + m01 * other.m12 + m02 * other.m22
        result.m12 = m10 * other.m02 + m11 * other.m12 + m12 * other.m22
        result.m22 = m20 * other.m02 + m21 * other.m12 + m22 * other.m22
        return result
    }

    fun transpose(): Matrix3f {
        val result = Matrix3f()
        result.m00 = m00
        result.m10 = m01
        result.m20 = m02
        result.m01 = m10
        result.m11 = m11
        result.m21 = m12
        result.m02 = m20
        result.m12 = m21
        result.m22 = m22
        return result
    }

    fun toBuffer(buffer: FloatBuffer) {
        buffer.put(m00).put(m10).put(m20)
        buffer.put(m01).put(m11).put(m21)
        buffer.put(m02).put(m12).put(m22)
        buffer.flip()
    }

}
