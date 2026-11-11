package com.ensodai.avalon.presentation.relationshipRoadScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun RelationshipRoadScreen(
    navigation: Navigation,
    characterId: Uuid,
    viewModel: RelationshipRoadScreenViewModel = koinViewModel {
        parametersOf(characterId)
    }
) {
    val actions = remember { viewModel.actions }
    val viewState by viewModel.viewState.collectAsState()

    RelationshipRoadScreenContent(
        actions = actions,
        viewState = viewState,
        navigation = navigation
    )
}
