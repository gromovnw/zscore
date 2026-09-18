package it.gromov.zscore.bungee

import it.gromov.zscore.ZScoreBootstrap
import it.gromov.zscore.platform.ZScoreLogger
import java.util.logging.Level
import net.md_5.bungee.api.plugin.Plugin

class ZScoreBungeePlugin : Plugin() {

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
            asyncExecutor = { runnable -> proxy.scheduler.runAsync(this, runnable) },
            currentVersion = description.version,
            updateAssetPrefix = "zScore-Bungee-",
            applyUpdate = { bytes -> file.writeBytes(bytes) }
        )
        bootstrap.enable()

        proxy.pluginManager.registerListener(this, ZScoreBungeeListener(bootstrap))
        proxy.pluginManager.registerCommand(this, ZScoreBungeeCommand(bootstrap))
    }
}
