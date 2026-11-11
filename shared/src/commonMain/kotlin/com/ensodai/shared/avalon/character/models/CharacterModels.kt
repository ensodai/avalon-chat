@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.shared.avalon.character.models

import com.ensodai.shared.avalon.chat.models.ChatSessionPreviewResponse
import com.ensodai.shared.avalon.chat.models.ChatVariantResponse
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class CharacterPublishedResponse(
    val character: CharacterResponse,
    val chatSession: ChatSessionPreviewResponse,
    val chatVariant: ChatVariantResponse
)

@Serializable
data class CharacterImageResponse(
    @Contextual
    val id: Uuid,
    val imageUrl: String,
    val thumbHash: String? = null,
    val isPrivate: Boolean = false,
    val energyCost: Int = 0,
    val requiredLevel: Int? = null,
    val isUnlocked: Boolean = false
)

@Serializable
sealed interface UnlockCharacterImageResult {
    @Serializable
    data class Success(val image: CharacterImageResponse) : UnlockCharacterImageResult

    @Serializable
    data class InsufficientEnergy(val requiredEnergy: Int, val currentEnergy: Int) : UnlockCharacterImageResult

    @Serializable
    data class Error(val message: String) : UnlockCharacterImageResult
}


@Serializable
data class CharacterAvatarResponse(
    @Contextual
    val id: Uuid,
    val imageUrl: String,
    val thumbHash: String? = null
)

@Serializable
data class WelcomeCardImageResponse(
    @Contextual
    val id: Uuid,
    val imageUrl: String,
    val thumbHash: String? = null
)

@Serializable
data class WelcomeCardResponse(
    @Contextual
    val id: Uuid,
    val welcomeCardDescription: String? = null,
    val welcomeCardImages: List<WelcomeCardImageResponse>? = null,
    val startColor: String,
    val endColor: String,
    val cardColorLight: String,
    val isEnableLight: Boolean
)

@Serializable
data class ClaimEnergyResponse(
    @Contextual
    val characterId: Uuid,
    val claimedAmount: Int,
    val remainingUnclaimedEnergy: Double,
    val userReserveBalance: Int
)

@Serializable
data class CharacterEnergyResponse(
    @Contextual
    val characterId: Uuid,
    val unclaimedEnergy: Double
)

@Serializable
data class CharacterPreviewResponse(
    @Contextual
    val id: Uuid,
    val characterName: String,
    val shortDescription: String,
    val avatarUrl: String?,
    val thumbHash: String? = null,
    val ownerNick: String,
    val visibility: CharacterVisibility,
    @Contextual
    val updatedAt: Instant,
    val messageCountRating: Int = 0,
    val eventsCount: Int = 0
)

@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
    val limit: Int
)

@Serializable
data class CharacterResponse(
    @Contextual
    val id: Uuid,
    val ownerNick: String,
    val characterName: String,
    val shortDescription: String,
    val fullDescription: String?,
    val isPublicFullDescription: Boolean,
    val visibility: CharacterVisibility,
    val avatar: CharacterAvatarResponse?,
    val images: List<CharacterImageResponse>,
    val welcomeCard: WelcomeCardResponse?,
    val firstMessage: String?,
    val canEdit: Boolean,
    @Contextual
    val updatedAt: Instant,
    val temperature: Float,
    val isDeleted: Boolean,
    val unclaimedEnergy: Double? = null,
    val totalEarnedEnergy: Double? = null,
    val isOutreachEnabled: Boolean,
    val sharedMedia: List<String> = emptyList(),
    val voice: VoiceModel = VoiceModel.ZEPHYR,
    val relationshipLevel: Int = 1,
    val experiencePoints: Long = 0,
    val eventsCount: Int = 0,
    val interactionFormat: InteractionFormat = InteractionFormat.LITERARY_RPG,
    val scenario: String? = null,
    val mesExample: String? = null
)

@Serializable
data class UpdateCharacterOutreachRequest(
    @Contextual
    val characterId: Uuid,
    val isOutreachEnabled: Boolean
)

@Serializable
data class WelcomeCardRequest(
    val welcomeCardDescription: String? = null,
    val cardColorStart: String,
    val cardColorEnd: String,
    val cardColorLight: String,
    val isEnableLight: Boolean
)

@Serializable
data class CharacterPromptDetailsInput(
    val scenario: String? = null,
    val mesExample: String? = null,
    val customSystemPrompt: String? = null,
    val postHistoryInstructions: String? = null,
    val creator: String? = null,
    val characterVersion: String? = null,
    val creatorNotes: String? = null,
    val isImported: Boolean = false
)

