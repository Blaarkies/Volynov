package display.gui

import display.events.MouseButtonEvent
import display.gui.GuiElementPhases.*
import io.reactivex.Observable

interface HasClick : HasHover {

    val onClick: () -> Unit

    fun handleLeftClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>): Boolean {
        val isHovered = isHover(startEvent.location)
        if (isHovered) {
            currentPhase = ACTIVE

            event.takeLast(1)
                .doOnComplete { currentPhase = IDLE }
                .filter { isHover(it.location) }
                .subscribe { onClick() }
        }
        return isHovered
    }
}
