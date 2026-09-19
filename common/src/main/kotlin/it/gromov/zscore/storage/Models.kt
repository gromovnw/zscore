package it.gromov.zscore.storage

data class PlayerRecord(
    val uuid: String,
    val nickname: String,
    val ip: String,
    val firstSeen: Long,
    val lastSeen: Long,
    val joinCount: Long,
    val lastNode: String
)

data class NameEntry(val nickname: String, val firstSeen: Long, val lastSeen: Long, val seenCount: Long)

data class IpEntry(val ip: String, val firstSeen: Long, val lastSeen: Long, val seenCount: Long)

data class PlayerProfile(val player: PlayerRecord, val names: List<NameEntry>, val ips: List<IpEntry>)

data class JoinOutcome(val newPlayer: Boolean, val nicknameNew: Boolean, val ipNew: Boolean, val queued: Boolean)

data class PendingReport(val id: Long, val uuid: String, val nickname: String, val ip: String, val occurredAt: Long, val attempts: Int)

data class StorageStats(
    val type: StorageType,
    val nodeId: String,
    val players: Long,
    val pendingReports: Long,
    val clockOffsetMillis: Long
)

data class ReportPolicy(val resendAfterMillis: Long)
