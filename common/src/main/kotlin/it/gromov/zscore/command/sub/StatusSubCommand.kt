package it.gromov.zscore.command.sub

import it.gromov.zscore.command.SubCommand
import it.gromov.zscore.platform.ZScoreSender
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.HeartbeatService
import it.gromov.zscore.service.MessageService
import it.gromov.zscore.service.PlayerReportService
import it.gromov.zscore.storage.StorageType

class StatusSubCommand(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val playerReportService: PlayerReportService,
    private val heartbeatService: HeartbeatService
) : SubCommand {

    override val name = "status"
    override val permission = "zscore.command.admin"

    override fun execute(sender: ZScoreSender, args: Array<String>) {
        val config = configService.config
        val messages = configService.messages
        messageService.send(
            sender,
            messages.statusLine,
            mapOf(
                "enabled" to config.general.enabled.toString(),
                "configured" to config.api.isConfigured().toString()
            )
        )
        messageService.send(sender, messages.statusHeartbeatLine, mapOf("state" to heartbeatService.state()))

        val problems = config.api.configProblems() +
            config.storage.configProblems() +
            config.delivery.configProblems() +
            config.heartbeat.configProblems()
        if (problems.isEmpty()) {
            messageService.send(sender, messages.statusNoProblems)
        } else {
            messageService.send(sender, messages.statusProblemsHeader)
            for (problem in problems) {
                messageService.send(sender, messages.statusProblemEntryFormat, mapOf("problem" to problem))
            }
        }

        if (config.storage.resolvedType() == StorageType.SQLITE) {
            messageService.send(sender, messages.statusSqliteHint)
        }
        playerReportService.storageStatsAsync { stats, error ->
            if (stats == null) {
                messageService.send(sender, messages.statusStorageFailed, mapOf("error" to (error ?: "")))
                return@storageStatsAsync
            }
            messageService.send(
                sender,
                messages.statusStorageLine,
                mapOf(
                    "type" to stats.type.name,
                    "node" to stats.nodeId,
                    "players" to stats.players.toString(),
                    "pending" to stats.pendingReports.toString(),
                    "offset" to stats.clockOffsetMillis.toString()
                )
            )
        }
    }
}
