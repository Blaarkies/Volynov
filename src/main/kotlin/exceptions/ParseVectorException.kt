package exceptions

class ParseVectorException(
    private val line: String,
    private val index: Int,
    private val fromException: NumberFormatException? = null
) : Exception() {

    override val cause: Throwable?
        get() = fromException
    override val message: String
        get() = "Cannot parse vector from \"$line\", at line $index"
}
