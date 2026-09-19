package it.gromov.zscore.spigot

import it.gromov.zscore.ZScoreBootstrap
import it.gromov.zscore.platform.IpUtil
import it.gromov.zscore.platform.ZScoreLogger
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.logging.Level

class ZScoreSpigotPlugin : JavaPlugin(), Listener, CommandExecutor, TabCompleter {

    private lateinit var bootstrap: ZScoreBootstrap

    override fun onEnable() {
        bootstrap = ZScoreBootstrap(
            dataFolder = dataFolder,
            logger = object : ZScoreLogger {
                override fun info(message: String) {
                    logger.log(Level.INFO, message)
                }

                override fun warn(message: String, error: Throwable?) {
                    logger.log(Level.WARNING, message, error)
                }
            },
            asyncExecutor = { runnable -> server.scheduler.runTaskAsynchronously(this, runnable) },
            platform = "spigot",
            currentVersion = description.version,
            updateAssetPrefix = "zScore-Spigot-",
            applyUpdate = ::applyUpdate
        )
        bootstrap.enable()

        server.pluginManager.registerEvents(this, this)
        getCommand("zscore")?.setExecutor(this)
        getCommand("zscore")?.setTabCompleter(this)
    }

    override fun onDisable() {
        bootstrap.disable()
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val ip = IpUtil.hostAddressOf(player.address) ?: return
        bootstrap.playerReportService.reportJoinAsync(player.name, player.uniqueId.toString(), ip)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        bootstrap.commandService.command.execute(SpigotSender(sender), args)
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        if (args.size != 1) {
            return emptyList()
        }
        return bootstrap.commandService.command.suggest(args[0])
    }

    private fun applyUpdate(bytes: ByteArray) {
        val updateDir = File(dataFolder.parentFile, "update")
        if (!updateDir.exists()) {
            updateDir.mkdirs()
        }
        File(updateDir, getFile().name).writeBytes(bytes)
    }
}
