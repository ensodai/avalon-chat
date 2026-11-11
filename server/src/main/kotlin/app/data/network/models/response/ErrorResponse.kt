package app.data.network.models.response

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val message: String)