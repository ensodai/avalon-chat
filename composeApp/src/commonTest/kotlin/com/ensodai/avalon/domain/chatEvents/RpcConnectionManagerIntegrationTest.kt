package com.ensodai.avalon.domain.chatEvents

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.ensodai.shared.avalon.gifts.models.LocationResultResponse
import com.ensodai.shared.avalon.rpc.services.UserRpcService
import com.ensodai.shared.avalon.user.models.*
import com.ensodai.avalon.data.repository.local.DatastoreManager
import com.ensodai.avalon.data.repository.remote.rpc.RpcCallExecutor
import com.ensodai.avalon.data.repository.remote.rpc.UserRpcServiceRemoteImpl
import com.ensodai.avalon.di.RpcConnectionManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.rpc.RpcClient
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class RpcConnectionManagerIntegrationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val mockEngine = MockEngine { respond("OK", HttpStatusCode.OK) }
    private val httpClient = HttpClient(mockEngine)

    /**
     * ТЕСТ 1: Проверка безусловного реконнекта боевого RpcConnectionManager при onAppForeground().
     */
    @Test
    fun testOnAppForegroundMustForceReconnectEvenWhenSocketWasReportedAlive() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val dataStore = FakePreferencesDataStore()
        val datastoreManager = DatastoreManager(dataStore, testScope)

        val manager = RpcConnectionManager(
            httpClient = httpClient,
            datastoreManager = datastoreManager,
            scope = testScope,
            json = json
        )

        var reconnectSignalReceived = false
        val job = testScope.launch {
            manager.reconnectSignal.collect {
                reconnectSignalReceived = true
            }
        }
        testScope.advanceUntilIdle()

        manager.onAppForeground()
        testScope.advanceUntilIdle()

        assertTrue(reconnectSignalReceived, "onAppForeground ОБЯЗАН безусловно отправлять reconnectSignal!")
        job.cancel()
    }

    /**
     * ТЕСТ 2: Проверка авто-повтора реального класса UserRpcServiceRemoteImpl через RpcCallExecutor.
     */
    @Test
    fun testAutoRetryMustRecoverFromRealClientCancelledException() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val dataStore = FakePreferencesDataStore()
        val datastoreManager = DatastoreManager(dataStore, testScope)

        val manager = RpcConnectionManager(
            httpClient = httpClient,
            datastoreManager = datastoreManager,
            scope = testScope,
            json = json
        )
        val executor = RpcCallExecutor(manager)

        var backendAttemptsCount = 0
        var shouldThrowClientCancelled = true

        val stubBackend = object : UserRpcServiceStub() {
            override suspend fun getUserAiSettings(): UserAiSettingsResponse {
                backendAttemptsCount++
                if (shouldThrowClientCancelled) {
                    shouldThrowClientCancelled = false
                    throw CancellationException("Client cancelled")
                }
                return createDummyAiSettingsResponse()
            }
        }

        val realService = object : UserRpcServiceRemoteImpl(manager, executor) {
            override suspend fun getService(): UserRpcService = stubBackend
        }

        val result = realService.getUserAiSettings()

        assertEquals(2, backendAttemptsCount, "Боевой UserRpcServiceRemoteImpl обязан выполнить retry при Client cancelled!")
        assertEquals("google", result.providerId)
    }

    /**
     * ТЕСТ 3 (Каталог Deep Research #18): In-Flight Storm (Шторм 10 параллельных запросов при обрыве сокета).
     * 10 одновременных вызовов RPC падают из-за ClosedReceiveChannelException / Client cancelled,
     * все 10 прозрачно восстанавливаются и возвращают 100% результат.
     */
    @Test
    fun testInFlightStormConcurrentRpcCallsRecoverOnSingleFlightReconnect() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val dataStore = FakePreferencesDataStore()
        val datastoreManager = DatastoreManager(dataStore, testScope)

        val manager = RpcConnectionManager(
            httpClient = httpClient,
            datastoreManager = datastoreManager,
            scope = testScope,
            json = json
        )
        val executor = RpcCallExecutor(manager)

        val totalCallsCount = AtomicInteger(0)
        val hasFailedOnce = Array(10) { false }

        val stubBackend = object : UserRpcServiceStub() {
            override suspend fun checkNicknameAvailability(request: CheckNicknameRequest): AvailableResultResponse {
                totalCallsCount.incrementAndGet()
                val index = request.nickname.removePrefix("user_").toInt()
                if (!hasFailedOnce[index]) {
                    hasFailedOnce[index] = true
                    throw CancellationException("ClosedReceiveChannelException: Channel was closed")
                }
                return AvailableResultResponse(available = true)
            }
        }

        val realService = object : UserRpcServiceRemoteImpl(manager, executor) {
            override suspend fun getService(): UserRpcService = stubBackend
        }

        // Запускаем 10 параллельных запросов
        val deferredResults = (0 until 10).map { i ->
            testScope.async {
                realService.checkNicknameAvailability(CheckNicknameRequest(nickname = "user_$i"))
            }
        }

        testScope.advanceUntilIdle()

        val results = deferredResults.awaitAll()
        assertEquals(10, results.size)
        results.forEach { assertTrue(it.available) }
        // Все 10 запросов упали по 1 разу и восстановились со 2-й попытки (итого 20 попыток бэкенда)
        assertEquals(20, totalCallsCount.get(), "Все 10 параллельных вызовов должны были успешно повториться!")
    }

    /**
     * ТЕСТ 4 (Каталог Deep Research #22): User Cancellation vs Network Error.
     * Закрытие экрана / отмена родительского скоупа НЕ должно вызывать retry сокета.
     */
    @Test
    fun testUserCancellationDoesNotTriggerNetworkRetry() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val dataStore = FakePreferencesDataStore()
        val datastoreManager = DatastoreManager(dataStore, testScope)

        val manager = RpcConnectionManager(
            httpClient = httpClient,
            datastoreManager = datastoreManager,
            scope = testScope,
            json = json
        )
        val executor = RpcCallExecutor(manager)

        var backendAttempts = 0
        val stubBackend = object : UserRpcServiceStub() {
            override suspend fun getUserAiSettings(): UserAiSettingsResponse {
                backendAttempts++
                delay(1000) // имитируем долгий запрос
                return createDummyAiSettingsResponse()
            }
        }

        val realService = object : UserRpcServiceRemoteImpl(manager, executor) {
            override suspend fun getService(): UserRpcService = stubBackend
        }

        val job = testScope.launch {
            realService.getUserAiSettings()
        }

        // Пользователь закрыл экран через 100 мс (отмена корутины экрана)
        testScope.testScheduler.advanceTimeBy(100)
        job.cancel()
        testScope.advanceUntilIdle()

        // Запрос выполнился ровно 1 раз и не пошел на retry
        assertEquals(1, backendAttempts, "Отмена пользователем не должна вызывать retry сокета!")
    }

    /**
     * ТЕСТ 5 (Каталог Deep Research #31): Ретрай терминальных ошибок (400 Bad Request / 403 Forbidden).
     * Терминальные ошибки бэкенда выбрасываются сразу без бесконечных повторов.
     */
    @Test
    fun testTerminalErrorDoesNotRetryAndThrowsImmediately() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val dataStore = FakePreferencesDataStore()
        val datastoreManager = DatastoreManager(dataStore, testScope)

        val manager = RpcConnectionManager(
            httpClient = httpClient,
            datastoreManager = datastoreManager,
            scope = testScope,
            json = json
        )
        val executor = RpcCallExecutor(manager)

        var attempts = 0
        val stubBackend = object : UserRpcServiceStub() {
            override suspend fun checkNicknameAvailability(request: CheckNicknameRequest): AvailableResultResponse {
                attempts++
                throw IllegalArgumentException("400 Bad Request: Nickname contains invalid characters")
            }
        }

        val realService = object : UserRpcServiceRemoteImpl(manager, executor) {
            override suspend fun getService(): UserRpcService = stubBackend
        }

        assertFailsWith<IllegalArgumentException> {
            realService.checkNicknameAvailability(CheckNicknameRequest(nickname = "invalid!"))
        }

        assertEquals(1, attempts, "Терминальная ошибка 400 Bad Request должна завершаться немедленно без retry!")
    }

    /**
     * ТЕСТ 6 (Каталог Deep Research #10 / #13): Исчерпание попыток при 100% падении сети.
     * При постоянной сетевой ошибке вызов выполняется ровно maxAttempts (3) раз и выбрасывает исключение.
     */
    @Test
    fun testMaxAttemptsExhaustedThrowsPermanentNetworkException() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val dataStore = FakePreferencesDataStore()
        val datastoreManager = DatastoreManager(dataStore, testScope)

        val manager = RpcConnectionManager(
            httpClient = httpClient,
            datastoreManager = datastoreManager,
            scope = testScope,
            json = json
        )
        val executor = RpcCallExecutor(manager)

        var totalAttempts = 0
        val stubBackend = object : UserRpcServiceStub() {
            override suspend fun getUserAccountInfo(): UserAccountInfoResponse {
                totalAttempts++
                throw CancellationException("Connection reset by peer")
            }
        }

        val realService = object : UserRpcServiceRemoteImpl(manager, executor) {
            override suspend fun getService(): UserRpcService = stubBackend
        }

        assertFailsWith<CancellationException> {
            realService.getUserAccountInfo()
        }

        assertEquals(3, totalAttempts, "При 100% обрыве сети должно быть ровно 3 попытки (maxAttempts) перед выбросом ошибки!")
    }

    /**
     * ТЕСТ 7 (Каталог Deep Research #15 / #23): Single-Flight Concurrency & Double-Checked Locking.
     * При одновременном запросе getActiveClient() из 20 корутин создается ровно 1 экземпляр сокета.
     */
    @Test
    fun testRapidFlappingSingleFlightConcurrency() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val dataStore = FakePreferencesDataStore()
        val datastoreManager = DatastoreManager(dataStore, testScope)

        val socketCreationsCount = AtomicInteger(0)
        val fakeClient: RpcClient = object : RpcClient, CoroutineScope by testScope {
            override suspend fun <T> call(call: kotlinx.rpc.RpcCall): T = error("Not needed")
            override fun <T> callServerStreaming(call: kotlinx.rpc.RpcCall): Flow<T> = error("Not needed")
        }

        val manager = object : RpcConnectionManager(
            httpClient = httpClient,
            datastoreManager = datastoreManager,
            scope = testScope,
            json = json
        ) {
            override suspend fun createRpcClient(): RpcClient {
                socketCreationsCount.incrementAndGet()
                delay(50) // имитируем сетевую задержку хэндшейка сокета
                return fakeClient
            }

            override suspend fun authenticateClient(client: RpcClient, token: String) {}
        }

        // 20 параллельных корутин одновременно запрашивают сокет во время хэндшейка
        val clients = (0 until 20).map {
            testScope.async {
                manager.getActiveClient()
            }
        }

        testScope.advanceUntilIdle()

        val resolvedClients = clients.awaitAll()
        assertEquals(20, resolvedClients.size)
        // Single-Flight Mutex обязан был объединить все 20 запросов в ровно 1 создание сокета!
        assertEquals(1, socketCreationsCount.get(), "SingleFlight Mutex обязан создавать ровно 1 сокет при шторме параллельных запросов!")
        resolvedClients.forEach { client ->
            assertTrue(client === fakeClient, "Все 20 корутин обязаны получить один и тот же синглтон-сокет!")
        }
    }

    /**
     * ТЕСТ 8 (Каталог Deep Research #25): Обрыв стрима и мгновенный сигнал реконнекта.
     * При сбое стрима notifyStreamFailure инвалидирует сокет и посылает reconnectSignal.
     */
    @Test
    fun testStreamFailureNotificationTriggersSocketInvalidation() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val dataStore = FakePreferencesDataStore()
        val datastoreManager = DatastoreManager(dataStore, testScope)

        val manager = RpcConnectionManager(
            httpClient = httpClient,
            datastoreManager = datastoreManager,
            scope = testScope,
            json = json
        )

        var reconnectSignalReceived = false
        val job = testScope.launch {
            manager.reconnectSignal.collect {
                reconnectSignalReceived = true
            }
        }
        testScope.advanceUntilIdle()

        // Стрим упал посреди чтения
        manager.notifyStreamFailure(CancellationException("ClosedReceiveChannelException: Stream broken"))
        testScope.advanceUntilIdle()

        assertTrue(reconnectSignalReceived, "notifyStreamFailure ОБЯЗАН инвалидировать сокет и слать reconnectSignal!")
        job.cancel()
    }

    private fun createDummyAiSettingsResponse() = UserAiSettingsResponse(
        userId = Uuid.random(),
        providerId = "google",
        availableProviders = listOf(
            AiProviderResponse(id = "google", displayName = "Google AI", isDefault = true)
        ),
        baseUrl = "https://example.com",
        apiKey = "key",
        textModel = "model",
        ttsModel = "tts",
        imageModel = "image",
        imagePromptModel = "prompt",
        summaryModel = "summary",
        isCustomProviderEnabled = false
    )
}

