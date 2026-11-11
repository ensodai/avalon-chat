@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package com.ensodai.avalon.presentation.characterProfileScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.useCases.characterProfileScreenUseCase.GetCharacterProfileByIdFlowUseCase
import com.ensodai.avalon.domain.useCases.characterProfileScreenUseCase.GetSharedMediaUseCase
import com.ensodai.avalon.domain.useCases.characterProfileScreenUseCase.ReportCharacterUseCase
import com.ensodai.avalon.domain.useCases.characterProfileScreenUseCase.UnlockCharacterImageUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.GetChatStatusUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.GetCharacterByIdUseCase
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.SelectActiveLlmConfigForChatUseCase
import com.ensodai.avalon.domain.useCases.exportCharacterCardUseCase.ExportCharacterCardUseCase
import com.ensodai.avalon.domain.useCases.lorebookUseCase.ImportStandaloneLorebookUseCase
import com.ensodai.avalon.domain.useCases.paymentSreenUseCase.GetUserTariffUseCase
import com.ensodai.avalon.utils.IODispatcher
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import com.ensodai.avalon.presentation.characterProfileScreen.action.CharacterProfileScreenActions
import com.ensodai.avalon.presentation.characterProfileScreen.action.changeReportMessage
import com.ensodai.avalon.presentation.characterProfileScreen.action.closeReportDialog
import com.ensodai.avalon.presentation.characterProfileScreen.action.createViewState
import com.ensodai.avalon.presentation.characterProfileScreen.action.dismissExportMessage
import com.ensodai.avalon.presentation.characterProfileScreen.action.exportCharacterCard
import com.ensodai.avalon.presentation.characterProfileScreen.action.fetchUserTariff
import com.ensodai.avalon.presentation.characterProfileScreen.action.observeLlmConfigs
import com.ensodai.avalon.presentation.characterProfileScreen.action.observeSharedMedia
import com.ensodai.avalon.presentation.characterProfileScreen.action.reportCharacter
import com.ensodai.avalon.presentation.characterProfileScreen.action.selectActiveLlmConfig
import com.ensodai.avalon.presentation.characterProfileScreen.action.setCardFlipped
import com.ensodai.avalon.presentation.characterProfileScreen.action.showReportDialog
import com.ensodai.avalon.presentation.characterProfileScreen.action.toggleCardFlip
import com.ensodai.avalon.presentation.characterProfileScreen.action.updateTypingStatus
import com.ensodai.avalon.presentation.characterProfileScreen.viewState.CharacterProfileScreenViewState
import com.ensodai.shared.avalon.character.models.AttachUserCharacterLorebookRequest
import com.ensodai.shared.avalon.character.models.DetachUserCharacterLorebookRequest
import com.ensodai.avalon.presentation.core.BaseViewModel
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.io.encoding.Base64
import kotlin.uuid.Uuid

@KoinViewModel
class CharacterProfileScreenViewModel(
    private val getCharacterById: GetCharacterProfileByIdFlowUseCase,
    private val getSharedMedia: GetSharedMediaUseCase,
    private val reportCharacter: ReportCharacterUseCase,
    @InjectedParam val characterId: Uuid,
    @InjectedParam val fullScreenImage: Boolean,
    @InjectedParam val characterHasAvatars: Boolean,
    private val getChatStatus: GetChatStatusUseCase,
    private val unlockCharacterImage: UnlockCharacterImageUseCase,
    private val getCharacterByIdUseCase: GetCharacterByIdUseCase,
    private val selectActiveLlmConfigForChat: SelectActiveLlmConfigForChatUseCase,
    private val getUserTariff: GetUserTariffUseCase,
    private val exportCharacterCardUseCase: ExportCharacterCardUseCase,
    private val characterRpcService: CharacterRpcService,
    private val importStandaloneLorebookUseCase: ImportStandaloneLorebookUseCase
) : BaseViewModel<CharacterProfileScreenViewState, CharacterProfileScreenActions>(
    initialState = CharacterProfileScreenViewState(
        startWithFullScreenImages = fullScreenImage,
        characterHasAvatars = characterHasAvatars
    )
) {

    init {
        createViewState(
            characterId = characterId,
            getCharacterById = getCharacterById
        )
        loadPromptDetailsAndLorebooks()
        observeSharedMedia(
            getSharedMedia = getSharedMedia
        )
        updateTypingStatus(
            getChatStatus = getChatStatus
        )
        observeLlmConfigs(
            getCharacterByIdUseCase = getCharacterByIdUseCase
        )
        fetchUserTariff(
            getUserTariff = getUserTariff
        )
    }

    private fun loadPromptDetailsAndLorebooks() {
        viewModelScope.launch(IODispatcher) {
            runCatching {
                val characterResponse = characterRpcService.getCharacter(characterId)
                val lorebooks = characterRpcService.getCharacterLorebooks(characterId)
                updateViewState {
                    it.copy(
                        scenario = characterResponse.scenario,
                        mesExample = characterResponse.mesExample,
                        lorebooks = lorebooks
                    )
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun loadUserLorebooksLibrary() {
        viewModelScope.launch(IODispatcher) {
            runCatching {
                val library = characterRpcService.getUserLorebooksLibrary()
                updateViewState { it.copy(userLibraryLorebooks = library) }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun attachUserLorebook(lorebookId: Uuid) {
        viewModelScope.launch(IODispatcher) {
            runCatching {
                val binding = characterRpcService.attachUserLorebook(
                    AttachUserCharacterLorebookRequest(characterId = characterId, lorebookId = lorebookId)
                )
                updateViewState {
                    it.copy(lorebooks = it.lorebooks.filter { lb -> lb.lorebookId != lorebookId } + binding)
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun detachUserLorebook(lorebookId: Uuid) {
        viewModelScope.launch(IODispatcher) {
            runCatching {
                characterRpcService.detachUserLorebook(
                    DetachUserCharacterLorebookRequest(characterId = characterId, lorebookId = lorebookId)
                )
                updateViewState {
                    it.copy(lorebooks = it.lorebooks.filter { lb -> lb.lorebookId != lorebookId })
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun importUserLorebook(file: PlatformFile) {
        viewModelScope.launch(IODispatcher) {
            runCatching {
                val bytes = file.readBytes()
                val base64 = Base64.Default.encode(bytes)
                importStandaloneLorebookUseCase.execute(
                    fileBase64 = base64,
                    attachToCharacterId = characterId,
                    isPublic = false
                ).getOrThrow()
            }.onSuccess {
                loadUserLorebooksLibrary()
                loadPromptDetailsAndLorebooks()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    override val actions = CharacterProfileScreenActions(
        onReportCharacter = { reportCharacter(reportCharacter, characterId) },
        onShowReportDialog = { showReportDialog() },
        onCloseReportDialog = { closeReportDialog() },
        onChangeReportMessage = { changeReportMessage(it) },
        onUnlockImage = { imageId -> unlockCharacterImage.invoke(imageId) },
        onSelectActiveLlmConfig = { selectActiveLlmConfig(selectActiveLlmConfigForChat, it) },
        onExportCharacterCard = { exportCharacterCard(exportCharacterCardUseCase, it) },
        onDismissExportMessage = { dismissExportMessage() },
        onToggleCardFlip = { toggleCardFlip() },
        onSetCardFlipped = { setCardFlipped(it) },
        onAttachUserLorebook = { attachUserLorebook(it) },
        onDetachUserLorebook = { detachUserLorebook(it) },
        onLoadUserLorebooksLibrary = { loadUserLorebooksLibrary() },
        onImportLorebook = { importUserLorebook(it) }
    )
}
