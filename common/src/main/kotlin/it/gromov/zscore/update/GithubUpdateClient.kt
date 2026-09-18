package it.gromov.zscore.update

import com.google.gson.Gson
import it.gromov.zscore.update.dto.GithubReleaseDto
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class GithubUpdateClient {

    private val gson = Gson()

    fun fetchLatestRelease(): GithubReleaseDto {
        val connection = URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = TIMEOUT_MILLIS
        connection.readTimeout = TIMEOUT_MILLIS
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("User-Agent", "zScore-Plugin")

        try {
            val status = connection.responseCode
            val body = readBody(if (status in 200..299) connection.inputStream else connection.errorStream)
            if (status != 200) {
                throw GithubUpdateException("HTTP $status при запросе последнего релиза: ${trimmed(body)}")
            }
            val release = gson.fromJson(body, GithubReleaseDto::class.java)
            if (release?.tagName == null) {
                throw GithubUpdateException("Пустой ответ GitHub API")
            }
            return release
        } finally {
            connection.disconnect()
        }
    }

    fun findAssetDownloadUrl(release: GithubReleaseDto, assetPrefix: String): String {
        return release.assets.firstOrNull { it.name.startsWith(assetPrefix) }?.browserDownloadUrl
            ?: throw GithubUpdateException("В релизе ${release.tagName} не найден файл с префиксом $assetPrefix")
    }

    fun downloadAsset(url: String): ByteArray {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = TIMEOUT_MILLIS
        connection.readTimeout = TIMEOUT_MILLIS
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("User-Agent", "zScore-Plugin")

        try {
            val status = connection.responseCode
            if (status != 200) {
                throw GithubUpdateException("HTTP $status при скачивании обновления")
            }
            return readBytes(connection.inputStream)
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

    private fun readBytes(stream: InputStream): ByteArray {
        val buffer = ByteArrayOutputStream()
        val chunk = ByteArray(8192)
        var read: Int
        while (stream.read(chunk).also { read = it } != -1) {
            buffer.write(chunk, 0, read)
        }
        return buffer.toByteArray()
    }

    private fun trimmed(body: String): String {
        return if (body.length > 300) body.substring(0, 300) + "..." else body
    }

    private companion object {
        const val LATEST_RELEASE_URL = "https://api.github.com/repos/gromovnw/zscore/releases/latest"
        const val TIMEOUT_MILLIS = 10000
    }
}
