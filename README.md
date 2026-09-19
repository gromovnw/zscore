# zScore

Модуль сбора статистики игроков [zDonate](https://zdonate.me): запоминает игроков в своей базе (SQLite или MySQL) и передаёт факты подключения (ник, UUID, IP). Мультиплатформенный плагин — одна и та же логика на Velocity, BungeeCord и Spigot.

## Установка

```
./gradlew build
```

Джарники (все зависимости уже внутри):

- `velocity/build/libs/zScore-Velocity-<version>.jar`
- `bungee/build/libs/zScore-Bungee-<version>.jar`
- `spigot/build/libs/zScore-Spigot-<version>.jar`

Ставить нужный джарник на прокси (Velocity/BungeeCord) либо на Spigot-сервер без прокси, перезапустить.

```
/zscore setup <shopId> <serverId> <pluginKey>
/zscore testconnection
```

Реквизиты — в личном кабинете zDonate, магазин → сервер.

## Команды

`/zscore setup|status|testconnection|player|reload|enable|disable|help`.

`/zscore player <ник|uuid>` показывает, что плагин помнит об игроке: первый и последний вход, число входов, прежние ники и IP.

## Хранилище

По умолчанию данные лежат в SQLite (`plugins/zScore/zscore.db`). Если zScore стоит на нескольких прокси, укажите общую MySQL — тогда все узлы работают с одним набором данных и не расходятся:

```yaml
storage:
  type: MYSQL
  mysql:
    host: localhost
    port: 3306
    database: zscore
    username: zscore
    password: пароль
```

Таблицы создаются автоматически.

## Требования

- Velocity: Java 17, Velocity 3.4+.
- BungeeCord: Java 8+.
- Spigot/Paper: Java 8+, 1.8+.

## Автообновление

При старте плагин сам проверяет [релизы на GitHub](https://github.com/gromovnw/zscore/releases/latest) и, если есть новее, скачивает свой джарник (Spigot — в `plugins/update/`, применится на следующем рестарте; Velocity/BungeeCord — перезаписывает свой файл напрямую, тоже применится на следующем рестарте). Отключается в `config.yml` (`update.enabled: false`).

## Разработка

Архитектура и договорённости — в [CLAUDE.md](CLAUDE.md).
