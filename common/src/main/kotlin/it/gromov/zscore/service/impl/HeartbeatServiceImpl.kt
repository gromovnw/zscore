package it.gromov.zscore.service.impl

import it.gromov.zscore.http.PlayerApiClient
import it.gromov.zscore.platform.ZScoreLogger
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.HeartbeatService
import it.gromov.zscore.service.StorageService
import it.gromov.zscore.util.TaskScheduler
import java.util.concurrent.atomic.AtomicBoolean

class HeartbeatServiceImpl(
    private val configService: ConfigService,
    private val storageService: StorageService,
    private val client: PlayerApiClient,
    private val logger: ZScoreLogger,
    private val asyncExecutor: (Runnable) -> Unit,
    private val platform: String,
    private val version: String
) : HeartbeatService {

    private val scheduler = TaskScheduler("zScore-heartbeat")
    private val inFlight = AtomicBoolean(false)

    @Volatile
    private var lastSuccess = 0L

    @Volatile
    private var failing = false

    override fun enable() {
        schedule()
    }

    override fun reload() {
        scheduler.clear()
        schedule()
    }

    override fun disable() {
        scheduler.stop()
    }

    override fun state(): String {
        val config = configService.config
        if (!config.heartbeat.enabled || !config.general.enabled) {
            return "выключен"
        }
        if (lastSuccess == 0L) {
            return if (failing) "нет связи" else "ожидание"
        }
        val ago = (System.currentTimeMillis() - lastSuccess) / 1000
        return if (failing) "нет связи, последний успех $ago с назад" else "ок, последний $ago с назад"
    }

    private fun schedule() {
        scheduler.start()
        val seconds = configService.config.heartbeat.intervalSeconds.coerceIn(1, 30)
        scheduler.every(seconds * 1000L, Runnable { tick() })
    }

    private fun tick() {
        val config = configService.config
        if (!config.heartbeat.enabled || !config.general.enabled || !config.api.isConfigured()) {
            return
        }
        if (!inFlight.compareAndSet(false, true)) {
            return
        }
        asyncExecutor(
            Runnable {
                try {
                    client.heartbeat(platform, version, storageService.nodeId)
                    lastSuccess = System.currentTimeMillis()
                    if (failing) {
                        failing = false
                        logger.info("zScore: связь с zDonate восстановлена")
                    }
                } catch (error: Exception) {
                    if (!failing) {
                        failing = true
                        logger.warn("zScore: heartbeat не доходит до zDonate: ${error.message}")
                    }
                } finally {
                    inFlight.set(false)
                }
            }
        )
    }
}
