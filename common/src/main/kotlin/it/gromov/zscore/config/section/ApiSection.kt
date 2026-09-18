package it.gromov.zscore.config.section

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.NameModifier
import eu.okaeri.configs.annotation.NameStrategy
import eu.okaeri.configs.annotation.Names

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
class ApiSection : OkaeriConfig() {

    var baseUrl: String = "https://api.zdonate.me"
    var shopId: String = "YOUR-SHOP-ID"
    var serverId: String = "YOUR-SERVER-ID"
    var pluginKey: String = "YOUR-PLUGIN-KEY"
    var requestTimeoutSeconds: Int = 5

    fun isConfigured(): Boolean {
        return shopId != "YOUR-SHOP-ID" && serverId != "YOUR-SERVER-ID" && pluginKey != "YOUR-PLUGIN-KEY"
    }
}
