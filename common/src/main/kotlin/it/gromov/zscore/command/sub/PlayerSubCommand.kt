package it.gromov.zscore.command.sub

import it.gromov.zscore.command.SubCommand
import it.gromov.zscore.platform.ZScoreSender
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.MessageService
import it.gromov.zscore.service.PlayerReportService
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PlayerSubCommand(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val playerReportService: PlayerReportService
) : SubCommand {

    override val name = "player"
    override val permission = "zscore.command.admin"

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

    override fun execute(sender: ZScoreSender, args: Array<String>) {
        if (args.isEmpty()) {
            messageService.send(sender, configService.messages.playerUsage)
            return
        }

        val query = args[0]
        val messages = configService.messages
        playerReportService.lookupPlayerAsync(query) { profile, error ->
            if (error != null) {
                messageService.send(sender, messages.playerLookupFailed, mapOf("error" to error))
                return@lookupPlayerAsync
            }
            if (profile == null) {
                messageService.send(sender, messages.playerNotFound, mapOf("query" to query))
                return@lookupPlayerAsync
            }

            val player = profile.player
            messageService.send(sender, messages.playerHeader, mapOf("nickname" to player.nickname, "uuid" to player.uuid))
            messageService.send(
                sender,
                messages.playerSeenLine,
                mapOf(
                    "first" to formatter.format(Instant.ofEpochMilli(player.firstSeen)),
                    "last" to formatter.format(Instant.ofEpochMilli(player.lastSeen)),
                    "joins" to player.joinCount.toString(),
                    "node" to player.lastNode
                )
            )
            messageService.send(sender, messages.playerNamesLine, mapOf("names" to profile.names.joinToString(", ") { it.nickname }))
            messageService.send(sender, messages.playerIpsLine, mapOf("ips" to profile.ips.joinToString(", ") { it.ip }))
        }
    }
}
