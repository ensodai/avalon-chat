package com.ensodai.avalon.data.repository.local.base

import androidx.room3.withReadTransaction
import androidx.room3.withWriteTransaction
import com.ensodai.avalon.data.db.AppDataBase
import kotlinx.coroutines.*
import org.koin.core.annotation.Single
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

private class DbLockElement : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key
    companion object Key : CoroutineContext.Key<DbLockElement>
}

@Single
class DatabaseExecutor(
    val database: AppDataBase
) {
    // Долгоживущий скоуп приложения (не отменяется при навигации UI)
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    suspend fun <T> write(block: suspend AppDataBase.() -> T): T {
        if (coroutineContext[DbLockElement] != null) {
            throw IllegalStateException("DEADLOCK PREVENTED: Nested dbWrite detected! Do not call dbWrite/dbRead inside another dbWrite/dbRead block.")
        }
        return withContext(NonCancellable + DbLockElement()) {
            database.withWriteTransaction {
                database.block()
            }
        }
    }

    suspend fun <T> read(block: suspend AppDataBase.() -> T): T {
        if (coroutineContext[DbLockElement] != null) {
            throw IllegalStateException("DEADLOCK PREVENTED: Nested dbRead detected! Do not call dbWrite/dbRead inside another dbWrite/dbRead block.")
        }
        return withContext(NonCancellable + DbLockElement()) {
            database.withReadTransaction {
                database.block()
            }
        }
    }
}
