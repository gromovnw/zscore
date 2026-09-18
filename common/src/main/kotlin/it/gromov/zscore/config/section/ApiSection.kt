package it.gromov.zscore.config.section

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.NameModifier
import eu.okaeri.configs.annotation.NameStrategy
import eu.okaeri.configs.annotation.Names

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
class ApiSection : OkaeriConfig() {

    var shopId: String = "YOUR-SHOP-ID"
    var serverId: String = "YOUR-SERVER-ID"
    var pluginKey: String = "YOUR-PLUGIN-KEY"
    var requestTimeoutSeconds: Int = 5

    fun isConfigured(): Boolean {
        return shopId != "YOUR-SHOP-ID" && serverId != "YOUR-SERVER-ID" && pluginKey != "YOUR-PLUGIN-KEY"
    }

    fun configProblems(): List<String> {
        val problems = mutableListOf<String>()

        if (!isConfigured()) {
            problems.add("плагин ещё не настроен, используйте /zscore setup <shopId> <serverId> <pluginKey>")
            return problems
        }

        if (shopId.isBlank() || shopId.length > MAX_ID_LENGTH || shopId.any { it.isWhitespace() }) {
            problems.add("shopId содержит недопустимые символы или слишком длинный")
        }
        if (serverId.isBlank() || serverId.length > MAX_ID_LENGTH || serverId.any { it.isWhitespace() }) {
            problems.add("serverId содержит недопустимые символы или слишком длинный")
        }
        if (pluginKey.length < MIN_PLUGIN_KEY_LENGTH) {
            problems.add("pluginKey подозрительно короткий — проверьте, что он скопирован из личного кабинета целиком")
        }
        if (requestTimeoutSeconds < MIN_TIMEOUT_SECONDS || requestTimeoutSeconds > MAX_TIMEOUT_SECONDS) {
            problems.add("request-timeout-seconds должен быть в диапазоне $MIN_TIMEOUT_SECONDS..$MAX_TIMEOUT_SECONDS")
        }

        return problems
    }

    companion object {
        private const val MAX_ID_LENGTH = 128
        private const val MIN_PLUGIN_KEY_LENGTH = 20
        private const val MIN_TIMEOUT_SECONDS = 1
        private const val MAX_TIMEOUT_SECONDS = 60
    }
}
