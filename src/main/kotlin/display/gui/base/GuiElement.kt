package display.gui.base

import display.graphic.Color
import display.graphic.SnipRegion
import display.gui.LayoutController
import display.gui.LayoutPosition
import org.jbox2d.common.Vec2

interface GuiElement {

    val offset: Vec2
    val scale: Vec2
    // TODO: add angle
    val color: Color
    val updateCallback: (GuiElement) -> Unit
    var id: GuiElementIdentifierType
    var currentPhase: GuiElementPhases

    fun render(parentSnipRegion: SnipRegion?) = Unit

    fun update() = updateCallback(this) // TODO: turn into change detection to save performance

    fun addOffset(movement: Vec2): Unit = updateOffset(offset.add(movement))

    fun updateOffset(newOffset: Vec2) {
        offset.set(newOffset)
    }

    fun updateScale(newScale: Vec2): Vec2 = scale.set(newScale)

    fun placeOnEdge(edge: LayoutPosition, parent: Vec2, adjustedScale: Vec2? = null) =
        updateOffset(LayoutController.getOffsetForLayoutPosition(edge, parent, adjustedScale ?: scale))

}
