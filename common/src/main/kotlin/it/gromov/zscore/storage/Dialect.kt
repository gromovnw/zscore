package it.gromov.zscore.storage

import java.sql.SQLException

interface Dialect {
    val type: StorageType
    val forUpdate: String
    val serverTimeSql: String?

    fun createStatements(tables: Tables): List<String>
    fun isDuplicateKey(error: SQLException): Boolean
    fun isRetryable(error: SQLException): Boolean
}

class Tables(prefix: String) {
    val meta = "${prefix}meta"
    val players = "${prefix}players"
    val names = "${prefix}player_names"
    val ips = "${prefix}player_ips"
    val outbox = "${prefix}outbox"
}
