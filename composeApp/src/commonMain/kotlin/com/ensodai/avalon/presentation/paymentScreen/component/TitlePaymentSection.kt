package com.ensodai.avalon.presentation.paymentScreen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.await_purchase
import com.ensodai.avalon.generated.resources.max_tariff_reached
import com.ensodai.avalon.generated.resources.sub_current_plan_pattern
import com.ensodai.avalon.presentation.benchmark.BenchmarkTags
import com.ensodai.avalon.presentation.paymentScreen.viewState.PaymentUiStatus
import com.ensodai.avalon.presentation.paymentScreen.viewState.PaymentUiStatus.IDLE
import com.ensodai.avalon.presentation.paymentScreen.viewState.PaymentUiStatus.PROCESSING
import com.ensodai.avalon.presentation.paymentScreen.viewState.PaymentUiStatus.SUCCESS
import com.ensodai.avalon.presentation.paymentScreen.viewState.UserTariff
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun TitlePaymentSection(
    modifier: Modifier,
    paymentStatus: PaymentUiStatus,
    currentTariff: UserTariff = UserTariff.Free,
    isMaxTariff: Boolean = false,
    onCloseClick: () -> Unit,
) {
    val colors = AvalonTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 100.dp)
    ) {
        if (paymentStatus == IDLE) {
            IconButton(
                onClick = { onCloseClick() },
                modifier = Modifier
                    .testTag(BenchmarkTags.PaymentCloseButton)
                    .align(Alignment.TopEnd)
                    .padding(top = 30.dp)
            ) {
                Icon(IconsManager.Close, contentDescription = "Close", tint = colors.textSecondary)
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            when (paymentStatus) {
                IDLE -> {
                    Text(
                        text = "UPGRADE CORE",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = colors.textMain
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                    )
                    Text(
                        text = if (isMaxTariff) stringResource(Res.string.max_tariff_reached) else stringResource(Res.string.sub_current_plan_pattern, currentTariff.title),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isMaxTariff) colors.premiumAccent else colors.textSecondary,
                            fontWeight = if (isMaxTariff) FontWeight.Bold else FontWeight.Normal
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                PROCESSING -> {
                    Text(
                        text = stringResource(Res.string.await_purchase),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = colors.textMain
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }


                SUCCESS -> {

                }
            }
        }
    }
}
