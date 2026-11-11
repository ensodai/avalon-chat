package app.data.network.models.request

import kotlinx.serialization.Serializable

@Serializable
data class UnarchiveChatRequest(val sessionIds: List<String>)
