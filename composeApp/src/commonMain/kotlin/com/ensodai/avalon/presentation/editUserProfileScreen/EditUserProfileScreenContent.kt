package com.ensodai.avalon.presentation.editUserProfileScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.ensodai.avalon.domain.useCases.editUserProfileScreenUseCase.NicknameValidator
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.cgange_user_name
import com.ensodai.avalon.generated.resources.nickname_rules
import com.ensodai.avalon.generated.resources.user_name
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.editUserProfileScreen.actions.EditUserProfileScreenActions
import com.ensodai.avalon.presentation.editUserProfileScreen.viewState.EditUserProfileScreenViewState
import com.ensodai.avalon.presentation.editUserProfileScreen.viewState.LoadingStateChangeNickName
import com.ensodai.avalon.presentation.editUserProfileScreen.viewState.isErrorNickName
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserProfileScreenContent(
    viewState: EditUserProfileScreenViewState,
    navigation: Navigation,
    actions: EditUserProfileScreenActions,
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {
    val colors = AvalonTheme.colors

    LaunchedEffect(viewState.loadingState) {
        when (viewState.loadingState) {
            LoadingStateChangeNickName.Error -> {
                onEnableSwipeable()
            }

            LoadingStateChangeNickName.Idle -> {
                onEnableSwipeable()
            }

            LoadingStateChangeNickName.Loading -> {
                onDisableSwipeable()
            }

            LoadingStateChangeNickName.Success -> {
                onEnableSwipeable()
                navigation.navigateBack()
            }
        }
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            GemIslandTopBar(
                title = {
                    Text(
                        text = stringResource(Res.string.user_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    NavigationBackButton(
                        onBackButtonClick = {
                            if (viewState.loadingState != LoadingStateChangeNickName.Loading) {
                                navigation.navigateBack()
                            }
                        }
                    )
                },
                actions = {
                    IconButton(
                        onClick = { actions.onSaveNickName() },
                        enabled = viewState.validationResult is NicknameValidator.ValidationResult.Available
                    ) {
                        Icon(
                            imageVector = IconsManager.Check,
                            contentDescription = "Done"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.cgange_user_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = viewState.nickName,
                    onValueChange = { newValue ->
                        actions.onChangeNickName(newValue)
                    },
                    shape = RoundedCornerShape(8.dp),
                    minLines = 1,
                    maxLines = 1,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.surfaceHigh,
                        unfocusedContainerColor = colors.surfaceHigh,
                        cursorColor = colors.neonAccent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = colors.error,
                        errorContainerColor = colors.surfaceHigh,
                        errorCursorColor = colors.neonAccent,
                    ),
                    placeholder = {
                        Text(
                            text = stringResource(Res.string.user_name),
                            color = colors.placeholderText,
                        )
                    },
                    isError = viewState.isErrorNickName,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    enabled = viewState.loadingState != LoadingStateChangeNickName.Loading
                )

                Spacer(modifier = Modifier.height(4.dp))

                when (viewState.validationResult) {
                    NicknameValidator.ValidationResult.Available -> {
                        AvailableNicknameText(viewState.validationMessage)
                    }

                    NicknameValidator.ValidationResult.CheckingAvailability -> {
                        LoadingServerValidation(viewState.validationMessage)
                    }

                    NicknameValidator.ValidationResult.ConsecutiveSpecialChars -> {
                        ErrorValidationResultText(viewState.validationMessage)
                    }

                    NicknameValidator.ValidationResult.Empty -> {
                        ErrorValidationResultText(viewState.validationMessage)
                    }

                    NicknameValidator.ValidationResult.Inappropriate -> {
                        ErrorValidationResultText(viewState.validationMessage)
                    }

                    NicknameValidator.ValidationResult.InvalidCharacters -> {
                        ErrorValidationResultText(viewState.validationMessage)
                    }

                    NicknameValidator.ValidationResult.InvalidStartOrEnd -> {
                        ErrorValidationResultText(viewState.validationMessage)
                    }

                    NicknameValidator.ValidationResult.NetworkError -> {
                        ErrorValidationResultText(viewState.validationMessage)
                    }

                    NicknameValidator.ValidationResult.SystemName -> {
                        ErrorValidationResultText(viewState.validationMessage)
                    }

                    NicknameValidator.ValidationResult.Taken -> {
                        ErrorValidationResultText(viewState.validationMessage)
                    }

                    NicknameValidator.ValidationResult.TooLong -> {
                        ErrorValidationResultText(viewState.validationMessage)
                    }

                    NicknameValidator.ValidationResult.TooShort -> {
                        ErrorValidationResultText(viewState.validationMessage)
                    }
                }
                Text(
                    text = stringResource(Res.string.nickname_rules),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorValidationResultText(text: String) {
    Text(
        text = text,
        color = AvalonTheme.colors.error,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
    )
}

@Composable
private fun AvailableNicknameText(text: String) {
    Text(
        text = text,
        color = AvalonTheme.colors.successAccent,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
    )
}

@Composable
private fun LoadingServerValidation(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            color = AvalonTheme.colors.strokeSubtle,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
        )
        Spacer(modifier = Modifier.width(8.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = AvalonTheme.colors.strokeSubtle,
            strokeWidth = 2.dp
        )
    }
}
