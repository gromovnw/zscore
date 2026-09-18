package it.gromov.zscore.service.impl

import it.gromov.zscore.http.PlayerApiClient
import it.gromov.zscore.http.PlayerApiException
import it.gromov.zscore.platform.ZScoreLogger
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.PlayerReportService

class PlayerReportServiceImpl(
    private val configService: ConfigService,
    private val logger: ZScoreLogger,
    private val asyncExecutor: (Runnable) -> Unit
) : PlayerReportService {

    private lateinit var client: PlayerApiClient

    override fun enable() {
        client = PlayerApiClient(configService)
    }

    override fun reload() {
        client = PlayerApiClient(configService)
    }

    override fun disable() {
    }

    override fun reportJoinAsync(nickname: String, uuid: String, ip: String) {
        val api = configService.config.api
        if (!configService.config.general.enabled || !api.isConfigured()) {
            return
        }
        asyncExecutor(
            Runnable {
                try {
                    client.reportJoin(nickname, uuid, ip)
                } catch (exception: PlayerApiException) {
                    logger.warn("zScore: не удалось отправить данные об игроке $nickname ($ip): ${exception.message}")
                }
            }
        )
    }

    override fun testConnectionAsync(onResult: (success: Boolean, error: String?) -> Unit) {
        asyncExecutor(
            Runnable {
                try {
                    client.testConnection()
                    onResult(true, null)
                } catch (exception: Exception) {
                    onResult(false, exception.message)
                }
            }
        )
    }
}
