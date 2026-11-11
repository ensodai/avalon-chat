package com.ensodai.avalon.domain.lorebook

import com.ensodai.shared.avalon.character.models.AttachLorebookRequest
import com.ensodai.shared.avalon.character.models.CharacterLorebookBindingDto
import com.ensodai.shared.avalon.character.models.CreateStandaloneLorebookRequest
import com.ensodai.shared.avalon.character.models.DetachLorebookRequest
import com.ensodai.shared.avalon.character.models.SetPrimaryLorebookRequest
import com.ensodai.shared.avalon.character.models.ToggleLorebookBindingRequest
import com.ensodai.shared.avalon.character.models.UserLorebookSummaryDto
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class LorebookManager(
    private val characterRpcService: CharacterRpcService
) {
    private val _characterBindings = MutableStateFlow<List<CharacterLorebookBindingDto>>(emptyList())
    val characterBindings: StateFlow<List<CharacterLorebookBindingDto>> = _characterBindings.asStateFlow()

    private val _userLibrary = MutableStateFlow<List<UserLorebookSummaryDto>>(emptyList())
    val userLibrary: StateFlow<List<UserLorebookSummaryDto>> = _userLibrary.asStateFlow()

    private var activeCharacterId: Uuid? = null

    suspend fun loadCharacterLorebooks(characterId: Uuid) {
        activeCharacterId = characterId
        val bindings = characterRpcService.getCharacterLorebooks(characterId)
        _characterBindings.value = bindings
    }

    suspend fun loadUserLibrary() {
        val library = characterRpcService.getUserLorebooksLibrary()
        _userLibrary.value = library
    }

    suspend fun toggleBinding(characterId: Uuid, lorebookId: Uuid, isEnabled: Boolean) {
        val previous = _characterBindings.value
        _characterBindings.update { list ->
            list.map { if (it.lorebookId == lorebookId) it.copy(isEnabled = isEnabled) else it }
        }
        try {
            characterRpcService.toggleLorebookBinding(
                ToggleLorebookBindingRequest(
                    characterId = characterId,
                    lorebookId = lorebookId,
                    isEnabled = isEnabled
                )
            )
        } catch (e: Exception) {
            _characterBindings.value = previous
            throw e
        }
    }

    suspend fun setPrimary(characterId: Uuid, lorebookId: Uuid) {
        val previous = _characterBindings.value
        _characterBindings.update { list ->
            list.map { it.copy(isPrimary = it.lorebookId == lorebookId) }
        }
        try {
            characterRpcService.setPrimaryLorebook(
                SetPrimaryLorebookRequest(
                    characterId = characterId,
                    lorebookId = lorebookId
                )
            )
        } catch (e: Exception) {
            _characterBindings.value = previous
            throw e
        }
    }

    suspend fun detachLorebook(characterId: Uuid, lorebookId: Uuid) {
        val previous = _characterBindings.value
        _characterBindings.update { list ->
            list.filterNot { it.lorebookId == lorebookId }
        }
        try {
            characterRpcService.detachLorebook(
                DetachLorebookRequest(
                    characterId = characterId,
                    lorebookId = lorebookId
                )
            )
        } catch (e: Exception) {
            _characterBindings.value = previous
            throw e
        }
    }

    suspend fun attachFromLibrary(characterId: Uuid, lorebookId: Uuid): CharacterLorebookBindingDto {
        val newBinding = characterRpcService.attachLorebook(
            AttachLorebookRequest(
                characterId = characterId,
                lorebookId = lorebookId,
                isEnabled = true,
                isPrimary = _characterBindings.value.isEmpty()
            )
        )
        _characterBindings.update { it + newBinding }
        return newBinding
    }

    suspend fun createAndAttachBook(
        characterId: Uuid,
        name: String,
        description: String?,
        isPublic: Boolean
    ) {
        characterRpcService.createStandaloneLorebook(
            CreateStandaloneLorebookRequest(
                name = name,
                description = description,
                isPublic = isPublic,
                attachToCharacterId = characterId
            )
        )
        loadCharacterLorebooks(characterId)
    }

    fun notifyLorebookMetadataUpdated(
        lorebookId: Uuid,
        name: String,
        description: String?,
        isPublic: Boolean,
        avatarUrl: String? = null
    ) {
        _characterBindings.update { list ->
            list.map {
                if (it.lorebookId == lorebookId) {
                    it.copy(name = name, description = description, isPublic = isPublic, avatarUrl = avatarUrl)
                } else it
            }
        }
        _userLibrary.update { list ->
            list.map {
                if (it.id == lorebookId) {
                    it.copy(name = name, description = description, isPublic = isPublic, avatarUrl = avatarUrl)
                } else it
            }
        }
    }

    fun notifyEntriesCountChanged(lorebookId: Uuid, delta: Int) {
        _characterBindings.update { list ->
            list.map {
                if (it.lorebookId == lorebookId) {
                    it.copy(entriesCount = (it.entriesCount + delta).coerceAtLeast(0))
                } else it
            }
        }
        _userLibrary.update { list ->
            list.map {
                if (it.id == lorebookId) {
                    it.copy(entriesCount = (it.entriesCount + delta).coerceAtLeast(0))
                } else it
            }
        }
    }

    suspend fun saveLorebookToLibrary(lorebookId: Uuid) {
        characterRpcService.saveLorebookToLibrary(lorebookId)
        loadUserLibrary()
    }

    suspend fun removeLorebookFromLibrary(lorebookId: Uuid) {
        characterRpcService.removeLorebookFromLibrary(lorebookId)
        _userLibrary.update { list -> list.filterNot { it.id == lorebookId } }
    }

    suspend fun deleteLorebook(lorebookId: Uuid) {
        characterRpcService.deleteLorebook(lorebookId)
        _userLibrary.update { list -> list.filterNot { it.id == lorebookId } }
        _characterBindings.update { list -> list.filterNot { it.lorebookId == lorebookId } }
    }
}
