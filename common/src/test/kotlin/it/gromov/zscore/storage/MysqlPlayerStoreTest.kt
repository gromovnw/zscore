package it.gromov.zscore.storage

import java.util.UUID
import kotlin.test.BeforeTest
import org.junit.jupiter.api.Assumptions.assumeTrue

class MysqlPlayerStoreTest : PlayerStoreContract() {

    private val config = System.getenv("ZSCORE_TEST_MYSQL")?.split(":")
    private val prefix = "t" + UUID.randomUUID().toString().replace("-", "").take(10) + "_"

    @BeforeTest
    fun requireDatabase() {
        assumeTrue(config != null && config.size >= 5, "ZSCORE_TEST_MYSQL=host:port:database:user:password не задан")
    }

    override fun newStore(node: String): PlayerStore {
        val c = config!!
        return PlayerStore(MysqlConnector(c[0], c[1].toInt(), c[2], c[3], c.drop(4).joinToString(":"), false, 10), MysqlDialect, prefix, node)
    }

    override fun cleanup() {
        val c = config ?: return
        MysqlConnector(c[0], c[1].toInt(), c[2], c[3], c.drop(4).joinToString(":"), false, 10).open().use { connection ->
            connection.createStatement().use { statement ->
                for (table in listOf("meta", "players", "player_names", "player_ips", "outbox")) {
                    statement.execute("DROP TABLE IF EXISTS $prefix$table")
                }
            }
        }
    }
}
