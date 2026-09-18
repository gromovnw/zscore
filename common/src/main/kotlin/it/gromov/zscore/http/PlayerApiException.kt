package it.gromov.zscore.http

class PlayerApiException(val statusCode: Int, responseBody: String) :
    Exception("HTTP $statusCode: ${trimmed(responseBody)}") {

    companion object {
        private fun trimmed(body: String): String {
            return if (body.length > 300) body.substring(0, 300) + "..." else body
        }
    }
}
