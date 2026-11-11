@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.shared.avalon.rpc.services

import com.ensodai.shared.avalon.character.models.*
import com.ensodai.shared.avalon.session.models.ReportResponse
import kotlinx.rpc.annotations.Rpc
import kotlin.uuid.Uuid

@Rpc
interface CharacterRpcService {
    suspend fun searchCharacter(query: String, page: Int = 1): PaginatedResponse<CharacterPreviewResponse>
    suspend fun getCharacter(characterPublicId: Uuid): CharacterResponse
    suspend fun getCharacters(category: String = "all", page: Int = 1): PaginatedResponse<CharacterPreviewResponse>
    suspend fun deleteCharacter(characterId: Uuid)
    
    suspend fun updateName(request: UpdateNameRequest)
    suspend fun updateDescription(request: UpdateDescriptionRequest)
    suspend fun updateFullDescription(request: UpdateFullDescriptionRequest)
    suspend fun updateScenario(request: UpdateScenarioRequest)
    suspend fun updateMesExample(request: UpdateMesExampleRequest)
    suspend fun updatePrivacy(request: UpdatePrivacyRequest)
    suspend fun updateTemperature(request: UpdateTemperatureRequest)
    suspend fun updateFirstMessage(request: UpdateFirstMessageRequest)
    suspend fun updateOutreach(request: UpdateCharacterOutreachRequest)
    suspend fun updateVoice(request: UpdateVoiceRequest)
    suspend fun updateInteractionFormat(request: UpdateInteractionFormatRequest)
    
    suspend fun publishCharacter(request: PublishCharacterRequest): CharacterPublishedResponse
    suspend fun updateWelcomeCard(request: UpdateWelcomeCardRequest): CharacterResponse
    suspend fun updateCharacterImages(request: UpdateImagesRequest): UpdateImagesResponse
    
    suspend fun claimEnergy(characterId: Uuid): ClaimEnergyResponse
    suspend fun getCharactersEnergy(): List<CharacterEnergyResponse>
    
    suspend fun getCharacterEvents(characterId: Uuid): List<CharacterEventDto>
    suspend fun updateCharacterEvents(request: UpdateCharacterEventsRequest)
    suspend fun getCharacterEventsRoadmap(characterId: Uuid): List<CharacterEventRoadmapDto>
    
    suspend fun getWarmOutreachSettings(characterId: Uuid): WarmOutreachSettingsDto
    suspend fun updateWarmOutreachSettings(request: UpdateWarmOutreachSettingsRequest)
    
    suspend fun unlockCharacterImage(characterImageId: Uuid): UnlockCharacterImageResult
    
    // Методы спецификации Character Card V2 / V3
    suspend fun exportCharacterCard(characterId: Uuid, format: CharacterCardExportFormat): ExportCharacterCardResponse
    suspend fun importCharacterCard(request: ImportCharacterCardRequest): CharacterPublishedResponse
    suspend fun getCharacterPromptDetails(characterId: Uuid): CharacterPromptDetailsDto?
    suspend fun updateCharacterPromptDetails(request: UpdateCharacterPromptDetailsRequest)
    suspend fun getCharacterAlternateGreetings(characterId: Uuid): List<CharacterAlternateGreetingDto>
    suspend fun updateAlternateGreetings(request: UpdateAlternateGreetingsRequest)
    suspend fun getCharacterLorebook(characterId: Uuid): CharacterLorebookDetailsDto?
    suspend fun updateCharacterLorebook(request: UpdateCharacterLorebookRequest)
    suspend fun updateLorebookSettings(request: UpdateLorebookSettingsRequest)
    suspend fun saveLorebookEntry(request: SaveLorebookEntryRequest): CharacterLorebookEntryItemDto
    suspend fun deleteLorebookEntry(request: DeleteLorebookEntryRequest)
    suspend fun toggleLorebookEntry(request: ToggleLorebookEntryRequest)

    // Мульти-лорбуки и библиотека знаний
    suspend fun getCharacterLorebooks(characterId: Uuid): List<CharacterLorebookBindingDto>
    suspend fun attachLorebook(request: AttachLorebookRequest): CharacterLorebookBindingDto
    suspend fun detachLorebook(request: DetachLorebookRequest)
    suspend fun toggleLorebookBinding(request: ToggleLorebookBindingRequest)
    suspend fun setPrimaryLorebook(request: SetPrimaryLorebookRequest)
    suspend fun getUserLorebooksLibrary(): List<UserLorebookSummaryDto>
    suspend fun getPublicLorebooks(query: String?, page: Int, limit: Int): PaginatedResponse<UserLorebookSummaryDto>
    suspend fun createStandaloneLorebook(request: CreateStandaloneLorebookRequest): CharacterLorebookDetailsDto
    suspend fun forkLorebook(request: ForkLorebookRequest): CharacterLorebookDetailsDto
    suspend fun getLorebookById(lorebookId: Uuid): CharacterLorebookDetailsDto?
    suspend fun deleteLorebook(lorebookId: Uuid)
    suspend fun saveLorebookToLibrary(lorebookId: Uuid)
    suspend fun removeLorebookFromLibrary(lorebookId: Uuid)
    suspend fun reportLorebook(request: ReportLorebookRequest): ReportResponse

    // Пользовательские книги знаний персонажа
    suspend fun attachUserLorebook(request: AttachUserCharacterLorebookRequest): CharacterLorebookBindingDto
    suspend fun detachUserLorebook(request: DetachUserCharacterLorebookRequest)

    // Импорт и экспорт отдельных файлов лорбуков
    suspend fun importStandaloneLorebook(request: ImportStandaloneLorebookRequest): CharacterLorebookDetailsDto
    suspend fun exportStandaloneLorebook(lorebookId: Uuid): ExportStandaloneLorebookResponse
}


