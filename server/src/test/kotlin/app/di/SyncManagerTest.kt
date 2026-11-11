package app.di

import app.data.db.DatabaseFactory
import app.di.AppKoinModule
import app.di.PaymentModule
import app.di.TelegramModule
import app.di.SpecialBindingsModule
import app.domain.syncmanager.SyncManager
import app.domain.syncmanager.SyncDataRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi
import kotlin.time.Clock
import kotlin.time.Instant

import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariants
import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.users.UserPersonas

import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.inList
import kotlin.test.assertEquals
import app.utils.dbQuery

@OptIn(ExperimentalUuidApi::class)
class SyncManagerTest : KoinTest {

    private val syncManager: SyncManager by inject()

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            println(">>> Setup Test Environment")
            DatabaseFactory.init()
            startKoin {
                printLogger(Level.ERROR)
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
            println(">>> TearDown Test Environment")
            stopKoin()
        }
    }

    @Test
    fun testDifferentialSyncForSystemUser(): Unit = runBlocking {
        val systemUserId = Uuid.parse("8bd56bfb-54e2-4d44-b1c7-be6bc29cb99d")
        
        // 1. Делаем первую синхронизацию, чтобы получить актуальное время (since)
        val firstResponse = syncManager.execute(SyncDataRequest(systemUserId, Instant.fromEpochMilliseconds(0)))
        val currentSince = firstResponse.latestTimestamp

        // 2. Искусственно удаляем одну сессию в базе
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
        
        assertNotNull(testSessionId, "Не нашли ни одной сессии в базе для теста")

        // 3. Вызываем ВТОРУЮ (дифференциальную) синхронизацию с новым временем
        val response = syncManager.execute(SyncDataRequest(systemUserId, currentSince))
        
        val isInDeleted = response.deletedChatSessionIds.contains(testSessionId)
        val isInUpdated = response.updatedChatSessions.any { it.id == testSessionId }

        // 4. Проверяем оба массива
        assertTrue(isInDeleted, "КРИТИЧЕСКАЯ ОШИБКА БЕКЕНДА: удаленная сессия НЕ ПОПАЛА в список deletedChatSessionIds!")
        assertFalse(isInUpdated, "КРИТИЧЕСКАЯ ОШИБКА БЕКЕНДА: удаленная сессия ПОПАЛА в список updatedChatSessions!")
    }

    @Test
    fun testDataIntegrityNoDeletedItemsInActiveLists() = runBlocking {
        val systemUserId = Uuid.parse("8bd56bfb-54e2-4d44-b1c7-be6bc29cb99d")
        // Первая синхронизация
        val request = SyncDataRequest(systemUserId, Instant.fromEpochMilliseconds(0))
        val response = syncManager.execute(request)

        dbQuery("test_data_integrity") {
            // 1. Проверяем ChatSessions
            val activeSessionIds = response.updatedChatSessions.map { it.id }
            if (activeSessionIds.isNotEmpty()) {
                val deletedSessionsLeak = ChatSessions.select(ChatSessions.id)
                    .where { (ChatSessions.id inList activeSessionIds) and (ChatSessions.deletedAt.isNotNull()) }
                    .count()
                assertEquals(0L, deletedSessionsLeak, "В списке updatedChatSessions протекли удаленные сессии!")
            }

            // 2. Проверяем ChatVariants
            val activeVariantIds = response.updatedChatVariants.map { it.id }
            if (activeVariantIds.isNotEmpty()) {
                val deletedVariantsLeak = ChatVariants.select(ChatVariants.id)
                    .where { (ChatVariants.id inList activeVariantIds) and (ChatVariants.deletedAt.isNotNull()) }
                    .count()
                assertEquals(0L, deletedVariantsLeak, "В списке updatedChatVariants протекли удаленные варианты!")
            }

            // 3. Проверяем Characters
            val activeCharacterIds = response.updatedCharacters.map { it.id }
            if (activeCharacterIds.isNotEmpty()) {
                val deletedCharacters = AICharacters.select(AICharacters.id)
                    .where { (AICharacters.id inList activeCharacterIds) and (AICharacters.deletedAt.isNotNull()) }
                    .map { it[AICharacters.id].value }

                val validForcedIds = response.updatedChatSessions.map { it.characterId }.toSet()
                for (deletedCharId in deletedCharacters) {
                    assertTrue(
                        validForcedIds.contains(deletedCharId),
                        "Удаленный персонаж $deletedCharId попал в ответ, хотя активной сессии с ним НЕТ!"
                    )
                }
            }

            // 4. Проверяем Personas
            val activePersonaIds = response.updatedUserPersonas.map { it.id }
            if (activePersonaIds.isNotEmpty()) {
                val deletedPersonasLeak = UserPersonas.select(UserPersonas.id)
                    .where { (UserPersonas.id inList activePersonaIds) and (UserPersonas.deletedAt.isNotNull()) }
                    .count()
                assertEquals(0L, deletedPersonasLeak, "В списке updatedUserPersonas протекли удаленные персоны!")
            }
        }
    }

    @Test
    fun testDeleteUseCaseAndSync(): Unit = runBlocking {
        val systemUserId = Uuid.parse("8bd56bfb-54e2-4d44-b1c7-be6bc29cb99d")
        val deleteUseCase: app.domain.session.usecases.DeleteChatSessionsBatchUseCase by inject()

        // 1. Делаем первую синхронизацию, чтобы зафиксировать время (это имитация клиента, который уже залогинен)
        val firstResponse = syncManager.execute(SyncDataRequest(systemUserId, Instant.fromEpochMilliseconds(0)))
        val currentSince = firstResponse.latestTimestamp

        // 2. Находим любую активную сессию
        val testSessionId = firstResponse.updatedChatSessions.firstOrNull()?.id
        assertNotNull(testSessionId, "Не нашли ни одной сессии в базе для теста")

        // 3. Вызываем РЕАЛЬНЫЙ код удаления чата (точно так же, как это делает другой клиент)
        val request = com.ensodai.shared.avalon.chat.models.BatchDeleteChatsRequest(listOf(testSessionId))
        deleteUseCase.execute(request, systemUserId, null)

        // 4. Сразу после этого веб-клиент получает SyncTrigger и запрашивает дифференциальную синхронизацию
        val response = syncManager.execute(SyncDataRequest(systemUserId, currentSince))
        
        // 5. Проверяем, отдал ли бэкенд ID удаленного чата
        val isInDeleted = response.deletedChatSessionIds.contains(testSessionId)
        assertTrue(isInDeleted, "КРИТИЧЕСКАЯ ОШИБКА БЕКЕНДА: UseCase удалил чат, но ID не попал в deletedChatSessionIds при немедленной синхронизации!")
    }
}
