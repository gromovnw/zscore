package it.gromov.zscore.http

import it.gromov.zscore.http.dto.PlayerSeenRequestDto
import it.gromov.zscore.service.ConfigService
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class PlayerApiClient(private val configService: ConfigService) {

    fun reportJoin(nickname: String, uuid: String, ip: String) {
        val body = PlayerSeenRequestDto(nickname, uuid, ip).toJson()
        try {
            execute("POST", "/api/plugin/players/seen", body)
        } catch (exception: IOException) {
            execute("POST", "/api/plugin/players/seen", body)
        }
    }

    fun testConnection() {
        execute("GET", "/api/plugin/players/ping", null)
    }

    private fun execute(method: String, path: String, body: String?) {
        val api = configService.config.api
        val timeoutMillis = maxOf(2, api.requestTimeoutSeconds) * 1000

        val connection = URL(API_BASE_URL + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = timeoutMillis
            connection.readTimeout = timeoutMillis
            connection.setRequestProperty("X-Shop-Id", api.shopId)
            connection.setRequestProperty("X-Server-Id", api.serverId)
            connection.setRequestProperty("X-Plugin-Key", api.pluginKey)
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "zScore-Plugin")

            if (body != null) {
                connection.doOutput = true
                val payload = body.toByteArray(StandardCharsets.UTF_8)
                connection.setFixedLengthStreamingMode(payload.size)
                connection.outputStream.use { it.write(payload) }
            }

            val status = connection.responseCode
            if (status < 200 || status >= 300) {
                throw PlayerApiException(status, readBody(connection.errorStream))
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun readBody(stream: InputStream?): String {
        if (stream == null) {
            return ""
        }
        return BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { it.readText() }
    }

    private companion object {
        const val API_BASE_URL = "https://api.zdonate.me"
    }
}
