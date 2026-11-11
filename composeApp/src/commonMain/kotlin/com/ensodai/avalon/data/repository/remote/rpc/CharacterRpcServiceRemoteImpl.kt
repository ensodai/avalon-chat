@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.avalon.data.repository.remote.rpc

import com.ensodai.shared.avalon.character.models.*
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.shared.avalon.session.models.ReportResponse
import com.ensodai.avalon.di.RpcConnectionManager
import kotlinx.rpc.withService
import kotlin.uuid.Uuid

open class CharacterRpcServiceRemoteImpl(
    private val manager: RpcConnectionManager,
    private val executor: RpcCallExecutor
) : CharacterRpcService {

    open suspend fun getService(): CharacterRpcService = manager.getActiveClient().withService<CharacterRpcService>()

    override suspend fun searchCharacter(
        query: String,
        page: Int
    ): PaginatedResponse<CharacterPreviewResponse> =
        executor.execute("searchCharacter", getService = { getService() }) { searchCharacter(query, page) }

    override suspend fun getCharacter(characterPublicId: Uuid): CharacterResponse =
        executor.execute("getCharacter", getService = { getService() }) { getCharacter(characterPublicId) }

    override suspend fun getCharacters(
        category: String,
        page: Int
    ): PaginatedResponse<CharacterPreviewResponse> =
        executor.execute("getCharacters", getService = { getService() }) { getCharacters(category, page) }

    override suspend fun deleteCharacter(characterId: Uuid) =
        executor.execute("deleteCharacter", getService = { getService() }) { deleteCharacter(characterId) }

    override suspend fun updateName(request: UpdateNameRequest) =
        executor.execute("updateName", getService = { getService() }) { updateName(request) }

    override suspend fun updateDescription(request: UpdateDescriptionRequest) =
        executor.execute("updateDescription", getService = { getService() }) { updateDescription(request) }

    override suspend fun updateFullDescription(request: UpdateFullDescriptionRequest) =
        executor.execute("updateFullDescription", getService = { getService() }) { updateFullDescription(request) }

    override suspend fun updateScenario(request: UpdateScenarioRequest) =
        executor.execute("updateScenario", getService = { getService() }) { updateScenario(request) }

    override suspend fun updateMesExample(request: UpdateMesExampleRequest) =
        executor.execute("updateMesExample", getService = { getService() }) { updateMesExample(request) }

    override suspend fun updatePrivacy(request: UpdatePrivacyRequest) =
        executor.execute("updatePrivacy", getService = { getService() }) { updatePrivacy(request) }

    override suspend fun updateTemperature(request: UpdateTemperatureRequest) =
        executor.execute("updateTemperature", getService = { getService() }) { updateTemperature(request) }

    override suspend fun updateFirstMessage(request: UpdateFirstMessageRequest) =
        executor.execute("updateFirstMessage", getService = { getService() }) { updateFirstMessage(request) }

    override suspend fun updateOutreach(request: UpdateCharacterOutreachRequest) =
        executor.execute("updateOutreach", getService = { getService() }) { updateOutreach(request) }

    override suspend fun updateVoice(request: UpdateVoiceRequest) =
        executor.execute("updateVoice", getService = { getService() }) { updateVoice(request) }

    override suspend fun updateInteractionFormat(request: UpdateInteractionFormatRequest) =
        executor.execute("updateInteractionFormat", getService = { getService() }) { updateInteractionFormat(request) }

    override suspend fun publishCharacter(request: PublishCharacterRequest): CharacterPublishedResponse =
        executor.execute("publishCharacter", getService = { getService() }) { publishCharacter(request) }

    override suspend fun updateWelcomeCard(request: UpdateWelcomeCardRequest): CharacterResponse =
        executor.execute("updateWelcomeCard", getService = { getService() }) { updateWelcomeCard(request) }

    override suspend fun updateCharacterImages(request: UpdateImagesRequest): UpdateImagesResponse =
        executor.execute("updateCharacterImages", getService = { getService() }) { updateCharacterImages(request) }

    override suspend fun claimEnergy(characterId: Uuid): ClaimEnergyResponse =
        executor.execute("claimEnergy", getService = { getService() }) { claimEnergy(characterId) }

    override suspend fun getCharactersEnergy(): List<CharacterEnergyResponse> =
        executor.execute("getCharactersEnergy", getService = { getService() }) { getCharactersEnergy() }

    override suspend fun getCharacterEvents(characterId: Uuid): List<CharacterEventDto> =
        executor.execute("getCharacterEvents", getService = { getService() }) { getCharacterEvents(characterId) }

    override suspend fun updateCharacterEvents(request: UpdateCharacterEventsRequest) =
        executor.execute("updateCharacterEvents", getService = { getService() }) { updateCharacterEvents(request) }

    override suspend fun getCharacterEventsRoadmap(characterId: Uuid): List<CharacterEventRoadmapDto> =
        executor.execute("getCharacterEventsRoadmap", getService = { getService() }) { getCharacterEventsRoadmap(characterId) }

    override suspend fun getWarmOutreachSettings(characterId: Uuid): WarmOutreachSettingsDto =
        executor.execute("getWarmOutreachSettings", getService = { getService() }) { getWarmOutreachSettings(characterId) }

    override suspend fun updateWarmOutreachSettings(request: UpdateWarmOutreachSettingsRequest) =
        executor.execute("updateWarmOutreachSettings", getService = { getService() }) { updateWarmOutreachSettings(request) }

    override suspend fun unlockCharacterImage(characterImageId: Uuid): UnlockCharacterImageResult =
        executor.execute("unlockCharacterImage", getService = { getService() }) { unlockCharacterImage(characterImageId) }

    override suspend fun exportCharacterCard(characterId: Uuid, format: CharacterCardExportFormat): ExportCharacterCardResponse =
        executor.execute("exportCharacterCard", getService = { getService() }) { exportCharacterCard(characterId, format) }

    override suspend fun importCharacterCard(request: ImportCharacterCardRequest): CharacterPublishedResponse =
        executor.execute("importCharacterCard", getService = { getService() }) { importCharacterCard(request) }

    override suspend fun getCharacterPromptDetails(characterId: Uuid): CharacterPromptDetailsDto? =
        executor.execute("getCharacterPromptDetails", getService = { getService() }) { getCharacterPromptDetails(characterId) }

    override suspend fun updateCharacterPromptDetails(request: UpdateCharacterPromptDetailsRequest) =
        executor.execute("updateCharacterPromptDetails", getService = { getService() }) { updateCharacterPromptDetails(request) }

    override suspend fun getCharacterAlternateGreetings(characterId: Uuid): List<CharacterAlternateGreetingDto> =
        executor.execute("getCharacterAlternateGreetings", getService = { getService() }) { getCharacterAlternateGreetings(characterId) }

    override suspend fun updateAlternateGreetings(request: UpdateAlternateGreetingsRequest) =
        executor.execute("updateAlternateGreetings", getService = { getService() }) { updateAlternateGreetings(request) }

    override suspend fun getCharacterLorebook(characterId: Uuid): CharacterLorebookDetailsDto? =
        executor.execute("getCharacterLorebook", getService = { getService() }) { getCharacterLorebook(characterId) }

    override suspend fun updateCharacterLorebook(request: UpdateCharacterLorebookRequest) =
        executor.execute("updateCharacterLorebook", getService = { getService() }) { updateCharacterLorebook(request) }

    override suspend fun updateLorebookSettings(request: UpdateLorebookSettingsRequest) =
        executor.execute("updateLorebookSettings", getService = { getService() }) { updateLorebookSettings(request) }

    override suspend fun saveLorebookEntry(request: SaveLorebookEntryRequest): CharacterLorebookEntryItemDto =
        executor.execute("saveLorebookEntry", getService = { getService() }) { saveLorebookEntry(request) }

    override suspend fun deleteLorebookEntry(request: DeleteLorebookEntryRequest) =
        executor.execute("deleteLorebookEntry", getService = { getService() }) { deleteLorebookEntry(request) }

    override suspend fun toggleLorebookEntry(request: ToggleLorebookEntryRequest) =
        executor.execute("toggleLorebookEntry", getService = { getService() }) { toggleLorebookEntry(request) }

    override suspend fun getCharacterLorebooks(characterId: Uuid): List<CharacterLorebookBindingDto> =
        executor.execute("getCharacterLorebooks", getService = { getService() }) { getCharacterLorebooks(characterId) }

    override suspend fun attachLorebook(request: AttachLorebookRequest): CharacterLorebookBindingDto =
        executor.execute("attachLorebook", getService = { getService() }) { attachLorebook(request) }

    override suspend fun detachLorebook(request: DetachLorebookRequest) =
        executor.execute("detachLorebook", getService = { getService() }) { detachLorebook(request) }

    override suspend fun toggleLorebookBinding(request: ToggleLorebookBindingRequest) =
        executor.execute("toggleLorebookBinding", getService = { getService() }) { toggleLorebookBinding(request) }

    override suspend fun setPrimaryLorebook(request: SetPrimaryLorebookRequest) =
        executor.execute("setPrimaryLorebook", getService = { getService() }) { setPrimaryLorebook(request) }

    override suspend fun getUserLorebooksLibrary(): List<UserLorebookSummaryDto> =
        executor.execute("getUserLorebooksLibrary", getService = { getService() }) { getUserLorebooksLibrary() }

    override suspend fun createStandaloneLorebook(request: CreateStandaloneLorebookRequest): CharacterLorebookDetailsDto =
        executor.execute("createStandaloneLorebook", getService = { getService() }) { createStandaloneLorebook(request) }

    override suspend fun forkLorebook(request: ForkLorebookRequest): CharacterLorebookDetailsDto =
        executor.execute("forkLorebook", getService = { getService() }) { forkLorebook(request) }

    override suspend fun getLorebookById(lorebookId: Uuid): CharacterLorebookDetailsDto? =
        executor.execute("getLorebookById", getService = { getService() }) { getLorebookById(lorebookId) }

    override suspend fun deleteLorebook(lorebookId: Uuid) =
        executor.execute("deleteLorebook", getService = { getService() }) { deleteLorebook(lorebookId) }

    override suspend fun saveLorebookToLibrary(lorebookId: Uuid) =
        executor.execute("saveLorebookToLibrary", getService = { getService() }) { saveLorebookToLibrary(lorebookId) }

    override suspend fun removeLorebookFromLibrary(lorebookId: Uuid) =
        executor.execute("removeLorebookFromLibrary", getService = { getService() }) { removeLorebookFromLibrary(lorebookId) }

    override suspend fun getPublicLorebooks(query: String?, page: Int, limit: Int): PaginatedResponse<UserLorebookSummaryDto> =
        executor.execute("getPublicLorebooks", getService = { getService() }) { getPublicLorebooks(query, page, limit) }

    override suspend fun reportLorebook(request: ReportLorebookRequest): ReportResponse =
        executor.execute("reportLorebook", getService = { getService() }) { reportLorebook(request) }

    override suspend fun attachUserLorebook(request: AttachUserCharacterLorebookRequest): CharacterLorebookBindingDto =
        executor.execute("attachUserLorebook", getService = { getService() }) { attachUserLorebook(request) }

    override suspend fun detachUserLorebook(request: DetachUserCharacterLorebookRequest) =
        executor.execute("detachUserLorebook", getService = { getService() }) { detachUserLorebook(request) }

    override suspend fun importStandaloneLorebook(request: ImportStandaloneLorebookRequest): CharacterLorebookDetailsDto =
        executor.execute("importStandaloneLorebook", getService = { getService() }) { importStandaloneLorebook(request) }

    override suspend fun exportStandaloneLorebook(lorebookId: Uuid): ExportStandaloneLorebookResponse =
        executor.execute("exportStandaloneLorebook", getService = { getService() }) { exportStandaloneLorebook(lorebookId) }
}
