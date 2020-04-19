package utility

import org.jbox2d.common.Vec2
import org.junit.jupiter.api.Test

import utility.Common.getTimingFunctionEaseIn
import utility.Common.getTimingFunctionEaseOut
import utility.Common.getTimingFunctionFullSine
import utility.Common.getTimingFunctionSigmoid
import utility.Common.joinLists
import utility.Common.roundFloat

internal class CommonTest {

    fun testest() {

        val data = getChartedData {
            getTimingFunctionEaseIn(1f - it)
        }
        println(data)
    }

    private fun getChartedData(timingFunction: (Float) -> Float): String {
        val resolution = 50
        val values = (0 until resolution).map { it / resolution.minus(1).toFloat() }
            .map { timingFunction(it) }
            .map { roundFloat(it, 2) }

        val visualGraph = HashMap<Pair<Int, Int>, Float>()
        values.withIndex().forEach { (index, y) ->
            visualGraph[Pair(index, (y * (resolution)).toInt())] = y
        }

        val intRange = (0 until resolution).toList()
        val result = joinLists(intRange, intRange)
            .sortedBy { (x, y) -> -y }
            .sortedBy { (x, y) -> x }
            .map { (x, y) -> visualGraph[Pair(y, x)] }
            .map { if (it != null) "x" else "." }
            .chunked(resolution)
            .map { it.joinToString("") }
            .joinToString("\n")
        return result
    }
}
