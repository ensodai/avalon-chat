package com.ensodai.avalon.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import androidx.room3.Update
import com.ensodai.avalon.data.db.dto.ChatVariant
import com.ensodai.avalon.data.db.models.chat.ChatVariantEntity
import com.ensodai.avalon.data.db.models.chat.ChatVariantImageEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
abstract class ChatVariantDao {

    @Query("UPDATE chat_variant_entity SET name = :name, updated_at = :updatedAt WHERE id = :id")
    abstract suspend fun updateVariantName(id: Uuid, name: String, updatedAt: Instant)

    @Transaction
    open suspend fun updateVariantImageTransaction(
        variantId: Uuid,
        newImage: ChatVariantImageEntity?
    ) {
        // Сначала удаляем старую картинку, привязанную к этому варианту
        deleteImageByVariantId(variantId)

        // Если новая картинка пришла (не null), сохраняем её
        if (newImage != null) {
            insertImage(newImage)
        }
        // Если newImage == null, то картинка просто удалится и таблица останется пустой для этого варианта
    }


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_upsertChatVariant(chatVariant: ChatVariantEntity)

    @Update
    protected abstract suspend fun _update_upsertChatVariant(chatVariant: ChatVariantEntity)

    @Transaction
    open suspend fun upsertChatVariant(chatVariant: ChatVariantEntity) {
        _insert_upsertChatVariant(chatVariant)
        _update_upsertChatVariant(chatVariant)
    }// 2. Обновление активного варианта в сессии

    @Query("UPDATE chat_session_entity SET active_chat_variant_id = :variantId, updated_at = :updatedAt WHERE id = :sessionId")
    abstract suspend fun updateActiveSessionVariant(
        sessionId: Uuid,
        variantId: Uuid,
        updatedAt: Instant
    )

    // 3. Транзакционный метод, который объединяет оба действия
    @Transaction
    open suspend fun createAndActivateVariant(chatVariant: ChatVariantEntity) {
        upsertChatVariant(chatVariant)
        updateActiveSessionVariant(
            sessionId = chatVariant.chatSessionId,
            variantId = chatVariant.id,
            updatedAt = chatVariant.updatedAt
        )
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_upsertVariant(chatVariant: ChatVariantEntity)

    @Update
    protected abstract suspend fun _update_upsertVariant(chatVariant: ChatVariantEntity)

    @Transaction
    open suspend fun upsertVariant(chatVariant: ChatVariantEntity) {
        _insert_upsertVariant(chatVariant)
        _update_upsertVariant(chatVariant)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertImage(chatVariantImage: ChatVariantImageEntity)

    @Update
    protected abstract suspend fun _update_insertImage(chatVariantImage: ChatVariantImageEntity)

    @Transaction
    open suspend fun insertImage(chatVariantImage: ChatVariantImageEntity) {
        _insert_insertImage(chatVariantImage)
        _update_insertImage(chatVariantImage)
    }

    @Query("DELETE FROM chat_variant_image WHERE chat_variant_id = :variantId")
    abstract suspend fun deleteImageByVariantId(variantId: Uuid)

    @Transaction
    open suspend fun upsertChatVariantState(
        variant: ChatVariantEntity,
        image: ChatVariantImageEntity?
    ) {
        upsertVariant(variant)

        deleteImageByVariantId(variant.id)

        if (image != null) {
            insertImage(image)
        }
    }

    @Query("DELETE FROM chat_variant_entity WHERE id IN (:ids)")
    abstract suspend fun deleteVariants(ids: List<Uuid>)

    @Query("SELECT EXISTS(SELECT 1 FROM chat_variant_entity WHERE id = :id)")
    abstract suspend fun exists(id: Uuid): Boolean

    @Query(
        """
        SELECT 
            cv.id,
            
            -- ЛОГИКА ИМЕНИ: 
            CASE 
                WHEN cv.name IS NOT NULL AND cv.name != '' THEN cv.name 
                ELSE char.name 
            END AS name,
            
            NULL AS messageText,

            -- ВСТРАИВАЕМЫЙ ОБЪЕКТ (Картинка Варианта):
            cvi.id AS image_id,
            cvi.image_url AS image_url,
            cvi.thumb_hash AS thumb_hash,

            -- ВСТРАИВАЕМЫЙ ОБЪЕКТ (Аватар Персонажа):
            ca.id AS default_image_id,
            ca.image_url AS default_image_url,
            ca.thumb_hash AS default_thumb_hash

        FROM chat_variant_entity AS cv
        
        -- 1. Джойним сессию (нужна для получения character_id)
        INNER JOIN chat_session_entity AS cs ON cv.chat_session_id = cs.id
        
        -- 2. Джойним персонажа (нужен для имени и дефолтной аватарки)
        INNER JOIN character_entity AS char ON cs.character_id = char.id
        
        -- 3. Левый джойн для аватарки персонажа
        LEFT JOIN character_avatar_entity AS ca ON char.id = ca.character_id
        
        -- 4. Левый джойн для картинки варианта
        LEFT JOIN chat_variant_image AS cvi ON cv.id = cvi.chat_variant_id
        
        WHERE cv.chat_session_id = :sessionId
        
        -- СОРТИРОВКА: Просто по дате создания, новые сверху
        ORDER BY cv.created_at DESC
    """
    )
    abstract fun getChatVariants(sessionId: Uuid?): Flow<List<ChatVariant>>

    @Query("UPDATE chat_variant_entity SET unread_count = unread_count + :amount WHERE id = :id")
    open abstract suspend fun incrementUnreadCount(id: Uuid, amount: Int = 1)

    @Query("UPDATE chat_variant_entity SET unread_count = 0 WHERE id = :id")
    abstract suspend fun clearUnreadCount(id: Uuid)

    @Query("SELECT EXISTS(SELECT 1 FROM chat_variant_entity WHERE id = :variantId)")
    abstract suspend fun isVariantExists(variantId: Uuid): Boolean
}
