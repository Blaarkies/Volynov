package utility

import org.jbox2d.common.Vec2

class Expect(private val value: Any?) {

    private var target: Any? = null
        set(type) {
            if (target == null) {
                field = type
            } else throw Exception("Cannot compare multiple values")
        }
    private var isNot = false
    val not: Expect
        get() {
            isNot = !isNot
            return this
        }

    val truly: Expect
        get() {
            assert(value == !isNot) { "Expected $value to be ${!isNot}" }
            return this
        }

    val falsy: Expect
        get() = not.truly

    fun same(expectedValue: Any, message: () -> String = { "" }) {
        target = expectedValue
        assert((value == expectedValue) == !isNot) {
            "Expected $value " +
                    "to${if (isNot) " not" else ""} be " +
                    "$expectedValue. ${message()}"
        }
    }

    fun same(expectedValue: Vec2, message: () -> String = { "" }) {
        target = expectedValue
        val isEqual = (value as Vec2).sub(expectedValue).length() == 0f
        assert(isEqual == !isNot) {
            "Expected $value " +
                    "to${if (isNot) " not" else ""} be " +
                    "$expectedValue. ${message()}"
        }
    }

    fun close(expectedValue: Float, range: Float, message: () -> String = { "" }) {
        target = expectedValue
        val minValue = expectedValue - range
        val maxValue = expectedValue + range
        assert(
            (((value as Float) > minValue)
                    && (value < maxValue))
                    == !isNot) {
            "Expected $value " +
                    "to${if (isNot) " not" else ""} be " +
                    "in range of [$minValue] to [$maxValue]." +
                    message()
        }
    }

    companion object {
        fun expect(value: Any) = Expect(value)
    }

}
