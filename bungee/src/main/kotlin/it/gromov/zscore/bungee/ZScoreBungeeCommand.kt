package it.gromov.zscore.bungee

import it.gromov.zscore.ZScoreBootstrap
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor

class ZScoreBungeeCommand(private val bootstrap: ZScoreBootstrap) :
    Command("zscore", "zscore.command.admin"), TabExecutor {

    override fun execute(sender: CommandSender, args: Array<String>) {
        bootstrap.commandService.command.execute(BungeeSender(sender), args)
    }

    override fun onTabComplete(sender: CommandSender, args: Array<String>): Iterable<String> {
        if (args.size != 1) {
            return emptyList()
        }
        return bootstrap.commandService.command.suggest(args[0])
    }
}
