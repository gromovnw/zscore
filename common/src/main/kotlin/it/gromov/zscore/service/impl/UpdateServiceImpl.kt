package it.gromov.zscore.service.impl

import it.gromov.zscore.platform.ZScoreLogger
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.UpdateService
import it.gromov.zscore.update.GithubUpdateClient

class UpdateServiceImpl(
    private val configService: ConfigService,
    private val logger: ZScoreLogger,
    private val asyncExecutor: (Runnable) -> Unit,
    private val currentVersion: String,
    private val assetPrefix: String,
    private val applyUpdate: (ByteArray) -> Unit
) : UpdateService {

    private val client = GithubUpdateClient()

    override fun enable() {
        if (!configService.config.update.enabled) {
            return
        }
        asyncExecutor(Runnable(this::checkAndDownload))
    }

    override fun reload() {
    }

    override fun disable() {
    }

    private fun checkAndDownload() {
        try {
            val release = client.fetchLatestRelease()
            val remoteVersion = stripLeadingV(release.tagName ?: return)

            if (compareVersions(remoteVersion, currentVersion) <= 0) {
                return
            }

            val downloadUrl = client.findAssetDownloadUrl(release, assetPrefix)
            val bytes = client.downloadAsset(downloadUrl)
            applyUpdate(bytes)

            logger.info(
                "Доступна новая версия zScore $remoteVersion (текущая: $currentVersion). " +
                    "Обновление скачано и будет применено при следующем перезапуске."
            )
        } catch (exception: Exception) {
            logger.warn("Не удалось проверить обновления zScore: ${exception.message}", exception)
        }
    }

    private fun stripLeadingV(version: String): String {
        return if (version.startsWith("v") || version.startsWith("V")) version.substring(1) else version
    }

    private fun compareVersions(a: String, b: String): Int {
        val partsA = a.split(".")
        val partsB = b.split(".")
        val length = maxOf(partsA.size, partsB.size)
        for (i in 0 until length) {
            val valueA = partsA.getOrNull(i)?.let(::parsePart) ?: 0
            val valueB = partsB.getOrNull(i)?.let(::parsePart) ?: 0
            if (valueA != valueB) {
                return valueA.compareTo(valueB)
            }
        }
        return 0
    }

    private fun parsePart(part: String): Int {
        return part.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
    }
}
