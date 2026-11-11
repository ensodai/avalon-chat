@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.avalon.presentation.userCharactersScreen.viewState

import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.presentation.core.LoadingState
import kotlin.uuid.Uuid

data class UserCharacterScreenViewState(
    val loadingState: LoadingState = LoadingState.Idle,
    val claimingCharacterId: Uuid? = null,
    val claimSuccessEvent: Pair<Uuid, Int>? = null
) : BaseViewState()