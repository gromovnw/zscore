package it.gromov.zscore.http.dto

import it.gromov.zscore.util.JsonUtil

class HeartbeatRequestDto(
    private val platform: String,
    private val version: String,
    private val node: String
) {
    fun toJson(): String {
        return "{\"platform\":\"${JsonUtil.escape(platform)}\"," +
            "\"version\":\"${JsonUtil.escape(version)}\"," +
            "\"node\":\"${JsonUtil.escape(node)}\"}"
    }
}
