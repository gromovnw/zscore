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
    var statusLine: String = "{prefix}&7enabled: &f{enabled}&7, настроен: &f{configured}"
    var testConnectionRunning: String = "{prefix}&7Проверяем соединение с zDonate..."
    var testConnectionSuccess: String = "{prefix}&aСоединение с zDonate установлено успешно."
    var testConnectionFailed: String = "{prefix}&cНе удалось подключиться к zDonate: &f{error}"
    var helpHeader: String = "&8&m--------&r &bzScore&r &8&m--------"
    var helpEntryFormat: String = "&b/zscore {name}"
    var setupUsage: String = "{prefix}&cИспользование: &f/zscore setup <shop-id> <server-id> <plugin-key>"
    var setupSuccess: String = "{prefix}&aНастройки сохранены. shop-id: &f{shop-id}&a, server-id: &f{server-id}"
    var setupFailed: String = "{prefix}&cНе удалось сохранить настройки: &f{error}"
    var setupInvalidArgument: String = "{prefix}&cНекорректные параметры: &f{error}"
    var statusNoProblems: String = "{prefix}&aКонфигурация корректна, проблем не найдено."
    var statusProblemsHeader: String = "{prefix}&cОбнаружены проблемы конфигурации:"
    var statusProblemEntryFormat: String = "&7 — &f{problem}"
    var enableAlready: String = "{prefix}&7zScore уже включён."
    var enableSuccess: String = "{prefix}&azScore включён."
    var enableFailed: String = "{prefix}&cНе удалось включить zScore: &f{error}"
    var disableAlready: String = "{prefix}&7zScore уже выключен."
    var disableSuccess: String = "{prefix}&azScore выключен."
    var disableFailed: String = "{prefix}&cНе удалось выключить zScore: &f{error}"
    var statusStorageLine: String = "{prefix}&7хранилище: &f{type}&7, узел: &f{node}&7, игроков: &f{players}&7, в очереди: &f{pending}&7, сдвиг часов: &f{offset} мс"
    var statusStorageFailed: String = "{prefix}&cХранилище недоступно: &f{error}"
    var statusHeartbeatLine: String = "{prefix}&7heartbeat: &f{state}"
    var statusSqliteHint: String = "{prefix}&eSQLite хранит данные только на этом сервере. Для нескольких прокси укажите storage.type: MYSQL."
    var playerUsage: String = "{prefix}&cИспользование: &f/zscore player <ник|uuid>"
    var playerNotFound: String = "{prefix}&cИгрок &f{query}&c не найден в базе zScore."
    var playerLookupFailed: String = "{prefix}&cНе удалось получить данные игрока: &f{error}"
    var playerHeader: String = "{prefix}&7Игрок &f{nickname} &8({uuid})"
    var playerSeenLine: String = "&7 — первый вход: &f{first}&7, последний: &f{last}&7, входов: &f{joins}&7, узел: &f{node}"
    var playerNamesLine: String = "&7 — ники: &f{names}"
    var playerIpsLine: String = "&7 — IP: &f{ips}"
}
