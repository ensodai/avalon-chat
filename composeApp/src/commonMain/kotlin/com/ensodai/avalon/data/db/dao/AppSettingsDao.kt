package com.ensodai.avalon.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.ensodai.avalon.data.db.models.AppSettingsEntity
import com.ensodai.avalon.data.db.models.ChatBackgroundImageEntity
import com.ensodai.avalon.data.db.models.ShowPromoFlagsEntity
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleFillType
import com.ensodai.avalon.theme.models.BubbleShapeType
import com.ensodai.avalon.theme.models.ChatBackgroundType
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
abstract class AppSettingsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertDefaultSettings(appSettingsEntity: AppSettingsEntity)

    @Update
    protected abstract suspend fun _update_insertDefaultSettings(appSettingsEntity: AppSettingsEntity)

    @Transaction
    open suspend fun insertDefaultSettings(appSettingsEntity: AppSettingsEntity) {
        _insert_insertDefaultSettings(appSettingsEntity)
        _update_insertDefaultSettings(appSettingsEntity)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertShowPromoFlags(showPromoFlagsEntity: ShowPromoFlagsEntity)

    @Update
    protected abstract suspend fun _update_insertShowPromoFlags(showPromoFlagsEntity: ShowPromoFlagsEntity)

    @Transaction
    open suspend fun insertShowPromoFlags(showPromoFlagsEntity: ShowPromoFlagsEntity) {
        _insert_insertShowPromoFlags(showPromoFlagsEntity)
        _update_insertShowPromoFlags(showPromoFlagsEntity)
    }

    @Query("SELECT * FROM show_promo_flags_entity LIMIT 1")
    abstract suspend fun getShowPromoFlags(): ShowPromoFlagsEntity?

    @Query("SELECT * FROM show_promo_flags_entity LIMIT 1")
    abstract fun getShowPromoFlagsFlow(): Flow<ShowPromoFlagsEntity?>

    @Update
    abstract suspend fun updateShowPromoFlags(flags: ShowPromoFlagsEntity)

    @Query("SELECT is_safety_reminder_accepted FROM app_settings WHERE id = :id LIMIT 1")
    abstract suspend fun getSafetyReminderAccepted(id: Int = 1): Boolean

    @Query("UPDATE app_settings SET is_safety_reminder_accepted = 1 WHERE id = :id")
    abstract suspend fun acceptSafetyReminder(id: Int = 1)

    @Query("SELECT auto_replace_api_keys FROM app_settings WHERE id = :id LIMIT 1")
    abstract suspend fun getAutoReplaceAPIKeys(id: Int = 1): Boolean

    @Query("SELECT auto_replace_api_keys FROM app_settings WHERE id = :id LIMIT 1")
    abstract fun getAutoReplaceAPIKeysFlow(id: Int = 1): Flow<Boolean?>

    @Query("SELECT language FROM app_settings WHERE id = :id LIMIT 1")
    abstract suspend fun getLanguage(id: Int = 1): String?

    @Query("SELECT language FROM app_settings WHERE id = :id LIMIT 1")
    abstract fun getLanguageFlow(id: Int = 1): Flow<String?>

    @Query("DELETE FROM app_settings")
    abstract suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insert(settings: AppSettingsEntity)

    @Update
    protected abstract suspend fun _update_insert(settings: AppSettingsEntity)

    @Transaction
    open suspend fun insert(settings: AppSettingsEntity) {
        _insert_insert(settings)
        _update_insert(settings)
    }

    @Query("SELECT * FROM app_settings")
    abstract suspend fun getAllSettings(): List<AppSettingsEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertOrUpdateSettings(settings: AppSettingsEntity)

    @Update
    protected abstract suspend fun _update_insertOrUpdateSettings(settings: AppSettingsEntity)

    @Transaction
    open suspend fun insertOrUpdateSettings(settings: AppSettingsEntity) {
        _insert_insertOrUpdateSettings(settings)
        _update_insertOrUpdateSettings(settings)
    }

    @Query("UPDATE app_settings SET language = :language WHERE id = 1")
    abstract suspend fun updateLanguage(language: String)

    @Query("SELECT * FROM app_settings WHERE id = 1")
    abstract suspend fun getSettings(): AppSettingsEntity?

    @Query("UPDATE app_settings SET auto_replace_api_keys = :isEnable WHERE id = :id")
    abstract suspend fun updateAutoReplaceSetting(id: Int = 1, isEnable: Boolean)

    @Query("SELECT flow_mode FROM app_settings LIMIT 1")
    abstract fun getFlowMode(): Flow<Boolean?>

    @Query("SELECT flow_mode FROM app_settings LIMIT 1")
    abstract suspend fun getCurrentFlowMode(): Boolean

    @Query("UPDATE app_settings SET flow_mode = NOT flow_mode")
    abstract suspend fun toggleFlowMode()

    @Query("UPDATE app_settings SET local_mode = NOT local_mode WHERE id = :id")
    abstract suspend fun onChangeLocalMode(id: Int = 1)

    @Query("SELECT local_mode FROM app_settings LIMIT 1")
    abstract fun getLocalModeFlow(): Flow<Boolean?>

    @Query("SELECT local_mode FROM app_settings LIMIT 1")
    abstract suspend fun getLocalMode(): Boolean

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    abstract fun getSettingsFlow(): Flow<AppSettingsEntity?>

    @Query(
        """
        UPDATE app_settings
        SET
            user_bubble_fill_type = :bubbleFillType,
            custom_user_bubble_color = :userColor,
            user_bubble_gradient_start_color = NULL,
            user_bubble_gradient_end_color = NULL
        WHERE id = 1
        """
    )
    abstract suspend fun updateChatBubbleColors(
        userColor: String,
        bubbleFillType: BubbleFillType = BubbleFillType.Solid
    )

    @Query(
        """
        UPDATE app_settings
        SET
            chat_background_type = :chatBackgroundType,
            chat_background_url = :url,
            chat_background_blur = :blur,
            chat_background_color = NULL,
            chat_background_pattern_id = NULL
        WHERE id = 1
        """
    )
    abstract suspend fun updateChatBackground(
        url: String?,
        blur: Int,
        chatBackgroundType: ChatBackgroundType
    )

    @Query(
        """
        UPDATE app_settings
        SET
            chat_background_type = 'default',
            chat_background_url = NULL,
            chat_background_blur = 0,
            chat_background_color = NULL,
            chat_background_pattern_id = NULL
        WHERE id = 1
        """
    )
    abstract suspend fun resetChatBackground()

    @Query("UPDATE app_settings SET theme_id = :themePreset WHERE id = 1")
    abstract suspend fun updateThemePreset(themePreset: AvalonThemePreset)

    @Query("SELECT * FROM chat_background_images")
    abstract fun getChatBackgroundImagesFlow(): Flow<List<ChatBackgroundImageEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertChatBackgroundImage(image: ChatBackgroundImageEntity)

    @Update
    protected abstract suspend fun _update_insertChatBackgroundImage(image: ChatBackgroundImageEntity)

    @Transaction
    open suspend fun insertChatBackgroundImage(image: ChatBackgroundImageEntity) {
        _insert_insertChatBackgroundImage(image)
        _update_insertChatBackgroundImage(image)
    }

    @Query("DELETE FROM chat_background_images WHERE id = :id")
    abstract suspend fun deleteChatBackgroundImage(id: Uuid)

    @Query(
        """
        UPDATE app_settings
        SET bubble_font_size = :fontSize,
            bubble_shape_type = :shapeType,
            bubble_corner_radius = :cornerRadius
        WHERE id = 1
        """
    )
    abstract suspend fun updateBubbleStyle(
        fontSize: Int,
        shapeType: BubbleShapeType,
        cornerRadius: Int
    )

    @Query(
        """
        UPDATE app_settings
        SET user_bubble_fill_type = 'solid',
            custom_user_bubble_color = NULL,
            user_bubble_gradient_start_color = NULL,
            user_bubble_gradient_end_color = NULL
        WHERE id = 1
        """
    )
    abstract suspend fun resetBubbleColorOverride()

    @Query("SELECT show_ai_warning FROM app_settings WHERE id = :id LIMIT 1")
    abstract fun getShowAiWarningFlow(id: Int = 1): Flow<Boolean>

    @Query("SELECT show_ai_warning FROM app_settings WHERE id = :id LIMIT 1")
    abstract suspend fun getShowAiWarning(id: Int = 1): Boolean

    @Query("UPDATE app_settings SET show_ai_warning = :isShow WHERE id = :id")
    abstract suspend fun updateShowAiWarning(id: Int = 1, isShow: Boolean)

    @Query("SELECT chat_roleplay_preset FROM app_settings WHERE id = :id LIMIT 1")
    abstract fun getChatRoleplayPresetFlow(id: Int = 1): Flow<ChatRoleplayPreset?>

    @Query("UPDATE app_settings SET chat_roleplay_preset = :preset WHERE id = :id")
    abstract suspend fun updateChatRoleplayPreset(preset: ChatRoleplayPreset, id: Int = 1)
}
