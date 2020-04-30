package utility

import org.jbox2d.common.Vec2
import java.io.File
import java.nio.DoubleBuffer
import java.util.*
import kotlin.math.*

object Common {

    @Throws(Exception::class)
    fun loadResource(fileName: String): String {
        var result = ""
        Class.forName(Common::class.java.name)
            .getResourceAsStream(fileName)
            .use {
                Scanner(it, "UTF-8").use { scanner -> result = scanner.useDelimiter("\\A").next() }
            }
        return result
    }

    fun getSafePath(resourcePath: String): String {
        val alternativePath = "src/main/resources/${resourcePath.substring(1)}"
        return when {
            File(alternativePath).exists() -> alternativePath
            else -> resourcePath
        }
    }

    fun <T, S> joinLists(aList: List<T>, bList: List<S>): Sequence<Pair<T, S>> = sequence {
        aList.forEach { aItem ->
            bList.forEach { bItem ->
                yield(Pair(aItem, bItem))
            }
        }
    }

    fun <T, S> joinListsNoDuplicate(aList: List<T>, bList: List<S>): Sequence<Pair<T, S>> =
        joinLists(aList, bList).filter { (a, b) -> a != b }

    fun roundDouble(value: Double, decimals: Int = 2): Double {
        val multiplier = 10.0.pow(decimals.toDouble())
        return (value * multiplier).roundToInt() / multiplier
    }

    fun roundFloat(value: Float, decimals: Int = 2): Float {
        val multiplier = 10f.pow(decimals)
        return value.times(multiplier).roundToInt().div(multiplier)
    }

    const val Pi2 = 2f * PI.toFloat()

    val vectorUnit = Vec2(1f, 1f)

    fun makeVec2(list: List<Float>): Vec2 = Vec2(list[0], list[1])

    fun makeVec2(x: Number, y: Number): Vec2 = Vec2(x.toFloat(), y.toFloat())

    fun makeVec2(x: DoubleBuffer, y: DoubleBuffer): Vec2 = makeVec2(x.get(), y.get())

    fun makeVec2(duplicate: Number): Vec2 = makeVec2(duplicate, duplicate)

    fun makeVec2Circle(angle: Float): Vec2 = Vec2(cos(angle), sin(angle))

    val radianToDegree = Math.toDegrees(1.0).toFloat()

    fun getTimingFunctionEaseOut(interpolateStep: Float) = getTimingFunctionFullSine(sqrt(interpolateStep))

    fun getTimingFunctionEaseIn(interpolateStep: Float) = 1f - getTimingFunctionEaseOut(1f - interpolateStep)

    fun getTimingFunctionFullSine(interpolateStep: Float) = (sin(interpolateStep * PI - PI * .5) * .5 + .5).toFloat()

    fun getTimingFunctionSigmoid(interpolateStep: Float, centerGradient: Float = 1f) =
        (1f / (1f + exp((-(interpolateStep - .5f) * 10f)) * centerGradient)) * 1.023f - 0.0022f

    fun getRandomDirection() = Math.random().toFloat() * 2f * PI.toFloat()

    fun getRandomMixed() = Math.random().toFloat() * 2f - 1f

    fun getRandomSign() = if (Math.random() > .5) -1f else 1f

}

fun Vec2.toList(): List<Float> = listOf(this.x, this.y)
