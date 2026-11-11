package com.ensodai.avalon.presentation.userAccountScreen.component.bottomSheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.core_energy
import com.ensodai.avalon.generated.resources.daily_at_utc
import com.ensodai.avalon.generated.resources.increase_limit
import com.ensodai.avalon.generated.resources.recovery
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnergyContentBottomSheet(
    current: Int,
    max: Int,
    isShowUpgradeButton: Boolean,
    onUpgradeClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AvalonTheme.colors
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ModalBottomSheet(
        containerColor = colors.overlaySurface,
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {

            Text(
                text = stringResource(Res.string.core_energy),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textMain,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$current",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
                )
                Text(
                    text = " / $max",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.placeholderText,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val progress = if (max > 0) current.toFloat() / max.toFloat() else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(50)),
                color = colors.accentSecondary,
                trackColor = colors.progressTrack,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = colors.surfaceHigh,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = IconsManager.Update,
                        contentDescription = null,
                        tint = colors.placeholderText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(Res.string.recovery),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary
                        )
                        Text(
                            text = stringResource(Res.string.daily_at_utc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMain
                        )
                    }
                }
            }

//            Spacer(modifier = Modifier.height(24.dp))
//
//
//            if (isShowUpgradeButton) {
//                TechnoButton(
//                    text = stringResource(Res.string.sys_quota_buy_energy_btn),
//                    onClick = {
//                        onDismiss()
//                        onUpgradeClick()
//                    },
//                    modifier = Modifier.fillMaxWidth(),
//                    isPrimary = true,
//                    primaryColor = colors.neonAccent,
//                    onPrimaryColor = colors.userBubbleTextColor
//                )
//            }
        }
    }
}
