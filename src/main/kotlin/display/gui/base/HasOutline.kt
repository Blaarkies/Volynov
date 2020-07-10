package display.gui.base

import dI
import display.draw.Drawer
import display.draw.TextureEnum
import display.graphic.BasicShapes
import display.graphic.Color
import display.graphic.SnipRegion

interface HasOutline : GuiElement {

    var outline: FloatArray
    var activeBackground: FloatArray
    var backgroundColor: Color

    fun calculateVisuals() {
        val linePoints = BasicShapes.square
            .chunked(2)
            .flatMap { (x, y) -> listOf(x * scale.x, y * scale.y) }
        outline = Drawer.getLine(linePoints, color, startWidth = 1f, wrapAround = true)
        activeBackground = Drawer.getColoredData(linePoints, backgroundColor).toFloatArray()
    }

    override fun render(parentSnipRegion: SnipRegion?) {
        dI.textures.getTexture(TextureEnum.white_pixel).bind()
        dI.renderer.drawStrip(outline, offset, useCamera = false, snipRegion = parentSnipRegion)

        super.render(parentSnipRegion)
    }

}
