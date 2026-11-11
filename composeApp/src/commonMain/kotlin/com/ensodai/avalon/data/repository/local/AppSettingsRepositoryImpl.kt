package com.ensodai.avalon.data.repository.local

import com.ensodai.avalon.data.repository.local.base.BaseRepository
import com.ensodai.avalon.data.repository.local.base.DatabaseExecutor

import com.ensodai.avalon.data.db.LocaleHelper
import com.ensodai.avalon.data.db.dao.AppSettingsDao
import com.ensodai.avalon.data.db.models.AppSettingsEntity
import com.ensodai.avalon.data.db.models.ChatBackgroundImageEntity
import com.ensodai.avalon.data.db.models.ShowPromoFlagsEntity
import com.ensodai.avalon.domain.models.ChatBackgroundImage
import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import com.ensodai.avalon.locale.AppLocaleManager
import com.ensodai.avalon.theme.AvalonColors
import com.ensodai.avalon.theme.AvalonThemePresets
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleFill
import com.ensodai.avalon.theme.models.BubbleFillType
import com.ensodai.avalon.theme.models.BubbleShapeType
import com.ensodai.avalon.theme.models.ChatBackgroundConfig
import com.ensodai.avalon.theme.models.ChatBackgroundType
import com.ensodai.avalon.theme.models.ChatPatternPreset
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import com.ensodai.avalon.theme.toColor
import com.ensodai.avalon.theme.contrastText
import com.ensodai.avalon.utils.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppSettingsRepositoryImpl(
    dbExecutor: DatabaseExecutor,
    private val appSettingsDao: AppSettingsDao,
    private val logger: AppLogger
) : BaseRepository(dbExecutor), AppSettingsRepository {

    override fun getAvalonColorsFlow(): Flow<AvalonColors> {
        return dbObserve { appSettingsDao.getSettingsFlow() }.map(::resolveAvalonColors)
    }

    override suspend fun updateChatBubbleColors(userColorHex: String) {
        dbWrite { appSettingsDao.updateChatBubbleColors(userColorHex) }
    }

    override suspend fun updateChatBackground(url: String?, blur: Int) {
        dbWrite {
            appSettingsDao.updateChatBackground(
                url = url,
                blur = blur,
                chatBackgroundType = if (url == null) ChatBackgroundType.Default else ChatBackgroundType.Image
            )
        }
    }

    override fun getChatBackgroundImagesFlow(): Flow<List<ChatBackgroundImage>> {
        return dbObserve { appSettingsDao.getChatBackgroundImagesFlow() }.map { entities ->
            entities.map { entity ->
                ChatBackgroundImage(
                    id = entity.id,
                    url = entity.url
                )
            }
        }
    }

    override suspend fun addChatBackgroundImage(id: kotlin.uuid.Uuid, url: String) {
        dbWrite {
            appSettingsDao.insertChatBackgroundImage(
                ChatBackgroundImageEntity(
                    id = id,
                    url = url
                )
            )
        }
    }

    override suspend fun removeChatBackgroundImage(id: kotlin.uuid.Uuid) {
        dbWrite { appSettingsDao.deleteChatBackgroundImage(id) }
    }

    override suspend fun resetChatBackground() {
        dbWrite { appSettingsDao.resetChatBackground() }
    }

    override suspend fun getSafetyReminderAccepted(): Boolean {
        return dbRead { appSettingsDao.getSafetyReminderAccepted() }
    }

    override suspend fun acceptSafetyReminder() {
        dbWrite { appSettingsDao.acceptSafetyReminder() }
    }

    override suspend fun getLanguageSettings(): Flow<String?> {
        return dbObserve { appSettingsDao.getLanguageFlow() }.map { language ->
            LocaleHelper.getLocaleNameByCode(language)
        }
    }

    override suspend fun getSavedLanguage(): String? {
        return dbRead { appSettingsDao.getLanguage() }?.let { LocaleHelper.getLocaleCodeByName(it) }
    }

    override suspend fun getAutoReplaceSetting(): Boolean {
        return dbRead { appSettingsDao.getAutoReplaceAPIKeys() }
    }

    override suspend fun getAutoReplaceSettingFlow(): Flow<Boolean> {
        return dbObserve { appSettingsDao.getAutoReplaceAPIKeysFlow() }.map { it ?: false }
    }

    override suspend fun changeAppLanguage(language: String) {
        val languageCode = LocaleHelper.getLocaleCodeByName(language)
        AppLocaleManager.applyLanguage(languageCode)

        dbWrite {
            val allRows = appSettingsDao.getAllSettings()
            val correctRow = allRows.find { it.id == 1 }

            if (correctRow != null && allRows.size == 1) {
                appSettingsDao.updateLanguage(languageCode)
                return@dbWrite
            }

            val baseSettings = correctRow
                ?: allRows.firstOrNull()
                ?: AppSettingsEntity(id = 1)

            val fixedSettings = baseSettings.copy(
                id = 1,
                language = languageCode
            )

            appSettingsDao.deleteAll()
            appSettingsDao.insert(fixedSettings)

            logger.i(
                "AppSettingsRepositoryImpl",
                "AppSettings table was healed. Old rows count: ${allRows.size}. New state: ID=1, Lang=$languageCode"
            )
        }
    }

    override suspend fun changeAutoReplaceSetting(isEnable: Boolean) {
        dbWrite {
            val allRows = appSettingsDao.getAllSettings()
            val correctRow = allRows.find { it.id == 1 }

            if (correctRow != null && allRows.size == 1) {
                appSettingsDao.insert(correctRow.copy(autoReplaceAPIKeys = isEnable))
                return@dbWrite
            }

            val baseSettings = correctRow
                ?: allRows.firstOrNull()
                ?: AppSettingsEntity(id = 1)

            val fixedSettings = baseSettings.copy(
                id = 1,
                autoReplaceAPIKeys = isEnable
            )

            appSettingsDao.deleteAll()
            appSettingsDao.insert(fixedSettings)
        }
    }

    override fun getFlowMode(): Flow<Boolean> {
        return dbObserve { appSettingsDao.getFlowMode() }.map { it ?: false }
    }

    override suspend fun getCurrentFlowMode(): Boolean {
        return dbRead { appSettingsDao.getCurrentFlowMode() }
    }

    override suspend fun toggleFlowMode() {
        dbWrite { appSettingsDao.toggleFlowMode() }
    }

    override suspend fun onChangeLocalMode() {
        dbWrite { appSettingsDao.onChangeLocalMode() }
    }

    override fun getLocalModeFlow(): Flow<Boolean> {
        return dbObserve { appSettingsDao.getLocalModeFlow() }.map { it ?: false }
    }

    override suspend fun getLocalMode(): Boolean {
        return dbRead { appSettingsDao.getLocalMode() }
    }

    override fun getShowPromoFlagsFlow(): Flow<ShowPromoFlagsEntity?> {
        return dbObserve { appSettingsDao.getShowPromoFlagsFlow() }
    }

    override suspend fun getShowPromoFlags(): ShowPromoFlagsEntity? {
        return dbRead { appSettingsDao.getShowPromoFlags() }
    }

    override suspend fun updateShowPromoFlags(flags: ShowPromoFlagsEntity) {
        dbWrite {
            val existing = appSettingsDao.getShowPromoFlags()
            if (existing != null) {
                appSettingsDao.updateShowPromoFlags(flags.copy(id = existing.id))
            } else {
                appSettingsDao.insertShowPromoFlags(flags.copy(id = 1))
            }
        }
    }

    override suspend fun updateThemePreset(preset: AvalonThemePreset) {
        dbWrite {
            val allRows = appSettingsDao.getAllSettings()
            val correctRow = allRows.find { it.id == 1 }

            if (correctRow != null && allRows.size == 1) {
                appSettingsDao.updateThemePreset(preset)
                appSettingsDao.resetBubbleColorOverride()
                return@dbWrite
            }

            val baseSettings = correctRow
                ?: allRows.firstOrNull()
                ?: AppSettingsEntity(id = 1)

            val fixedSettings = baseSettings.copy(
                id = 1,
                themePreset = preset,
                userBubbleFillType = BubbleFillType.Solid,
                userBubbleSolidColor = null,
                userBubbleGradientStartColor = null,
                userBubbleGradientEndColor = null,
                botBubbleFillType = BubbleFillType.Solid,
                botBubbleSolidColor = null,
                botBubbleGradientStartColor = null,
                botBubbleGradientEndColor = null
            )

            appSettingsDao.deleteAll()
            appSettingsDao.insert(fixedSettings)

            logger.i(
                "AppSettingsRepositoryImpl",
                "AppSettings table was healed. Old rows count: ${allRows.size}. New state: ID=1, Theme=${preset.storageKey}"
            )
        }
    }

    override suspend fun updateBubbleStyle(
        fontSize: Int,
        shapeType: BubbleShapeType,
        cornerRadius: Int
    ) {
        dbWrite {
            appSettingsDao.updateBubbleStyle(
                fontSize = fontSize,
                shapeType = shapeType,
                cornerRadius = cornerRadius
            )
        }
    }

    override suspend fun resetBubbleColorOverride() {
        dbWrite { appSettingsDao.resetBubbleColorOverride() }
    }

    override fun getShowAiWarningFlow(): Flow<Boolean> {
        return dbObserve { appSettingsDao.getSettingsFlow() }.map { it?.showAiWarning ?: true }
    }

    override suspend fun getShowAiWarning(): Boolean {
        return dbRead { appSettingsDao.getSettings() }?.showAiWarning ?: true
    }

    override suspend fun changeShowAiWarning(isShow: Boolean) {
        dbWrite {
            val allRows = appSettingsDao.getAllSettings()
            val correctRow = allRows.find { it.id == 1 }

            if (correctRow != null && allRows.size == 1) {
                appSettingsDao.insert(correctRow.copy(showAiWarning = isShow))
                return@dbWrite
            }

            val baseSettings = correctRow
                ?: allRows.firstOrNull()
                ?: AppSettingsEntity(id = 1)

            val fixedSettings = baseSettings.copy(
                id = 1,
                showAiWarning = isShow
            )

            appSettingsDao.deleteAll()
            appSettingsDao.insert(fixedSettings)
        }
    }

    override fun getChatRoleplayPresetFlow(): Flow<ChatRoleplayPreset> {
        return dbObserve { appSettingsDao.getChatRoleplayPresetFlow() }.map { it ?: ChatRoleplayPreset.STANDARD_BUBBLE }
    }

    override suspend fun updateChatRoleplayPreset(preset: ChatRoleplayPreset) {
        dbWrite { appSettingsDao.updateChatRoleplayPreset(preset) }
    }

    private fun resolveAvalonColors(settings: AppSettingsEntity?): AvalonColors {
        if (settings == null) return AvalonThemePresets.fallback

        val baseColors = AvalonThemePresets.resolve(settings.themePreset)

        val resolvedUserBubble = resolveBubbleFill(
            fillType = settings.userBubbleFillType,
            solidColor = settings.userBubbleSolidColor,
            gradientStartColor = settings.userBubbleGradientStartColor,
            gradientEndColor = settings.userBubbleGradientEndColor,
            fallback = baseColors.userBubble
        )
        // Если юзер кастомизировал bubble fill — выбираем текст по худшему случаю заливки.
        val hasUserOverride = settings.userBubbleSolidColor != null
                || settings.userBubbleGradientStartColor != null

        val userTextColor = if (hasUserOverride) {
            resolvedUserBubble.contrastText()
        } else baseColors.userBubbleTextColor

        return baseColors.copy(
            userBubble = resolvedUserBubble,
            botBubble = baseColors.botBubble,
            userBubbleTextColor = userTextColor,
            botBubbleTextColor = baseColors.botBubbleTextColor,
            chatBackground = resolveChatBackground(
                settings = settings,
                fallback = baseColors.chatBackground,
                fallbackBaseColor = baseColors.background,
                fallbackAccentColor = baseColors.neonAccent
            ),
            bubbleFontSize = settings.bubbleFontSize.coerceIn(12, 30),
            bubbleShapeType = settings.bubbleShapeType,
            bubbleCornerRadius = settings.bubbleCornerRadius.coerceIn(4, 16)
        )
    }

    private fun resolveBubbleFill(
        fillType: BubbleFillType,
        solidColor: String?,
        gradientStartColor: String?,
        gradientEndColor: String?,
        fallback: BubbleFill
    ): BubbleFill {
        return when (fillType) {
            BubbleFillType.Solid -> {
                solidColor?.toColor()?.let(BubbleFill::Solid) ?: fallback
            }

            BubbleFillType.Gradient -> {
                val startColor = gradientStartColor?.toColor()
                val endColor = gradientEndColor?.toColor()
                if (startColor != null && endColor != null) {
                    BubbleFill.Gradient(startColor = startColor, endColor = endColor)
                } else {
                    fallback
                }
            }
        }
    }

    private fun resolveChatBackground(
        settings: AppSettingsEntity,
        fallback: ChatBackgroundConfig,
        fallbackBaseColor: androidx.compose.ui.graphics.Color,
        fallbackAccentColor: androidx.compose.ui.graphics.Color
    ): ChatBackgroundConfig {
        return when (settings.chatBackgroundType) {
            ChatBackgroundType.Default -> fallback
            ChatBackgroundType.Solid -> {
                settings.chatBackgroundColor?.toColor()?.let(ChatBackgroundConfig::Solid) ?: fallback
            }

            ChatBackgroundType.Image -> {
                settings.chatBackgroundUrl?.let { url ->
                    ChatBackgroundConfig.Image(
                        url = url,
                        blurRadius = settings.chatBackgroundBlur,
                        dimAmount = settings.chatBackgroundDimAmount
                    )
                } ?: fallback
            }

            ChatBackgroundType.Pattern -> {
                ChatBackgroundConfig.Pattern(
                    preset = settings.chatBackgroundPatternPreset ?: ChatPatternPreset.Aurora,
                    baseColor = settings.chatBackgroundColor?.toColor() ?: fallbackBaseColor,
                    tintColor = settings.userBubbleGradientStartColor?.toColor()
                        ?: settings.userBubbleSolidColor?.toColor()
                        ?: settings.botBubbleGradientStartColor?.toColor()
                        ?: settings.botBubbleSolidColor?.toColor()
                        ?: fallbackAccentColor,
                    intensity = settings.chatBackgroundDimAmount
                        .coerceIn(0f, 1f)
                        .takeIf { it > 0f }
                        ?: 0.18f
                )
            }
        }
    }
}
