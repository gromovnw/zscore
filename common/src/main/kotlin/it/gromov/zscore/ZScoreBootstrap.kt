package it.gromov.zscore

import it.gromov.zscore.platform.ZScoreLogger
import it.gromov.zscore.service.CommandService
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.MessageService
import it.gromov.zscore.service.PlayerReportService
import it.gromov.zscore.service.UpdateService
import it.gromov.zscore.service.impl.CommandServiceImpl
import it.gromov.zscore.service.impl.ConfigServiceImpl
import it.gromov.zscore.service.impl.MessageServiceImpl
import it.gromov.zscore.service.impl.PlayerReportServiceImpl
import it.gromov.zscore.service.impl.UpdateServiceImpl
import java.io.File

class ZScoreBootstrap(
    dataFolder: File,
    logger: ZScoreLogger,
    asyncExecutor: (Runnable) -> Unit,
    currentVersion: String,
    updateAssetPrefix: String,
    applyUpdate: (ByteArray) -> Unit
) {
    val configService: ConfigService = ConfigServiceImpl(dataFolder)
    val messageService: MessageService = MessageServiceImpl(configService)
    val playerReportService: PlayerReportService = PlayerReportServiceImpl(configService, logger, asyncExecutor)
    val commandService: CommandService = CommandServiceImpl(configService, messageService, playerReportService, logger, ::reload)
    val updateService: UpdateService =
        UpdateServiceImpl(configService, logger, asyncExecutor, currentVersion, updateAssetPrefix, applyUpdate)

    fun enable() {
        configService.enable()
        messageService.enable()
        playerReportService.enable()
        commandService.enable()
        updateService.enable()
    }

    fun reload() {
        configService.reload()
        messageService.reload()
        playerReportService.reload()
        commandService.reload()
    }

    fun disable() {
        updateService.disable()
        commandService.disable()
        playerReportService.disable()
        messageService.disable()
        configService.disable()
    }
}
