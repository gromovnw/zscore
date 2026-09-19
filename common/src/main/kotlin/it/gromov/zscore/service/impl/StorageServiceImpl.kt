package it.gromov.zscore.service.impl

import it.gromov.zscore.config.section.StorageSection
import it.gromov.zscore.platform.ZScoreLogger
import it.gromov.zscore.service.ConfigService
import it.gromov.zscore.service.StorageService
import it.gromov.zscore.storage.ClockSync
import it.gromov.zscore.storage.Connector
import it.gromov.zscore.storage.Dialect
import it.gromov.zscore.storage.MysqlConnector
import it.gromov.zscore.storage.MysqlDialect
import it.gromov.zscore.storage.PlayerStore
import it.gromov.zscore.storage.SqliteConnector
import it.gromov.zscore.storage.SqliteDialect
import it.gromov.zscore.storage.StorageException
import it.gromov.zscore.storage.StorageType
import java.io.File
import java.util.UUID

class StorageServiceImpl(
    private val configService: ConfigService,
    private val logger: ZScoreLogger,
    private val dataFolder: File,
    private val platform: String
) : StorageService {

    private val clock = ClockSync()
    private var current: PlayerStore? = null

    override var nodeId: String = platform
        private set

    @Synchronized
    override fun enable() {
        open()
    }

    @Synchronized
    override fun reload() {
        current?.close()
        current = null
        open()
    }

    @Synchronized
    override fun disable() {
        current?.close()
        current = null
    }

    @Synchronized
    override fun store(): PlayerStore {
        return current ?: throw StorageException("хранилище не настроено, проверьте секцию storage в config.yml")
    }

    private fun open() {
        val section = configService.config.storage
        val problems = section.configProblems()
        if (problems.isNotEmpty()) {
            logger.warn("zScore: хранилище не запущено — ${problems.joinToString("; ")}")
            return
        }

        val type = section.resolvedType() ?: return
        nodeId = resolveNodeId(section)
        val connector: Connector
        val dialect: Dialect
        if (type == StorageType.SQLITE) {
            connector = SqliteConnector(File(dataFolder, section.sqlite.file))
            dialect = SqliteDialect
        } else {
            val mysql = section.mysql
            connector = MysqlConnector(
                mysql.host, mysql.port, mysql.database, mysql.username, mysql.password, mysql.useSsl, mysql.connectionTimeoutSeconds
            )
            dialect = MysqlDialect
        }
        current = PlayerStore(connector, dialect, section.tablePrefix, nodeId, clock)
        logger.info("zScore: хранилище $type, узел $nodeId")
    }

    private fun resolveNodeId(section: StorageSection): String {
        if (section.nodeId != "auto") {
            return section.nodeId
        }
        val file = File(dataFolder, "node-id")
        val saved = if (file.isFile) file.readText().trim() else ""
        if (StorageSection.NODE_ID.matches(saved)) {
            return saved
        }
        val generated = platform + "-" + UUID.randomUUID().toString().substring(0, 8)
        try {
            file.writeText(generated)
        } catch (error: java.io.IOException) {
            logger.warn("zScore: не удалось сохранить идентификатор узла в ${file.name}: ${error.message}")
        }
        return generated
    }
}
