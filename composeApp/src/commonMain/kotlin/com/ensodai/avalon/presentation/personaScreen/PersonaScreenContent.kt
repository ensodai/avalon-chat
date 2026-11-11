package com.ensodai.avalon.presentation.personaScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.delete_persona
import com.ensodai.avalon.generated.resources.person_screen
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.AlertDialogDeleteItem
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.presentation.personaScreen.action.PersonaScreenAction
import com.ensodai.avalon.presentation.personaScreen.component.AlertDialogAboutPerson
import com.ensodai.avalon.presentation.personaScreen.component.DescriptionSection
import com.ensodai.avalon.presentation.personaScreen.component.NameSection
import com.ensodai.avalon.presentation.personaScreen.component.PersonaButtonSection
import com.ensodai.avalon.presentation.personaScreen.viewState.PersonaLoadingState
import com.ensodai.avalon.presentation.personaScreen.viewState.PersonaScreenViewState
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaScreenContent(
    viewState: PersonaScreenViewState,
    actions: PersonaScreenAction,
    navigation: Navigation,
    isUpdatePersona: Boolean,
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {

    AvalonBackHandler(enabled = viewState.loadingState == PersonaLoadingState.Loading) {
        if (viewState.loadingState == PersonaLoadingState.Loading) {
            return@AvalonBackHandler
        }
    }

    LaunchedEffect(viewState.loadingState) {
        if (viewState.loadingState == PersonaLoadingState.Success) {
            navigation.navigateBack()
        }
    }

    LaunchedEffect(viewState.loadingState) {
        if (viewState.loadingState is PersonaLoadingState.Loading) {
            onDisableSwipeable()
        } else {
            onEnableSwipeable()
        }
    }

    AlertDialogDeleteItem(
        title = stringResource(Res.string.delete_persona),
        onDismiss = { actions.onCloseAlertDialog() },
        onConfirm = { actions.onDeletePersona() },
        isShowDialog = viewState.isShowDeleteAlertDialog
    )
    AlertDialogAboutPerson(
        isShowDialog = viewState.isShowAboutAlertDialog,
        onDismiss = { actions.onCloseAlertDialog() },
        onConfirm = { actions.onCloseAlertDialog() }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            GemIslandTopBar(
                title = {
                    Text(
                        text = stringResource(Res.string.person_screen),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    NavigationBackButton(
                        onBackButtonClick = {
                            if (viewState.loadingState != PersonaLoadingState.Loading) {
                                navigation.navigateBack()
                            }
                        }
                    )
                },
                actions = {
                    IconButton(
                        onClick = { actions.onShowAboutPersonAlertDialog() },
                    ) {
                        Icon(
                            imageVector = IconsManager.Info,
                            contentDescription = "Delete"
                        )
                    }
                    if (isUpdatePersona) {
                        IconButton(
                            onClick = { actions.onShowDeletePersonaAlertDialog() },
                        ) {
                            Icon(
                                imageVector = IconsManager.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    }
                }
            )
        },

        ) { innerPadding ->
        val innerPadding = innerPadding
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            NameSection(
                name = viewState.name,
                loadingState = viewState.loadingState,
                isErrorName = viewState.isErrorName,
                onChangeName = { actions.onChangeName(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            DescriptionSection(
                description = viewState.description,
                loadingState = viewState.loadingState,
                onChangeDescription = { actions.onChangeDescription(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PersonaButtonSection(
                isUpdatePersona = isUpdatePersona,
                loadingState = viewState.loadingState,
                onCreateNewPersonaClick = { actions.onCreateNewPersona() },
                onSaveChangesClick = { actions.onSaveChanges() },
            )
        }
    }
}
