package display.gui.special

import display.events.MouseButtonEvent
import display.graphic.Color
import display.gui.base.GuiElement
import display.gui.base.GuiElementPhase
import display.gui.elements.GuiButton
import io.reactivex.Observable
import org.jbox2d.common.Vec2
import utility.Common.pressAndHoldAction

open class GuiButtonRepeater(
    offset: Vec2 = Vec2(),
    scale: Vec2 = Vec2(200f, 50f),
    title: String = "",
    textSize: Float = .2f,
    color: Color = Color.WHITE.setAlpha(.7f),
    onClick: () -> Unit = {},
    updateCallback: (GuiElement) -> Unit = {})
    : GuiButton(offset, scale, title, textSize, color, onClick, updateCallback) {

    override fun handleLeftClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>): Boolean {
        val isHovered = isHover(startEvent.location) && currentPhase != GuiElementPhase.DISABLED
        if (isHovered) {
            currentPhase = GuiElementPhase.ACTIVE

            pressAndHoldAction(event)
                .doOnComplete { currentPhase = GuiElementPhase.IDLE }
                .subscribe { onClick() }
        }
        return isHovered
    }

}
