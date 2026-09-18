package it.gromov.zscore.command.sub

import it.gromov.zscore.command.SubCommand
import it.gromov.zscore.platform.ZScoreLogger
import it.gromov.zscore.platform.ZScoreSender
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.MessageService

class DisableSubCommand(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val logger: ZScoreLogger
) : SubCommand {

    override val name = "disable"
    override val permission = "zscore.command.admin"

    override fun execute(sender: ZScoreSender, args: Array<String>) {
        if (!configService.config.general.enabled) {
            messageService.send(sender, configService.messages.disableAlready)
            return
        }

        try {
            configService.config.general.enabled = false
            configService.config.save()
            messageService.send(sender, configService.messages.disableSuccess)
        } catch (exception: Exception) {
            messageService.send(sender, configService.messages.disableFailed, mapOf("error" to exception.message.toString()))
            logger.warn("Ошибка выключения zScore: ${exception.message}", exception)
        }
    }
}
