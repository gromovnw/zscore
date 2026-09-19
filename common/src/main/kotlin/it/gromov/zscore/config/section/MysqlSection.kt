package it.gromov.zscore.config.section

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.NameModifier
import eu.okaeri.configs.annotation.NameStrategy
import eu.okaeri.configs.annotation.Names

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
class MysqlSection : OkaeriConfig() {
    var host: String = "localhost"
    var port: Int = 3306
    var database: String = "zscore"
    var username: String = "root"
    var password: String = ""
    var useSsl: Boolean = false
    var connectionTimeoutSeconds: Int = 10
}
