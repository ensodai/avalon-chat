package com.ensodai.avalon.presentation.addCharacterScreen.viewState

sealed class CreateStages {

    data object NameStage : CreateStages()

    data object VisibleStage : CreateStages()

    data object AvatarStage : CreateStages()

    data object ShortDescriptionStage : CreateStages()

    data object TemperatureStage : CreateStages()

    data object CategoriesStage : CreateStages()

    data object FullDescriptionStage : CreateStages()

    data object ScenarioStage : CreateStages()

    data object MesExampleStage : CreateStages()

    data object LorebookStage : CreateStages()

    data object FirstMessageStage : CreateStages()

    data object PublishCharacterStage : CreateStages()

    data object HelloCardStage : CreateStages()

    data object OutreachStage : CreateStages()
}