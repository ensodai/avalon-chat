package app.data.repository

import app.data.db.tables.users.UserAiSetting
import app.data.db.tables.users.UserAiSettings
import app.data.db.tables.users.UserAiSettingsDto
import app.utils.dbQuery
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.upsert
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UserAiSettingsRepository {

    suspend fun getSettingsByUserId(userId: Uuid): UserAiSettingsDto? = dbQuery("getSettingsByUserId") {
        UserAiSettings.select(
            UserAiSettings.userId,
            UserAiSettings.providerId,
            UserAiSettings.baseUrl,
            UserAiSettings.apiKey,
            UserAiSettings.textModel,
            UserAiSettings.ttsModel,
            UserAiSettings.imageModel,
            UserAiSettings.imagePromptModel,
            UserAiSettings.summaryModel,
            UserAiSettings.isCustomProviderEnabled
        )
            .where { UserAiSettings.userId eq userId }
            .singleOrNull()
            ?.let { row ->
                UserAiSettingsDto(
                    userId = row[UserAiSettings.userId].value,
                    providerId = row[UserAiSettings.providerId],
                    baseUrl = row[UserAiSettings.baseUrl],
                    apiKey = row[UserAiSettings.apiKey],
                    textModel = row[UserAiSettings.textModel],
                    ttsModel = row[UserAiSettings.ttsModel],
                    imageModel = row[UserAiSettings.imageModel],
                    imagePromptModel = row[UserAiSettings.imagePromptModel],
                    summaryModel = row[UserAiSettings.summaryModel],
                    isCustomProviderEnabled = row[UserAiSettings.isCustomProviderEnabled]
                )
            }
    }

    suspend fun getOrCreateSettings(userId: Uuid): UserAiSettingsDto = dbQuery("getOrCreateSettings") {
        val existing = getSettingsByUserIdInternal(userId)
        if (existing != null) return@dbQuery existing

        val newEntity = UserAiSetting.new {
            this.userId = EntityID(userId, app.data.db.tables.users.AppUsers)
            this.providerId = com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE
            this.baseUrl = "https://generativelanguage.googleapis.com"
            this.apiKey = null
            this.textModel = "gemini-2.5-flash"
            this.ttsModel = "gemini-2.5-flash-preview-tts"
            this.imageModel = "gemini-2.5-flash-image"
            this.imagePromptModel = "gemini-2.5-flash"
            this.summaryModel = "gemini-2.5-flash-lite"
            this.isCustomProviderEnabled = false
        }

        UserAiSettingsDto(
            userId = newEntity.userId.value,
            providerId = newEntity.providerId,
            baseUrl = newEntity.baseUrl,
            apiKey = newEntity.apiKey,
            textModel = newEntity.textModel,
            ttsModel = newEntity.ttsModel,
            imageModel = newEntity.imageModel,
            imagePromptModel = newEntity.imagePromptModel,
            summaryModel = newEntity.summaryModel,
            isCustomProviderEnabled = newEntity.isCustomProviderEnabled
        )
    }

    suspend fun saveOrUpdateSettings(
        userId: Uuid,
        providerId: com.ensodai.shared.avalon.user.models.AiProviderType = com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE,
        baseUrl: String = "https://generativelanguage.googleapis.com",
        apiKey: String? = null,
        textModel: String = "gemini-2.5-flash",
        ttsModel: String = "gemini-2.5-flash-preview-tts",
        imageModel: String = "gemini-2.5-flash-image",
        imagePromptModel: String = "gemini-2.5-flash",
        summaryModel: String = "gemini-2.5-flash-lite",
        isCustomProviderEnabled: Boolean = false
    ): UserAiSettingsDto = dbQuery("saveOrUpdateSettings") {
        UserAiSettings.upsert(UserAiSettings.userId) {
            it[UserAiSettings.userId] = EntityID(userId, app.data.db.tables.users.AppUsers)
            it[UserAiSettings.providerId] = providerId
            it[UserAiSettings.baseUrl] = baseUrl.trim()
            it[UserAiSettings.apiKey] = apiKey?.trim()
            it[UserAiSettings.textModel] = textModel.trim()
            it[UserAiSettings.ttsModel] = ttsModel.trim()
            it[UserAiSettings.imageModel] = imageModel.trim()
            it[UserAiSettings.imagePromptModel] = imagePromptModel.trim()
            it[UserAiSettings.summaryModel] = summaryModel.trim()
            it[UserAiSettings.isCustomProviderEnabled] = isCustomProviderEnabled
        }

        UserAiSettingsDto(
            userId = userId,
            providerId = providerId,
            baseUrl = baseUrl.trim(),
            apiKey = apiKey?.trim(),
            textModel = textModel.trim(),
            ttsModel = ttsModel.trim(),
            imageModel = imageModel.trim(),
            imagePromptModel = imagePromptModel.trim(),
            summaryModel = summaryModel.trim(),
            isCustomProviderEnabled = isCustomProviderEnabled
        )
    }

    private fun getSettingsByUserIdInternal(userId: Uuid): UserAiSettingsDto? {
        return UserAiSettings.select(
            UserAiSettings.userId,
            UserAiSettings.providerId,
            UserAiSettings.baseUrl,
            UserAiSettings.apiKey,
            UserAiSettings.textModel,
            UserAiSettings.ttsModel,
            UserAiSettings.imageModel,
            UserAiSettings.imagePromptModel,
            UserAiSettings.summaryModel,
            UserAiSettings.isCustomProviderEnabled
        )
            .where { UserAiSettings.userId eq userId }
            .singleOrNull()
            ?.let { row ->
                UserAiSettingsDto(
                    userId = row[UserAiSettings.userId].value,
                    providerId = row[UserAiSettings.providerId],
                    baseUrl = row[UserAiSettings.baseUrl],
                    apiKey = row[UserAiSettings.apiKey],
                    textModel = row[UserAiSettings.textModel],
                    ttsModel = row[UserAiSettings.ttsModel],
                    imageModel = row[UserAiSettings.imageModel],
                    imagePromptModel = row[UserAiSettings.imagePromptModel],
                    summaryModel = row[UserAiSettings.summaryModel],
                    isCustomProviderEnabled = row[UserAiSettings.isCustomProviderEnabled]
                )
            }
    }
}
