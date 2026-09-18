package it.gromov.zscore.velocity

import com.google.inject.Inject
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import it.gromov.zscore.ZScoreBootstrap
import it.gromov.zscore.platform.ZScoreLogger
import org.slf4j.Logger
import java.nio.file.Path

@Plugin(
    id = "zscore",
    name = "zScore",
    version = "1.0.0",
    description = "Модуль сбора статистики zDonate: репортит факты подключения игроков к прокси",
    authors = ["gromov"]
)
class ZScoreVelocityPlugin @Inject constructor(
    private val server: ProxyServer,
    private val logger: Logger,
    @DataDirectory private val dataDirectory: Path
) {

    private lateinit var bootstrap: ZScoreBootstrap

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        bootstrap = ZScoreBootstrap(
            dataDirectory.toFile(),
            object : ZScoreLogger {
                override fun warn(message: String, error: Throwable?) {
                    if (error != null) logger.warn(message, error) else logger.warn(message)
                }
            }
        ) { runnable -> server.scheduler.buildTask(this, runnable).schedule() }
        bootstrap.enable()

        server.commandManager.register(
            server.commandManager.metaBuilder("zscore").build(),
            object : SimpleCommand {
                override fun execute(invocation: SimpleCommand.Invocation) {
                    bootstrap.commandService.command.execute(VelocitySender(invocation.source()), invocation.arguments())
                }

                override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
                    return invocation.source().hasPermission("zscore.command.admin")
                }

                override fun suggest(invocation: SimpleCommand.Invocation): List<String> {
                    val args = invocation.arguments()
                    if (args.size != 1) {
                        return emptyList()
                    }
                    return bootstrap.commandService.command.suggest(args[0])
                }
            }
        )
    }

    @Subscribe
    fun onPostLogin(event: PostLoginEvent) {
        val player = event.player
        val ip = player.remoteAddress.address.hostAddress
        bootstrap.playerReportService.reportJoinAsync(player.username, player.uniqueId.toString(), ip)
    }
}
