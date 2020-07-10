package display.gui.base

import display.graphic.Color
import display.graphic.SnipRegion
import org.jbox2d.common.Vec2

interface GuiElement {

    val offset: Vec2
    val scale: Vec2
    val color: Color
    val updateCallback: (GuiElement) -> Unit
    var id: GuiElementIdentifierType
    var currentPhase: GuiElementPhases

    fun render(parentSnipRegion: SnipRegion?) = Unit

    fun update() = updateCallback(this)

    fun addOffset(movement: Vec2): Unit = updateOffset(offset.add(movement))

    fun updateOffset(newOffset: Vec2) {
        offset.set(newOffset)
    }

    fun updateScale(newScale: Vec2): Vec2 = scale.set(newScale)

}
