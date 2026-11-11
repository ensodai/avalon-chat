package com.ensodai.avalon.presentation.personaScreen.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.person_description
import com.ensodai.avalon.generated.resources.person_description_placeholder
import com.ensodai.avalon.presentation.commonComponents.textFields.TechnoInput
import com.ensodai.avalon.presentation.personaScreen.viewState.PersonaLoadingState
import org.jetbrains.compose.resources.stringResource

@Composable
fun DescriptionSection(
    description: String,
    onChangeDescription: (String) -> Unit,
    loadingState: PersonaLoadingState,
) {

    Column {
        Text(
            text = stringResource(Res.string.person_description),
            style = MaterialTheme.typography.titleLarge,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        TechnoInput(
            value = description,
            onValueChange = { newValue ->
                onChangeDescription(newValue.take(1000))
            },
            placeholder = stringResource(Res.string.person_description_placeholder),
            maxLines = 8,
            minLines = 8,
            maxLength = 1000,
            enabled = loadingState != PersonaLoadingState.Loading
        )
    }
}
