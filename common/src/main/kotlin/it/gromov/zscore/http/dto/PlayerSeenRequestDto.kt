package it.gromov.zscore.http.dto

import it.gromov.zscore.util.JsonUtil

class PlayerSeenRequestDto(
    private val nickname: String,
    private val uuid: String,
    private val ip: String
) {
    fun toJson(): String {
        return "{\"nickname\":\"${JsonUtil.escape(nickname)}\"," +
            "\"uuid\":\"${JsonUtil.escape(uuid)}\"," +
            "\"ip\":\"${JsonUtil.escape(ip)}\"}"
    }
}
