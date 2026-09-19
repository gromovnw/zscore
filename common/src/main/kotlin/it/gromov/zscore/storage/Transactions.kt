package it.gromov.zscore.storage

import java.sql.Connection
import java.sql.SQLException

object Transactions {

    private const val ATTEMPTS = 6

    fun <T> run(connection: Connection, dialect: Dialect, block: () -> T): T {
        var attempt = 0
        while (true) {
            attempt++
            connection.autoCommit = false
            try {
                val result = block()
                connection.commit()
                return result
            } catch (error: SQLException) {
                runCatching { connection.rollback() }
                val retry = dialect.isDuplicateKey(error) || dialect.isRetryable(error)
                if (!retry || attempt >= ATTEMPTS) {
                    throw error
                }
                Thread.sleep(15L * attempt)
            } catch (error: RuntimeException) {
                runCatching { connection.rollback() }
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }
}
