package it.gromov.zscore.spigot

import it.gromov.zscore.platform.ZScoreSender
import org.bukkit.command.CommandSender

class SpigotSender(private val sender: CommandSender) : ZScoreSender {

    override fun sendMessage(message: String) {
        sender.sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }
}
