package it.gromov.zscore.service.impl

import eu.okaeri.configs.ConfigManager
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer
import it.gromov.zscore.config.MessagesConfig
import it.gromov.zscore.config.ZScoreConfig
import it.gromov.zscore.service.ConfigService
import java.io.File

class ConfigServiceImpl(private val dataFolder: File) : ConfigService {

    override lateinit var config: ZScoreConfig
        private set

    override lateinit var messages: MessagesConfig
        private set

    override fun enable() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        reload()
    }

    override fun reload() {
        config = load(ZScoreConfig::class.java, "config.yml")
        messages = load(MessagesConfig::class.java, "messages.yml")
    }

    override fun disable() {
    }

    private fun <T : OkaeriConfig> load(type: Class<T>, fileName: String): T {
        return ConfigManager.create(type) {
            it.withConfigurer(YamlSnakeYamlConfigurer())
            it.withBindFile(File(dataFolder, fileName))
            it.withRemoveOrphans(true)
            it.saveDefaults()
            it.load(true)
        }
    }
}
