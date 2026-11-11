package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.cancel
import com.ensodai.avalon.generated.resources.change_character_description
import com.ensodai.avalon.generated.resources.change_character_first_message
import com.ensodai.avalon.generated.resources.change_character_mes_example
import com.ensodai.avalon.generated.resources.change_character_name
import com.ensodai.avalon.generated.resources.change_character_privacy
import com.ensodai.avalon.generated.resources.change_character_scenario
import com.ensodai.avalon.generated.resources.character_name
import com.ensodai.avalon.generated.resources.description
import com.ensodai.avalon.generated.resources.interaction_format_dialog_title
import com.ensodai.avalon.generated.resources.mes_example_stage_desc
import com.ensodai.avalon.generated.resources.outreach_setting_dialog_title
import com.ensodai.avalon.generated.resources.save
import com.ensodai.avalon.generated.resources.scenario_stage_desc
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.commonComponents.privacyCards.InteractionFormatCardColumn
import com.ensodai.avalon.presentation.commonComponents.privacyCards.PrivacyCardColumn
import com.ensodai.avalon.presentation.commonComponents.textFields.TechnoInput
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editCharacterScreen.action.BottomSheetActions
import com.ensodai.avalon.presentation.editCharacterScreen.action.BottomSheetEditActions
import com.ensodai.avalon.presentation.editCharacterScreen.viewState.BottomSheetEditCharacterScreen
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.utils.EMPTY_STRING
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomSheetEditCharacterScreen(
    state: BottomSheetEditCharacterScreen,
    bottomSheetActions: BottomSheetActions,
    loadingState: LoadingState,
    bottomSheetEditActions: BottomSheetEditActions,
    characterName: String,
    characterDescription: String,
    characterVisibility: CharacterVisibility,
    firstMessage: String?,
    isOutreachEnabled: Boolean,
    interactionFormat: InteractionFormat,
    scenario: String?,
    mesExample: String?
) {

    when (state) {
        BottomSheetEditCharacterScreen.None -> {}
        BottomSheetEditCharacterScreen.EditNameBottomSheet -> {
            EditNameBottomSheet(
                bottomSheetActions = bottomSheetActions,
                loadingState = loadingState,
                bottomSheetEditActions = bottomSheetEditActions,
                characterName = characterName
            )
        }

        BottomSheetEditCharacterScreen.EditDescriptionBottomSheet -> {
            EditDescriptionBottomSheet(
                bottomSheetActions = bottomSheetActions,
                loadingState = loadingState,
                bottomSheetEditActions = bottomSheetEditActions,
                characterDescription = characterDescription
            )
        }

        BottomSheetEditCharacterScreen.EditScenarioBottomSheet -> {
            EditScenarioBottomSheet(
                bottomSheetActions = bottomSheetActions,
                loadingState = loadingState,
                bottomSheetEditActions = bottomSheetEditActions,
                scenario = scenario
            )
        }

        BottomSheetEditCharacterScreen.EditMesExampleBottomSheet -> {
            EditMesExampleBottomSheet(
                bottomSheetActions = bottomSheetActions,
                loadingState = loadingState,
                bottomSheetEditActions = bottomSheetEditActions,
                mesExample = mesExample
            )
        }

        BottomSheetEditCharacterScreen.EditFirstMessageBottomSheet -> {
            EditFirstMessageBottomSheet(
                bottomSheetActions = bottomSheetActions,
                loadingState = loadingState,
                bottomSheetEditActions = bottomSheetEditActions,
                firstMessage = firstMessage ?: EMPTY_STRING
            )
        }

        BottomSheetEditCharacterScreen.EditVisibilityBottomSheet -> {
            EditCharacterVisibilityBottomSheet(
                bottomSheetActions = bottomSheetActions,
                loadingState = loadingState,
                bottomSheetEditActions = bottomSheetEditActions,
                characterVisibility = characterVisibility
            )
        }

        BottomSheetEditCharacterScreen.EditOutreachBottomSheet -> {
            EditCharacterOutreachBottomSheet(
                bottomSheetActions = bottomSheetActions,
                loadingState = loadingState,
                bottomSheetEditActions = bottomSheetEditActions,
                isOutreachEnabled = isOutreachEnabled
            )
        }

        BottomSheetEditCharacterScreen.EditInteractionFormatBottomSheet -> {
            EditCharacterInteractionFormatBottomSheet(
                bottomSheetActions = bottomSheetActions,
                loadingState = loadingState,
                bottomSheetEditActions = bottomSheetEditActions,
                interactionFormat = interactionFormat
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNameBottomSheet(
    bottomSheetActions: BottomSheetActions,
    loadingState: LoadingState,
    bottomSheetEditActions: BottomSheetEditActions,
    characterName: String
) {
    val colors = AvalonTheme.colors
    var text by remember { mutableStateOf(characterName) }

    val isValid = text.length in 3..30

    ModalBottomSheet(
        onDismissRequest = { bottomSheetActions.onCloseBottomSheet() },
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {

            Text(
                text = stringResource(Res.string.change_character_name),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textMain,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            TechnoInput(
                value = text,
                onValueChange = { text = it },
                placeholder = stringResource(Res.string.character_name),
                maxLength = 30,
                isError = text.length in 0..2,
                enabled = loadingState != LoadingState.Loading
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TechnoButton(
                    text = stringResource(Res.string.cancel),
                    onClick = { bottomSheetActions.onCloseBottomSheet() },
                    isPrimary = false,
                    modifier = Modifier.weight(1f),
                    enabled = loadingState != LoadingState.Loading
                )

                TechnoButton(
                    text = stringResource(Res.string.save),
                    onClick = { bottomSheetEditActions.onUpdateCharacterName(text) },
                    isPrimary = true,
                    isLoading = loadingState == LoadingState.Loading,
                    modifier = Modifier.weight(1f),
                    enabled = isValid && loadingState != LoadingState.Loading
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDescriptionBottomSheet(
    bottomSheetActions: BottomSheetActions,
    loadingState: LoadingState,
    bottomSheetEditActions: BottomSheetEditActions,
    characterDescription: String
) {
    val colors = AvalonTheme.colors
    var text by remember { mutableStateOf(characterDescription) }

    val isValid = text.length in 3..300

    ModalBottomSheet(
        onDismissRequest = { bottomSheetActions.onCloseBottomSheet() },
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {

            Text(
                text = stringResource(Res.string.change_character_description),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textMain,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            TechnoInput(
                value = text,
                onValueChange = { text = it },
                placeholder = stringResource(Res.string.description),
                maxLength = 300,
                isError = text.length in 0..2,
                enabled = loadingState != LoadingState.Loading,
                maxLines = 6
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TechnoButton(
                    text = stringResource(Res.string.cancel),
                    onClick = { bottomSheetActions.onCloseBottomSheet() },
                    isPrimary = false,
                    modifier = Modifier.weight(1f),
                    enabled = loadingState != LoadingState.Loading
                )

                TechnoButton(
                    text = stringResource(Res.string.save),
                    onClick = { bottomSheetEditActions.onUpdateCharacterDescription(text) },
                    isPrimary = true,
                    isLoading = loadingState == LoadingState.Loading,
                    modifier = Modifier.weight(1f),
                    enabled = isValid && loadingState != LoadingState.Loading
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFirstMessageBottomSheet(
    bottomSheetActions: BottomSheetActions,
    loadingState: LoadingState,
    bottomSheetEditActions: BottomSheetEditActions,
    firstMessage: String
) {
    val colors = AvalonTheme.colors
    var text by remember { mutableStateOf(firstMessage) }

    ModalBottomSheet(
        onDismissRequest = { bottomSheetActions.onCloseBottomSheet() },
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {

            Text(
                text = stringResource(Res.string.change_character_first_message),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textMain,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            TechnoInput(
                value = text,
                onValueChange = { text = it },
                placeholder = stringResource(Res.string.description),
                maxLength = 600,
                enabled = loadingState != LoadingState.Loading,
                maxLines = 6
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TechnoButton(
                    text = stringResource(Res.string.cancel),
                    onClick = { bottomSheetActions.onCloseBottomSheet() },
                    isPrimary = false,
                    modifier = Modifier.weight(1f),
                    enabled = loadingState != LoadingState.Loading
                )

                TechnoButton(
                    text = stringResource(Res.string.save),
                    onClick = { bottomSheetEditActions.onUpdateCharacterFirstMessage(text) },
                    isPrimary = true,
                    isLoading = loadingState == LoadingState.Loading,
                    modifier = Modifier.weight(1f),
                    enabled = loadingState != LoadingState.Loading
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCharacterVisibilityBottomSheet(
    bottomSheetActions: BottomSheetActions,
    loadingState: LoadingState,
    bottomSheetEditActions: BottomSheetEditActions,
    characterVisibility: CharacterVisibility
) {
    val colors = AvalonTheme.colors
    var currentVisibility by remember { mutableStateOf(characterVisibility) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { bottomSheetActions.onCloseBottomSheet() },
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {

            Text(
                text = stringResource(Res.string.change_character_privacy),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textMain,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            PrivacyCardColumn(
                selectedOption = currentVisibility,
                onOptionSelected = { currentVisibility = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                TechnoButton(
                    text = stringResource(Res.string.cancel),
                    onClick = { bottomSheetActions.onCloseBottomSheet() },
                    isPrimary = false,
                    modifier = Modifier.weight(1f),
                    enabled = loadingState != LoadingState.Loading
                )

                TechnoButton(
                    text = stringResource(Res.string.save),
                    onClick = { bottomSheetEditActions.onUpdateCharacterVisibility(currentVisibility) },
                    isPrimary = true,
                    isLoading = loadingState == LoadingState.Loading,
                    modifier = Modifier.weight(1f),
                    enabled = loadingState != LoadingState.Loading
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCharacterOutreachBottomSheet(
    bottomSheetActions: BottomSheetActions,
    loadingState: LoadingState,
    bottomSheetEditActions: BottomSheetEditActions,
    isOutreachEnabled: Boolean
) {
    val colors = AvalonTheme.colors
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { bottomSheetActions.onCloseBottomSheet() },
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {

            Text(
                text = stringResource(Res.string.outreach_setting_dialog_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textMain,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            com.ensodai.avalon.presentation.commonComponents.privacyCards.OutreachCardColumn(
                isOutreachEnabled = isOutreachEnabled,
                onOptionSelected = { selected ->
                    if (loadingState != LoadingState.Loading) {
                        bottomSheetEditActions.onUpdateCharacterOutreach(selected)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCharacterInteractionFormatBottomSheet(
    bottomSheetActions: BottomSheetActions,
    loadingState: LoadingState,
    bottomSheetEditActions: BottomSheetEditActions,
    interactionFormat: InteractionFormat
) {
    val colors = AvalonTheme.colors
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { bottomSheetActions.onCloseBottomSheet() },
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {

            Text(
                text = stringResource(Res.string.interaction_format_dialog_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textMain,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            InteractionFormatCardColumn(
                selectedFormat = interactionFormat,
                onOptionSelected = { selected ->
                    if (loadingState != LoadingState.Loading) {
                        bottomSheetEditActions.onUpdateCharacterInteractionFormat(selected)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScenarioBottomSheet(
    bottomSheetActions: BottomSheetActions,
    loadingState: LoadingState,
    bottomSheetEditActions: BottomSheetEditActions,
    scenario: String?
) {
    val colors = AvalonTheme.colors
    var text by remember { mutableStateOf(scenario ?: EMPTY_STRING) }

    ModalBottomSheet(
        onDismissRequest = { bottomSheetActions.onCloseBottomSheet() },
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = stringResource(Res.string.change_character_scenario),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textMain,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            TechnoInput(
                value = text,
                onValueChange = { text = it },
                placeholder = stringResource(Res.string.scenario_stage_desc),
                maxLength = 2000,
                enabled = loadingState != LoadingState.Loading,
                maxLines = 8
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TechnoButton(
                    text = stringResource(Res.string.cancel),
                    onClick = { bottomSheetActions.onCloseBottomSheet() },
                    isPrimary = false,
                    modifier = Modifier.weight(1f),
                    enabled = loadingState != LoadingState.Loading
                )

                TechnoButton(
                    text = stringResource(Res.string.save),
                    onClick = { bottomSheetEditActions.onUpdateCharacterScenario(text.ifBlank { null }) },
                    isPrimary = true,
                    isLoading = loadingState == LoadingState.Loading,
                    modifier = Modifier.weight(1f),
                    enabled = loadingState != LoadingState.Loading
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMesExampleBottomSheet(
    bottomSheetActions: BottomSheetActions,
    loadingState: LoadingState,
    bottomSheetEditActions: BottomSheetEditActions,
    mesExample: String?
) {
    val colors = AvalonTheme.colors
    var text by remember { mutableStateOf(mesExample ?: EMPTY_STRING) }

    ModalBottomSheet(
        onDismissRequest = { bottomSheetActions.onCloseBottomSheet() },
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = stringResource(Res.string.change_character_mes_example),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textMain,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            TechnoInput(
                value = text,
                onValueChange = { text = it },
                placeholder = stringResource(Res.string.mes_example_stage_desc),
                maxLength = 4000,
                enabled = loadingState != LoadingState.Loading,
                maxLines = 10
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TechnoButton(
                    text = stringResource(Res.string.cancel),
                    onClick = { bottomSheetActions.onCloseBottomSheet() },
                    isPrimary = false,
                    modifier = Modifier.weight(1f),
                    enabled = loadingState != LoadingState.Loading
                )

                TechnoButton(
                    text = stringResource(Res.string.save),
                    onClick = { bottomSheetEditActions.onUpdateCharacterMesExample(text.ifBlank { null }) },
                    isPrimary = true,
                    isLoading = loadingState == LoadingState.Loading,
                    modifier = Modifier.weight(1f),
                    enabled = loadingState != LoadingState.Loading
                )
            }
        }
    }
}

