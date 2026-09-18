package it.gromov.zscore.service

import it.gromov.zscore.config.MessagesConfig
import it.gromov.zscore.config.ZScoreConfig

interface ConfigService : Service {
    val config: ZScoreConfig
    val messages: MessagesConfig
}
