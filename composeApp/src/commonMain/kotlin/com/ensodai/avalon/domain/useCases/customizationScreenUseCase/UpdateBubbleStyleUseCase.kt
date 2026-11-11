package com.ensodai.avalon.domain.useCases.customizationScreenUseCase

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import com.ensodai.avalon.theme.models.BubbleShapeType
import org.koin.core.annotation.Factory

@Factory
class UpdateBubbleStyleUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend operator fun invoke(fontSize: Int, shapeType: BubbleShapeType, cornerRadius: Int) {
        appSettingsRepository.updateBubbleStyle(
            fontSize = fontSize.coerceIn(12, 30),
            shapeType = shapeType,
            cornerRadius = cornerRadius.coerceIn(4, 16)
        )
    }
}
