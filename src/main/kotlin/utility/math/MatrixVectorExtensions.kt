package utility.math

import org.joml.*
import java.nio.FloatBuffer

fun Matrix4f.clone(): Matrix4f = Matrix4f(this)

fun Matrix4f.toBuffer(buffer: FloatBuffer) {
    buffer.put(m00()).put(m10()).put(m20()).put(m30())
    buffer.put(m01()).put(m11()).put(m21()).put(m31())
    buffer.put(m02()).put(m12()).put(m22()).put(m32())
    buffer.put(m03()).put(m13()).put(m23()).put(m33())
    buffer.flip()
}

fun Matrix3f.toBuffer(buffer: FloatBuffer) {
    buffer.put(m00).put(m10).put(m20)
    buffer.put(m01).put(m11).put(m21)
    buffer.put(m02).put(m12).put(m22)
    buffer.flip()
}

fun Matrix2f.toBuffer(buffer: FloatBuffer) {
    buffer.put(m00).put(m10)
    buffer.put(m01).put(m11)
    buffer.flip()
}

fun Vector4f.toBuffer(buffer: FloatBuffer) {
    buffer.put(x).put(y).put(z).put(w)
    buffer.flip()
}

fun Vector3f.toBuffer(buffer: FloatBuffer) {
    buffer.put(x).put(y).put(z)
    buffer.flip()
}

fun Vector2f.toBuffer(buffer: FloatBuffer) {
    buffer.put(x).put(y)
    buffer.flip()
}
