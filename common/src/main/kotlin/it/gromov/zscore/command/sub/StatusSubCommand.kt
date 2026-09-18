package it.gromov.zscore.command.sub

import it.gromov.zscore.command.SubCommand
import it.gromov.zscore.platform.ZScoreSender
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.MessageService

class StatusSubCommand(
    private val configService: ConfigService,
    private val messageService: MessageService
) : SubCommand {

    override val name = "status"
    override val permission = "zscore.command.admin"

    override fun execute(sender: ZScoreSender, args: Array<String>) {
        val api = configService.config.api
        messageService.send(
            sender,
            configService.messages.statusLine,
            mapOf(
                "enabled" to configService.config.general.enabled.toString(),
                "configured" to api.isConfigured().toString()
            )
        )

        val problems = api.configProblems()
        if (problems.isEmpty()) {
            messageService.send(sender, configService.messages.statusNoProblems)
            return
        }

        messageService.send(sender, configService.messages.statusProblemsHeader)
        for (problem in problems) {
            messageService.send(sender, configService.messages.statusProblemEntryFormat, mapOf("problem" to problem))
        }
    }
}
