package it.gromov.zscore.util

import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class TaskScheduler(private val threadName: String) {

    private var executor: ScheduledThreadPoolExecutor? = null
    private val tasks = mutableListOf<ScheduledFuture<*>>()

    @Synchronized
    fun start() {
        if (executor != null) {
            return
        }
        executor = ScheduledThreadPoolExecutor(1) { runnable ->
            Thread(runnable, threadName).apply { isDaemon = true }
        }.apply { removeOnCancelPolicy = true }
    }

    @Synchronized
    fun every(periodMillis: Long, task: Runnable) {
        val pool = executor ?: return
        tasks.add(
            pool.scheduleWithFixedDelay(
                {
                    try {
                        task.run()
                    } catch (ignored: Throwable) {
                    }
                },
                periodMillis,
                periodMillis,
                TimeUnit.MILLISECONDS
            )
        )
    }

    @Synchronized
    fun clear() {
        tasks.forEach { it.cancel(false) }
        tasks.clear()
    }

    @Synchronized
    fun stop() {
        clear()
        executor?.shutdownNow()
        executor = null
    }
}
