package it.gromov.zscore.command.sub

import it.gromov.zscore.command.SubCommand
import it.gromov.zscore.platform.ZScoreSender
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.MessageService

class HelpSubCommand(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val subCommands: List<SubCommand>
) : SubCommand {

    override val name = "help"
    override val permission = "zscore.command.admin"

    override fun execute(sender: ZScoreSender, args: Array<String>) {
        messageService.send(sender, configService.messages.helpHeader)
        for (subCommand in subCommands) {
            if (subCommand.permission == null || sender.hasPermission(subCommand.permission!!)) {
                messageService.send(sender, configService.messages.helpEntryFormat, mapOf("name" to subCommand.name))
            }
        }
    }
}
