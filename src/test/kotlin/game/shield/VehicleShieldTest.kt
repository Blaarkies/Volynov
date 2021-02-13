package game.shield

import display.draw.Model
import display.draw.TextureEnum
import display.graphic.CameraType
import display.graphic.vertex.BasicSurfaces
import org.junit.jupiter.api.Test
import utility.WavefrontObject

import kotlin.math.pow

internal class VehicleShieldTest {

    @Test
    fun import() {
        WavefrontObject.import("missile_basic")
    }

    @Test
    fun export() {
        WavefrontObject.export(Model(BasicSurfaces.icosahedron, listOf(TextureEnum.metal), 1f, CameraType.UNIVERSE))
    }

    @Test
    fun blockDamage() {
        //        val shield = ForceField(FreeBody("", 1f))

        //        expect(shield.blockDamage(0f)).close(0f, .001f)

        (0..20).map { it * 20f }
            .forEach {
                println("${it.toInt()} ${blockDamage(it).toInt()}")
            }
    }

    fun blockDamage(amount: Float): Float {
        val factor = 5f
        val gradient = .5f
        val translate = factor.pow(1f / gradient)

        return (((amount * 2f) + translate).pow(gradient)) * factor - factor.pow(1f / gradient)
    }
}
