package it.gromov.zscore.service

import it.gromov.zscore.storage.PlayerProfile
import it.gromov.zscore.storage.StorageStats

interface PlayerReportService : Service {
    fun reportJoinAsync(nickname: String, uuid: String, ip: String)
    fun testConnectionAsync(onResult: (success: Boolean, error: String?) -> Unit)
    fun lookupPlayerAsync(query: String, onResult: (profile: PlayerProfile?, error: String?) -> Unit)
    fun storageStatsAsync(onResult: (stats: StorageStats?, error: String?) -> Unit)
}
