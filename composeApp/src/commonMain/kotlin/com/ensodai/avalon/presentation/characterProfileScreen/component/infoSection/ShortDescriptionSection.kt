package com.ensodai.avalon.presentation.characterProfileScreen.component.infoSection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.description
import com.ensodai.avalon.presentation.commonComponents.BaseItemAlphaAnimation
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShortDescriptionSection(
    shortDescription: String,
    isEditMode: Boolean,
    onChangeShortDescription: (String) -> Unit,
    updatedShorDescription: String,
    isErrorInputShortDescription: Boolean,
) {
    val colors = AvalonTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = stringResource(Res.string.description),
                fontWeight = FontWeight.Bold,
                color = colors.warningAccent
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box {
                BaseItemAlphaAnimation(isVisible = !isEditMode) {
                    Text(
                        text = shortDescription
                    )
                }
                BaseItemAlphaAnimation(isVisible = isEditMode) {
                    Column {
                        OutlinedTextField(
                            value = updatedShorDescription,
                            onValueChange = { newValue ->
                                onChangeShortDescription(newValue)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .imePadding(),
                            shape = RoundedCornerShape(8.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = colors.neonAccent,
                                focusedIndicatorColor = colors.strokeActive,
                                unfocusedIndicatorColor = colors.strokeActive,
                                errorIndicatorColor = colors.error,
                                errorContainerColor = Color.Transparent,
                                errorCursorColor = colors.neonAccent,
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            isError = isErrorInputShortDescription
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            modifier = Modifier
                                .align(Alignment.End),
                            text = "${updatedShorDescription.length}/160",
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
