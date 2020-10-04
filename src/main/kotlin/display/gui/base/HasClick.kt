package display.gui.base

import display.event.MouseButtonEvent
import display.gui.base.GuiElementPhase.*
import io.reactivex.Observable

interface HasClick : HasHover {

    val onClick: () -> Unit

    fun handleLeftClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>): Boolean {
        val isHovered = isHover(startEvent.location) && currentPhase != DISABLED
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
