package utility

import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

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
        val multiplier = 10.0.pow(decimals.toDouble())
        return (value * multiplier).roundToInt() / multiplier.toFloat()
    }

    val radianToDegree = Math.toDegrees(1.0).toFloat()

}
