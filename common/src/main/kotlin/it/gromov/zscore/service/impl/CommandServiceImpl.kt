package it.gromov.zscore.service.impl

import it.gromov.zscore.command.SubCommand
import it.gromov.zscore.command.ZScoreCommand
import it.gromov.zscore.command.sub.DisableSubCommand
import it.gromov.zscore.command.sub.EnableSubCommand
import it.gromov.zscore.command.sub.HelpSubCommand
import it.gromov.zscore.command.sub.PlayerSubCommand
import it.gromov.zscore.command.sub.ReloadSubCommand
import it.gromov.zscore.command.sub.SetupSubCommand
import it.gromov.zscore.command.sub.StatusSubCommand
import it.gromov.zscore.command.sub.TestConnectionSubCommand
import it.gromov.zscore.platform.ZScoreLogger
import it.gromov.zscore.service.CommandService
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.HeartbeatService
import it.gromov.zscore.service.MessageService
import it.gromov.zscore.service.PlayerReportService

class CommandServiceImpl(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val playerReportService: PlayerReportService,
    private val heartbeatService: HeartbeatService,
    private val logger: ZScoreLogger,
    private val reloadAction: () -> Unit
) : CommandService {

    override lateinit var command: ZScoreCommand
        private set

    override fun enable() {
        val registered = mutableListOf<SubCommand>()
        registered.add(SetupSubCommand(configService, messageService, logger))
        registered.add(ReloadSubCommand(configService, messageService, logger, reloadAction))
        registered.add(StatusSubCommand(configService, messageService, playerReportService, heartbeatService))
        registered.add(TestConnectionSubCommand(configService, messageService, playerReportService))
        registered.add(PlayerSubCommand(configService, messageService, playerReportService))
        registered.add(EnableSubCommand(configService, messageService, logger))
        registered.add(DisableSubCommand(configService, messageService, logger))
        registered.add(0, HelpSubCommand(configService, messageService, registered))

        command = ZScoreCommand(configService, messageService)
        registered.forEach { command.register(it) }
    }

    override fun reload() {
    }

    override fun disable() {
    }
}