private class FakePreferencesDataStore(initial: Preferences = emptyPreferences()) : DataStore<Preferences> {
    private val _data = MutableStateFlow(initial)
    override val data: Flow<Preferences> = _data
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(_data.value)
        _data.value = updated
        return updated
    }
}

private open class UserRpcServiceStub : UserRpcService {
    override suspend fun getUserAccountInfo(): UserAccountInfoResponse = error("Stub")
    override suspend fun checkNicknameAvailability(request: CheckNicknameRequest): AvailableResultResponse = error("Stub")
    override suspend fun updateUserNickname(request: UpdateNicknameRequest) = error("Stub")
    override suspend fun updateUserOutreach(request: UpdateUserOutreachRequest) = error("Stub")
    override suspend fun getUserLocation(): LocationResultResponse = error("Stub")
    override suspend fun getUserAiSettings(): UserAiSettingsResponse = error("Stub")
    override suspend fun updateUserAiSettings(request: UpdateUserAiSettingsRequest): UserAiSettingsResponse = error("Stub")
    override suspend fun testUserAiConnection(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse = error("Stub")
    override suspend fun testChatModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse = error("Stub")
    override suspend fun testTtsModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse = error("Stub")
    override suspend fun testImageModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse = error("Stub")
    override suspend fun getAvailableAiModels(request: GetAiCatalogModelsRequest): GetAiCatalogModelsResponse = error("Stub")
    override suspend fun updateUserFlowMode(request: UpdateUserFlowModeRequest) = error("Stub")
}
