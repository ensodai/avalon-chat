package com.ensodai.avalon.domain.messageSender


import com.ensodai.avalon.di.ApplicationScope
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.avalon.utils.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class TypingStatusManager(
    @ApplicationScope private val scope: CoroutineScope,
    private val logger: AppLogger
) {

    // Храним map айдишников чатов и качество сообщений, где сейчас идет печать
    private val _typingState = MutableStateFlow<Map<Uuid, TypingContext>>(emptyMap())
    val typingChatIds: StateFlow<Map<Uuid, TypingContext>> = _typingState.asStateFlow()

    // Храним Job таймеров для каждого чата, чтобы отменять их
    private val typingTimeoutJobs = mutableMapOf<Uuid, Job>()

    companion object {
        private const val WATCHDOG_TIMEOUT_MS = 10_000L // 10 сек тишины = смерть
    }

    /**
     * Вызываем это, когда:
     * 1. Пришел явный ивент {"type": "typing", "state": true}
     * 2. Пришел явный пульс
     * 3. Пришел кусок текста (Stream Token)
     */
    fun onActivityDetected(chatId: Uuid, rarity: Rarity) {
        // 1. ВСЕГДА продлеваем таймер жизни (Watchdog).
        // Это гарантирует, что статус не исчезнет, пока сервер шлет пульс.
        rescheduleWatchdog(chatId)

        // 2. ПРОВЕРЯЕМ, нужно ли обновлять UI.
        // Достаем текущее состояние для этого чата
        val currentContext = _typingState.value[chatId]

        // Обновляем StateFlow ТОЛЬКО если:
        // а) Этого чата раньше не было в списке (currentContext == null)
        // б) ИЛИ изменилась редкость (например, начал писать EPIC сообщение после COMMON)
        if (currentContext == null || currentContext.rarity != rarity) {
            _typingState.update { currentMap ->
                currentMap + (chatId to TypingContext(rarity))
            }
            // Timber.d("UI Updated: New typing state for $chatId")
        } else {
            // Если мы здесь, значит чат уже есть в мапе и rarity такая же.
            // МЫ НЕ ТРОГАЕМ _typingState.
            // UI не получает сигнала об обновлении -> Анимация не сбрасывается -> Мерцания нет.
            // Timber.d("UI Skipped: Same typing state for $chatId, just extended watchdog")
        }
    }

    private fun rescheduleWatchdog(chatId: Uuid) {
        typingTimeoutJobs[chatId]?.cancel()
        typingTimeoutJobs[chatId] = scope.launch {
            delay(WATCHDOG_TIMEOUT_MS)
            clearTyping(chatId)
            logger.d("TypingStatusManager", "💀 Watchdog killed typing for $chatId (Timeout)")
        }
    }

    fun clearTyping(chatId: Uuid) {
        logger.d("TypingStatusManager", "clearTyping chatId $chatId")
        _typingState.update { it - chatId }
        typingTimeoutJobs.remove(chatId)?.cancel()
    }

    fun clearAll() {
        logger.d("TypingStatusManager", "Cleaning all typing statuses")
        typingTimeoutJobs.values.forEach { it.cancel() }
        typingTimeoutJobs.clear()
        _typingState.value = emptyMap()
    }

    /**
     * Хелпер для UI.
     * Возвращает Flow<TypingContext?>, где null значит "не печатает".
     */
    fun observeChatTypingState(chatId: Uuid): Flow<TypingContext?> {
        return _typingState
            .map { it[chatId] }
            .distinctUntilChanged()
    }
}
