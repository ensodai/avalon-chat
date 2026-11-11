package com.ensodai.avalon.presentation.characterProfileScreen.component.infoSection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.author
import com.ensodai.avalon.presentation.commonComponents.copyToClipboardAndShowToast
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthorSection(authorName: String) {

    val copyToClipboard = copyToClipboardAndShowToast()
    val colors = AvalonTheme.colors

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(Res.string.author),
            fontWeight = FontWeight.Bold,
            color = colors.warningAccent
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier.clickable { copyToClipboard("@$authorName") },
            text = "@$authorName",
            color = colors.infoAccent
        )
    }

}
