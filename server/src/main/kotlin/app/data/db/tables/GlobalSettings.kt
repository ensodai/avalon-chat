package app.data.db.tables

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

object GlobalSettings : IntIdTable("global_settings") {
    val maxQuotaMessagesPerHour = integer("max_quota_messages_per_hour").default(40)
    val mainCharacterRules = text("main_character_rules").default("")
    val defaultGeminiModel = text("default_gemini_model").default("")
    val maxOutputTokens = integer("max_output_tokens").default(2000)
    val isEnablePaymentService = bool("is_enable_payment_service").default(false)
    val vertexLocation = varchar("vertex_location", 64).default("europe-west4")
    val gemini3VertexLocation = varchar("gemini_3_vertex_location", 64).default("us-central1")
    val audioVertexLocation = varchar("audio_vertex_location", 64).default("europe-west4")
}

class GlobalSetting(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GlobalSetting>(GlobalSettings)

    var maxQuotaMessagesPerHour: Int by GlobalSettings.maxQuotaMessagesPerHour
    var mainCharacterRules: String by GlobalSettings.mainCharacterRules
    var defaultGeminiModel: String by GlobalSettings.defaultGeminiModel
    var maxOutputTokens: Int by GlobalSettings.maxOutputTokens
}