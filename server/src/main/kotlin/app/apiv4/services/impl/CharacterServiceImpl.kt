@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package app.apiv4.services.impl

import app.domain.character.usecases.*
import com.ensodai.shared.avalon.character.models.*
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.shared.avalon.session.models.ReportResponse
import app.apiv4.infrastructure.BaseRpcService
import app.apiv4.infrastructure.RpcSessionContext
import io.ktor.server.application.ApplicationCall
import org.koin.core.component.KoinComponent
import kotlin.uuid.Uuid
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
@Factory
class CharacterServiceImpl(
    @InjectedParam private val call: ApplicationCall,
    @InjectedParam session: RpcSessionContext,
    private val getCharactersUseCase: GetCharactersUseCase,
    private val getCharacterUseCase: GetCharacterUseCase,
    private val searchCharacterUseCase: SearchCharacterUseCase,
    private val deleteCharacterUseCase: DeleteCharacterUseCase,
    private val updateCharacterNameUseCase: UpdateCharacterNameUseCase,
    private val updateCharacterDescriptionUseCase: UpdateCharacterDescriptionUseCase,
    private val updateCharacterFullDescriptionUseCase: UpdateCharacterFullDescriptionUseCase,
    private val updateCharacterPrivacyUseCase: UpdateCharacterPrivacyUseCase,
    private val updateCharacterTemperatureUseCase: UpdateCharacterTemperatureUseCase,
    private val updateFirstMessageUseCase: UpdateFirstMessageUseCase,
    private val updateWelcomeCardUseCase: UpdateWelcomeCardUseCase,
    private val publishCharacterUseCase: PublishCharacterUseCase,
    private val updateCharacterImagesUseCase: UpdateCharacterImagesUseCase,
    private val claimCharacterEnergyUseCase: ClaimCharacterEnergyUseCase,
    private val getCharactersEnergyUseCase: GetCharactersEnergyUseCase,
    private val updateCharacterOutreachUseCase: UpdateCharacterOutreachUseCase,
    private val updateCharacterVoiceUseCase: UpdateCharacterVoiceUseCase,
    private val updateCharacterInteractionFormatUseCase: UpdateCharacterInteractionFormatUseCase,
    private val getCharacterEventsUseCase: GetCharacterEventsUseCase,
    private val updateCharacterEventsUseCase: UpdateCharacterEventsUseCase,
    private val getCharacterEventsRoadmapUseCase: GetCharacterEventsRoadmapUseCase,
    private val getWarmOutreachSettingsUseCase: GetWarmOutreachSettingsUseCase,
    private val updateWarmOutreachSettingsUseCase: UpdateWarmOutreachSettingsUseCase,
    private val unlockCharacterImageUseCase: UnlockCharacterImageUseCase,
    private val exportCharacterCardUseCase: ExportCharacterCardUseCase,
    private val getCharacterSpecDetailsUseCase: GetCharacterSpecDetailsUseCase,
    private val updateCharacterPromptDetailsUseCase: UpdateCharacterPromptDetailsUseCase,
    private val updateAlternateGreetingsUseCase: UpdateAlternateGreetingsUseCase,
    private val updateCharacterLorebookUseCase: UpdateCharacterLorebookUseCase,
    private val updateLorebookSettingsUseCase: UpdateLorebookSettingsUseCase,
    private val saveLorebookEntryUseCase: SaveLorebookEntryUseCase,
    private val deleteLorebookEntryUseCase: DeleteLorebookEntryUseCase,
    private val toggleLorebookEntryUseCase: ToggleLorebookEntryUseCase,
    private val importCharacterCardUseCase: ImportCharacterCardUseCase,
    private val manageCharacterLorebooksUseCase: ManageCharacterLorebooksUseCase,
    private val getPublicLorebooksUseCase: GetPublicLorebooksUseCase,
    private val reportLorebookUseCase: ReportLorebookUseCase,
    private val importStandaloneLorebookUseCase: ImportStandaloneLorebookUseCase,
    private val exportStandaloneLorebookUseCase: ExportStandaloneLorebookUseCase
) : BaseRpcService(session), CharacterRpcService, KoinComponent {

    override suspend fun getCharacters(category: String, page: Int): PaginatedResponse<CharacterPreviewResponse> {
        return getCharactersUseCase.execute(category, page)
    }

    override suspend fun getCharacter(characterPublicId: Uuid): CharacterResponse {
        ensureAuthorized()
        return getCharacterUseCase.execute(characterPublicId, userId())
    }

    override suspend fun searchCharacter(query: String, page: Int): PaginatedResponse<CharacterPreviewResponse> {
        return searchCharacterUseCase.execute(query, page)
    }

    override suspend fun deleteCharacter(characterId: Uuid) {
        ensureAuthorized()
        deleteCharacterUseCase.execute(characterId, userId(), sessionId())
    }

    override suspend fun updateName(request: UpdateNameRequest) {
        ensureAuthorized()
        updateCharacterNameUseCase.execute(request, userId())
    }

    override suspend fun updateDescription(request: UpdateDescriptionRequest) {
        ensureAuthorized()
        updateCharacterDescriptionUseCase.execute(request, userId())
    }

    override suspend fun updateFullDescription(request: UpdateFullDescriptionRequest) {
        ensureAuthorized()
        updateCharacterFullDescriptionUseCase.execute(request, userId())
    }

    override suspend fun updateScenario(request: UpdateScenarioRequest) {
        ensureAuthorized()
        updateCharacterPromptDetailsUseCase.updateScenario(request.characterId, request.scenario, userId())
    }

    override suspend fun updateMesExample(request: UpdateMesExampleRequest) {
        ensureAuthorized()
        updateCharacterPromptDetailsUseCase.updateMesExample(request.characterId, request.mesExample, userId())
    }

    override suspend fun updatePrivacy(request: UpdatePrivacyRequest) {
        ensureAuthorized()
        updateCharacterPrivacyUseCase.execute(request, userId())
    }

    override suspend fun updateTemperature(request: UpdateTemperatureRequest) {
        ensureAuthorized()
        updateCharacterTemperatureUseCase.execute(request, userId())
    }

    override suspend fun updateFirstMessage(request: UpdateFirstMessageRequest) {
        ensureAuthorized()
        updateFirstMessageUseCase.execute(request, userId())
    }

    override suspend fun updateOutreach(request: UpdateCharacterOutreachRequest) {
        ensureAuthorized()
        updateCharacterOutreachUseCase.execute(request, userId())
    }

    override suspend fun updateVoice(request: UpdateVoiceRequest) {
        ensureAuthorized()
        updateCharacterVoiceUseCase.execute(request, userId())
    }

    override suspend fun updateInteractionFormat(request: UpdateInteractionFormatRequest) {
        ensureAuthorized()
        updateCharacterInteractionFormatUseCase.execute(request, userId())
    }

    override suspend fun updateWelcomeCard(request: UpdateWelcomeCardRequest): CharacterResponse {
        ensureAuthorized()
        updateWelcomeCardUseCase.execute(request, userId())
        return getCharacter(request.characterId)
    }

    override suspend fun updateCharacterImages(request: UpdateImagesRequest): UpdateImagesResponse {
        ensureAuthorized()
        updateCharacterImagesUseCase.execute(request, userId())
        val details = getCharacter(request.characterId)
        return UpdateImagesResponse(
            avatar = details.avatar,
            gallery = details.images
        )
    }

    override suspend fun publishCharacter(request: PublishCharacterRequest): CharacterPublishedResponse {
        ensureAuthorized()
        return publishCharacterUseCase.execute(request, userId())
    }

    override suspend fun claimEnergy(characterId: Uuid): ClaimEnergyResponse {
        ensureAuthorized()
        return claimCharacterEnergyUseCase.execute(userId(), characterId)
    }

    override suspend fun getCharactersEnergy(): List<CharacterEnergyResponse> {
        ensureAuthorized()
        return getCharactersEnergyUseCase.execute(userId())
    }

    override suspend fun getCharacterEvents(characterId: Uuid): List<CharacterEventDto> {
        ensureAuthorized()
        return getCharacterEventsUseCase.invoke(characterId, userId())
    }

    override suspend fun updateCharacterEvents(request: UpdateCharacterEventsRequest) {
        ensureAuthorized()
        updateCharacterEventsUseCase.invoke(request, userId())
    }

    override suspend fun getCharacterEventsRoadmap(characterId: Uuid): List<CharacterEventRoadmapDto> {
        ensureAuthorized()
        return getCharacterEventsRoadmapUseCase(characterId, userId())
    }

    override suspend fun getWarmOutreachSettings(characterId: Uuid): WarmOutreachSettingsDto {
        ensureAuthorized()
        return getWarmOutreachSettingsUseCase.execute(characterId, userId())
    }

    override suspend fun updateWarmOutreachSettings(request: UpdateWarmOutreachSettingsRequest) {
        ensureAuthorized()
        updateWarmOutreachSettingsUseCase.execute(request, userId())
    }

    override suspend fun unlockCharacterImage(characterImageId: Uuid): UnlockCharacterImageResult {
        ensureAuthorized()
        return unlockCharacterImageUseCase.execute(characterImageId, userId())
    }

    override suspend fun exportCharacterCard(characterId: Uuid, format: CharacterCardExportFormat): ExportCharacterCardResponse {
        ensureAuthorized()
        return exportCharacterCardUseCase.execute(characterId, format, userId())
    }

    override suspend fun importCharacterCard(request: ImportCharacterCardRequest): CharacterPublishedResponse {
        ensureAuthorized()
        return importCharacterCardUseCase.execute(request, userId())
    }

    override suspend fun getCharacterPromptDetails(characterId: Uuid): CharacterPromptDetailsDto? {
        ensureAuthorized()
        return getCharacterSpecDetailsUseCase.getPromptDetails(characterId)
    }

    override suspend fun updateCharacterPromptDetails(request: UpdateCharacterPromptDetailsRequest) {
        ensureAuthorized()
        updateCharacterPromptDetailsUseCase.execute(request, userId())
    }

    override suspend fun getCharacterAlternateGreetings(characterId: Uuid): List<CharacterAlternateGreetingDto> {
        ensureAuthorized()
        return getCharacterSpecDetailsUseCase.getAlternateGreetings(characterId)
    }

    override suspend fun updateAlternateGreetings(request: UpdateAlternateGreetingsRequest) {
        ensureAuthorized()
        updateAlternateGreetingsUseCase.execute(request, userId())
    }

    override suspend fun getCharacterLorebook(characterId: Uuid): CharacterLorebookDetailsDto? {
        ensureAuthorized()
        return getCharacterSpecDetailsUseCase.getLorebook(characterId)
    }

    override suspend fun updateCharacterLorebook(request: UpdateCharacterLorebookRequest) {
        ensureAuthorized()
        updateCharacterLorebookUseCase.execute(request, userId())
    }

    override suspend fun updateLorebookSettings(request: UpdateLorebookSettingsRequest) {
        ensureAuthorized()
        updateLorebookSettingsUseCase.execute(request, userId())
    }

    override suspend fun saveLorebookEntry(request: SaveLorebookEntryRequest): CharacterLorebookEntryItemDto {
        ensureAuthorized()
        return saveLorebookEntryUseCase.execute(request, userId())
    }

    override suspend fun deleteLorebookEntry(request: DeleteLorebookEntryRequest) {
        ensureAuthorized()
        deleteLorebookEntryUseCase.execute(request, userId())
    }

    override suspend fun toggleLorebookEntry(request: ToggleLorebookEntryRequest) {
        ensureAuthorized()
        toggleLorebookEntryUseCase.execute(request, userId())
    }

    override suspend fun getCharacterLorebooks(characterId: Uuid): List<CharacterLorebookBindingDto> {
        ensureAuthorized()
        return getCharacterSpecDetailsUseCase.getCharacterLorebooks(characterId, userId())
    }

    override suspend fun attachLorebook(request: AttachLorebookRequest): CharacterLorebookBindingDto {
        ensureAuthorized()
        return manageCharacterLorebooksUseCase.attachLorebook(request, userId())
    }

    override suspend fun detachLorebook(request: DetachLorebookRequest) {
        ensureAuthorized()
        manageCharacterLorebooksUseCase.detachLorebook(request, userId())
    }

    override suspend fun toggleLorebookBinding(request: ToggleLorebookBindingRequest) {
        ensureAuthorized()
        manageCharacterLorebooksUseCase.toggleLorebookBinding(request, userId())
    }

    override suspend fun setPrimaryLorebook(request: SetPrimaryLorebookRequest) {
        ensureAuthorized()
        manageCharacterLorebooksUseCase.setPrimaryLorebook(request, userId())
    }

    override suspend fun getUserLorebooksLibrary(): List<UserLorebookSummaryDto> {
        ensureAuthorized()
        return getCharacterSpecDetailsUseCase.getUserLorebooksLibrary(userId())
    }

    override suspend fun createStandaloneLorebook(request: CreateStandaloneLorebookRequest): CharacterLorebookDetailsDto {
        ensureAuthorized()
        return manageCharacterLorebooksUseCase.createStandaloneLorebook(request, userId())
    }

    override suspend fun forkLorebook(request: ForkLorebookRequest): CharacterLorebookDetailsDto {
        ensureAuthorized()
        return manageCharacterLorebooksUseCase.forkLorebook(request, userId())
    }

    override suspend fun getLorebookById(lorebookId: Uuid): CharacterLorebookDetailsDto? {
        ensureAuthorized()
        return getCharacterSpecDetailsUseCase.getLorebookById(lorebookId, userId())
    }

    override suspend fun deleteLorebook(lorebookId: Uuid) {
        ensureAuthorized()
        manageCharacterLorebooksUseCase.deleteLorebook(lorebookId, userId())
    }

    override suspend fun saveLorebookToLibrary(lorebookId: Uuid) {
        ensureAuthorized()
        manageCharacterLorebooksUseCase.saveLorebookToLibrary(lorebookId, userId())
    }

    override suspend fun removeLorebookFromLibrary(lorebookId: Uuid) {
        ensureAuthorized()
        manageCharacterLorebooksUseCase.removeLorebookFromLibrary(lorebookId, userId())
    }

    override suspend fun getPublicLorebooks(query: String?, page: Int, limit: Int): PaginatedResponse<UserLorebookSummaryDto> {
        ensureAuthorized()
        return getPublicLorebooksUseCase.execute(query, page, limit, userId())
    }

    override suspend fun reportLorebook(request: ReportLorebookRequest): ReportResponse {
        ensureAuthorized()
        return reportLorebookUseCase.execute(request, userId())
    }

    override suspend fun attachUserLorebook(request: AttachUserCharacterLorebookRequest): CharacterLorebookBindingDto {
        ensureAuthorized()
        return manageCharacterLorebooksUseCase.attachUserLorebook(request, userId())
    }

    override suspend fun detachUserLorebook(request: DetachUserCharacterLorebookRequest) {
        ensureAuthorized()
        manageCharacterLorebooksUseCase.detachUserLorebook(request, userId())
    }

    override suspend fun importStandaloneLorebook(request: ImportStandaloneLorebookRequest): CharacterLorebookDetailsDto {
        ensureAuthorized()
        return importStandaloneLorebookUseCase.execute(request, userId())
    }

    override suspend fun exportStandaloneLorebook(lorebookId: Uuid): ExportStandaloneLorebookResponse {
        ensureAuthorized()
        return exportStandaloneLorebookUseCase.execute(lorebookId, userId())
    }
}

