package it.gromov.zscore.command

import it.gromov.zscore.platform.ZScoreSender
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.MessageService

class ZScoreCommand(
    private val configService: ConfigService,
    private val messageService: MessageService
) {
    private val subCommands = LinkedHashMap<String, SubCommand>()

    fun register(subCommand: SubCommand) {
        subCommands[subCommand.name.lowercase()] = subCommand
    }

    fun getSubCommands(): List<SubCommand> {
        return subCommands.values.toList()
    }

    fun execute(sender: ZScoreSender, args: Array<String>) {
        val target = if (args.isEmpty()) subCommands["help"] else subCommands[args[0].lowercase()]

        if (target == null) {
            messageService.send(sender, configService.messages.unknownSubCommand)
            return
        }
        if (target.permission != null && !sender.hasPermission(target.permission!!)) {
            messageService.send(sender, configService.messages.noPermission)
            return
        }

        val rest = if (args.isEmpty()) args else args.copyOfRange(1, args.size)
        target.execute(sender, rest)
    }

    fun suggest(partial: String): List<String> {
        val lowered = partial.lowercase()
        return subCommands.keys.filter { it.startsWith(lowered) }
    }
}
