package it.gromov.zscore.service.impl

import it.gromov.zscore.http.PlayerApiClient
import it.gromov.zscore.platform.ZScoreLogger
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.DeliveryService
import it.gromov.zscore.service.PlayerReportService
import it.gromov.zscore.service.StorageService
import it.gromov.zscore.storage.PlayerProfile
import it.gromov.zscore.storage.ReportPolicy
import it.gromov.zscore.storage.StorageException
import it.gromov.zscore.storage.StorageStats

class PlayerReportServiceImpl(
    private val configService: ConfigService,
    private val storageService: StorageService,
    private val deliveryService: DeliveryService,
    private val client: PlayerApiClient,
    private val logger: ZScoreLogger,
    private val asyncExecutor: (Runnable) -> Unit
) : PlayerReportService {

    override fun enable() {
    }

    override fun reload() {
    }

    override fun disable() {
    }

    override fun reportJoinAsync(nickname: String, uuid: String, ip: String) {
        if (!configService.config.general.enabled) {
            return
        }
        asyncExecutor(
            Runnable {
                try {
                    val policy = ReportPolicy(configService.config.delivery.resendAfterMinutes * 60_000L)
                    val outcome = storageService.store().recordJoin(uuid, nickname, ip, policy)
                    if (outcome.queued) {
                        deliveryService.flushAsync()
                    }
                } catch (error: StorageException) {
                    logger.warn("zScore: хранилище недоступно (${error.message}), отправляем $nickname напрямую")
                    sendDirect(nickname, uuid, ip)
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

    override fun lookupPlayerAsync(query: String, onResult: (profile: PlayerProfile?, error: String?) -> Unit) {
        asyncExecutor(
            Runnable {
                try {
                    onResult(storageService.store().findProfile(query), null)
                } catch (error: StorageException) {
                    onResult(null, error.message)
                }
            }
        )
    }

    override fun storageStatsAsync(onResult: (stats: StorageStats?, error: String?) -> Unit) {
        asyncExecutor(
            Runnable {
                try {
                    onResult(storageService.store().stats(), null)
                } catch (error: StorageException) {
                    onResult(null, error.message)
                }
            }
        )
    }

    private fun sendDirect(nickname: String, uuid: String, ip: String) {
        if (!configService.config.api.isConfigured()) {
            return
        }
        try {
            client.reportJoin(nickname, uuid, ip)
        } catch (exception: Exception) {
            logger.warn("zScore: не удалось отправить данные об игроке $nickname ($ip): ${exception.message}")
        }
    }
}
