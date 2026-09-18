package it.gromov.zscore.util

object PlaceholderUtil {

    fun apply(text: String, placeholders: Map<String, String>): String {
        if (placeholders.isEmpty()) {
            return text
        }
        var result = text
        for ((key, value) in placeholders) {
            result = result.replace("{$key}", value)
        }
        return result
    }
}
