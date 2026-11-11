package app.di

import app.data.db.DatabaseFactory
import app.domain.syncmanager.SyncManager
import app.domain.syncmanager.SyncDataRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi
import kotlin.time.Clock
import kotlin.time.Instant

import app.data.db.tables.chatSessions.ChatSessions
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.core.eq
import app.utils.dbQuery

@OptIn(ExperimentalUuidApi::class)
class LeakTest : KoinTest {

    private val syncManager: SyncManager by inject()

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            DatabaseFactory.init()
            startKoin {
                modules(
                    AppKoinModule().module(),
                    PaymentModule().module(),
                    TelegramModule().module(),
                    SpecialBindingsModule().module()
                )
            }
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            stopKoin()
        }
    }

    @Test
    fun testDifferentialSyncLeak() = runBlocking {
        val systemUserId = Uuid.parse("8bd56bfb-54e2-4d44-b1c7-be6bc29cb99d")
        
        // 1. Делаем первый синк
        val firstResponse = syncManager.execute(SyncDataRequest(systemUserId, Instant.fromEpochMilliseconds(0)))
        val since = firstResponse.latestTimestamp

        // 2. Искусственно удаляем сессию
        val testSessionId = dbQuery("test_delete") {
            val sessionRow = ChatSessions
                .select(ChatSessions.id)
                .where { ChatSessions.userId eq systemUserId }
                .limit(1)
                .firstOrNull()
                
            val sessionId = sessionRow?.get(ChatSessions.id)?.value
                ?: return@dbQuery null
                
            ChatSessions.update({ ChatSessions.id eq sessionId }) {
                it[deletedAt] = Clock.System.now()
            }
            sessionId
        }
        
        // 3. Делаем второй синк (дифференциальный)
        val secondResponse = syncManager.execute(SyncDataRequest(systemUserId, since))
        
        val isInDeleted = secondResponse.deletedChatSessionIds.contains(testSessionId)
        val isInUpdated = secondResponse.updatedChatSessions.any { it.id == testSessionId }

        assertTrue(isInDeleted, "СЕССИЯ НЕ ПОПАЛА В УДАЛЕННЫЕ!")
        assertFalse(isInUpdated, "СЕССИЯ ПРОТЕКЛА В ОБНОВЛЕННЫЕ!")
    }
}
