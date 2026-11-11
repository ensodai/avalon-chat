package app.data.network.models.request

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class ReportCharacterRequest(
    val characterId: String,
    val message: String
)

@Serializable
data class ReportRequest(
    val characterId: @Contextual Uuid,
    val message: String
)