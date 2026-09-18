package it.gromov.zscore.util

object JsonUtil {

    fun escape(value: String): String {
        val builder = StringBuilder(value.length + 8)
        for (c in value) {
            when (c) {
                '"' -> builder.append("\\\"")
                '\\' -> builder.append("\\\\")
                '\n' -> builder.append("\\n")
                '\r' -> builder.append("\\r")
                '\t' -> builder.append("\\t")
                else -> if (c.code < 0x20) builder.append("\\u%04x".format(c.code)) else builder.append(c)
            }
        }
        return builder.toString()
    }
}
