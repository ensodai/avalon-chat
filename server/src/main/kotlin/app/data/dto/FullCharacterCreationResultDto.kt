package app.data.dto

import app.data.db.tables.chatSessions.ChatSessionInternalDto
import app.data.db.tables.chatSessions.ChatVariantInternalDto
import app.data.db.tables.images.CharacterAvatarInternalDto
import app.data.db.tables.images.CharacterImageInternalDto
import app.data.db.tables.images.WelcomeCardImageInternalDto
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class CharacterCreationResult(
    val characterPublicId: Uuid,
    val savedAvatar: CharacterAvatarInternalDto?,
    val savedImages: List<CharacterImageInternalDto>,
    val savedWelcomeCardImages: List<WelcomeCardImageInternalDto>,
    val chatSession: ChatSessionInternalDto,
    val firstChatVariant: ChatVariantInternalDto,
    val updatedAt: Instant
)

data class CompleteCreationResult(
    val characterResponse: com.ensodai.shared.avalon.character.models.CharacterResponse,
    val chatSessionResponse: com.ensodai.shared.avalon.chat.models.ChatSessionPreviewResponse,
    val firstChatVariant: com.ensodai.shared.avalon.chat.models.ChatVariantResponse
)