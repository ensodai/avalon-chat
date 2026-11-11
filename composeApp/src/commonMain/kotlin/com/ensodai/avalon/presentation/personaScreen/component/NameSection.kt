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
import com.ensodai.avalon.generated.resources.person_name
import com.ensodai.avalon.generated.resources.person_name_placeholder
import com.ensodai.avalon.presentation.commonComponents.textFields.TechnoInput
import com.ensodai.avalon.presentation.personaScreen.viewState.PersonaLoadingState
import org.jetbrains.compose.resources.stringResource


@Composable
fun NameSection(
    name: String,
    isErrorName: Boolean,
    onChangeName: (String) -> Unit,
    loadingState: PersonaLoadingState
) {
    Column {
        Text(
            text = stringResource(Res.string.person_name),
            style = MaterialTheme.typography.titleLarge,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        TechnoInput(
            value = name,
            onValueChange = { newValue ->
                onChangeName(newValue.take(20))
            },
            placeholder = stringResource(Res.string.person_name_placeholder),
            maxLines = 1,
            minLines = 1,
            maxLength = 20,
            isError = isErrorName,
            enabled = loadingState != PersonaLoadingState.Loading
        )
    }
}
