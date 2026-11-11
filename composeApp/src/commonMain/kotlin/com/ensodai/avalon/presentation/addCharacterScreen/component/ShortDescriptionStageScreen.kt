package com.ensodai.avalon.presentation.addCharacterScreen.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.short_description
import com.ensodai.avalon.generated.resources.short_description_stage
import com.ensodai.avalon.presentation.commonComponents.textFields.TechnoInput
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun ShortDescriptionStageScreen(
    shortDescription: String,
    onChangeShortDescription: (String) -> Unit,
    isErrorShortDescription: Boolean
) {
    val colors = AvalonTheme.colors
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(Res.string.short_description_stage),
            style = TextStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMain
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.short_description),
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        TechnoInput(
            value = shortDescription,
            onValueChange = onChangeShortDescription,
            placeholder = "Designer from Night City ",
            maxLines = 1,
            maxLength = 160,
            isError = isErrorShortDescription
        )
    }
}