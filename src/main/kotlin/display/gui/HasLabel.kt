package display.gui

import dI
import display.graphic.SnipRegion
import display.text.TextJustify
import utility.Common

interface HasLabel : GuiElement {

    val justify: TextJustify
    var title: String
    val textSize: Float

    override fun render(parentSnipRegion: SnipRegion?) {
        super.render(parentSnipRegion)

        dI.renderer.drawText(title,
            offset, Common.vectorUnit.mul(textSize),
            color, justify,
            false, parentSnipRegion)
    }

}
