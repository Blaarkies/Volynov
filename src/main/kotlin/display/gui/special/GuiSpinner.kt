package display.gui.special

import display.events.DistanceCalculator
import display.events.MouseButtonEvent
import display.graphic.Color
import display.graphic.Color.Companion.WHITE
import display.graphic.SnipRegion
import display.gui.base.*
import display.gui.base.GuiElementPhase.IDLE
import display.gui.elements.GuiButton
import display.gui.elements.GuiLabel
import display.text.TextJustify
import io.reactivex.Observable
import org.jbox2d.common.Vec2
import utility.Common.Pi
import utility.Common.roundFloat

class GuiSpinner(override val offset: Vec2 = Vec2(),
                 override val scale: Vec2 = Vec2(61f, 16f),
                 override var color: Color = WHITE,
                 override val updateCallback: (GuiElement) -> Unit = {},
                 val onClickMore: () -> Unit = {},
                 val onClickLess: () -> Unit = {},
                 labelCallback: (GuiElement) -> Unit = {})
    : HasElements {

    override val localElements = mutableListOf<GuiElement>()
    override val localElementOffsets = HashMap<GuiElement, Vec2>()

    override lateinit var topRight: Vec2
    override lateinit var bottomLeft: Vec2
    override var id = GuiElementIdentifierType.DEFAULT
    override var currentPhase = IDLE

    init {
        localElements.addAll(listOf(
            GuiButtonRepeater(Vec2(-50f, 8f), Vec2(11f, 8f), "V", .07f, angle = Pi, onClick = onClickMore),
            GuiButtonRepeater(Vec2(-50f, -8f), Vec2(11f, 8f), "V", .07f, onClick = onClickLess),
            GuiLabel(Vec2(50f, 0f), TextJustify.RIGHT, textSize = .14f, updateCallback = labelCallback)
        ))
        localElementOffsets.putAll(localElements.map { Pair(it, it.offset.clone()) })

        calculateElementRegion()
    }

    override fun handleLeftClick(startEvent: MouseButtonEvent, event: Observable<MouseButtonEvent>): Boolean {
        val isHovered = isHover(startEvent.location)
        if (isHovered) {
            val localTakeEvent = localElements.filterIsInstance<HasClick>()
                .any { it.handleLeftClick(startEvent, event) }
            if (localTakeEvent) return true

            currentPhase = GuiElementPhase.ACTIVE

            val distanceCalculator = DistanceCalculator()
            event.doOnComplete { currentPhase = IDLE }
                .subscribe {
                    val movement = distanceCalculator.getLastDistance(it.location)
                    when {
                        movement.y > 0 -> onClickMore()
                        movement.y < 0 -> onClickLess()
                    }
                }
        }
        return isHovered
    }

}
