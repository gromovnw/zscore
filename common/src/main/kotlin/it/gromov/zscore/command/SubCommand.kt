package it.gromov.zscore.command

import it.gromov.zscore.platform.ZScoreSender

interface SubCommand {
    val name: String
    val permission: String?
    fun execute(sender: ZScoreSender, args: Array<String>)
}
