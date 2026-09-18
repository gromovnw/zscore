package it.gromov.zscore.velocity

import com.velocitypowered.api.command.CommandSource
import it.gromov.zscore.platform.ZScoreSender
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class VelocitySender(private val source: CommandSource) : ZScoreSender {

    override fun sendMessage(message: String) {
        source.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message))
    }

    override fun hasPermission(permission: String): Boolean {
        return source.hasPermission(permission)
    }
}
