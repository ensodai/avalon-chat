package app.data.db.tables.users

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import com.ensodai.shared.avalon.user.models.AiProviderType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

object UserAiSettings : BaseUuidTable("user_ai_settings") {
    val userId = reference("user_id", AppUsers, onDelete = ReferenceOption.CASCADE).uniqueIndex("idx_user_ai_settings_user_id")
    val providerId = enumerationByName<AiProviderType>("provider_id", 32).default(AiProviderType.GOOGLE)
    val baseUrl = varchar("base_url", 500).default("https://generativelanguage.googleapis.com")
    val apiKey = text("api_key").nullable()
    val textModel = varchar("text_model", 150).default("gemini-3.1-flash-lite")
    val ttsModel = varchar("tts_model", 150).default("gemini-2.5-flash-preview-tts")
    val imageModel = varchar("image_model", 150).default("gemini-2.5-flash-image")
    val imagePromptModel = varchar("image_prompt_model", 150).default("gemini-3.1-flash-lite")
    val summaryModel = varchar("summary_model", 150).default("gemini-3.1-flash-lite")
    val isCustomProviderEnabled = bool("is_custom_provider_enabled").default(false)
}

class UserAiSetting(id: EntityID<Uuid>) : BaseUuidEntity(id, UserAiSettings) {
    companion object : BaseUuidEntityClass<UserAiSetting>(UserAiSettings)

    var userId by UserAiSettings.userId
    var providerId by UserAiSettings.providerId
    var baseUrl by UserAiSettings.baseUrl
    var apiKey by UserAiSettings.apiKey
    var textModel by UserAiSettings.textModel
    var ttsModel by UserAiSettings.ttsModel
    var imageModel by UserAiSettings.imageModel
    var imagePromptModel by UserAiSettings.imagePromptModel
    var summaryModel by UserAiSettings.summaryModel
    var isCustomProviderEnabled by UserAiSettings.isCustomProviderEnabled
}

@Serializable
data class UserAiSettingsDto(
    val userId: Uuid,
    val providerId: AiProviderType = AiProviderType.GOOGLE,
    val baseUrl: String = "https://generativelanguage.googleapis.com",
    val apiKey: String?,
    val textModel: String = "gemini-3.1-flash-lite",
    val ttsModel: String = "gemini-2.5-flash-preview-tts",
    val imageModel: String = "gemini-2.5-flash-image",
    val imagePromptModel: String = "gemini-3.1-flash-lite",
    val summaryModel: String = "gemini-3.1-flash-lite",
    val isCustomProviderEnabled: Boolean = false
)
