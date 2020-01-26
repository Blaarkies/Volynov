import java.nio.FloatBuffer

class Matrix4f {

    private var m00 = 0f
    private var m01 = 0f
    private var m02 = 0f
    private var m03 = 0f
    private var m10 = 0f
    private var m11 = 0f
    private var m12 = 0f
    private var m13 = 0f
    private var m20 = 0f
    private var m21 = 0f
    private var m22 = 0f
    private var m23 = 0f
    private var m30 = 0f
    private var m31 = 0f
    private var m32 = 0f
    private var m33 = 0f

    constructor() {
        setIdentity()
    }

    constructor(col1: Vector4f, col2: Vector4f, col3: Vector4f, col4: Vector4f) {
        m00 = col1.x
        m10 = col1.y
        m20 = col1.z
        m30 = col1.w
        m01 = col2.x
        m11 = col2.y
        m21 = col2.z
        m31 = col2.w
        m02 = col3.x
        m12 = col3.y
        m22 = col3.z
        m32 = col3.w
        m03 = col4.x
        m13 = col4.y
        m23 = col4.z
        m33 = col4.w
    }

    fun setIdentity() {
        m00 = 1f
        m11 = 1f
        m22 = 1f
        m33 = 1f
        m01 = 0f
        m02 = 0f
        m03 = 0f
        m10 = 0f
        m12 = 0f
        m13 = 0f
        m20 = 0f
        m21 = 0f
        m23 = 0f
        m30 = 0f
        m31 = 0f
        m32 = 0f
    }

    fun add(other: Matrix4f): Matrix4f {
        val result = Matrix4f()
        result.m00 = m00 + other.m00
        result.m10 = m10 + other.m10
        result.m20 = m20 + other.m20
        result.m30 = m30 + other.m30
        result.m01 = m01 + other.m01
        result.m11 = m11 + other.m11
        result.m21 = m21 + other.m21
        result.m31 = m31 + other.m31
        result.m02 = m02 + other.m02
        result.m12 = m12 + other.m12
        result.m22 = m22 + other.m22
        result.m32 = m32 + other.m32
        result.m03 = m03 + other.m03
        result.m13 = m13 + other.m13
        result.m23 = m23 + other.m23
        result.m33 = m33 + other.m33
        return result
    }

    fun negate(): Matrix4f {
        return multiply(-1f)
    }

    fun subtract(other: Matrix4f): Matrix4f {
        return add(other.negate())
    }

    fun multiply(scalar: Float): Matrix4f {
        val result = Matrix4f()
        result.m00 = m00 * scalar
        result.m10 = m10 * scalar
        result.m20 = m20 * scalar
        result.m30 = m30 * scalar
        result.m01 = m01 * scalar
        result.m11 = m11 * scalar
        result.m21 = m21 * scalar
        result.m31 = m31 * scalar
        result.m02 = m02 * scalar
        result.m12 = m12 * scalar
        result.m22 = m22 * scalar
        result.m32 = m32 * scalar
        result.m03 = m03 * scalar
        result.m13 = m13 * scalar
        result.m23 = m23 * scalar
        result.m33 = m33 * scalar
        return result
    }

    fun multiply(vector: Vector4f): Vector4f {
        val x =
            m00 * vector.x + m01 * vector.y + m02 * vector.z + m03 * vector.w
        val y =
            m10 * vector.x + m11 * vector.y + m12 * vector.z + m13 * vector.w
        val z =
            m20 * vector.x + m21 * vector.y + m22 * vector.z + m23 * vector.w
        val w =
            m30 * vector.x + m31 * vector.y + m32 * vector.z + m33 * vector.w
        return Vector4f(x, y, z, w)
    }

    fun multiply(other: Matrix4f): Matrix4f {
        val result = Matrix4f()
        result.m00 = m00 * other.m00 + m01 * other.m10 + m02 * other.m20 + m03 * other.m30
        result.m10 = m10 * other.m00 + m11 * other.m10 + m12 * other.m20 + m13 * other.m30
        result.m20 = m20 * other.m00 + m21 * other.m10 + m22 * other.m20 + m23 * other.m30
        result.m30 = m30 * other.m00 + m31 * other.m10 + m32 * other.m20 + m33 * other.m30
        result.m01 = m00 * other.m01 + m01 * other.m11 + m02 * other.m21 + m03 * other.m31
        result.m11 = m10 * other.m01 + m11 * other.m11 + m12 * other.m21 + m13 * other.m31
        result.m21 = m20 * other.m01 + m21 * other.m11 + m22 * other.m21 + m23 * other.m31
        result.m31 = m30 * other.m01 + m31 * other.m11 + m32 * other.m21 + m33 * other.m31
        result.m02 = m00 * other.m02 + m01 * other.m12 + m02 * other.m22 + m03 * other.m32
        result.m12 = m10 * other.m02 + m11 * other.m12 + m12 * other.m22 + m13 * other.m32
        result.m22 = m20 * other.m02 + m21 * other.m12 + m22 * other.m22 + m23 * other.m32
        result.m32 = m30 * other.m02 + m31 * other.m12 + m32 * other.m22 + m33 * other.m32
        result.m03 = m00 * other.m03 + m01 * other.m13 + m02 * other.m23 + m03 * other.m33
        result.m13 = m10 * other.m03 + m11 * other.m13 + m12 * other.m23 + m13 * other.m33
        result.m23 = m20 * other.m03 + m21 * other.m13 + m22 * other.m23 + m23 * other.m33
        result.m33 = m30 * other.m03 + m31 * other.m13 + m32 * other.m23 + m33 * other.m33
        return result
    }

