package it.gromov.zscore.storage

import java.sql.SQLException

object SqliteDialect : Dialect {

    override val type = StorageType.SQLITE
    override val forUpdate = ""
    override val serverTimeSql: String? = null

    override fun createStatements(tables: Tables): List<String> = listOf(
        "CREATE TABLE IF NOT EXISTS ${tables.players} (" +
            "uuid TEXT NOT NULL PRIMARY KEY, " +
            "last_nickname TEXT NOT NULL COLLATE NOCASE, " +
            "last_ip TEXT NOT NULL, " +
            "first_seen INTEGER NOT NULL, " +
            "last_seen INTEGER NOT NULL, " +
            "join_count INTEGER NOT NULL, " +
            "last_node TEXT NOT NULL, " +
            "reported_at INTEGER NOT NULL DEFAULT 0)",
        "CREATE INDEX IF NOT EXISTS ${tables.players}_nickname ON ${tables.players} (last_nickname)",
        "CREATE TABLE IF NOT EXISTS ${tables.names} (" +
            "uuid TEXT NOT NULL, " +
            "nickname TEXT NOT NULL COLLATE NOCASE, " +
            "first_seen INTEGER NOT NULL, " +
            "last_seen INTEGER NOT NULL, " +
            "seen_count INTEGER NOT NULL, " +
            "PRIMARY KEY (uuid, nickname))",
        "CREATE INDEX IF NOT EXISTS ${tables.names}_nickname ON ${tables.names} (nickname)",
        "CREATE TABLE IF NOT EXISTS ${tables.ips} (" +
            "uuid TEXT NOT NULL, " +
            "ip TEXT NOT NULL, " +
            "first_seen INTEGER NOT NULL, " +
            "last_seen INTEGER NOT NULL, " +
            "seen_count INTEGER NOT NULL, " +
            "PRIMARY KEY (uuid, ip))",
        "CREATE TABLE IF NOT EXISTS ${tables.outbox} (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "uuid TEXT NOT NULL, " +
            "nickname TEXT NOT NULL, " +
            "ip TEXT NOT NULL, " +
            "occurred_at INTEGER NOT NULL, " +
            "attempts INTEGER NOT NULL DEFAULT 0, " +
            "next_attempt_at INTEGER NOT NULL, " +
            "claimed_by TEXT, " +
            "claimed_until INTEGER NOT NULL DEFAULT 0)",
        "CREATE INDEX IF NOT EXISTS ${tables.outbox}_due ON ${tables.outbox} (next_attempt_at, claimed_until)"
    )

    override fun isDuplicateKey(error: SQLException): Boolean {
        return error.message?.contains("UNIQUE constraint failed") == true ||
            error.message?.contains("PRIMARY KEY") == true
    }

    override fun isRetryable(error: SQLException): Boolean {
        val message = error.message ?: return false
        return message.contains("SQLITE_BUSY") || message.contains("database is locked")
    }
}
