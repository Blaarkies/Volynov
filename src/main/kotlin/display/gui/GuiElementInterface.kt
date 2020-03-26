package display.gui

import display.graphic.Color
import org.jbox2d.common.Vec2

interface GuiElementInterface {

    val offset: Vec2
    val scale: Vec2
    val title: String
    val textSize: Float
    val color: Color
    var id: GuiElementIdentifierType
    fun render()

}
