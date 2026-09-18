package it.gromov.zscore.util

object ColorUtil {

    private val HEX_PATTERN = Regex("&#([A-Fa-f0-9]{6})")
    private const val VALID_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx"

    fun colorize(text: String): String {
        if (text.isEmpty()) {
            return text
        }
        return translateAlternateColorCodes(applyHex(text))
    }

    private fun applyHex(text: String): String {
        if (!text.contains("&#")) {
            return text
        }
        return HEX_PATTERN.replace(text) { match ->
            val hex = match.groupValues[1]
            val builder = StringBuilder("§x")
            for (c in hex) {
                builder.append('§').append(c)
            }
            builder.toString()
        }
    }

    private fun translateAlternateColorCodes(text: String): String {
        val chars = text.toCharArray()
        for (i in 0 until chars.size - 1) {
            if (chars[i] == '&' && VALID_CODES.indexOf(chars[i + 1]) > -1) {
                chars[i] = '§'
                chars[i + 1] = chars[i + 1].lowercaseChar()
            }
        }
        return String(chars)
    }
}
