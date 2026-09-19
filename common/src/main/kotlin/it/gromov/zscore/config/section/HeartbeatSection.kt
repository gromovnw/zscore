package it.gromov.zscore.config.section

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.NameModifier
import eu.okaeri.configs.annotation.NameStrategy
import eu.okaeri.configs.annotation.Names

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
class HeartbeatSection : OkaeriConfig() {

    var enabled: Boolean = true
    var intervalSeconds: Int = 1

    fun configProblems(): List<String> {
        if (intervalSeconds !in 1..30) {
            return listOf("heartbeat.interval-seconds должен быть в диапазоне 1..30")
        }
        return emptyList()
    }
}
