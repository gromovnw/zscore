package it.gromov.zscore.config

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.NameModifier
import eu.okaeri.configs.annotation.NameStrategy
import eu.okaeri.configs.annotation.Names

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
class MessagesConfig : OkaeriConfig() {

    var prefix: String = "&8[&bzScore&8]&7 "
    var noPermission: String = "{prefix}&cУ вас нет прав на выполнение этой команды."
    var unknownSubCommand: String = "{prefix}&cНеизвестная подкоманда. Используйте &f/zscore help&c."
    var reloadSuccess: String = "{prefix}&aКонфигурация успешно перезагружена."
    var reloadFailed: String = "{prefix}&cНе удалось перезагрузить конфигурацию: &f{error}"
    var statusLine: String = "{prefix}&7enabled: &f{enabled}&7, настроен: &f{configured}&7, API: &f{api-base-url}"
    var testConnectionRunning: String = "{prefix}&7Проверяем соединение с zDonate..."
    var testConnectionSuccess: String = "{prefix}&aСоединение с zDonate установлено успешно."
    var testConnectionFailed: String = "{prefix}&cНе удалось подключиться к zDonate: &f{error}"
    var helpHeader: String = "&8&m--------&r &bzScore&r &8&m--------"
    var helpEntryFormat: String = "&b/zscore {name}"
    var setupUsage: String = "{prefix}&cИспользование: &f/zscore setup <shop-id> <server-id> <plugin-key>"
    var setupSuccess: String = "{prefix}&aНастройки сохранены. shop-id: &f{shop-id}&a, server-id: &f{server-id}"
    var setupFailed: String = "{prefix}&cНе удалось сохранить настройки: &f{error}"
}
