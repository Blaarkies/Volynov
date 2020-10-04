package display.draw.light

import display.draw.Drawer
import display.draw.TextureEnum
import display.graphic.Color
import org.jbox2d.common.Vec2
import utility.toList

open class LightBeam(a: Vec2, b: Vec2, val color: Color = Color.WHITE) : Light {

    var gpuData: FloatArray
    val texture = TextureEnum.white_pixel

    init {
        gpuData = Drawer.getLine(listOf(a, b).flatMap { it.toList() },
            color, startWidth = .02f)
    }

}
