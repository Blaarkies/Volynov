package display.gui

import display.graphic.Color
import display.graphic.SnipRegion
import org.jbox2d.common.Vec2

interface GuiElementInterface {

    var offset: Vec2
    val scale: Vec2
    val title: String
    val textSize: Float
    val color: Color
    val updateCallback: (GuiElement) -> Unit
    var id: GuiElementIdentifierType

    fun render(snipRegion: SnipRegion?)
    fun update()

    fun handleHover(location: Vec2)
    fun handleLeftClickDrag(location: Vec2, movement: Vec2): Boolean
    fun handleScroll(location: Vec2, movement: Vec2): Boolean

    fun addOffset(newOffset: Vec2)
    fun updateOffset(newOffset: Vec2)
    fun updateScale(newScale: Vec2)

    fun handleLeftClickPress(location: Vec2): Boolean
    fun handleLeftClickRelease(location: Vec2): Boolean
}
