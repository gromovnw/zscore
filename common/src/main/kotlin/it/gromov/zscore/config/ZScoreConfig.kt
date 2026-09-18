package it.gromov.zscore.config

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.NameModifier
import eu.okaeri.configs.annotation.NameStrategy
import eu.okaeri.configs.annotation.Names
import it.gromov.zscore.config.section.ApiSection
import it.gromov.zscore.config.section.GeneralSection
import it.gromov.zscore.config.section.UpdateSection

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
class ZScoreConfig : OkaeriConfig() {
    var general: GeneralSection = GeneralSection()
    var api: ApiSection = ApiSection()
    var update: UpdateSection = UpdateSection()
}
