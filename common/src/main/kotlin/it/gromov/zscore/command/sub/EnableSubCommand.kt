package it.gromov.zscore.command.sub

import it.gromov.zscore.command.SubCommand
import it.gromov.zscore.platform.ZScoreLogger
import it.gromov.zscore.platform.ZScoreSender
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.MessageService

class EnableSubCommand(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val logger: ZScoreLogger
) : SubCommand {

    override val name = "enable"
    override val permission = "zscore.command.admin"

    override fun execute(sender: ZScoreSender, args: Array<String>) {
        if (configService.config.general.enabled) {
            messageService.send(sender, configService.messages.enableAlready)
            return
        }

        try {
            configService.config.general.enabled = true
            configService.config.save()
            messageService.send(sender, configService.messages.enableSuccess)
        } catch (exception: Exception) {
            messageService.send(sender, configService.messages.enableFailed, mapOf("error" to exception.message.toString()))
            logger.warn("Ошибка включения zScore: ${exception.message}", exception)
        }
    }
}
