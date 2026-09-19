package it.gromov.zscore.storage

import java.sql.Connection
import java.sql.SQLException

class PlayerStore(
    connector: Connector,
    private val dialect: Dialect,
    tablePrefix: String,
    val nodeId: String,
    val clock: ClockSync = ClockSync()
) : AutoCloseable {

    private val tables = Tables(tablePrefix)
    private val holder = ConnectionHolder(connector, ::prepare)

    fun recordJoin(uuid: String, nickname: String, ip: String, policy: ReportPolicy): JoinOutcome {
        val name = nickname.take(MAX_NICKNAME)
        return query { connection ->
            val now = clock.now()
            Transactions.run(connection, dialect) { join(connection, uuid, name, ip, now, policy) }
        }
    }

    fun findProfile(search: String): PlayerProfile? {
        return query { connection ->
            val uuid = resolveUuid(connection, search.trim()) ?: return@query null
            val player = readPlayer(connection, uuid) ?: return@query null
            PlayerProfile(player, readNames(connection, uuid), readIps(connection, uuid))
        }
    }

    fun claimPending(limit: Int, leaseMillis: Long): List<PendingReport> {
        return query { connection ->
            val now = clock.now()
            val token = "$nodeId:${System.nanoTime()}"
            val claimed = connection.prepareStatement(
                "UPDATE ${tables.outbox} SET claimed_by = ?, claimed_until = ? " +
                    "WHERE claimed_until < ? AND next_attempt_at <= ? AND id IN (" +
                    "SELECT id FROM (SELECT id FROM ${tables.outbox} " +
                    "WHERE claimed_until < ? AND next_attempt_at <= ? ORDER BY id LIMIT ?) AS due)"
            ).use { statement ->
                statement.setString(1, token)
                statement.setLong(2, now + leaseMillis)
                statement.setLong(3, now)
                statement.setLong(4, now)
                statement.setLong(5, now)
                statement.setLong(6, now)
                statement.setInt(7, limit)
                statement.executeUpdate()
            }
            if (claimed == 0) {
                return@query emptyList<PendingReport>()
            }
            connection.prepareStatement(
                "SELECT id, uuid, nickname, ip, occurred_at, attempts FROM ${tables.outbox} WHERE claimed_by = ? ORDER BY id"
            ).use { statement ->
                statement.setString(1, token)
                statement.executeQuery().use { rows ->
                    val list = mutableListOf<PendingReport>()
                    while (rows.next()) {
                        list.add(
                            PendingReport(rows.getLong(1), rows.getString(2), rows.getString(3), rows.getString(4), rows.getLong(5), rows.getInt(6))
                        )
                    }
                    list
                }
            }
        }
    }

    fun completePending(id: Long) {
        query { connection ->
            connection.prepareStatement("DELETE FROM ${tables.outbox} WHERE id = ?").use { statement ->
                statement.setLong(1, id)
                statement.executeUpdate()
            }
        }
    }

    fun retryPending(id: Long, delayMillis: Long) {
        query { connection ->
            connection.prepareStatement(
                "UPDATE ${tables.outbox} SET attempts = attempts + 1, next_attempt_at = ?, claimed_by = NULL, claimed_until = 0 WHERE id = ?"
            ).use { statement ->
                statement.setLong(1, clock.now() + delayMillis)
                statement.setLong(2, id)
                statement.executeUpdate()
            }
        }
    }

    fun releasePending(ids: Collection<Long>) {
        if (ids.isEmpty()) {
            return
        }
        query { connection ->
            connection.prepareStatement("UPDATE ${tables.outbox} SET claimed_by = NULL, claimed_until = 0 WHERE id = ?").use { statement ->
                for (id in ids) {
                    statement.setLong(1, id)
                    statement.addBatch()
                }
                statement.executeBatch()
            }
        }
    }

    fun purgePending(olderThanMillis: Long): Int {
        return query { connection ->
            connection.prepareStatement("DELETE FROM ${tables.outbox} WHERE occurred_at < ?").use { statement ->
                statement.setLong(1, clock.now() - olderThanMillis)
                statement.executeUpdate()
            }
        }
    }

    fun syncClock() {
        query { connection -> clock.sync(connection, dialect) }
    }

    fun stats(): StorageStats {
        return query { connection ->
            StorageStats(
                dialect.type,
                nodeId,
                count(connection, tables.players),
                count(connection, tables.outbox),
                clock.offsetMillis
            )
        }
    }

    override fun close() {
        holder.close()
    }

    private fun <T> query(block: (Connection) -> T): T {
        try {
            return holder.use(block)
        } catch (error: SQLException) {
            throw StorageException("ошибка запроса к базе: ${error.message}", error)
        }
    }

    private fun prepare(connection: Connection) {
        SchemaManager.migrate(connection, dialect, tables)
        clock.sync(connection, dialect)
    }

    private fun join(connection: Connection, uuid: String, nickname: String, ip: String, now: Long, policy: ReportPolicy): JoinOutcome {
        val existing = connection.prepareStatement(
            "SELECT last_nickname, last_ip, first_seen, last_seen, reported_at FROM ${tables.players} WHERE uuid = ?${dialect.forUpdate}"
        ).use { statement ->
            statement.setString(1, uuid)
            statement.executeQuery().use { rows ->
                if (rows.next()) Known(rows.getString(1), rows.getString(2), rows.getLong(3), rows.getLong(4), rows.getLong(5)) else null
            }
        }

        if (existing == null) {
            connection.prepareStatement(
                "INSERT INTO ${tables.players} (uuid, last_nickname, last_ip, first_seen, last_seen, join_count, last_node, reported_at) " +
                    "VALUES (?, ?, ?, ?, ?, 1, ?, 0)"
            ).use { statement ->
                statement.setString(1, uuid)
                statement.setString(2, nickname)
                statement.setString(3, ip)
                statement.setLong(4, now)
                statement.setLong(5, now)
                statement.setString(6, nodeId)
                statement.executeUpdate()
            }
        } else {
            val newest = now >= existing.lastSeen
            connection.prepareStatement(
                "UPDATE ${tables.players} SET last_nickname = ?, last_ip = ?, first_seen = ?, last_seen = ?, " +
                    "join_count = join_count + 1, last_node = ? WHERE uuid = ?"
            ).use { statement ->
                statement.setString(1, if (newest) nickname else existing.nickname)
                statement.setString(2, if (newest) ip else existing.ip)
                statement.setLong(3, minOf(existing.firstSeen, now))
                statement.setLong(4, maxOf(existing.lastSeen, now))
                statement.setString(5, nodeId)
                statement.setString(6, uuid)
                statement.executeUpdate()
            }
        }

        val nicknameNew = touch(connection, tables.names, "nickname", uuid, nickname, now)
        val ipNew = touch(connection, tables.ips, "ip", uuid, ip, now)

        val reportedAt = existing?.reportedAt ?: 0L
        val due = existing == null || nicknameNew || ipNew || reportedAt == 0L || now - reportedAt >= policy.resendAfterMillis
        if (due) {
            enqueue(connection, uuid, nickname, ip, now)
            connection.prepareStatement("UPDATE ${tables.players} SET reported_at = ? WHERE uuid = ?").use { statement ->
                statement.setLong(1, now)
                statement.setString(2, uuid)
                statement.executeUpdate()
            }
        }
        return JoinOutcome(existing == null, nicknameNew, ipNew, due)
    }

    private fun touch(connection: Connection, table: String, column: String, uuid: String, value: String, now: Long): Boolean {
        val known = connection.prepareStatement("SELECT first_seen, last_seen FROM $table WHERE uuid = ? AND $column = ?").use { statement ->
            statement.setString(1, uuid)
            statement.setString(2, value)
            statement.executeQuery().use { rows -> if (rows.next()) rows.getLong(1) to rows.getLong(2) else null }
        }
        if (known == null) {
            connection.prepareStatement("INSERT INTO $table (uuid, $column, first_seen, last_seen, seen_count) VALUES (?, ?, ?, ?, 1)").use { statement ->
                statement.setString(1, uuid)
                statement.setString(2, value)
                statement.setLong(3, now)
                statement.setLong(4, now)
                statement.executeUpdate()
            }
            return true
        }
        connection.prepareStatement(
            "UPDATE $table SET first_seen = ?, last_seen = ?, seen_count = seen_count + 1 WHERE uuid = ? AND $column = ?"
        ).use { statement ->
            statement.setLong(1, minOf(known.first, now))
            statement.setLong(2, maxOf(known.second, now))
            statement.setString(3, uuid)
            statement.setString(4, value)
            statement.executeUpdate()
        }
        return false
    }

    private fun enqueue(connection: Connection, uuid: String, nickname: String, ip: String, now: Long) {
        connection.prepareStatement(
            "INSERT INTO ${tables.outbox} (uuid, nickname, ip, occurred_at, attempts, next_attempt_at, claimed_by, claimed_until) " +
                "VALUES (?, ?, ?, ?, 0, ?, NULL, 0)"
        ).use { statement ->
            statement.setString(1, uuid)
            statement.setString(2, nickname)
            statement.setString(3, ip)
            statement.setLong(4, now)
            statement.setLong(5, now)
            statement.executeUpdate()
        }
    }

    private fun resolveUuid(connection: Connection, search: String): String? {
        if (search.length == UUID_LENGTH && search.count { it == '-' } == 4) {
            return search.lowercase()
        }
        val sql = "SELECT uuid FROM ${tables.players} WHERE last_nickname = ? " +
            "UNION SELECT uuid FROM ${tables.names} WHERE nickname = ? LIMIT 1"
        return connection.prepareStatement(sql).use { statement ->
            statement.setString(1, search)
            statement.setString(2, search)
            statement.executeQuery().use { rows -> if (rows.next()) rows.getString(1) else null }
        }
    }

    private fun readPlayer(connection: Connection, uuid: String): PlayerRecord? {
        return connection.prepareStatement(
            "SELECT uuid, last_nickname, last_ip, first_seen, last_seen, join_count, last_node FROM ${tables.players} WHERE uuid = ?"
        ).use { statement ->
            statement.setString(1, uuid)
            statement.executeQuery().use { rows ->
                if (rows.next()) {
                    PlayerRecord(rows.getString(1), rows.getString(2), rows.getString(3), rows.getLong(4), rows.getLong(5), rows.getLong(6), rows.getString(7))
                } else {
                    null
                }
            }
        }
    }

    private fun readNames(connection: Connection, uuid: String): List<NameEntry> {
        return connection.prepareStatement(
            "SELECT nickname, first_seen, last_seen, seen_count FROM ${tables.names} WHERE uuid = ? ORDER BY first_seen"
        ).use { statement ->
            statement.setString(1, uuid)
            statement.executeQuery().use { rows ->
                val list = mutableListOf<NameEntry>()
                while (rows.next()) list.add(NameEntry(rows.getString(1), rows.getLong(2), rows.getLong(3), rows.getLong(4)))
                list
            }
        }
    }

    private fun readIps(connection: Connection, uuid: String): List<IpEntry> {
        return connection.prepareStatement(
            "SELECT ip, first_seen, last_seen, seen_count FROM ${tables.ips} WHERE uuid = ? ORDER BY last_seen DESC"
        ).use { statement ->
            statement.setString(1, uuid)
            statement.executeQuery().use { rows ->
                val list = mutableListOf<IpEntry>()
                while (rows.next()) list.add(IpEntry(rows.getString(1), rows.getLong(2), rows.getLong(3), rows.getLong(4)))
                list
            }
        }
    }

    private fun count(connection: Connection, table: String): Long {
        return connection.createStatement().use { statement ->
            statement.executeQuery("SELECT COUNT(*) FROM $table").use { rows -> if (rows.next()) rows.getLong(1) else 0L }
        }
    }

    private class Known(val nickname: String, val ip: String, val firstSeen: Long, val lastSeen: Long, val reportedAt: Long)

    private companion object {
        const val MAX_NICKNAME = 32
        const val UUID_LENGTH = 36
    }
}
