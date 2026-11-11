package com.ensodai.avalon.presentation.addCharacterScreen.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.description
import com.ensodai.avalon.generated.resources.full_description
import com.ensodai.avalon.generated.resources.full_description_stage
import com.ensodai.avalon.presentation.commonComponents.privacyCards.PrivacyFullDescriptionCardRow
import com.ensodai.avalon.presentation.commonComponents.textFields.TechnoInput
import com.ensodai.avalon.utils.EMPTY_STRING
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun FullDescriptionStagesScreen(
    fullDescription: String?,
    fullDescriptionPrivacy: CharacterVisibility,
    onChangeFullDescriptionPrivacy: (CharacterVisibility) -> Unit,
    onChangeFullDescription: (String) -> Unit
) {
    val colors = AvalonTheme.colors
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)

        ) {
            Text(
                text = stringResource(Res.string.full_description_stage),
                style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.full_description),
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrivacyFullDescriptionCardRow(
                selectedOption = fullDescriptionPrivacy,
                onOptionSelected = { onChangeFullDescriptionPrivacy(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TechnoInput(
                value = fullDescription ?: EMPTY_STRING,
                onValueChange = { newValue ->
                    if (newValue.length <= 21000) {
                        onChangeFullDescription(newValue)
                    } else {
                        val truncatedText = newValue.take(21000)
                        onChangeFullDescription(truncatedText)
                    }
                },
                placeholder = stringResource(Res.string.description),
                maxLines = 11,
                minLines = 11,
                maxLength = 21000,
                isError = false
            )
        }
    }
}
