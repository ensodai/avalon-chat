package app.data.network.models.request

import kotlinx.serialization.Serializable

@Serializable
data class DeleteCharactersRequest(val characterPublicIds: List<String>)
