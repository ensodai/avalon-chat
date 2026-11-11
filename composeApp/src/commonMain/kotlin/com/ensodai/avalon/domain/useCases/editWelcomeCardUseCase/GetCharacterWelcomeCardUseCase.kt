package com.ensodai.avalon.domain.useCases.editWelcomeCardUseCase

import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.domain.useCases.commonUseCase.WelcomeCardConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetCharacterWelcomeCardUseCase(
    private val localCharacterRepository: LocalCharacterRepository,
) {

    fun get(characterId: Uuid): Flow<WelcomeCardConfig?> {
        return localCharacterRepository.getCharacterWelcomeCardUseCase(characterId)
            .map { container ->

                if (container == null) return@map null

                val imageDtos = container.images.map { imageEntity ->
                    WelcomeCardImageDto(
                        imagePublicId = imageEntity.id,
                        url = imageEntity.imageUrl,
                        thumbHash = imageEntity.thumbHash
                    )
                }
                WelcomeCardConfig(
                    welcomeCardImages = imageDtos,
                    welcomeCardDescription = container.card.description,
                    startColor = container.card.colorStart,
                    endColor = container.card.colorEnd,
                    colorLight = container.card.colorLight,
                    isEnableLight = container.card.isEnableLight
                )
            }
    }

}