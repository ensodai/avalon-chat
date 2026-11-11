package com.ensodai.avalon.domain.useCases.customizationScreenUseCase

import com.ensodai.avalon.theme.models.AvalonThemePreset
import org.koin.core.annotation.Factory

@Factory
class GetAllThemePresetsUseCase {
    operator fun invoke(): List<AvalonThemePreset> {
        return AvalonThemePreset.entries
    }
}