@Serializable
data class CharacterLorebookEntryInput(
    val keys: List<String>,
    val secondaryKeys: List<String>? = null,
    val content: String,
    val comment: String? = null,
    val enabled: Boolean = true,
    val constant: Boolean = false,
    val selective: Boolean = false,
    val insertionOrder: Int = 0,
    val position: LorebookEntryPosition = LorebookEntryPosition.BEFORE_CHAR,
    val useRegex: Boolean = false,
    val priority: Int = 10,
    val probability: Int = 100,
    val scanDepthOverride: Int? = null,
    val stickyTurns: Int = 0,
    val cooldownTurns: Int = 0,
    val delayMessages: Int = 0,
    val secondaryLogic: SecondaryFilterLogic = SecondaryFilterLogic.AND_ANY,
    val excludeRecursion: Boolean = false,
    val preventRecursion: Boolean = false,
    val delayUntilRecursion: Boolean = false,
    val depth: Int = 4
)

@Serializable
data class CharacterLorebookInput(
    val name: String = "Default Lorebook",
    val description: String? = null,
    val scanDepth: Int = 5,
    val tokenBudget: Int? = null,
    val recursiveScanning: Boolean = false,
    val entries: List<CharacterLorebookEntryInput> = emptyList()
)

@Serializable
data class SaveCharacterRequest(
    val characterName: String,
    val fullDescription: String?,
    val shortDescription: String,
    val temperature: Float,
    val visibility: CharacterVisibility,
    val isPublicFullDescription: Boolean,
    val category: String?,
    val firstMessage: String?,
    val welcomeCard: WelcomeCardRequest? = null,
    val isOutreachEnabled: Boolean = false,
    val voice: VoiceModel = VoiceModel.ZEPHYR,
    val events: List<CharacterEventDto> = emptyList(),
    val interactionFormat: InteractionFormat = InteractionFormat.LITERARY_RPG,
    val promptDetails: CharacterPromptDetailsInput? = null,
    val alternateGreetings: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val lorebook: CharacterLorebookInput? = null,
    val attachedLorebookIds: List<@Contextual Uuid> = emptyList()
)


// Request models for specific updates
@Serializable
data class UpdateInteractionFormatRequest(
    @Contextual val characterId: Uuid,
    val interactionFormat: InteractionFormat
)

@Serializable
data class UpdateNameRequest(@Contextual val characterId: Uuid, val newName: String)

@Serializable
data class UpdateDescriptionRequest(@Contextual val characterId: Uuid, val description: String)

@Serializable
data class UpdateFullDescriptionRequest(
    @Contextual
    val characterId: Uuid,
    val fullDescription: String,
    val isPublicFullDescription: Boolean
)

@Serializable
data class UpdateScenarioRequest(
    @Contextual
    val characterId: Uuid,
    val scenario: String?
)

@Serializable
data class UpdateMesExampleRequest(
    @Contextual
    val characterId: Uuid,
    val mesExample: String?
)

@Serializable
data class UpdatePrivacyRequest(@Contextual val characterId: Uuid, val characterVisibility: CharacterVisibility)

@Serializable
data class UpdateTemperatureRequest(@Contextual val characterId: Uuid, val temperature: Float)

@Serializable
data class UpdateFirstMessageRequest(@Contextual val characterId: Uuid, val message: String)

@Serializable
data class UpdateVoiceRequest(
    @Contextual val characterId: Uuid,
    val voice: VoiceModel
)

@Serializable
data class UpdateWelcomeCardRequest(
    @Contextual
    val characterId: Uuid,
    val description: String? = null,
    val colorStart: String,
    val colorEnd: String,
    val colorLight: String,
    val isLightEnabled: Boolean,
    val deletedImagesIds: List<@Contextual Uuid>? = null,
    val imageTokens: List<String>? = null
)

@Serializable
data class GalleryImageSpec(
    val imageToken: String? = null,
    @Contextual
    val imageId: Uuid? = null,
    val isPrivate: Boolean = false,
    val energyCost: Int = 0,
    val requiredLevel: Int? = null
)

@Serializable
data class PublishCharacterRequest(
    val characterData: SaveCharacterRequest,
    val imageTokens: List<String>? = null,
    val avatarToken: String? = null,
    val welcomeCardImageTokens: List<String>? = null,
    val galleryImages: List<GalleryImageSpec>? = null
)

