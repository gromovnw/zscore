package it.gromov.zscore.storage

import java.sql.Connection

object SchemaManager {

    private const val CURRENT_VERSION = 1
    private const val VERSION_KEY = "schema_version"

    fun migrate(connection: Connection, dialect: Dialect, tables: Tables) {
        connection.createStatement().use { statement ->
            statement.execute(metaDdl(dialect, tables))
        }
        val version = readVersion(connection, tables)
        if (version > CURRENT_VERSION) {
            throw StorageException("схема базы новее плагина (версия $version), обновите zScore")
        }
        if (version == CURRENT_VERSION) {
            return
        }
        connection.createStatement().use { statement ->
            for (sql in dialect.createStatements(tables)) {
                statement.execute(sql)
            }
        }
        writeVersion(connection, tables, version)
    }

    private fun metaDdl(dialect: Dialect, tables: Tables): String {
        return if (dialect.type == StorageType.MYSQL) {
            "CREATE TABLE IF NOT EXISTS ${tables.meta} (k VARCHAR(64) NOT NULL, v VARCHAR(255) NOT NULL, PRIMARY KEY (k)) " +
                "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        } else {
            "CREATE TABLE IF NOT EXISTS ${tables.meta} (k TEXT NOT NULL PRIMARY KEY, v TEXT NOT NULL)"
        }
    }

    private fun readVersion(connection: Connection, tables: Tables): Int {
        return connection.prepareStatement("SELECT v FROM ${tables.meta} WHERE k = ?").use { statement ->
            statement.setString(1, VERSION_KEY)
            statement.executeQuery().use { rows -> if (rows.next()) rows.getString(1).toIntOrNull() ?: 0 else 0 }
        }
    }

    private fun writeVersion(connection: Connection, tables: Tables, previous: Int) {
        val sql = if (previous == 0) {
            "INSERT INTO ${tables.meta} (k, v) VALUES (?, ?)"
        } else {
            "UPDATE ${tables.meta} SET v = ? WHERE k = ?"
        }
        connection.prepareStatement(sql).use { statement ->
            if (previous == 0) {
                statement.setString(1, VERSION_KEY)
                statement.setString(2, CURRENT_VERSION.toString())
            } else {
                statement.setString(1, CURRENT_VERSION.toString())
                statement.setString(2, VERSION_KEY)
            }
            try {
                statement.executeUpdate()
            } catch (error: java.sql.SQLException) {
                if (!(dialectDuplicate(error))) throw error
            }
        }
    }

    private fun dialectDuplicate(error: java.sql.SQLException): Boolean {
        return error.errorCode == 1062 || error.message?.contains("UNIQUE constraint failed") == true
    }
}
