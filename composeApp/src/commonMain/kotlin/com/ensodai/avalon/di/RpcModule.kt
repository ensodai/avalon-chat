package com.ensodai.avalon.di

import com.ensodai.shared.avalon.rpc.services.*
import com.ensodai.shared.avalon.chat.services.ChatRpcService
import com.ensodai.avalon.data.repository.remote.rpc.*
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.rpc.RpcClient
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

import kotlinx.coroutines.CoroutineScope
import com.ensodai.avalon.data.repository.local.DatastoreManager

@Module
@ComponentScan("com.ensodai.avalon.domain.chatEvents")
class RpcModule {

    @Single
    fun provideRpcConnectionManager(
        @KtorHttpClient httpClient: HttpClient,
        datastoreManager: DatastoreManager,
        @ApplicationScope appScope: CoroutineScope,
        json: Json
    ): RpcConnectionManager {
        return RpcConnectionManager(httpClient, datastoreManager, appScope, json)
    }

    @Single
    fun provideRpcCallExecutor(manager: RpcConnectionManager): RpcCallExecutor {
        return RpcCallExecutor(manager)
    }

    @Single
    fun provideAuthRpcService(manager: RpcConnectionManager, executor: RpcCallExecutor): AuthRpcService {
        return AuthRpcServiceRemoteImpl(manager, executor)
    }

    @Single
    fun provideAppVersionRpcService(manager: RpcConnectionManager, executor: RpcCallExecutor): AppVersionRpcService {
        return AppVersionRpcServiceRemoteImpl(manager, executor)
    }

    @Single
    fun provideCategoryRpcService(manager: RpcConnectionManager, executor: RpcCallExecutor): CategoryRpcService {
        return CategoryRpcServiceRemoteImpl(manager, executor)
    }

    @Single
    fun provideCharacterRpcService(manager: RpcConnectionManager, executor: RpcCallExecutor): CharacterRpcService {
        return CharacterRpcServiceRemoteImpl(manager, executor)
    }

    @Single
    fun provideChatRpcService(manager: RpcConnectionManager, executor: RpcCallExecutor): ChatRpcService {
        return ChatRpcServiceRemoteImpl(manager, executor)
    }

    @Single
    fun provideGiftsRpcService(manager: RpcConnectionManager, executor: RpcCallExecutor): GiftsRpcService {
        return GiftsRpcServiceRemoteImpl(manager, executor)
    }

    @Single
    fun provideLiveCallRpcService(manager: RpcConnectionManager): LiveCallRpcService {
        return LiveCallRpcServiceRemoteImpl(manager)
    }

    @Single
    fun providePersonaRpcService(manager: RpcConnectionManager, executor: RpcCallExecutor): PersonaRpcService {
        return PersonaRpcServiceRemoteImpl(manager, executor)
    }

    @Single
    fun provideSessionSupportRpcService(manager: RpcConnectionManager, executor: RpcCallExecutor): SessionSupportRpcService {
        return SessionSupportRpcServiceRemoteImpl(manager, executor)
    }

    @Single
    fun provideSyncRpcService(manager: RpcConnectionManager, executor: RpcCallExecutor): SyncRpcService {
        return SyncRpcServiceRemoteImpl(manager, executor)
    }

    @Single
    fun provideUserRpcService(manager: RpcConnectionManager, executor: RpcCallExecutor): UserRpcService {
        return UserRpcServiceRemoteImpl(manager, executor)
    }

    @Single
    fun provideFileUploadRpcService(manager: RpcConnectionManager, executor: RpcCallExecutor): FileUploadRpcService {
        return FileUploadRpcServiceRemoteImpl(manager, executor)
    }
}
