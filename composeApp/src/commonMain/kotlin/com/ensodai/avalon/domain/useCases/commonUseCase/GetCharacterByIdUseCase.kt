package com.ensodai.avalon.domain.useCases.commonUseCase

import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.avalon.data.db.models.character.CharacterDetailsWithConfigs
import com.ensodai.avalon.domain.models.chat.ChatCharacterUi
import com.ensodai.avalon.domain.models.chat.WelcomeColors
import com.ensodai.avalon.domain.models.chat.WelcomeInfo
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetCharacterByIdUseCase(
    private val localCharacterRepository: LocalCharacterRepository,
    private val characterRpcService: CharacterRpcService,
    private val characterSaver: CharacterSaver
) {

    operator fun invoke(characterId: Uuid?): Flow<ChatCharacterUi?> {
        if (characterId == null) return flowOf(null)

        return channelFlow {
            val producerScope = this

            val localJob = producerScope.launch {
                localCharacterRepository.getCharacterWithDetailsFlow(characterId)
                    .collect { tuple ->
                        if (tuple.characterDetails == null) {
                            send(null)
                        } else {
                            send(tuple.toUiModel())
                        }
                    }
            }

            producerScope.launch {
                runCatching {
                    characterRpcService.getCharacter(characterPublicId = characterId)
                }.fold(
                    onSuccess = { character ->
                        characterSaver.save(character)
                    },
                    onFailure = {
                        it.printStackTrace()
                    }
                )
            }

            localJob.join()
        }
    }

    private fun CharacterDetailsWithConfigs.toUiModel(): ChatCharacterUi? {
        val character = this.characterDetails ?: return null
        val availableLlmConfigs = this.availableConfigs
        val selectedConfig = when {
            this.selectedLlmConfig != null -> this.selectedLlmConfig
            this.availableConfigs.isEmpty() -> AvailableLlmConfigs(
                codeName = "default",
                displayName = "default",
                price = 1
            )

            else -> availableLlmConfigs.first()
        }
        return ChatCharacterUi(
            id = character.character.id,
            name = character.character.name,
            avatarUrl = character.avatar?.imageUrl,
            firstMessage = character.character.firstMessage,
            welcomeInfo = if (character.welcomeCard != null) {
                WelcomeInfo(
                    description = character.welcomeCard.description,
                    imageUrls = character.welcomeImages.map {
                        WelcomeCardImageDto(
                            imagePublicId = it.id,
                            url = it.imageUrl,
                            thumbHash = it.thumbHash
                        )
                    },
                    colors = WelcomeColors(
                        start = character.welcomeCard.colorStart,
                        end = character.welcomeCard.colorEnd,
                        light = character.welcomeCard.colorLight,
                        isLightEnabled = character.welcomeCard.isEnableLight
                    )
                )
            } else null,
            availableLlmConfigs = availableLlmConfigs,
            selectedConfig = selectedConfig
        )
    }
}