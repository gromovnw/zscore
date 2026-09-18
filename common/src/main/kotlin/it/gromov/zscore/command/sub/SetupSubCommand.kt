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

        val shopId = args[0].trim()
        val serverId = args[1].trim()
        val pluginKey = args[2].trim()

        val validationError = validate(shopId, serverId, pluginKey)
        if (validationError != null) {
            messageService.send(sender, configService.messages.setupInvalidArgument, mapOf("error" to validationError))
            return
        }

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

    private fun validate(shopId: String, serverId: String, pluginKey: String): String? {
        if (shopId.isEmpty() || shopId.length > MAX_ID_LENGTH) {
            return "shopId пустой или длиннее $MAX_ID_LENGTH символов"
        }
        if (serverId.isEmpty() || serverId.length > MAX_ID_LENGTH) {
            return "serverId пустой или длиннее $MAX_ID_LENGTH символов"
        }
        if (pluginKey.length < MIN_PLUGIN_KEY_LENGTH) {
            return "pluginKey выглядит обрезанным — скопируйте ключ целиком из личного кабинета"
        }
        return null
    }

    private companion object {
        const val MAX_ID_LENGTH = 128
        const val MIN_PLUGIN_KEY_LENGTH = 20
    }
}
