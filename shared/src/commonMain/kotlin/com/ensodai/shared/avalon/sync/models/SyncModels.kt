@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package com.ensodai.shared.avalon.sync.models

import com.ensodai.shared.avalon.auth.models.AppUserResponse
import com.ensodai.shared.avalon.persona.models.PersonaResponse
import com.ensodai.shared.avalon.character.models.CharacterResponse
import com.ensodai.shared.avalon.chat.models.ChatSessionPreviewResponse
import com.ensodai.shared.avalon.chat.models.ChatVariantResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class LlmConfigResponse(
    val codeName: String,
    val displayName: String,
    val price: Int
)

@Serializable
data class SyncResponse(
    val updatedAppUser: AppUserResponse?,
    val updatedUserPersonas: List<PersonaResponse>,
    val deletedUserPersonaIds: List<@Contextual Uuid>,
    val updatedCharacters: List<CharacterResponse>,
    val deletedCharacterIds: List<@Contextual Uuid>,
    val updatedChatSessions: List<ChatSessionPreviewResponse>,
    val deletedChatSessionIds: List<@Contextual Uuid>,
    val updatedChatVariants: List<ChatVariantResponse>,
    val deletedChatVariantIds: List<@Contextual Uuid>,
    @Contextual
    val latestTimestamp: Instant,
    val deletedSlots: List<@Contextual Uuid>,
    val availableLlm: List<LlmConfigResponse>
)

@Serializable
data class SyncChangesRequest(
    @Contextual
    val since: Instant? = null
)
