package it.gromov.zscore.bungee

import it.gromov.zscore.platform.ZScoreSender
import net.md_5.bungee.api.CommandSender

class BungeeSender(private val sender: CommandSender) : ZScoreSender {

    @Suppress("DEPRECATION")
    override fun sendMessage(message: String) {
        sender.sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }
}
