package display.draw.light

import dI
import display.draw.Drawer
import display.draw.TextureEnum
import display.graphic.Color
import engine.freeBody.Particle
import org.jbox2d.common.Vec2
import utility.Common
import utility.StopWatch
import utility.lerp
import utility.toList

class LaserBeam(a: Vec2,
                b: Vec2,
                normal: Vec2,
                targetVelocity: Vec2,
                color: Color = Color("#ff2f2fFF").setAlpha(.7f))
    : LightBeam(a, b, color) {

    private val stopWatch = StopWatch()
    private val fadeTime = 500f
    var active = true
    lateinit var lastB: Vec2
    lateinit var lastNormal: Vec2
    lateinit var lastTargetVelocity: Vec2

    init {
        update(a, b, true, normal, targetVelocity)
    }

    fun update(a: Vec2,
               b: Vec2 = lastB,
               activate: Boolean = false,
               normal: Vec2? = null,
               targetVelocity: Vec2? = null) {
        if (activate) stopWatch.reset()

        val strength = (fadeTime - stopWatch.elapsedTime).div(fadeTime).coerceIn(0f, 1f)
            .let { Common.getTimingFunctionEaseIn(it) }

        gpuData = Drawer.getLine(listOf(a, b).flatMap { it.toList() },
            color.setAlpha(strength), startWidth = .02f)
        lastB = b

        if (normal != null) lastNormal = normal
        if (targetVelocity != null) lastTargetVelocity = targetVelocity
        active = if (activate) true else (strength > 0f)

        if (dI.gameState.tickTime.rem(100) < 25f) {
            dI.gameState.activeCallbacks.add {
                Particle("laser_dust", dI.gameState.particles, dI.gameState.world, lastTargetVelocity,
                    b, lastNormal.mul(3f), .2f, 200f, TextureEnum.rcs_puff, color = color,
                    createdAt = dI.gameState.tickTime)
            }
        }
    }

}
