package it.gromov.zscore.storage

import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

abstract class PlayerStoreContract {

    private val opened = mutableListOf<PlayerStore>()
    private val hour = ReportPolicy(60 * 60_000L)
    private val always = ReportPolicy(0L)

    protected abstract fun newStore(node: String): PlayerStore

    protected open fun cleanup() {}

    private fun store(node: String = "node-a"): PlayerStore = newStore(node).also { opened.add(it) }

    @AfterTest
    fun tearDown() {
        opened.forEach { it.close() }
        opened.clear()
        cleanup()
    }

    @Test
    fun firstJoinIsNewAndQueued() {
        val store = store()
        val uuid = UUID.randomUUID().toString()
        val outcome = store.recordJoin(uuid, "Steve", "1.2.3.4", hour)
        assertTrue(outcome.newPlayer)
        assertTrue(outcome.nicknameNew)
        assertTrue(outcome.ipNew)
        assertTrue(outcome.queued)
    }

    @Test
    fun repeatJoinWithSameDataIsNotQueuedWithinWindow() {
        val store = store()
        val uuid = UUID.randomUUID().toString()
        store.recordJoin(uuid, "Steve", "1.2.3.4", hour)
        val outcome = store.recordJoin(uuid, "Steve", "1.2.3.4", hour)
        assertFalse(outcome.newPlayer)
        assertFalse(outcome.queued)
        assertEquals(2L, store.findProfile(uuid)!!.player.joinCount)
    }

    @Test
    fun nicknameAndIpChangesAreQueued() {
        val store = store()
        val uuid = UUID.randomUUID().toString()
        store.recordJoin(uuid, "Steve", "1.2.3.4", hour)

        val renamed = store.recordJoin(uuid, "Alex", "1.2.3.4", hour)
        assertTrue(renamed.nicknameNew)
        assertTrue(renamed.queued)

        val moved = store.recordJoin(uuid, "Alex", "5.6.7.8", hour)
        assertTrue(moved.ipNew)
        assertTrue(moved.queued)

        val profile = store.findProfile(uuid)!!
        assertEquals("Alex", profile.player.nickname)
        assertEquals("5.6.7.8", profile.player.ip)
        assertEquals(listOf("Steve", "Alex"), profile.names.map { it.nickname })
        assertEquals(setOf("1.2.3.4", "5.6.7.8"), profile.ips.map { it.ip }.toSet())
    }

    @Test
    fun zeroWindowQueuesEveryJoin() {
        val store = store()
        val uuid = UUID.randomUUID().toString()
        store.recordJoin(uuid, "Steve", "1.2.3.4", always)
        assertTrue(store.recordJoin(uuid, "Steve", "1.2.3.4", always).queued)
    }

    @Test
    fun lookupByNicknameIsCaseInsensitiveAndFindsOldNames() {
        val store = store()
        val uuid = UUID.randomUUID().toString()
        store.recordJoin(uuid, "Steve", "1.2.3.4", hour)
        store.recordJoin(uuid, "Alex", "1.2.3.4", hour)
        assertEquals(uuid, store.findProfile("sTeVe")!!.player.uuid)
        assertEquals(uuid, store.findProfile("ALEX")!!.player.uuid)
        assertNull(store.findProfile("nobody"))
    }

    @Test
    fun twoNodesJoiningSamePlayerConcurrentlyConverge() {
        val a = store("node-a")
        val b = store("node-b")
        val uuid = UUID.randomUUID().toString()
        val perNode = 40
        val newPlayers = AtomicInteger()
        val pool = Executors.newFixedThreadPool(4)
        val start = CountDownLatch(1)
        val done = CountDownLatch(4)
        for (node in listOf(a, b, a, b)) {
            pool.execute {
                start.await()
                repeat(perNode / 2) {
                    if (node.recordJoin(uuid, "Steve", "1.2.3.4", hour).newPlayer) newPlayers.incrementAndGet()
                }
                done.countDown()
            }
        }
        start.countDown()
        assertTrue(done.await(60, TimeUnit.SECONDS))
        pool.shutdown()

        val player = assertNotNull(a.findProfile(uuid)).player
        assertEquals(1, newPlayers.get())
        assertEquals((perNode * 2).toLong(), player.joinCount)
        assertTrue(player.firstSeen <= player.lastSeen)
        assertEquals(1, a.claimPending(100, 60_000L).size)
    }

    @Test
    fun outboxIsClaimedByOnlyOneNode() {
        val a = store("node-a")
        val b = store("node-b")
        repeat(40) { a.recordJoin(UUID.randomUUID().toString(), "P$it", "1.1.1.$it", hour) }

        val claimedA = mutableListOf<Long>()
        val claimedB = mutableListOf<Long>()
        val pool = Executors.newFixedThreadPool(2)
        val start = CountDownLatch(1)
        val done = CountDownLatch(2)
        pool.execute { start.await(); repeat(4) { claimedA.addAll(a.claimPending(10, 60_000L).map { r -> r.id }) }; done.countDown() }
        pool.execute { start.await(); repeat(4) { claimedB.addAll(b.claimPending(10, 60_000L).map { r -> r.id }) }; done.countDown() }
        start.countDown()
        assertTrue(done.await(60, TimeUnit.SECONDS))
        pool.shutdown()

        assertEquals(40, claimedA.size + claimedB.size)
        assertTrue(claimedA.intersect(claimedB.toSet()).isEmpty())
    }

    @Test
    fun pendingReportLifecycle() {
        val store = store()
        store.recordJoin(UUID.randomUUID().toString(), "Steve", "1.2.3.4", hour)

        val first = store.claimPending(10, 60_000L).single()
        assertEquals(0, first.attempts)
        assertTrue(store.claimPending(10, 60_000L).isEmpty())

        store.retryPending(first.id, 60_000L)
        assertTrue(store.claimPending(10, 60_000L).isEmpty())

        store.retryPending(first.id, -1L)
        val second = store.claimPending(10, 60_000L).single()
        assertEquals(2, second.attempts)

        store.completePending(second.id)
        assertEquals(0L, store.stats().pendingReports)
    }

    @Test
    fun releasedClaimsBecomeAvailableAgain() {
        val store = store()
        store.recordJoin(UUID.randomUUID().toString(), "Steve", "1.2.3.4", hour)
        val claimed = store.claimPending(10, 60_000L)
        store.releasePending(claimed.map { it.id })
        assertEquals(1, store.claimPending(10, 60_000L).size)
    }
}