@Serializable
data class UpdateImagesResponse(
    val avatar: CharacterAvatarResponse?,
    val gallery: List<CharacterImageResponse>
)

@Serializable
data class UpdateWelcomeCardImagesResponse(
    val welcomeCardImages: List<WelcomeCardImageResponse>
)

@Serializable
data class UpdateImagesRequest(
    @Contextual
    val characterId: Uuid,
    @Contextual
    val newAvatarId: Uuid?,
    val deletedImagesIds: List<@Contextual Uuid>,
    val imageTokens: List<String>? = null,
    val avatarToken: String? = null,
    val galleryImages: List<GalleryImageSpec>? = null
)

@Serializable
data class DeleteCharacterRequest(
    @Contextual
    val characterId: Uuid
)

// ==========================================
// Модели спецификаций Character Card V2 / V3
// ==========================================

@Serializable
enum class CharacterCardExportFormat {
    PNG_V3,
    PNG_V2,
    JSON_V3,
    JSON_V2
}

@Serializable
enum class LorebookEntryPosition {
    BEFORE_CHAR,
    AFTER_CHAR,
    BEFORE_DESC,
    AFTER_DESC,
    PERSONALITY,
    SCENARIO,
    AT_DEPTH
}

@Serializable
enum class SecondaryFilterLogic {
    AND_ANY,
    AND_ALL,
    NOT_ANY,
    NOT_ALL
}

@Serializable
data class CharacterPromptDetailsDto(
    @Contextual
    val characterId: Uuid,
    val scenario: String? = null,
    val mesExample: String? = null,
    val customSystemPrompt: String? = null,
    val postHistoryInstructions: String? = null,
    val creator: String? = null,
    val characterVersion: String? = null,
    val creatorNotes: String? = null,
    val isImported: Boolean = false
)

@Serializable
data class CharacterAlternateGreetingDto(
    @Contextual
    val id: Uuid,
    @Contextual
    val characterId: Uuid,
    val greetingText: String,
    val title: String? = null,
    val orderIndex: Int = 0
)

@Serializable
data class CharacterLorebookEntryItemDto(
    @Contextual
    val id: Uuid,
    val keys: List<String>,
    val secondaryKeys: List<String>? = null,
    val content: String,
    val comment: String? = null,
    val enabled: Boolean = true,
    val constant: Boolean = false,
    val selective: Boolean = false,
    val insertionOrder: Int = 0,
    val position: LorebookEntryPosition = LorebookEntryPosition.BEFORE_CHAR,
    val useRegex: Boolean = false,
    val priority: Int = 10,
    val probability: Int = 100,
    val scanDepthOverride: Int? = null,
    val stickyTurns: Int = 0,
    val cooldownTurns: Int = 0,
    val delayMessages: Int = 0,
    val secondaryLogic: SecondaryFilterLogic = SecondaryFilterLogic.AND_ANY,
    val excludeRecursion: Boolean = false,
    val preventRecursion: Boolean = false,
    val delayUntilRecursion: Boolean = false,
    val depth: Int = 4
)

@Serializable
data class CharacterLorebookDetailsDto(
    @Contextual
    val id: Uuid,
    @Contextual
    val characterId: Uuid? = null,
    val name: String,
    val description: String? = null,
    val scanDepth: Int = 5,
    val tokenBudget: Int? = null,
    val recursiveScanning: Boolean = false,
    val isPublic: Boolean = false,
    val isOwner: Boolean = true,
    val creatorName: String? = null,
    val avatarUrl: String? = null,
    val entries: List<CharacterLorebookEntryItemDto> = emptyList()
)

@Serializable
data class ExportCharacterCardResponse(
    val format: CharacterCardExportFormat,
    val fileBase64: String,
    val fileName: String,
    val mimeType: String
)

@Serializable
data class UpdateCharacterPromptDetailsRequest(
    @Contextual
    val characterId: Uuid,
    val scenario: String? = null,
    val mesExample: String? = null,
    val customSystemPrompt: String? = null,
    val postHistoryInstructions: String? = null,
    val creator: String? = null,
    val characterVersion: String? = null,
    val creatorNotes: String? = null
)

@Serializable
data class UpdateAlternateGreetingsRequest(
    @Contextual
    val characterId: Uuid,
    val greetings: List<CharacterAlternateGreetingDto>
)

@Serializable
data class UpdateCharacterLorebookRequest(
    @Contextual
    val lorebookId: Uuid,
    val name: String,
    val description: String? = null,
    val scanDepth: Int,
    val tokenBudget: Int? = null,
    val recursiveScanning: Boolean,
    val isPublic: Boolean,
    val avatarUrl: String? = null,
    val entries: List<CharacterLorebookEntryItemDto>
)

