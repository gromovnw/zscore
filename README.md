# zScore

Модуль сбора статистики игроков [zDonate](https://zdonate.me): репортит на бэкенд факты подключения игрока к серверу (ник, UUID, IP). Мультиплатформенный плагин — одна и та же логика на Velocity, BungeeCord и Spigot.

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

`/zscore setup|status|testconnection|reload|help`.

## Требования

- Velocity: Java 17, Velocity 3.4+.
- BungeeCord: Java 8+.
- Spigot/Paper: Java 8+, 1.8+.

## Разработка

Архитектура и договорённости — в [CLAUDE.md](CLAUDE.md).
