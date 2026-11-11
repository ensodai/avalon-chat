package com.ensodai.avalon.domain.sessionWorker

import com.ensodai.avalon.data.repository.local.DatastoreManager
import com.ensodai.avalon.di.ApplicationScope
import com.ensodai.avalon.di.RpcConnectionManager
import com.ensodai.shared.avalon.rpc.services.UserRpcService
import com.ensodai.avalon.utils.L
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.Job
import org.koin.core.annotation.Single

@Single
class RaceConditionTester(
    private val datastoreManager: DatastoreManager,
    private val rpcConnectionManager: RpcConnectionManager,
    private val userRpcService: UserRpcService,
    @ApplicationScope private val scope: CoroutineScope
) {
    private var counter = 0
    private val TAG = "RACE_TEST"

    /**
     * СЦЕНАРИЙ 1: "КЛАССИКА"
     * RPC соединение переподключается с протухшим токеном и лезет делать рефреш.
     * Одновременно защищенный RPC запрос летит на сервер и сталкивается с протухшим токеном.
     */
    fun triggerRaceWsVsRpc() {
        val testId = ++counter
        L.e(TAG, "🏁 [$testId] ЗАПУСК СЦЕНАРИЯ: WS Reconnect vs RPC Request")

        scope.launch {
            // 1. ОТРАВЛЯЕМ КЭШ
            val realRefreshToken = datastoreManager.getRefreshToken() ?: return@launch
            val realUserId = datastoreManager.getUserId() ?: return@launch

            // JWT от 2018 года (валидный по структуре, но протухший)
            val expiredJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlRlc3QiLCJpYXQiOjE1MTYyMzkwMjIsImV4cCI6MTUxNjIzOTAyMn0.X"

            datastoreManager.saveAuthData(expiredJwt, realRefreshToken, realUserId)
            L.d(TAG, "💀 [$testId] Токен отравлен (установлен expired JWT)")

            // Даем время записать данные
            delay(200)

            // 2. ЗАПУСКАЕМ ГОНКУ
            // БОЕЦ 1: Переподключение WebSocket
            val wsJob = launch(IODispatcher) {
                L.d(TAG, "🥊 [$testId] WS: Триггерим мгновенный реконнект сокета...")
                try {
                    rpcConnectionManager.triggerInstantReconnect()
                } catch (e: Exception) {
                    L.e(TAG, "[$testId] WS Error: $e")
                }
            }

            // БОЕЦ 2: Защищенный RPC запрос
            val rpcJob = launch(IODispatcher) {
                L.d(TAG, "🥊 [$testId] RPC: Запуск защищенного запроса...")
                try {
                    userRpcService.getUserAccountInfo()
                    L.d(TAG, "✅ [$testId] RPC: Успех (значит рефреш прошел)")
                } catch (e: Exception) {
                    L.w(TAG, "[$testId] RPC Exception: $e")
                }
            }

            joinAll(wsJob, rpcJob)
            L.e(TAG, "🏁 [$testId] Тест завершен. Если приложение зависло — баг воспроизвелся.")
        }
    }

    /**
     * СЦЕНАРИЙ 2: "СПАМ-АТАКА"
     * 50 защищенных RPC запросов одновременно отправляются при протухшем токене.
     */
    fun triggerSpamAttack() {
        val testId = ++counter
        L.e(TAG, "🔥 [$testId] ЗАПУСК СТРЕСС-ТЕСТА: 50 RPC запросов vs 1 Mutex")

        scope.launch {
            // 1. Отравляем токен
            val realRefreshToken = datastoreManager.getRefreshToken() ?: return@launch
            val realUserId = datastoreManager.getUserId() ?: return@launch
            val expiredJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlRlc3QiLCJpYXQiOjE1MTYyMzkwMjIsImV4cCI6MTUxNjIzOTAyMn0.X"

            datastoreManager.saveAuthData(expiredJwt, realRefreshToken, realUserId)
            delay(200)

            L.d(TAG, "💀 [$testId] Токен отравлен. Выпускаем кракена...")

            val jobs = mutableListOf<Job>()

            // 2. БОЕЦ 1: Реконнект сокета
            jobs += launch(IODispatcher) {
                try {
                    rpcConnectionManager.triggerInstantReconnect()
                } catch (e: Exception) { L.e(TAG, "[$testId] WS Error: $e") }
            }

            // 3. АРМИЯ: 50 параллельных запросов
            repeat(50) { i ->
                jobs += launch(IODispatcher) {
                    try {
                        delay((0..50).random().toLong())
                        L.v(TAG, "🔫 [$testId] Запрос #$i полетел")
                        userRpcService.getUserAccountInfo()
                        L.d(TAG, "✅ [$testId] Запрос #$i: Успех")
                    } catch (e: Exception) {
                        L.w(TAG, "[$testId] Запрос #$i Exception: $e")
                    }
                }
            }

            joinAll(*jobs.toTypedArray())
            L.e(TAG, "🏁 [$testId] Стресс-тест завершен.")
        }
    }
}
