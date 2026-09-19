package it.gromov.zscore.storage

import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.locks.ReentrantLock

class ConnectionHolder(private val connector: Connector, private val onOpen: (Connection) -> Unit) : AutoCloseable {

    private val lock = ReentrantLock()
    private var connection: Connection? = null

    fun <T> use(block: (Connection) -> T): T {
        lock.lock()
        try {
            val current = ensure()
            try {
                return block(current)
            } catch (error: SQLException) {
                if (!isAlive(current)) {
                    reset()
                }
                throw error
            }
        } finally {
            lock.unlock()
        }
    }

    override fun close() {
        lock.lock()
        try {
            reset()
        } finally {
            lock.unlock()
        }
    }

    private fun ensure(): Connection {
        val existing = connection
        if (existing != null && isAlive(existing)) {
            return existing
        }
        reset()
        val opened = try {
            connector.open()
        } catch (error: SQLException) {
            throw StorageException("не удалось подключиться к базе: ${error.message}", error)
        } catch (error: LinkageError) {
            throw StorageException("не удалось загрузить драйвер базы: ${error.message}", error)
        }
        try {
            onOpen(opened)
        } catch (error: Exception) {
            runCatching { opened.close() }
            throw if (error is StorageException) error else StorageException("не удалось подготовить базу: ${error.message}", error)
        }
        connection = opened
        return opened
    }

    private fun isAlive(candidate: Connection): Boolean {
        return try {
            !candidate.isClosed && candidate.isValid(2)
        } catch (error: SQLException) {
            false
        }
    }

    private fun reset() {
        val old = connection ?: return
        connection = null
        runCatching { old.close() }
    }
}
