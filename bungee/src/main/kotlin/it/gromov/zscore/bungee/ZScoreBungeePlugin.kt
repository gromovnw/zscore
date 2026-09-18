package it.gromov.zscore.bungee

import it.gromov.zscore.ZScoreBootstrap
import it.gromov.zscore.platform.ZScoreLogger
import java.util.logging.Level
import net.md_5.bungee.api.plugin.Plugin

class ZScoreBungeePlugin : Plugin() {

    private lateinit var bootstrap: ZScoreBootstrap

    override fun onEnable() {
        bootstrap = ZScoreBootstrap(
            dataFolder,
            object : ZScoreLogger {
                override fun warn(message: String, error: Throwable?) {
                    logger.log(Level.WARNING, message, error)
                }
            }
        ) { runnable -> proxy.scheduler.runAsync(this, runnable) }
        bootstrap.enable()

        proxy.pluginManager.registerListener(this, ZScoreBungeeListener(bootstrap))
        proxy.pluginManager.registerCommand(this, ZScoreBungeeCommand(bootstrap))
    }
}
