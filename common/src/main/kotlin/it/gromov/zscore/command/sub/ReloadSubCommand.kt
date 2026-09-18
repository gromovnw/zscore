package it.gromov.zscore.command.sub

import it.gromov.zscore.command.SubCommand
import it.gromov.zscore.platform.ZScoreLogger
import it.gromov.zscore.platform.ZScoreSender
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.MessageService

class ReloadSubCommand(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val logger: ZScoreLogger,
    private val reloadAction: () -> Unit
) : SubCommand {

    override val name = "reload"
    override val permission = "zscore.command.admin"

    override fun execute(sender: ZScoreSender, args: Array<String>) {
        try {
            reloadAction()
            messageService.send(sender, configService.messages.reloadSuccess)
        } catch (exception: Exception) {
            messageService.send(sender, configService.messages.reloadFailed, mapOf("error" to exception.message.toString()))
            logger.warn("Ошибка перезагрузки конфигурации zScore: ${exception.message}", exception)
        }
    }
}
