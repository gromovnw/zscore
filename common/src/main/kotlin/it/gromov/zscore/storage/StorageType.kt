package it.gromov.zscore.storage

enum class StorageType {
    SQLITE,
    MYSQL;

    companion object {
        fun parse(raw: String): StorageType? {
            return when (raw.trim().uppercase()) {
                "SQLITE" -> SQLITE
                "MYSQL", "MARIADB" -> MYSQL
                else -> null
            }
        }
    }
}
