package it.gromov.zscore.service.impl

import it.gromov.zscore.http.PlayerApiClient
import it.gromov.zscore.http.PlayerApiException
import it.gromov.zscore.platform.ZScoreLogger
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.DeliveryService
import it.gromov.zscore.service.StorageService
import it.gromov.zscore.storage.PendingReport
import it.gromov.zscore.storage.StorageException
import it.gromov.zscore.util.TaskScheduler
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class DeliveryServiceImpl(
    private val configService: ConfigService,
    private val storageService: StorageService,
    private val client: PlayerApiClient,
    private val logger: ZScoreLogger,
    private val asyncExecutor: (Runnable) -> Unit
) : DeliveryService {

    private val scheduler = TaskScheduler("zScore-delivery")
    private val flushing = AtomicBoolean(false)

    @Volatile
    private var storageFailing = false

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

    override fun flushAsync() {
        asyncExecutor(Runnable { flush() })
    }

    private fun schedule() {
        scheduler.start()
        val delivery = configService.config.delivery
        scheduler.every(delivery.flushIntervalSeconds * 1000L, Runnable { flushAsync() })
        scheduler.every(MAINTENANCE_MILLIS, Runnable { asyncExecutor(Runnable { maintenance() }) })
    }

    private fun flush() {
        val config = configService.config
        if (!config.general.enabled || !config.api.isConfigured()) {
            return
        }
        if (!flushing.compareAndSet(false, true)) {
            return
        }
        try {
            val store = storageService.store()
            var rounds = 0
            while (rounds < MAX_ROUNDS) {
                rounds++
                val batch = store.claimPending(BATCH_SIZE, LEASE_MILLIS)
                storageRecovered()
                if (batch.isEmpty()) {
                    return
                }
                if (!deliver(batch)) {
                    return
                }
            }
        } catch (error: StorageException) {
            storageFailed(error)
        } finally {
            flushing.set(false)
        }
    }

    private fun deliver(batch: List<PendingReport>): Boolean {
        val store = storageService.store()
        val maxAttempts = configService.config.delivery.retryMaxAttempts
        for ((index, report) in batch.withIndex()) {
            try {
                client.reportJoin(report.nickname, report.uuid, report.ip)
                store.completePending(report.id)
            } catch (error: PlayerApiException) {
                if (error.statusCode == 400 || report.attempts + 1 >= maxAttempts) {
                    logger.warn("zScore: отчёт по игроку ${report.nickname} отброшен: ${error.message}")
                    store.completePending(report.id)
                } else {
                    store.retryPending(report.id, backoff(report.attempts))
                }
            } catch (error: IOException) {
                logger.warn("zScore: zDonate недоступен, отчёты останутся в очереди: ${error.message}")
                if (report.attempts + 1 >= maxAttempts) {
                    store.completePending(report.id)
                } else {
                    store.retryPending(report.id, backoff(report.attempts))
                }
                store.releasePending(batch.drop(index + 1).map { it.id })
                return false
            }
        }
        return true
    }

    private fun maintenance() {
        try {
            val store = storageService.store()
            store.syncClock()
            val days = configService.config.delivery.retentionDays
            val purged = store.purgePending(days * DAY_MILLIS)
            if (purged > 0) {
                logger.warn("zScore: удалено $purged неотправленных отчётов старше $days дн.")
            }
            storageRecovered()
        } catch (error: StorageException) {
            storageFailed(error)
        }
    }

    private fun backoff(attempts: Int): Long {
        val exponent = minOf(attempts, 8)
        return minOf(BASE_BACKOFF_MILLIS shl exponent, MAX_BACKOFF_MILLIS)
    }

    private fun storageFailed(error: StorageException) {
        if (!storageFailing) {
            storageFailing = true
            logger.warn("zScore: хранилище недоступно, игроки отправляются напрямую: ${error.message}")
        }
    }

    private fun storageRecovered() {
        if (storageFailing) {
            storageFailing = false
            logger.info("zScore: хранилище снова доступно")
        }
    }

    private companion object {
        const val BATCH_SIZE = 25
        const val MAX_ROUNDS = 20
        const val LEASE_MILLIS = 60_000L
        const val MAINTENANCE_MILLIS = 5 * 60_000L
        const val DAY_MILLIS = 24 * 60 * 60_000L
        const val BASE_BACKOFF_MILLIS = 15_000L
        const val MAX_BACKOFF_MILLIS = 60 * 60_000L
    }
}
