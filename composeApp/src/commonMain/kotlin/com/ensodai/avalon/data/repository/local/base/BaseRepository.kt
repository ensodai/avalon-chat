package com.ensodai.avalon.data.repository.local.base

import com.ensodai.avalon.data.db.AppDataBase
import com.ensodai.avalon.utils.PlatformLock
import com.ensodai.avalon.utils.withLock
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

abstract class BaseRepository(
    protected val dbExecutor: DatabaseExecutor
) {
    // Внутренний кеш для параметризованных потоков (чтобы не плодить дубликаты)
    private val flowCache = mutableMapOf<String, Any>()
    private val cacheLock = PlatformLock()

    /**
     * Безопасная запись в базу (строго через мьютекс и NonCancellable)
     */
    protected suspend fun <T> dbWrite(block: suspend AppDataBase.() -> T): T {
        return dbExecutor.write(block)
    }

    /**
     * Безопасное разовое чтение из базы
     */
    protected suspend fun <T> dbRead(block: suspend AppDataBase.() -> T): T {
        return dbExecutor.read(block)
    }

    /**
     * Для глобальных потоков (например, список всех чатов, настройки)
     */
    protected fun <T> dbObserve(block: () -> Flow<T>): Flow<T> {
        return block()
            .distinctUntilChanged()
            .buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            .shareIn(
                scope = dbExecutor.appScope,
                started = SharingStarted.WhileSubscribed(5000),
                replay = 1
            )
    }

    /**
     * Для потоков с параметрами (например, сообщения конкретного чата по chatId).
     * Автоматически кеширует поток по ключу.
     */
    @Suppress("UNCHECKED_CAST")
    protected fun <T> cachedObserve(cacheKey: String, block: () -> Flow<T>): Flow<T> {
        return cacheLock.withLock {
            flowCache.getOrPut(cacheKey) {
                block()
                    .distinctUntilChanged()
                    .buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
                    .shareIn(
                        scope = dbExecutor.appScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        replay = 1
                    )
            } as Flow<T>
        }
    }
}
