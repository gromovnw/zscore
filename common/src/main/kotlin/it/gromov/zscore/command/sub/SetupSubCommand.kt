package it.gromov.zscore.command.sub

import it.gromov.zscore.command.SubCommand
import it.gromov.zscore.platform.ZScoreLogger
import it.gromov.zscore.platform.ZScoreSender
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.MessageService

class SetupSubCommand(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val logger: ZScoreLogger
) : SubCommand {

    override val name = "setup"
    override val permission = "zscore.command.admin"

    override fun execute(sender: ZScoreSender, args: Array<String>) {
        if (args.size < 3) {
            messageService.send(sender, configService.messages.setupUsage)
            return
        }

        val shopId = args[0]
        val serverId = args[1]
        val pluginKey = args[2]

        try {
            val api = configService.config.api
            api.shopId = shopId
            api.serverId = serverId
            api.pluginKey = pluginKey
            configService.config.save()

            messageService.send(
                sender,
                configService.messages.setupSuccess,
                mapOf("shop-id" to shopId, "server-id" to serverId)
            )
        } catch (exception: Exception) {
            messageService.send(
                sender,
                configService.messages.setupFailed,
                mapOf("error" to exception.message.toString())
            )
            logger.warn("Ошибка сохранения настроек zScore: ${exception.message}", exception)
        }
    }
}
