package com.ensodai.avalon.domain.sessionWorker

import com.ensodai.avalon.di.ApplicationScope
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.shared.avalon.chat.services.ChatRpcService
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class MessageReadManager(
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val chatRpcService: ChatRpcService,
    @ApplicationScope private val scope: CoroutineScope,
    private val logger: AppLogger
) {

    fun markMessagesAsRead(containerIds: List<Uuid>) {
        if (containerIds.isEmpty()) return

        scope.launch(IODispatcher) {
            logger.d("MessageReadManager", "📖 Помечаем сообщения как прочитанные локально: $containerIds")
            // Оптимистичное локальное обновление
            runCatching {
                localChatHistoryRepository.markMessagesAsReadLocally(containerIds)
            }.onFailure { e ->
                logger.e("MessageReadManager", "❌ Ошибка при локальной пометке сообщений как прочитанных: ${e.message}")
            }

            // Запрос на сервер в ApplicationScope
            logger.d("MessageReadManager", "🌐 Отправляем запрос прочтения на сервер для: $containerIds")
            runCatching {
                chatRpcService.markMessagesAsRead(containerIds)
            }.fold(
                onSuccess = {
                    logger.d("MessageReadManager", "✅ Сервер успешно подтвердил прочтение сообщений")
                },
                onFailure = { e ->
                    logger.e("MessageReadManager", "❌ Ошибка при отправке прочтения на сервер: ${e.message}")
                }
            )
        }
    }

    fun markAllMessagesAsRead(variantId: Uuid) {
        scope.launch(IODispatcher) {
            runCatching {
                val unreadIds = localChatHistoryRepository.getUnreadModelContainerIds(variantId)
                if (unreadIds.isNotEmpty()) {
                    logger.d("MessageReadManager", "📖 Найдено ${unreadIds.size} непрочитанных сообщений для варианта $variantId. Помечаем прочитанными.")
                    markMessagesAsRead(unreadIds)
                } else {
                    logger.d("MessageReadManager", "📖 Нет непрочитанных сообщений для варианта $variantId")
                }
            }.onFailure { e ->
                logger.e("MessageReadManager", "❌ Ошибка при получении непрочитанных сообщений для варианта $variantId: ${e.message}")
            }
        }
    }

    suspend fun markAllMessagesAsReadSync(variantId: Uuid) {
        runCatching {
            val unreadIds = localChatHistoryRepository.getUnreadModelContainerIds(variantId)
            if (unreadIds.isNotEmpty()) {
                logger.d("MessageReadManager", "📖 Найдено ${unreadIds.size} непрочитанных сообщений для варианта $variantId. Помечаем прочитанными локально.")
                localChatHistoryRepository.markMessagesAsReadLocally(unreadIds)
                
                // Запрос на сервер в ApplicationScope (не блокирует)
                scope.launch(IODispatcher) {
                    logger.d("MessageReadManager", "🌐 Отправляем запрос прочтения на сервер для: $unreadIds")
                    runCatching {
                        chatRpcService.markMessagesAsRead(unreadIds)
                    }
                }
            } else {
                logger.d("MessageReadManager", "📖 Нет непрочитанных сообщений для варианта $variantId")
            }
        }.onFailure { e ->
            logger.e("MessageReadManager", "❌ Ошибка при получении непрочитанных сообщений: ${e.message}")
        }
    }
}
