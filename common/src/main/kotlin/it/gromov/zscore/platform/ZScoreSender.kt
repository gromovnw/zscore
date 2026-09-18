package it.gromov.zscore.platform

interface ZScoreSender {
    fun sendMessage(message: String)
    fun hasPermission(permission: String): Boolean
}
