package it.gromov.zscore.service

interface HeartbeatService : Service {
    fun state(): String
}
