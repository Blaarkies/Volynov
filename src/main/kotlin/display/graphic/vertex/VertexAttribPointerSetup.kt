package display.graphic.vertex

import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer

class VertexAttribPointerSetup(attributeSizes: List<Int>) {

    val vertexDimensionCount: Int = attributeSizes.sum()

    init {
        val strideSize = vertexDimensionCount * java.lang.Float.BYTES
        val bytesInt = java.lang.Float.BYTES.toLong()

        attributeSizes
            .fold(mutableListOf<VertexAttribPointer>()) { list, size ->
                if (list.isEmpty()) {
                    list.add(VertexAttribPointer(size))
                } else {
                    val last = list.last()
                    val index = last.index + 1
                    val offset = last.offset + last.size
                    list.add(
                        VertexAttribPointer(size, index, offset)
                    )
                }

                list
            }
            .forEach {
                glVertexAttribPointer(it.index, it.size, GL_FLOAT, false, strideSize, it.offset * bytesInt)
                glEnableVertexAttribArray(it.index)
            }
    }

    internal class VertexAttribPointer(
        val size: Int,
        val index: Int = 0,
        val offset: Int = 0,
    )

}
