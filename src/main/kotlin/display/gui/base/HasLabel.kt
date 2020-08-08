package display.gui.base

import dI
import display.graphic.SnipRegion
import display.gui.base.GuiElement
import display.text.TextJustify
import utility.Common.vectorUnit

interface HasLabel : GuiElement {

    val justify: TextJustify
    var title: String
    val textSize: Float
    var maxWidth: Float
    val angle: Float

    override fun render(parentSnipRegion: SnipRegion?) {
        super.render(parentSnipRegion)

        dI.renderer.drawText(title,
            offset, vectorUnit.mul(textSize),
            color, justify,
            false, parentSnipRegion,
            maxWidth, angle)
    }

}
