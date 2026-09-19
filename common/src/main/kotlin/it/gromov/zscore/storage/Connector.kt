package it.gromov.zscore.storage

import java.io.File
import java.sql.Connection
import java.sql.SQLException
import java.util.Properties

interface Connector {
    fun open(): Connection
}

class SqliteConnector(private val file: File) : Connector {

    override fun open(): Connection {
        file.absoluteFile.parentFile?.mkdirs()
        val properties = Properties()
        properties.setProperty("busy_timeout", "5000")
        properties.setProperty("journal_mode", "WAL")
        properties.setProperty("synchronous", "NORMAL")
        properties.setProperty("transaction_mode", "IMMEDIATE")
        return org.sqlite.JDBC().connect("jdbc:sqlite:${file.absolutePath}", properties)
            ?: throw SQLException("SQLite драйвер не принял адрес ${file.absolutePath}")
    }
}

class MysqlConnector(
    private val host: String,
    private val port: Int,
    private val database: String,
    private val username: String,
    private val password: String,
    private val useSsl: Boolean,
    private val timeoutSeconds: Int
) : Connector {

    override fun open(): Connection {
        val properties = Properties()
        properties.setProperty("user", username)
        properties.setProperty("password", password)
        properties.setProperty("connectTimeout", (timeoutSeconds * 1000).toString())
        properties.setProperty("socketTimeout", (timeoutSeconds * 3000).toString())
        properties.setProperty("sslMode", if (useSsl) "REQUIRED" else "DISABLED")
        properties.setProperty("allowPublicKeyRetrieval", "true")
        properties.setProperty("tcpKeepAlive", "true")
        properties.setProperty("characterEncoding", "UTF-8")
        return com.mysql.cj.jdbc.Driver().connect("jdbc:mysql://$host:$port/$database", properties)
            ?: throw SQLException("MySQL драйвер не принял адрес $host:$port/$database")
    }
}
