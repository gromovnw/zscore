package it.gromov.zscore.storage

import java.sql.SQLException

object MysqlDialect : Dialect {

    private const val TABLE_OPTIONS = "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"

    override val type = StorageType.MYSQL
    override val forUpdate = " FOR UPDATE"
    override val serverTimeSql: String? = "SELECT CAST(UNIX_TIMESTAMP(NOW(3)) * 1000 AS SIGNED)"

    override fun createStatements(tables: Tables): List<String> = listOf(
        "CREATE TABLE IF NOT EXISTS ${tables.players} (" +
            "uuid CHAR(36) NOT NULL, " +
            "last_nickname VARCHAR(32) NOT NULL, " +
            "last_ip VARCHAR(45) NOT NULL, " +
            "first_seen BIGINT NOT NULL, " +
            "last_seen BIGINT NOT NULL, " +
            "join_count BIGINT NOT NULL, " +
            "last_node VARCHAR(64) NOT NULL, " +
            "reported_at BIGINT NOT NULL DEFAULT 0, " +
            "PRIMARY KEY (uuid), " +
            "KEY idx_nickname (last_nickname)) $TABLE_OPTIONS",
        "CREATE TABLE IF NOT EXISTS ${tables.names} (" +
            "uuid CHAR(36) NOT NULL, " +
            "nickname VARCHAR(32) NOT NULL, " +
            "first_seen BIGINT NOT NULL, " +
            "last_seen BIGINT NOT NULL, " +
            "seen_count BIGINT NOT NULL, " +
            "PRIMARY KEY (uuid, nickname), " +
            "KEY idx_nickname (nickname)) $TABLE_OPTIONS",
        "CREATE TABLE IF NOT EXISTS ${tables.ips} (" +
            "uuid CHAR(36) NOT NULL, " +
            "ip VARCHAR(45) NOT NULL, " +
            "first_seen BIGINT NOT NULL, " +
            "last_seen BIGINT NOT NULL, " +
            "seen_count BIGINT NOT NULL, " +
            "PRIMARY KEY (uuid, ip)) $TABLE_OPTIONS",
        "CREATE TABLE IF NOT EXISTS ${tables.outbox} (" +
            "id BIGINT NOT NULL AUTO_INCREMENT, " +
            "uuid CHAR(36) NOT NULL, " +
            "nickname VARCHAR(32) NOT NULL, " +
            "ip VARCHAR(45) NOT NULL, " +
            "occurred_at BIGINT NOT NULL, " +
            "attempts INT NOT NULL DEFAULT 0, " +
            "next_attempt_at BIGINT NOT NULL, " +
            "claimed_by VARCHAR(96) NULL, " +
            "claimed_until BIGINT NOT NULL DEFAULT 0, " +
            "PRIMARY KEY (id), " +
            "KEY idx_due (next_attempt_at, claimed_until)) $TABLE_OPTIONS"
    )

    override fun isDuplicateKey(error: SQLException): Boolean = error.errorCode == 1062

    override fun isRetryable(error: SQLException): Boolean {
        return error.errorCode == 1213 || error.errorCode == 1205 || error.sqlState == "40001"
    }
}
