package com.ensodai.avalon.data.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction

@Dao
interface LogoutDao {
    @Query("DELETE FROM character_entity")
    abstract suspend fun deleteAllCharacters()

    @Query("DELETE FROM character_avatar_entity")
    abstract suspend fun deleteAllAvatars()

//    @Query("DELETE FROM character_config_entity")
//    abstract suspend fun deleteAllConfigs()

    @Query("DELETE FROM character_image_entity")
    abstract suspend fun deleteAllImages()

    @Query("DELETE FROM chat_session_entity")
    abstract suspend fun deleteAllSessions()

//    abstract suspend fun deleteAllMessages()
//
//    abstract suspend fun deleteAllMessageActions()


    @Query("DELETE FROM user")
    abstract suspend fun deleteAllUser()

    @Query("DELETE FROM persona")
    abstract suspend fun deleteAllPersona()

    @Query("DELETE FROM chat_variant_entity")
    abstract suspend fun deleteAllChatVariants()


    @Query("DELETE FROM chat_variant_image")
    abstract suspend fun deleteAllChatVariantImages()

    @Transaction
    open suspend fun deleteAll() {
//        deleteAllMessages()
//        deleteAllMessageActions()
//        deleteAllMessageImages()
        deleteAllCharacters()
        deleteAllAvatars()
//        deleteAllConfigs()
        deleteAllImages()
        deleteAllSessions()
        deleteAllUser()
        deleteAllPersona()
        deleteAllChatVariants()
//        deleteAllMessageSlots()
        deleteAllChatVariantImages()
    }
}
