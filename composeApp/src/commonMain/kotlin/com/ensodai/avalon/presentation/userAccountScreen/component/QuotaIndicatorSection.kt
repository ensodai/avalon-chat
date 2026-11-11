package com.ensodai.avalon.presentation.userAccountScreen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.one_hour
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun QuotaIndicatorSection(
    currentQuota: Int,
    maxQuota: Int,
    isLoading: Boolean
) {
    val colors = AvalonTheme.colors
    Column {
        QuotaIndicator3(
            currentQuota = currentQuota,
            maxQuota = maxQuota,
            isLoading = isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                fontWeight = FontWeight.Bold,
                text = "1",
                fontSize = 16.sp
            )
            Icon(
                imageVector = IconsManager.Bolt,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = colors.textMain
            )
            Text(
                fontWeight = FontWeight.Bold,
                text = " = ",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(1.dp))
            Text(
                fontWeight = FontWeight.Bold,
                text = "1",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = IconsManager.Message,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = IconsManager.Refresh,
                contentDescription = null,
            )
            Text(
                fontWeight = FontWeight.Bold,
                text = "1",
                fontSize = 16.sp
            )
            Text(
                fontWeight = FontWeight.Bold,
                text = stringResource(Res.string.one_hour),
                fontSize = 16.sp
            )
        }
    }

}
