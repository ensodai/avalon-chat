package com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen

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
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.action.EditCharacterFullDescriptionScreenActions
import com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.component.TopBarEditCharacterFullDescriptionScreen
import com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.viewState.EditCharacterFullDescriptionScreenViewState
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.AvalonTheme
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditCharacterFullDescriptionScreenContent(
    viewState: EditCharacterFullDescriptionScreenViewState,
    actions: EditCharacterFullDescriptionScreenActions,
    navigation: Navigation,
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {
    val colors = AvalonTheme.colors
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    var isKeyboardEnabled by remember { mutableStateOf(false) }
    var textFieldValue by remember(viewState.characterFullDescription) {
        mutableStateOf(
            TextFieldValue(
                text = viewState.characterFullDescription,
                selection = TextRange(0)
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBarEditCharacterFullDescriptionScreen(
                modifier = Modifier
                    .fillMaxWidth(),
                isLoading = viewState.loadingState == LoadingState.Loading,
                onBackButtonClick = {
                    if (viewState.loadingState != LoadingState.Loading) {
                        navigation.navigateBack()
                    }
                },
                onDoneButtonClick = {
                    actions.onUpdateCharacterFullDescription(textFieldValue.text)
                },
                characterVisibility = viewState.characterVisibility,
                onChangeCharacterVisibility = { actions.onUpdateFullDescriptionVisibility(it) }
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
                    if (newValue.text.length <= 21000) {
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
                        text = stringResource(Res.string.full_description),
                        color = colors.placeholderText
                    )
                }
            )

            Text(
                text = "${textFieldValue.text.length}/21000",
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
