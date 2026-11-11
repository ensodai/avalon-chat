package com.ensodai.avalon.presentation.settingScreen.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.delete_account
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun DeleteAccountSection(
    isShowDeleteAccountSection: Boolean,
    onClick: () -> Unit,
) {
    if (!isShowDeleteAccountSection) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                vertical = 16.dp,
                horizontal = 20.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(24.dp).offset(x = -3.dp),
            imageVector = IconsManager.DeleteOutline,
            contentDescription = "Language",
            tint = AvalonTheme.colors.error
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(Res.string.delete_account),
            color = AvalonTheme.colors.error
        )
    }

}
