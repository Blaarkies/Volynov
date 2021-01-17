package exceptions

class VectorIndexNotFoundException(
    private val hasMap: Map<Int, Any>,
    private val index: Int
) : Exception() {

    override val message: String
        get() = "Cannot find index \"$index\" in \"${hasMap.keys}\""

}
