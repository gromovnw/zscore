package it.gromov.zscore.service

interface PlayerReportService : Service {
    fun reportJoinAsync(nickname: String, uuid: String, ip: String)
    fun testConnectionAsync(onResult: (success: Boolean, error: String?) -> Unit)
}
