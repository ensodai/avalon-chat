package com.ensodai.avalon.presentation.personaScreen.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.create_new_person_button
import com.ensodai.avalon.generated.resources.persona_save_changes
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.personaScreen.viewState.PersonaLoadingState
import org.jetbrains.compose.resources.stringResource

@Composable
fun PersonaButtonSection(
    isUpdatePersona: Boolean,
    loadingState: PersonaLoadingState,
    onCreateNewPersonaClick: () -> Unit,
    onSaveChangesClick: () -> Unit
) {

    TechnoButton(
        text = if (isUpdatePersona) {
            stringResource(Res.string.persona_save_changes)
        } else {
            stringResource(Res.string.create_new_person_button)
        },
        onClick = if (isUpdatePersona) {
            onSaveChangesClick
        } else {
            onCreateNewPersonaClick
        },
        isPrimary = true,
        modifier = Modifier.fillMaxWidth(),
        isLoading = loadingState == PersonaLoadingState.Loading
    )
}
