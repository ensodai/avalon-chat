package com.ensodai.avalon.presentation.editCharacterTextScreen

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.full_description
import com.ensodai.avalon.generated.resources.mes_example_stage_desc
import com.ensodai.avalon.generated.resources.scenario_stage_desc
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editCharacterTextScreen.action.EditCharacterTextScreenActions
import com.ensodai.avalon.presentation.editCharacterTextScreen.component.TopBarEditCharacterTextScreen
import com.ensodai.avalon.presentation.editCharacterTextScreen.model.CharacterTextType
import com.ensodai.avalon.presentation.editCharacterTextScreen.viewState.EditCharacterTextScreenViewState
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.AvalonTheme
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource

private const val MAX_TEXT_LENGTH = 21000

@Composable
fun EditCharacterTextScreenContent(
    viewState: EditCharacterTextScreenViewState,
    actions: EditCharacterTextScreenActions,
    navigation: Navigation,
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {
    val colors = AvalonTheme.colors
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    var isKeyboardEnabled by remember { mutableStateOf(false) }
    var textFieldValue by remember(viewState.text) {
        mutableStateOf(
            TextFieldValue(
                text = viewState.text,
                selection = TextRange(viewState.text.length)
            )
        )
    }

    LaunchedEffect(viewState.loadingState) {
        when (viewState.loadingState) {
            LoadingState.Error -> {
                onEnableSwipeable()
            }

            LoadingState.Idle -> {}
            LoadingState.Loading -> {
                onDisableSwipeable()
            }

            LoadingState.Success -> {
                onEnableSwipeable()
                navigation.navigateBack()
            }
        }
    }

    AvalonBackHandler(viewState.loadingState == LoadingState.Loading) {
        if (viewState.loadingState != LoadingState.Loading) {
            navigation.navigateBack()
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            if (interaction is PressInteraction.Release) {
                isKeyboardEnabled = true
            }
        }
    }

    val placeholderText = when (viewState.textType) {
        CharacterTextType.PERSONALITY -> stringResource(Res.string.full_description)
        CharacterTextType.SCENARIO -> stringResource(Res.string.scenario_stage_desc)
        CharacterTextType.MES_EXAMPLE -> stringResource(Res.string.mes_example_stage_desc)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBarEditCharacterTextScreen(
                modifier = Modifier.fillMaxWidth(),
                textType = viewState.textType,
                characterVisibility = viewState.characterVisibility,
                isLoading = viewState.loadingState == LoadingState.Loading,
                onBackButtonClick = {
                    if (viewState.loadingState != LoadingState.Loading) {
                        navigation.navigateBack()
                    }
                },
                onDoneButtonClick = {
                    actions.onSaveText(textFieldValue.text)
                },
                onChangeCharacterVisibility = { actions.onChangeVisibility(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            TextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    if (newValue.text.length <= MAX_TEXT_LENGTH) {
                        textFieldValue = newValue
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .focusRequester(focusRequester),
                readOnly = !isKeyboardEnabled,
                interactionSource = interactionSource,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = colors.textMain,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                enabled = viewState.loadingState != LoadingState.Loading,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = colors.neonAccent
                ),
                placeholder = {
                    Text(
                        text = placeholderText,
                        color = colors.placeholderText
                    )
                }
            )

            Text(
                text = "${textFieldValue.text.length}/$MAX_TEXT_LENGTH",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .imePadding()
                    .padding(16.dp),
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
