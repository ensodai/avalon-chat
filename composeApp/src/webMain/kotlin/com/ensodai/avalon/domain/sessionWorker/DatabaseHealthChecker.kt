package com.ensodai.avalon.domain.sessionWorker

import com.ensodai.avalon.data.db.AppDataBase
import com.ensodai.avalon.utils.L
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

class DatabaseHealthChecker(
    private val appDataBase: AppDataBase
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Выполняет диагностический "ping" к базе данных.
     * Вызывается при возвращении приложения из фонового режима.
     */
    fun pingDatabaseOnWakeUp() {
        scope.launch {
            try {
                // Если база данных жива и OPFS Handle восстанавливается корректно, ответ придет за 10-20 мс.
                // Если она зависла в Deadlock, мы прерываем ожидание через 3 секунды.
                withTimeout<Unit>(3000L.milliseconds) {
                    appDataBase.appSettingsDao().getSafetyReminderAccepted()
                }
                L.d("DatabaseHealthChecker", "DB Ping OK! OPFS lock re-acquired successfully.")
            } catch (e: TimeoutCancellationException) {
                L.e("DatabaseHealthChecker", "DB Ping Timeout! OPFS Worker deadlocked after background suspend.")
                recoverByHardReload()
            } catch (e: Throwable) {
                L.e("DatabaseHealthChecker", "DB Ping Fatal Error: ${e.message}")
                recoverByHardReload()
            }
        }
    }

    /**
     * Гарантированный механизм очистки системных блокировок и памяти.
     */
    private fun recoverByHardReload() {
        L.d("DatabaseHealthChecker", "Initiating hard reset and page reload...")
        try {
            performHardResetAndReload()
        } catch (e: Throwable) {
            window.location.reload()
        }
    }
}

