package display.graphic

import org.lwjgl.opengl.ARBVertexArrayObject.*

class VertexArrayObject {

    val id: Int = glGenVertexArrays()

    fun bind(): VertexArrayObject {
        glBindVertexArray(id)
        return this
    }

    fun delete() {
        glDeleteVertexArrays(id)
    }

}
