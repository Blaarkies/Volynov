package display.events

import org.lwjgl.glfw.GLFW

class KeyboardEvent(val key: Int, val scancode: Int, val action: Int, val mods: Int) {

    val shiftHeld: Boolean
        get() = modsContainKey(GLFW.GLFW_MOD_SHIFT)

    private fun modsContainKey(key: Int): Boolean = mods.and(key) == key

}
