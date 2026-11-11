package com.ensodai.avalon.presentation.editTemperatureScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.cancel
import com.ensodai.avalon.generated.resources.save
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.presentation.addCharacterScreen.component.TemperatureStageScreen
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editTemperatureScreen.action.EditTemperatureScreenActions
import com.ensodai.avalon.presentation.editTemperatureScreen.component.TopBarEditTemperatureScreen
import com.ensodai.avalon.presentation.editTemperatureScreen.viewState.EditTemperatureScreenViewState
import com.ensodai.avalon.presentation.navigation.Navigation
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditTemperatureScreenContent(
    viewState: EditTemperatureScreenViewState,
    actions: EditTemperatureScreenActions,
    navigation: Navigation,
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {
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

    Scaffold(
        topBar = {
            TopBarEditTemperatureScreen(
                modifier = Modifier,
                onBackButtonClick = { navigation.navigateBack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            TemperatureStageScreen(
                sliderPosition = viewState.temperature,
                onChangeTemperature = { actions.onChangeTemperature(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TechnoButton(
                        text = stringResource(Res.string.cancel),
                        onClick = { navigation.navigateBack() },
                        isPrimary = false,
                        modifier = Modifier.weight(1f),
                        enabled = viewState.loadingState != LoadingState.Loading
                    )

                    TechnoButton(
                        text = stringResource(Res.string.save),
                        onClick = { actions.onSaveTemperature() },
                        isPrimary = true,
                        isLoading = viewState.loadingState == LoadingState.Loading,
                        modifier = Modifier.weight(1f),
                        enabled = viewState.loadingState != LoadingState.Loading
                    )
                }
            }
        }
    }
}
