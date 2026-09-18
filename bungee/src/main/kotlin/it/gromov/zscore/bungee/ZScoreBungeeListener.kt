package it.gromov.zscore.bungee

import it.gromov.zscore.ZScoreBootstrap
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.net.InetSocketAddress

class ZScoreBungeeListener(private val bootstrap: ZScoreBootstrap) : Listener {

    @EventHandler
    fun onPostLogin(event: PostLoginEvent) {
        val player: ProxiedPlayer = event.player
        val ip = (player.socketAddress as? InetSocketAddress)?.address?.hostAddress ?: return
        bootstrap.playerReportService.reportJoinAsync(player.name, player.uniqueId.toString(), ip)
    }
}
