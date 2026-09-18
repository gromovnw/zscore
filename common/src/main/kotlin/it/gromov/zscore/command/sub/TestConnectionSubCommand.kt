package it.gromov.zscore.command.sub

import it.gromov.zscore.command.SubCommand
import it.gromov.zscore.platform.ZScoreSender
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.MessageService
import it.gromov.zscore.service.PlayerReportService

class TestConnectionSubCommand(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val playerReportService: PlayerReportService
) : SubCommand {

    override val name = "testconnection"
    override val permission = "zscore.command.admin"

    override fun execute(sender: ZScoreSender, args: Array<String>) {
        messageService.send(sender, configService.messages.testConnectionRunning)
        playerReportService.testConnectionAsync { success, error ->
            if (success) {
                messageService.send(sender, configService.messages.testConnectionSuccess)
            } else {
                messageService.send(sender, configService.messages.testConnectionFailed, mapOf("error" to (error ?: "")))
            }
        }
    }
}
