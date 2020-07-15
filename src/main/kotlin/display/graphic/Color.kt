package display.graphic

class Color {

    val red: Float
    val green: Float
    val blue: Float
    val alpha: Float

    constructor(red: Float = 0f, green: Float = 0f, blue: Float = 0f, alpha: Float = 1f) {
        this.red = getSafeValue(red)
        this.green = getSafeValue(green)
        this.blue = getSafeValue(blue)
        this.alpha = getSafeValue(alpha)
    }

    operator fun times(value: Float) =
        Color(this.red * value, this.green * value, this.blue * value, this.alpha * value)

    operator fun plus(color: Color) =
        Color(this.red + color.red, this.green + color.green, this.blue + color.blue, this.alpha + color.alpha)

    constructor(red: Int, green: Int, blue: Int, alpha: Int) : this(
        getIntToFloat(red),
        getIntToFloat(green),
        getIntToFloat(blue),
        getIntToFloat(alpha)
    )

    /**
     * HEX color value requires format #rrggbbaa
     */
    constructor(hexValue: String) {
        val (red, green, blue, alpha) = hexValue.dropWhile { it == '#' }.chunked(2)
        this.red = getHexToFloat(red)
        this.green = getHexToFloat(green)
        this.blue = getHexToFloat(blue)
        this.alpha = getHexToFloat(alpha)
    }

    fun setAlpha(newValue: Float): Color = Color(red, green, blue, newValue)

    companion object {

        val WHITE = Color(1f, 1f, 1f, 1f)
        val GRAY = Color(.5f, .5f, .5f, 1f)
        val BLACK = Color(0f, 0f, 0f, 1f)
        val RED = Color(1f, 0f, 0f, 1f)
        val ORANGE = Color(1f, .5f, 0f, 1f)
        val YELLOW = Color(1f, 1f, 0f, 1f)
        val LIME = Color(1f, 1f, 0f, 1f)
        val GREEN = Color(0f, 1f, 0f, 1f)
        val CYAN = Color(0f, 1f, 1f, 1f)
        val BLUE = Color(0f, 0f, 1f, 1f)
        val MAGENTA = Color(1f, 0f, 1f, 1f)
        val TRANSPARENT = Color(0f, 0f, 0f, 0f)

//        val HEX = Color("#01FF6499")
//        val HSV = createFromHsv(.5f, 1f, .5f, 1f)

        val PALETTE8 = (1..8).map { createFromHsv(it / 8f, 1f, .5f) }
        val PALETTE_TINT10 =
            (1..8).map { createFromHsv(it / 8f, 1f, .8f) } + listOf(WHITE, createFromHsv(0f, 0f, .7f)).toList()

        private fun getSafeValue(value: Float) = value.coerceIn(0f, 1f)

        private fun getIntToFloat(value: Int) = getSafeValue(value / 255f)

        private fun getHexToFloat(value: String) = value.toShort(16).toFloat().let { getSafeValue(it / 255f) }

        fun createFromHsv(hue: Float = 0f, saturation: Float = 0f, light: Float = 0f, alpha: Float = 1f): Color {
            val rgbList = hslToRgb(hue, saturation, light)
            return Color(rgbList[0], rgbList[1], rgbList[2], alpha)
        }

        private fun hslToRgb(hue: Float, saturation: Float, light: Float): List<Float> {
            return if (saturation == 0f) {
                listOf(light, light, light)
            } else {
                val q = if (light < .5f) light * (1 + saturation) else light + saturation - light * saturation
                val p = 2 * light - q
                listOf(
                    hueToRgb(p, q, hue + 1f / 3f),
                    hueToRgb(p, q, hue),
                    hueToRgb(p, q, hue - 1f / 3f)
                )
            }
        }

        private fun hueToRgb(p: Float, q: Float, t: Float): Float {
            var t = t
            if (t < 0f) t += 1f
            if (t > 1f) t -= 1f
            if (t < 1f / 6f) return p + (q - p) * 6f * t
            if (t < 1f / 2f) return q
            return if (t < 2f / 3f) p + (q - p) * (2f / 3f - t) * 6f else p
        }

    }

}
