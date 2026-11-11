package com.ensodai.avalon.domain.repository.local

import com.ensodai.avalon.data.db.models.ShowPromoFlagsEntity
import com.ensodai.avalon.domain.models.ChatBackgroundImage
import com.ensodai.avalon.theme.AvalonColors
import com.ensodai.avalon.theme.models.BubbleShapeType
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface AppSettingsRepository {

    fun getAvalonColorsFlow(): Flow<AvalonColors>

    suspend fun updateChatBubbleColors(userColorHex: String)

    suspend fun updateChatBackground(url: String?, blur: Int)

    fun getChatBackgroundImagesFlow(): Flow<List<ChatBackgroundImage>>

    suspend fun addChatBackgroundImage(id: Uuid, url: String)

    suspend fun removeChatBackgroundImage(id: Uuid)

    suspend fun resetChatBackground()

    suspend fun getSafetyReminderAccepted(): Boolean

    suspend fun acceptSafetyReminder()

    suspend fun getLanguageSettings(): Flow<String?>

    suspend fun getSavedLanguage(): String?

    suspend fun getAutoReplaceSetting(): Boolean

    suspend fun getAutoReplaceSettingFlow(): Flow<Boolean>

    suspend fun changeAppLanguage(language: String)

    suspend fun changeAutoReplaceSetting(isEnable: Boolean)

    fun getFlowMode(): Flow<Boolean>

    suspend fun getCurrentFlowMode(): Boolean

    suspend fun toggleFlowMode()

    suspend fun onChangeLocalMode()

    fun getLocalModeFlow(): Flow<Boolean>

    suspend fun getLocalMode(): Boolean

    fun getShowPromoFlagsFlow(): Flow<ShowPromoFlagsEntity?>

    suspend fun getShowPromoFlags(): ShowPromoFlagsEntity?

    suspend fun updateShowPromoFlags(flags: ShowPromoFlagsEntity)

    suspend fun updateThemePreset(preset: com.ensodai.avalon.theme.models.AvalonThemePreset)

    suspend fun updateBubbleStyle(
        fontSize: Int,
        shapeType: BubbleShapeType,
        cornerRadius: Int
    )

    suspend fun resetBubbleColorOverride()

    fun getShowAiWarningFlow(): Flow<Boolean>

    suspend fun getShowAiWarning(): Boolean

    suspend fun changeShowAiWarning(isShow: Boolean)

    fun getChatRoleplayPresetFlow(): Flow<ChatRoleplayPreset>

    suspend fun updateChatRoleplayPreset(preset: ChatRoleplayPreset)
}
