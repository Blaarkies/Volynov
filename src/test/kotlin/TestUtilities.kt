import utility.Common.joinLists
import utility.Common.roundFloat
import kotlin.math.absoluteValue

object TestUtilities {

    fun getVisualGraphString(values: List<Pair<Float, Float>>): String {
        val visualGraph = HashMap<Pair<Float, Float>, String>()

        val xBuckets = 1.let {
            val min = values.minBy { (x, _) -> x }!!.first
            val max = values.maxBy { (x, _) -> x }!!.first
            val bucketSize = (max - min) * .1f
            (0..9).map { min + bucketSize * it }
        }
        val yBuckets = 1.let {
            val min = values.minBy { (_, y) -> y }!!.second
            val max = values.maxBy { (_, y) -> y }!!.second
            val bucketSize = (max - min) * .1f
            (0..9).map { min + bucketSize * it }
        }

        values.forEach { (x, y) ->
            val xStep = xBuckets.minBy { (x - it).absoluteValue }!!
            val yStep = xBuckets.minBy { (x - it).absoluteValue }!!
            visualGraph[Pair(xStep, yStep)] = "x"
        }

//        return visualGraph.toList()
//            .sortedBy { (key, _) -> key }
//            .joinToString()

        val result = joinLists(yBuckets, xBuckets)
            .sortedBy { (x, y) -> -y }
            .sortedBy { (x, y) -> x }
            .map { (x, y) -> visualGraph[Pair(y, x)] }
            .map { if (it != null) "x" else "." }
            .chunked(10)
            .map { it.joinToString("") }
            .joinToString("\n")
        return result
    }

    fun getChartedData(resolution: Int, timingFunction: (Float) -> Float): String {
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
            .map { if (it != null) "█" else "`" }
            .chunked(resolution)
            .map { it.joinToString("") }
            .joinToString("\n")
        return result
    }

}
