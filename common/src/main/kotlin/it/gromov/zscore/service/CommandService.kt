package it.gromov.zscore.service

import it.gromov.zscore.command.ZScoreCommand

interface CommandService : Service {
    val command: ZScoreCommand
}
