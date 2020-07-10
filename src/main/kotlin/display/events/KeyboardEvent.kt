package display.events

import org.lwjgl.glfw.GLFW

class KeyboardEvent(
    val key: Int,
    val scancode: Int,
    override val action: Int,
    val mods: Int) : ButtonPress {

    val shiftHeld: Boolean
        get() = modsContainKey(GLFW.GLFW_MOD_SHIFT)

    val isEnter: Boolean
        get() = key == GLFW.GLFW_KEY_ENTER

    val isEscape: Boolean
        get() = key == GLFW.GLFW_KEY_ESCAPE

    val isTab: Boolean
        get() = key == GLFW.GLFW_KEY_TAB

    val isBackspace: Boolean
        get() = key == GLFW.GLFW_KEY_BACKSPACE

    private fun modsContainKey(key: Int): Boolean = mods.and(key) == key

}
