package display.gui.base

import display.events.MouseButtonEvent
import display.gui.base.GuiElementPhases.*
import io.reactivex.Observable

interface HasClick : HasHover {

    val onClick: () -> Unit

    fun handleLeftClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>): Boolean {
        val isHovered = isHover(startEvent.location)
        if (isHovered) {
            currentPhase = ACTIVE

            event.takeLast(1)
                .doOnComplete { currentPhase = IDLE }
                .filter { it.isRelease && isHover(it.location) }
                .subscribe { onClick() }
        }
        return isHovered
    }
}
