package com.ensodai.avalon.di

import com.ensodai.avalon.data.db.AppDataBase
import com.ensodai.avalon.data.repository.local.base.DatabaseExecutor
import com.ensodai.avalon.data.db.dao.AppSettingsDao
import com.ensodai.avalon.data.db.dao.CacheDao
import com.ensodai.avalon.data.db.dao.CharacterDao
import com.ensodai.avalon.data.db.dao.ChatHistoryDao
import com.ensodai.avalon.data.db.dao.ChatSessionDao
import com.ensodai.avalon.data.db.dao.ChatVariantDao
import com.ensodai.avalon.data.db.dao.GiftDao
import com.ensodai.avalon.data.db.dao.MainRulesDao
import com.ensodai.avalon.data.db.dao.MessageQueueRestorerDao
import com.ensodai.avalon.data.db.dao.UpdateCharacterDao
import com.ensodai.avalon.data.db.dao.UserDao
import com.ensodai.avalon.data.repository.local.AppSettingsRepositoryImpl
import com.ensodai.avalon.data.repository.local.DatastoreManager
import com.ensodai.avalon.data.repository.local.LocalCacheRepositoryImpl
import com.ensodai.avalon.data.repository.local.LocalCharacterRepositoryImpl
import com.ensodai.avalon.data.repository.local.LocalChatHistoryRepositoryImpl
import com.ensodai.avalon.data.repository.local.LocalChatSessionsRepositoryImpl
import com.ensodai.avalon.data.repository.local.LocalGiftsRepositoryImpl
import com.ensodai.avalon.data.repository.local.LocalUpdateCharacterRepositoryImpl
import com.ensodai.avalon.data.repository.local.LocalUserRepositoryImpl
import com.ensodai.avalon.data.repository.local.MainRulesRepositoryImpl
import com.ensodai.avalon.data.repository.local.MessageQueueRestorerRepositoryImpl
import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import com.ensodai.avalon.domain.repository.local.LocalCacheRepository
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.repository.local.LocalGiftsRepository
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import com.ensodai.avalon.domain.repository.local.MessageQueueRestorerRepository
import com.ensodai.avalon.domain.repository.remote.MainRulesRepository
import com.ensodai.avalon.domain.repository.remote.UserAiSettingsRepository
import com.ensodai.avalon.domain.repository.local.LocalCategoryRepository
import com.ensodai.avalon.data.repository.local.LocalCategoryRepositoryImpl
import com.ensodai.avalon.data.repository.remote.UserAiSettingsRepositoryImpl
import com.ensodai.shared.avalon.rpc.services.UserRpcService
import com.ensodai.avalon.utils.AppLogger
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.ensodai.avalon.data.repository.local.base")
class LocalRepositoryProvidersModule {


    @Single
    fun provideLocalCharacterRepository(
        dbExecutor: DatabaseExecutor,
        characterDao: CharacterDao,
        appDataBase: AppDataBase,
        datastoreManager: DatastoreManager,
        userDao: UserDao,
        chatHistoryDao: ChatHistoryDao,
        chatVariantDao: ChatVariantDao,
        cacheDao: CacheDao,
        logger: AppLogger
    ): LocalCharacterRepository {
        return LocalCharacterRepositoryImpl(
            dbExecutor = dbExecutor,
            characterDao = characterDao,
            appDatabase = appDataBase,
            datastoreManager = datastoreManager,
            userDao = userDao,
            chatHistoryDao = chatHistoryDao,
            chatVariantDao = chatVariantDao,
            cacheDao = cacheDao,
            logger = logger
        )
    }

    @Single
    fun provideLocalChatHistoryRepository(
        dbExecutor: DatabaseExecutor,
        appDataBase: AppDataBase,
        chatHistoryDao: ChatHistoryDao,
        chatSessionDao: ChatSessionDao,
        chatVariantDao: ChatVariantDao,
        logger: AppLogger
    ): LocalChatHistoryRepository {
        return LocalChatHistoryRepositoryImpl(
            dbExecutor = dbExecutor,
            appDatabase = appDataBase,
            chatHistoryDao = chatHistoryDao,
            chatSessionDao = chatSessionDao,
            chatVariantDao = chatVariantDao,
            logger = logger
        )
    }

    @Single
    fun provideLocalUserRepository(
        dbExecutor: DatabaseExecutor,
        userDao: UserDao,
        datastoreManager: DatastoreManager
    ): LocalUserRepository {
        return LocalUserRepositoryImpl(
            dbExecutor = dbExecutor,
            userDao = userDao,
            datastoreManager = datastoreManager
        )
    }

    @Single
    fun provideLocalChatSessionsRepository(
        dbExecutor: DatabaseExecutor,
        chatSessionsDao: ChatSessionDao,
        appDataBase: AppDataBase,
        chatVariantDao: ChatVariantDao,
        characterDao: CharacterDao,
        cacheDao: CacheDao,
        logger: AppLogger
    ): LocalChatSessionsRepository {
        return LocalChatSessionsRepositoryImpl(
            dbExecutor = dbExecutor,
            chatSessionsDao = chatSessionsDao,
            appDatabase = appDataBase,
            chatVariantDao = chatVariantDao,
            characterDao = characterDao,
            cacheDao = cacheDao,
            logger = logger
        )
    }

    @Single
    fun provideAppSettingsRepository(
        dbExecutor: DatabaseExecutor,
        appSettingsDao: AppSettingsDao,
        logger: AppLogger
    ): AppSettingsRepository {
        return AppSettingsRepositoryImpl(
            dbExecutor = dbExecutor,
            appSettingsDao = appSettingsDao,
            logger = logger
        )
    }

    @Single
    fun provideLocalCacheRepository(dbExecutor: DatabaseExecutor, cacheDao: CacheDao): LocalCacheRepository {
        return LocalCacheRepositoryImpl(dbExecutor = dbExecutor, cacheDao = cacheDao)
    }

    @Single
    fun provideMainRulesRepository(dbExecutor: DatabaseExecutor, mainRulesDao: MainRulesDao): MainRulesRepository {
        return MainRulesRepositoryImpl(dbExecutor = dbExecutor, mainRulesDao = mainRulesDao)
    }

    @Single
    fun provideLocalUpdateCharacterRepository(
        dbExecutor: DatabaseExecutor,
        updateCharacterDao: UpdateCharacterDao
    ): LocalUpdateCharacterRepository {
        return LocalUpdateCharacterRepositoryImpl(dbExecutor = dbExecutor, updateCharacterDao = updateCharacterDao)
    }

    @Single
    fun provideLocalGiftsRepository(dbExecutor: DatabaseExecutor, giftDao: GiftDao): LocalGiftsRepository {
        return LocalGiftsRepositoryImpl(dbExecutor = dbExecutor, giftDao = giftDao)
    }

    @Single
    fun provideLocalCategoryRepository(dbExecutor: DatabaseExecutor, cacheDao: CacheDao): LocalCategoryRepository {
        return LocalCategoryRepositoryImpl(dbExecutor = dbExecutor, cacheDao = cacheDao)
    }

    @Single
    fun provideMessageQueueRestorerRepository(
        dbExecutor: DatabaseExecutor,
        messageQueueRestorerDao: MessageQueueRestorerDao
    ): MessageQueueRestorerRepository {
        return MessageQueueRestorerRepositoryImpl(dbExecutor = dbExecutor, messageQueueRestorerDao = messageQueueRestorerDao)
    }

    @Single
    fun provideUserAiSettingsRepository(userRpcService: UserRpcService): UserAiSettingsRepository {
        return UserAiSettingsRepositoryImpl(userRpcService = userRpcService)
    }
}
