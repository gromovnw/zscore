package it.gromov.zscore.bungee

import it.gromov.zscore.ZScoreBootstrap
import it.gromov.zscore.platform.IpUtil
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class ZScoreBungeeListener(private val bootstrap: ZScoreBootstrap) : Listener {

    @EventHandler
    fun onPostLogin(event: PostLoginEvent) {
        val player: ProxiedPlayer = event.player
        val ip = IpUtil.hostAddressOf(player.socketAddress) ?: return
        bootstrap.playerReportService.reportJoinAsync(player.name, player.uniqueId.toString(), ip)
    }
}
