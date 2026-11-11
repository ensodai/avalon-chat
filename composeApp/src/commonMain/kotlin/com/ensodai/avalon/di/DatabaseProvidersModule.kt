package com.ensodai.avalon.di

import com.ensodai.avalon.data.db.AppDataBase
import com.ensodai.avalon.data.db.dao.AppSettingsDao
import com.ensodai.avalon.data.db.dao.CacheDao
import com.ensodai.avalon.data.db.dao.CharacterDao
import com.ensodai.avalon.data.db.dao.ChatHistoryDao
import com.ensodai.avalon.data.db.dao.ChatSessionDao
import com.ensodai.avalon.data.db.dao.ChatVariantDao
import com.ensodai.avalon.data.db.dao.GiftDao
import com.ensodai.avalon.data.db.dao.LogoutDao
import com.ensodai.avalon.data.db.dao.MainRulesDao
import com.ensodai.avalon.data.db.dao.MessageQueueRestorerDao
import com.ensodai.avalon.data.db.dao.MessageSlotDao
import com.ensodai.avalon.data.db.dao.UpdateCharacterDao
import com.ensodai.avalon.data.db.dao.UserDao
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class DatabaseProvidersModule {

    @Single
    fun provideCharacterDao(appDataBase: AppDataBase): CharacterDao =
        appDataBase.chatModelConfigDao()

    @Single
    fun provideChatHistoryDao(appDataBase: AppDataBase): ChatHistoryDao =
        appDataBase.chatHistoryDao()

    @Single
    fun provideAppSettingsDao(appDataBase: AppDataBase): AppSettingsDao =
        appDataBase.appSettingsDao()

    @Single
    fun provideCacheDao(appDataBase: AppDataBase): CacheDao = appDataBase.cacheDao()

    @Single
    fun provideLogoutDao(appDataBase: AppDataBase): LogoutDao = appDataBase.logoutDao()

    @Single
    fun provideUserDao(appDataBase: AppDataBase): UserDao = appDataBase.userDao()

    @Single
    fun provideMainRulesDao(appDataBase: AppDataBase): MainRulesDao = appDataBase.mainRulesDao()

    @Single
    fun provideChatSessionDao(appDataBase: AppDataBase): ChatSessionDao =
        appDataBase.chatSessionDao()

    @Single
    fun provideMessageSlotDao(appDataBase: AppDataBase): MessageSlotDao =
        appDataBase.messageSlotDao()

    @Single
    fun provideChatVariantDao(appDataBase: AppDataBase): ChatVariantDao =
        appDataBase.chatVariantDao()

    @Single
    fun provideUpdateCharacterDao(appDataBase: AppDataBase): UpdateCharacterDao =
        appDataBase.updateCharacterDao()

    @Single
    fun provideMessageQueueRestorerDao(appDataBase: AppDataBase): MessageQueueRestorerDao =
        appDataBase.messageQueueRestorerDao()

    @Single
    fun provideGiftDao(appDataBase: AppDataBase): GiftDao = appDataBase.giftDao()
}
