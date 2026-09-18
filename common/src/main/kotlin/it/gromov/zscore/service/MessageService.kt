package it.gromov.zscore.service

import it.gromov.zscore.platform.ZScoreSender

interface MessageService : Service {
    fun send(receiver: ZScoreSender, message: String)
    fun send(receiver: ZScoreSender, message: String, placeholders: Map<String, String>)
    fun format(message: String, placeholders: Map<String, String>): String
}
