package utility

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

    fun same(expectedValue: Any, message: () -> String = { "" }) {
        target = expectedValue
        assert((value == expectedValue) == !isNot) {
            "Expected $value " +
                    "to${if (isNot) " not" else ""} be " +
                    "$expectedValue. ${message()}"
        }
    }

    companion object {
        fun expect(value: Any) = Expect(value)
    }

}
