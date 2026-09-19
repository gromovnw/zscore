package it.gromov.zscore.storage

import java.io.File
import java.nio.file.Files

class SqlitePlayerStoreTest : PlayerStoreContract() {

    private val dir: File = Files.createTempDirectory("zscore-sqlite").toFile()

    override fun newStore(node: String): PlayerStore {
        return PlayerStore(SqliteConnector(File(dir, "zscore.db")), SqliteDialect, "zscore_", node)
    }

    override fun cleanup() {
        dir.deleteRecursively()
    }
}
