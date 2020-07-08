package display.gui

import display.events.MouseButtonEvent
import io.reactivex.Observable
import org.lwjgl.glfw.GLFW

interface HasClick : HasHover {

    val onClick: () -> Unit

    fun handleLeftClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>): Boolean {
        val isHovered = isHover(startEvent.location)
        if (isHovered) {
            currentPhase = GuiElementPhases.ACTIVE

            event.filter { it.action == GLFW.GLFW_RELEASE }
                .doFinally { currentPhase = GuiElementPhases.IDLE }
                .subscribe {
                    if (isHover(it.location)) {
                        onClick()
                    }
                }
        }
        return isHovered
    }
}
