package com.ensodai.avalon.presentation.relationshipRoadScreen.viewState

import com.ensodai.shared.avalon.character.models.CharacterEventRoadmapDto
import com.ensodai.avalon.presentation.core.BaseViewState
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class RelationshipRoadScreenViewState(
    val characterId: Uuid? = null,
    val isLoading: Boolean = false,
    val relationshipLevel: Int = 1,
    val currentExperience: Long = 0L,
    val roadmapItems: List<CharacterEventRoadmapDto> = emptyList(),
    val errorMessage: String? = null
) : BaseViewState()
