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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.access_explain
import com.ensodai.avalon.generated.resources.access_title
import com.ensodai.avalon.generated.resources.accumulation_explain
import com.ensodai.avalon.generated.resources.accumulation_title
import com.ensodai.avalon.generated.resources.btn_close
import com.ensodai.avalon.generated.resources.reserved_storage_title
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservedEnergyContentBottomSheet(
    onDismiss: () -> Unit,
    reserveBalance: Int,
    maxReserveBalance: Int,
    onBuyEnergyClick: () -> Unit,
) {
    val colors = AvalonTheme.colors
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = colors.overlaySurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = IconsManager.BatteryChargingFull,
                    contentDescription = null,
                    tint = colors.neonAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.reserved_storage_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textMain,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$reserveBalance",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
                )
                Text(
                    text = " / $maxReserveBalance",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.placeholderText,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val progress =
                if (maxReserveBalance > 0) reserveBalance.toFloat() / maxReserveBalance.toFloat() else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(50)),
                color = colors.neonAccent,
                trackColor = colors.progressTrack,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = colors.surfaceHigh,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = IconsManager.AutoMode,
                            contentDescription = null,
                            tint = colors.placeholderText,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(Res.string.accumulation_title),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textMain
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(Res.string.accumulation_explain),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 1.dp,
                        color = colors.progressTrack
                    )

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = IconsManager.LockOpen,
                            contentDescription = null,
                            tint = colors.neonAccent,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(Res.string.access_title),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textMain
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(Res.string.access_explain),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

//            Spacer(modifier = Modifier.height(24.dp))
//
//            TechnoButton(
//                text = stringResource(Res.string.sys_quota_buy_energy_btn),
//                onClick = {
//                    onDismiss()
//                    onBuyEnergyClick()
//                },
//                modifier = Modifier.fillMaxWidth(),
//                isPrimary = true,
//                primaryColor = colors.neonAccent,
//                onPrimaryColor = colors.userBubbleTextColor
//            )
        }
    }
}
