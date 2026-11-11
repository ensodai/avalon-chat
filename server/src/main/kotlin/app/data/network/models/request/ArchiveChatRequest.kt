package app.data.network.models.request

import kotlinx.serialization.Serializable

@Serializable
data class ArchiveChatRequest(val sessionIds: List<String>)
