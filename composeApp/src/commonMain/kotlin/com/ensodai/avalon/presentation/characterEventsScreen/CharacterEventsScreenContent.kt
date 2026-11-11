package com.ensodai.avalon.presentation.characterEventsScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.*
import com.ensodai.avalon.presentation.characterEventsScreen.action.CharacterEventsScreenActions
import com.ensodai.avalon.presentation.characterEventsScreen.component.CharacterEventsErrorCard
import com.ensodai.avalon.presentation.characterEventsScreen.component.CharacterEventItemCard
import com.ensodai.avalon.presentation.characterEventsScreen.component.CharacterEventsInfoCard
import com.ensodai.avalon.presentation.characterEventsScreen.viewState.CharacterEventsScreenViewState
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.AvalonTheme
import kotlin.uuid.ExperimentalUuidApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CharacterEventsScreenContent(
    actions: CharacterEventsScreenActions,
    viewState: CharacterEventsScreenViewState,
    navigation: Navigation
) {
    val colors = AvalonTheme.colors

    LaunchedEffect(viewState.saveSuccess) {
        if (viewState.saveSuccess) {
            navigation.navigateBack()
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            GemIslandTopBar(
                title = {
                    Text(
                        text = stringResource(Res.string.character_events_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textMain
                    )
                },
                navigationIcon = {
                    NavigationBackButton(
                        onBackButtonClick = { navigation.navigateBack() }
                    )
                }
            )
        },
        bottomBar = {
        if (!viewState.isLoading) {
                Surface(
                    color = colors.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        TechnoButton(
                            text = stringResource(Res.string.save_changes),
                            onClick = { actions.onSave() },
                            isPrimary = true,
                            isLoading = viewState.isSaving,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        containerColor = colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (viewState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.neonAccent)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Информационный блок
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        CharacterEventsInfoCard()
                    }

                    // Сообщение об ошибке
                    if (viewState.errorMessage != null) {
                        item {
                            CharacterEventsErrorCard(errorMessage = viewState.errorMessage)
                        }
                    }

                    // Список событий
                    if (viewState.events.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(Res.string.character_events_empty_state),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        itemsIndexed(viewState.events) { index, event ->
                            CharacterEventItemCard(
                                event = event,
                                index = index,
                                allEvents = viewState.events,
                                onDeleteEvent = actions.onDeleteEvent,
                                onRequiredLevelChanged = actions.onRequiredLevelChanged,
                                onEventNameChanged = actions.onEventNameChanged,
                                onTriggerScenarioChanged = actions.onTriggerScenarioChanged,
                                onPermanentTraitChanged = actions.onPermanentTraitChanged
                            )
                        }
                    }

                    // Кнопка добавления события
                    item {
                        val isAddButtonEnabled = viewState.events.size < 13
                        TechnoButton(
                            text = stringResource(Res.string.character_events_add_button),
                            onClick = { actions.onAddEvent() },
                            isPrimary = false,
                            enabled = isAddButtonEnabled,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}