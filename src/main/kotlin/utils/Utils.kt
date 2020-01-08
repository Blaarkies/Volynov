package utils

import java.util.*

object Utils {

    @Throws(Exception::class)
    fun loadResource(fileName: String): String {
        var result = ""
        Class.forName(Utils::class.java.name)
            .getResourceAsStream(fileName)
            .use {
                Scanner(it, "UTF-8").use { scanner -> result = scanner.useDelimiter("\\A").next() }
            }
        return result
    }

}
