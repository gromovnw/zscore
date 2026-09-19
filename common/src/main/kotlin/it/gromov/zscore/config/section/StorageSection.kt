package it.gromov.zscore.config.section

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.NameModifier
import eu.okaeri.configs.annotation.NameStrategy
import eu.okaeri.configs.annotation.Names
import it.gromov.zscore.storage.StorageType

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
class StorageSection : OkaeriConfig() {

    var type: String = "SQLITE"
    var tablePrefix: String = "zscore_"
    var nodeId: String = "auto"
    var sqlite: SqliteSection = SqliteSection()
    var mysql: MysqlSection = MysqlSection()

    fun resolvedType(): StorageType? = StorageType.parse(type)

    fun configProblems(): List<String> {
        val problems = mutableListOf<String>()

        val resolved = resolvedType()
        if (resolved == null) {
            problems.add("storage.type должен быть SQLITE или MYSQL, сейчас: $type")
            return problems
        }
        if (!TABLE_PREFIX.matches(tablePrefix)) {
            problems.add("storage.table-prefix может содержать только латиницу, цифры и _ (до $MAX_PREFIX_LENGTH символов)")
        }
        if (nodeId != "auto" && !NODE_ID.matches(nodeId)) {
            problems.add("storage.node-id может содержать только латиницу, цифры, - и _ (до $MAX_NODE_ID_LENGTH символов)")
        }

        when (resolved) {
            StorageType.SQLITE -> {
                if (sqlite.file.isBlank() || sqlite.file.contains("..")) {
                    problems.add("storage.sqlite.file пустой или содержит ..")
                }
            }
            StorageType.MYSQL -> {
                if (mysql.host.isBlank()) problems.add("storage.mysql.host не заполнен")
                if (mysql.port !in 1..65535) problems.add("storage.mysql.port должен быть в диапазоне 1..65535")
                if (mysql.database.isBlank()) problems.add("storage.mysql.database не заполнен")
                if (mysql.username.isBlank()) problems.add("storage.mysql.username не заполнен")
                if (mysql.connectionTimeoutSeconds !in 1..120) {
                    problems.add("storage.mysql.connection-timeout-seconds должен быть в диапазоне 1..120")
                }
            }
        }

        return problems
    }

    companion object {
        const val MAX_PREFIX_LENGTH = 32
        const val MAX_NODE_ID_LENGTH = 48
        val TABLE_PREFIX = Regex("^[A-Za-z0-9_]{0,$MAX_PREFIX_LENGTH}$")
        val NODE_ID = Regex("^[A-Za-z0-9_-]{1,$MAX_NODE_ID_LENGTH}$")
    }
}
