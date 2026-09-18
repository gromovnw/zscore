package it.gromov.zscore.service.impl

import it.gromov.zscore.platform.ZScoreSender
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.MessageService
import it.gromov.zscore.util.ColorUtil
import it.gromov.zscore.util.PlaceholderUtil

class MessageServiceImpl(private val configService: ConfigService) : MessageService {

    override fun enable() {
    }

    override fun reload() {
    }

    override fun disable() {
    }

    override fun send(receiver: ZScoreSender, message: String) {
        send(receiver, message, emptyMap())
    }

    override fun send(receiver: ZScoreSender, message: String, placeholders: Map<String, String>) {
        val formatted = format(message, placeholders)
        if (formatted.isNotEmpty()) {
            receiver.sendMessage(formatted)
        }
    }

    override fun format(message: String, placeholders: Map<String, String>): String {
        if (message.isEmpty()) {
            return ""
        }
        val merged = HashMap(placeholders)
        merged.putIfAbsent("prefix", configService.messages.prefix)
        return ColorUtil.colorize(PlaceholderUtil.apply(message, merged))
    }
}