    fun transpose(): Matrix4f {
        val result = Matrix4f()
        result.m00 = m00
        result.m10 = m01
        result.m20 = m02
        result.m30 = m03
        result.m01 = m10
        result.m11 = m11
        result.m21 = m12
        result.m31 = m13
        result.m02 = m20
        result.m12 = m21
        result.m22 = m22
        result.m32 = m23
        result.m03 = m30
        result.m13 = m31
        result.m23 = m32
        result.m33 = m33
        return result
    }

    fun toBuffer(buffer: FloatBuffer) {
        buffer.put(m00).put(m10).put(m20).put(m30)
        buffer.put(m01).put(m11).put(m21).put(m31)
        buffer.put(m02).put(m12).put(m22).put(m32)
        buffer.put(m03).put(m13).put(m23).put(m33)
        buffer.flip()
    }

    companion object {

        fun orthographic(
            left: Float,
            right: Float,
            bottom: Float,
            top: Float,
            near: Float,
            far: Float
        ): Matrix4f {
            val ortho = Matrix4f()
            val tx = -(right + left) / (right - left)
            val ty = -(top + bottom) / (top - bottom)
            val tz = -(far + near) / (far - near)
            ortho.m00 = 2f / (right - left)
            ortho.m11 = 2f / (top - bottom)
            ortho.m22 = -2f / (far - near)
            ortho.m03 = tx
            ortho.m13 = ty
            ortho.m23 = tz
            return ortho
        }

        fun frustum(
            left: Float,
            right: Float,
            bottom: Float,
            top: Float,
            near: Float,
            far: Float
        ): Matrix4f {
            val frustum = Matrix4f()
            val a = (right + left) / (right - left)
            val b = (top + bottom) / (top - bottom)
            val c = -(far + near) / (far - near)
            val d = -(2f * far * near) / (far - near)
            frustum.m00 = 2f * near / (right - left)
            frustum.m11 = 2f * near / (top - bottom)
            frustum.m02 = a
            frustum.m12 = b
            frustum.m22 = c
            frustum.m32 = -1f
            frustum.m23 = d
            frustum.m33 = 0f
            return frustum
        }

        fun perspective(fovy: Float, aspect: Float, near: Float, far: Float): Matrix4f {
            val perspective = Matrix4f()
            val f =
                (1f / Math.tan(Math.toRadians(fovy.toDouble()) / 2f)).toFloat()
            perspective.m00 = f / aspect
            perspective.m11 = f
            perspective.m22 = (far + near) / (near - far)
            perspective.m32 = -1f
            perspective.m23 = 2f * far * near / (near - far)
            perspective.m33 = 0f
            return perspective
        }

        fun translate(x: Float, y: Float, z: Float): Matrix4f {
            val translation = Matrix4f()
            translation.m03 = x
            translation.m13 = y
            translation.m23 = z
            return translation
        }

        fun rotate(angle: Float, x: Float, y: Float, z: Float): Matrix4f {
            var x = x
            var y = y
            var z = z
            val rotation = Matrix4f()
            val c = Math.cos(Math.toRadians(angle.toDouble())).toFloat()
            val s = Math.sin(Math.toRadians(angle.toDouble())).toFloat()
            var vec = Vector3f(x, y, z)
            if (vec.length() != 1f) {
                vec = vec.normalize()
                x = vec.x
                y = vec.y
                z = vec.z
            }
            rotation.m00 = x * x * (1f - c) + c
            rotation.m10 = y * x * (1f - c) + z * s
            rotation.m20 = x * z * (1f - c) - y * s
            rotation.m01 = x * y * (1f - c) - z * s
            rotation.m11 = y * y * (1f - c) + c
            rotation.m21 = y * z * (1f - c) + x * s
            rotation.m02 = x * z * (1f - c) + y * s
            rotation.m12 = y * z * (1f - c) - x * s
            rotation.m22 = z * z * (1f - c) + c
            return rotation
        }

        fun scale(x: Float, y: Float, z: Float): Matrix4f {
            val scaling = Matrix4f()
            scaling.m00 = x
            scaling.m11 = y
            scaling.m22 = z
            return scaling
        }
    }

}
