package utility

import display.events.ButtonPress
import io.reactivex.Observable
import io.reactivex.Observable.interval
import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import java.io.File
import java.nio.DoubleBuffer
import java.util.*
import java.util.concurrent.TimeUnit
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

    const val muCron = "µ"

    const val Pi = PI.toFloat()
    const val Pi2 = 2f * Pi
    const val PiH = Pi * .5f
    const val PiQ = Pi * .25f

    val vectorUnit
        get() = Vec2(1f, 1f)

    fun makeVec2(list: List<Float>): Vec2 = Vec2(list[0], list[1])

    fun makeVec2(x: Number, y: Number): Vec2 = Vec2(x.toFloat(), y.toFloat())

    fun makeVec2(x: DoubleBuffer, y: DoubleBuffer): Vec2 = makeVec2(x.get(), y.get())

    fun makeVec2(duplicate: Number): Vec2 = makeVec2(duplicate, duplicate)

    fun makeVec2Circle(angle: Float): Vec2 = Vec2(cos(angle), sin(angle))

    val radianToDegree = Math.toDegrees(1.0).toFloat()
    val degreeToRadian = 1f / radianToDegree

    fun getTimingFunctionEaseOut(interpolateStep: Float) = getTimingFunctionFullSine(sqrt(interpolateStep))

    fun getTimingFunctionEaseIn(interpolateStep: Float) = 1f - getTimingFunctionEaseOut(1f - interpolateStep)

    fun getTimingFunctionFullSine(interpolateStep: Float) = (sin(interpolateStep * Pi - Pi * .5f) * .5f + .5f)

    fun getTimingFunctionSigmoid(interpolateStep: Float, centerGradient: Float = 1f) =
        (1f / (1f + exp((-(interpolateStep - .5f) * 10f)) * centerGradient)) * 1.023f - 0.0022f

    fun getRandomDirection() = Math.random().toFloat() * Pi2

    fun getRandomMixed() = Math.random().toFloat() * 2f - 1f

    fun getRandomSign() = if (Math.random() > .5) -1f else 1f

    fun <T : ButtonPress> pressAndHoldAction(event: Observable<T>,
                           rampDuration: Float = 3000f,
                           startInterval: Float = 300f,
                           endInterval: Float = 25f): Observable<Unit> {
        val stopWatch = StopWatch()
        val outputStream = PublishSubject.create<Unit>()

        val pauseDuration = PublishSubject.create<Float>()
        val complete = event.filter { it.isRelease }
            .doOnNext { pauseDuration.onComplete() }

        pauseDuration
            .doOnComplete { outputStream.onComplete() }
            .switchMap { interval(it.toLong(), TimeUnit.MILLISECONDS) }
            .takeUntil(complete)
            .subscribe {
                outputStream.onNext(Unit)
                val duration = stopWatch.elapsedTime.div(rampDuration).coerceIn(0f, 1f)
                    .let { getTimingFunctionEaseIn(1f - it) * startInterval }
                    .coerceAtLeast(endInterval)
                pauseDuration.onNext(duration)
            }
        pauseDuration.onNext(0f)

        return outputStream
    }

}

fun Vec2.toList(): List<Float> = listOf(this.x, this.y)
fun Boolean.toSign(): Float = if (this) 1f else -1f
