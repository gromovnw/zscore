package it.gromov.zscore.storage

import java.sql.Connection

class ClockSync {

    @Volatile
    var offsetMillis: Long = 0
        private set

    fun now(): Long = System.currentTimeMillis() + offsetMillis

    fun sync(connection: Connection, dialect: Dialect) {
        val sql = dialect.serverTimeSql ?: return
        var bestRoundTrip = Long.MAX_VALUE
        var bestOffset = offsetMillis
        repeat(SAMPLES) {
            val before = System.currentTimeMillis()
            val serverNow = connection.createStatement().use { statement ->
                statement.executeQuery(sql).use { rows -> if (rows.next()) rows.getLong(1) else return }
            }
            val after = System.currentTimeMillis()
            val roundTrip = after - before
            if (roundTrip < bestRoundTrip) {
                bestRoundTrip = roundTrip
                bestOffset = serverNow - (before + roundTrip / 2)
            }
        }
        offsetMillis = bestOffset
    }

    private companion object {
        const val SAMPLES = 3
    }
}