@Serializable
data class ImportCharacterCardRequest(
    val fileBase64: String,
    val visibility: CharacterVisibility = CharacterVisibility.PRIVATE
)

@Serializable
data class UpdateLorebookSettingsRequest(
    @Contextual
    val lorebookId: Uuid,
    val name: String,
    val description: String? = null,
    val scanDepth: Int,
    val tokenBudget: Int? = null,
    val recursiveScanning: Boolean,
    val isPublic: Boolean,
    val avatarUrl: String? = null
)

@Serializable
data class SaveLorebookEntryRequest(
    @Contextual
    val lorebookId: Uuid,
    val entry: CharacterLorebookEntryItemDto
)

@Serializable
data class DeleteLorebookEntryRequest(
    @Contextual
    val lorebookId: Uuid,
    @Contextual
    val entryId: Uuid
)

@Serializable
data class ToggleLorebookEntryRequest(
    @Contextual
    val lorebookId: Uuid,
    @Contextual
    val entryId: Uuid,
    val enabled: Boolean
)

// ==========================================
// Модели мульти-лорбуков и библиотеки знаний
// ==========================================

@Serializable
data class CharacterLorebookBindingDto(
    @Contextual
    val bindingId: Uuid,
    @Contextual
    val characterId: Uuid,
    @Contextual
    val lorebookId: Uuid,
    val name: String,
    val description: String? = null,
    val isEnabled: Boolean = true,
    val isPrimary: Boolean = false,
    val priority: Int = 0,
    val entriesCount: Int = 0,
    val isOwner: Boolean = true,
    val isPublic: Boolean = false,
    val creatorName: String? = null,
    val avatarUrl: String? = null,
    val isCanon: Boolean = true
)

@Serializable
data class UserLorebookSummaryDto(
    @Contextual
    val id: Uuid,
    val name: String,
    val description: String? = null,
    val isPublic: Boolean = false,
    val isOwner: Boolean = true,
    val entriesCount: Int = 0,
    val creatorName: String? = null,
    val createdAt: String? = null,
    val isSavedToLibrary: Boolean = false,
    val avatarUrl: String? = null
)

@Serializable
data class ReportLorebookRequest(
    @Contextual
    val lorebookId: Uuid,
    val message: String
)

@Serializable
data class AttachLorebookRequest(
    @Contextual
    val characterId: Uuid,
    @Contextual
    val lorebookId: Uuid,
    val isEnabled: Boolean = true,
    val isPrimary: Boolean = false,
    val priority: Int = 0
)

@Serializable
data class DetachLorebookRequest(
    @Contextual
    val characterId: Uuid,
    @Contextual
    val lorebookId: Uuid
)

@Serializable
data class ToggleLorebookBindingRequest(
    @Contextual
    val characterId: Uuid,
    @Contextual
    val lorebookId: Uuid,
    val isEnabled: Boolean
)

@Serializable
data class SetPrimaryLorebookRequest(
    @Contextual
    val characterId: Uuid,
    @Contextual
    val lorebookId: Uuid
)

@Serializable
data class CreateStandaloneLorebookRequest(
    val name: String,
    val description: String? = null,
    val scanDepth: Int = 5,
    val tokenBudget: Int? = null,
    val recursiveScanning: Boolean = false,
    val isPublic: Boolean = false,
    val avatarUrl: String? = null,
    @Contextual
    val attachToCharacterId: Uuid? = null,
    val entries: List<CharacterLorebookEntryInput> = emptyList()
)

@Serializable
data class ForkLorebookRequest(
    @Contextual
    val lorebookId: Uuid,
    @Contextual
    val attachToCharacterId: Uuid? = null
)

@Serializable
data class AttachUserCharacterLorebookRequest(
    @Contextual
    val characterId: Uuid,
    @Contextual
    val lorebookId: Uuid
)

@Serializable
data class DetachUserCharacterLorebookRequest(
    @Contextual
    val characterId: Uuid,
    @Contextual
    val lorebookId: Uuid
)

@Serializable
data class ImportStandaloneLorebookRequest(
    val fileBase64: String,
    @Contextual
    val attachToCharacterId: Uuid? = null,
    val isPublic: Boolean = false
)

@Serializable
data class ExportStandaloneLorebookResponse(
    val fileBase64: String,
    val fileName: String,
    val mimeType: String = "application/json"
)





