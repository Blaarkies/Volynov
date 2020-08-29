package display.event

import org.lwjgl.glfw.GLFW

interface ButtonPress {

    val action: Int

    val isPress: Boolean
        get() = action == GLFW.GLFW_PRESS

    val isRelease: Boolean
        get() = action == GLFW.GLFW_RELEASE

}
