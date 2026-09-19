package it.gromov.zscore

import it.gromov.zscore.http.PlayerApiClient
import it.gromov.zscore.platform.ZScoreLogger
import it.gromov.zscore.service.CommandService
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.DeliveryService
import it.gromov.zscore.service.HeartbeatService
import it.gromov.zscore.service.MessageService
import it.gromov.zscore.service.PlayerReportService
import it.gromov.zscore.service.StorageService
import it.gromov.zscore.service.UpdateService
import it.gromov.zscore.service.impl.CommandServiceImpl
import it.gromov.zscore.service.impl.ConfigServiceImpl
import it.gromov.zscore.service.impl.DeliveryServiceImpl
import it.gromov.zscore.service.impl.HeartbeatServiceImpl
import it.gromov.zscore.service.impl.MessageServiceImpl
import it.gromov.zscore.service.impl.PlayerReportServiceImpl
import it.gromov.zscore.service.impl.StorageServiceImpl
import it.gromov.zscore.service.impl.UpdateServiceImpl
import java.io.File

class ZScoreBootstrap(
    dataFolder: File,
    logger: ZScoreLogger,
    asyncExecutor: (Runnable) -> Unit,
    platform: String,
    currentVersion: String,
    updateAssetPrefix: String,
    applyUpdate: (ByteArray) -> Unit
) {
    private val client: PlayerApiClient

    val configService: ConfigService = ConfigServiceImpl(dataFolder)
    val messageService: MessageService = MessageServiceImpl(configService)
    val storageService: StorageService = StorageServiceImpl(configService, logger, dataFolder, platform)
    val deliveryService: DeliveryService
    val heartbeatService: HeartbeatService
    val playerReportService: PlayerReportService
    val commandService: CommandService
    val updateService: UpdateService

    init {
        client = PlayerApiClient(configService)
        deliveryService = DeliveryServiceImpl(configService, storageService, client, logger, asyncExecutor)
        heartbeatService = HeartbeatServiceImpl(configService, storageService, client, logger, asyncExecutor, platform, currentVersion)
        playerReportService = PlayerReportServiceImpl(configService, storageService, deliveryService, client, logger, asyncExecutor)
        commandService = CommandServiceImpl(configService, messageService, playerReportService, heartbeatService, logger, ::reload)
        updateService = UpdateServiceImpl(configService, logger, asyncExecutor, currentVersion, updateAssetPrefix, applyUpdate)
    }

    fun enable() {
        configService.enable()
        messageService.enable()
        storageService.enable()
        deliveryService.enable()
        heartbeatService.enable()
        playerReportService.enable()
        commandService.enable()
        updateService.enable()
    }

    fun reload() {
        configService.reload()
        messageService.reload()
        storageService.reload()
        deliveryService.reload()
        heartbeatService.reload()
        playerReportService.reload()
        commandService.reload()
    }

    fun disable() {
        updateService.disable()
        commandService.disable()
        playerReportService.disable()
        heartbeatService.disable()
        deliveryService.disable()
        storageService.disable()
        messageService.disable()
        configService.disable()
    }
}
