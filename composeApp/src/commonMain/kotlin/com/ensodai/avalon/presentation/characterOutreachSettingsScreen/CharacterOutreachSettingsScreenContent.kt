package com.ensodai.avalon.presentation.characterOutreachSettingsScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.outreach_settings_allow_label
import com.ensodai.avalon.generated.resources.outreach_settings_section_desc
import com.ensodai.avalon.generated.resources.outreach_settings_slot_afternoon
import com.ensodai.avalon.generated.resources.outreach_settings_slot_evening
import com.ensodai.avalon.generated.resources.outreach_settings_slot_morning
import com.ensodai.avalon.generated.resources.outreach_settings_title
import com.ensodai.avalon.generated.resources.save
import com.ensodai.avalon.presentation.characterOutreachSettingsScreen.action.CharacterOutreachSettingsActions
import com.ensodai.avalon.presentation.characterOutreachSettingsScreen.component.OutreachSlotCard
import com.ensodai.avalon.presentation.characterOutreachSettingsScreen.viewState.CharacterOutreachSettingsViewState
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterOutreachSettingsScreenContent(
    viewState: CharacterOutreachSettingsViewState,
    actions: CharacterOutreachSettingsActions,
    navigation: Navigation
) {
    val colors = AvalonTheme.colors

    Scaffold(
        topBar = {
            GemIslandTopBar(
                title = {
                    Text(
                        text = stringResource(Res.string.outreach_settings_title),
                        color = colors.textMain,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    NavigationBackButton(onBackButtonClick = { navigation.navigateBack() })
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp)
            ) {
                TechnoButton(
                    text = stringResource(Res.string.save),
                    onClick = { actions.onSave() },
                    isPrimary = true,
                    isLoading = viewState.isSaving,
                    enabled = !viewState.isLoading && !viewState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        if (viewState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.neonAccent)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Отображение ошибки, если она есть
                if (viewState.errorMessage != null) {
                    item {
                        Text(
                            text = viewState.errorMessage,
                            color = colors.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )
                    }
                }

                // Общий тумблер активности
                item {
                    GemCard(
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.outreach_settings_allow_label),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.textMain
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(Res.string.outreach_settings_section_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Switch(
                                checked = viewState.isOutreachEnabled,
                                onCheckedChange = { actions.onToggleOutreach(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.background,
                                    checkedTrackColor = colors.neonAccent,
                                    uncheckedThumbColor = colors.textSecondary,
                                    uncheckedTrackColor = colors.cardBackground
                                )
                            )
                        }
                    }
                }

                // Слот 1: Утро
                item {
                    OutreachSlotCard(
                        title = stringResource(Res.string.outreach_settings_slot_morning),
                        isEnabled = viewState.outreachMorningEnabled,
                        onToggleSlot = { actions.onToggleMorningSlot(it) },
                        promptValue = viewState.outreachMorningPrompt,
                        onPromptChange = { actions.onMorningPromptChange(it) },
                        isError = viewState.outreachMorningPrompt.length > 250,
                        parentEnabled = viewState.isOutreachEnabled
                    )
                }

                // Слот 2: День
                item {
                    OutreachSlotCard(
                        title = stringResource(Res.string.outreach_settings_slot_afternoon),
                        isEnabled = viewState.outreachAfternoonEnabled,
                        onToggleSlot = { actions.onToggleAfternoonSlot(it) },
                        promptValue = viewState.outreachAfternoonPrompt,
                        onPromptChange = { actions.onAfternoonPromptChange(it) },
                        isError = viewState.outreachAfternoonPrompt.length > 250,
                        parentEnabled = viewState.isOutreachEnabled
                    )
                }

                // Слот 3: Вечер
                item {
                    OutreachSlotCard(
                        title = stringResource(Res.string.outreach_settings_slot_evening),
                        isEnabled = viewState.outreachEveningEnabled,
                        onToggleSlot = { actions.onToggleEveningSlot(it) },
                        promptValue = viewState.outreachEveningPrompt,
                        onPromptChange = { actions.onEveningPromptChange(it) },
                        isError = viewState.outreachEveningPrompt.length > 250,
                        parentEnabled = viewState.isOutreachEnabled
                    )
                }
            }
        }
    }
}
