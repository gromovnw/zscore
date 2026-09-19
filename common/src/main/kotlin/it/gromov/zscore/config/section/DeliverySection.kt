package it.gromov.zscore.config.section

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.NameModifier
import eu.okaeri.configs.annotation.NameStrategy
import eu.okaeri.configs.annotation.Names

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
class DeliverySection : OkaeriConfig() {

    var resendAfterMinutes: Int = 60
    var retryMaxAttempts: Int = 12
    var retentionDays: Int = 7
    var flushIntervalSeconds: Int = 30

    fun configProblems(): List<String> {
        val problems = mutableListOf<String>()
        if (resendAfterMinutes !in 0..10080) problems.add("delivery.resend-after-minutes должен быть в диапазоне 0..10080")
        if (retryMaxAttempts !in 1..100) problems.add("delivery.retry-max-attempts должен быть в диапазоне 1..100")
        if (retentionDays !in 1..90) problems.add("delivery.retention-days должен быть в диапазоне 1..90")
        if (flushIntervalSeconds !in 5..3600) problems.add("delivery.flush-interval-seconds должен быть в диапазоне 5..3600")
        return problems
    }
}
