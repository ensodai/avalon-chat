package com.ensodai.avalon.presentation.commonComponents.bottomSheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.domain.paymentManager.BillingState
import com.ensodai.avalon.domain.paymentManager.DisplayConsumableDto
import com.ensodai.avalon.domain.paymentManager.PaymentManager
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.buy_energy_current_balance
import com.ensodai.avalon.generated.resources.buy_energy_daily_quota
import com.ensodai.avalon.generated.resources.buy_energy_loading_products
import com.ensodai.avalon.generated.resources.buy_energy_no_products
import com.ensodai.avalon.generated.resources.buy_energy_purchase_success
import com.ensodai.avalon.generated.resources.buy_energy_purchase_success_subtitle
import com.ensodai.avalon.generated.resources.buy_energy_reserve_balance
import com.ensodai.avalon.generated.resources.buy_energy_sheet_subtitle
import com.ensodai.avalon.generated.resources.buy_energy_sheet_title
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.contrastText
import com.ensodai.avalon.theme.models.BubbleFill
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.border
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.ripple
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.PremiumSuccessDialog


sealed interface PurchaseUiStatus {
    object Idle : PurchaseUiStatus
    object Loading : PurchaseUiStatus
    object Success : PurchaseUiStatus
    data class Error(val message: String) : PurchaseUiStatus
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyEnergyBottomSheet(
    onDismiss: () -> Unit,
    paymentManager: PaymentManager = koinInject(),
    localUserRepository: LocalUserRepository = koinInject()
) {
    val colors = AvalonTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val userState by localUserRepository.getCurrentUserFlow().collectAsState(initial = null)
    val consumablesResult by paymentManager.consumablesFlow.collectAsState(initial = null)
    
    var purchaseStatus by remember { mutableStateOf<PurchaseUiStatus>(PurchaseUiStatus.Idle) }
    var selectedEnergyAmount by remember { mutableStateOf(0) }
    var buyingProductId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        paymentManager.loadProducts(forceRefresh = true)
    }

    LaunchedEffect(Unit) {
        paymentManager.billingState.collect { state ->
            when (state) {
                is BillingState.Idle -> {
                    purchaseStatus = PurchaseUiStatus.Idle
                    buyingProductId = null
                }
                is BillingState.Loading -> {
                    purchaseStatus = PurchaseUiStatus.Loading
                }
                is BillingState.Success -> {
                    if (state.productId == buyingProductId) {
                        purchaseStatus = PurchaseUiStatus.Success
                        buyingProductId = null
                    }
                }
                is BillingState.Error -> {
                    purchaseStatus = PurchaseUiStatus.Error(state.message)
                    buyingProductId = null
                }
            }
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = colors.overlaySurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Заголовок
                Text(
                    text = stringResource(Res.string.buy_energy_sheet_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.textMain,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(Res.string.buy_energy_sheet_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                // Карточка текущего баланса
                userState?.let { user ->
                    val totalEnergy = user.reserveBalance + user.currentQuota
                    Surface(
                        color = colors.surfaceHigh,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(Res.string.buy_energy_current_balance),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textSecondary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = IconsManager.Bolt,
                                        contentDescription = null,
                                        tint = colors.neonAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$totalEnergy ⚡",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textMain
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(Res.string.buy_energy_daily_quota, user.currentQuota, user.maxQuota),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = stringResource(Res.string.buy_energy_reserve_balance, user.reserveBalance),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Список продуктов
                val products = consumablesResult
                when {
                    products == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = colors.neonAccent,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    products.isFailure -> {
                        Text(
                            text = products.exceptionOrNull()?.message ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    products.isSuccess -> {
                        val items = products.getOrNull().orEmpty()
                        if (items.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.buy_energy_no_products),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items.forEach { item ->
                                    ConsumablePackRow(
                                        item = item,
                                        onClick = {
                                            if (purchaseStatus == PurchaseUiStatus.Idle) {
                                                selectedEnergyAmount = item.energyAmount
                                                buyingProductId = item.id
                                                paymentManager.buyConsumable(item.id)
                                            }
                                        },
                                        enabled = purchaseStatus == PurchaseUiStatus.Idle
                                    )
                                }
                            }
                        }
                    }
                }

                // Вывод ошибки покупки, если есть
                if (purchaseStatus is PurchaseUiStatus.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (purchaseStatus as PurchaseUiStatus.Error).message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Блокирующий оверлей загрузки
            LoadingOverlay(
                visible = purchaseStatus == PurchaseUiStatus.Loading
            )
        }
    }

    // Глобальный премиальный диалог успеха
    PremiumSuccessDialog(
        visible = purchaseStatus == PurchaseUiStatus.Success,
        title = stringResource(Res.string.buy_energy_purchase_success),
        subtitle = stringResource(Res.string.buy_energy_purchase_success_subtitle, selectedEnergyAmount),
        onDismiss = {
            purchaseStatus = PurchaseUiStatus.Idle
            onDismiss()
        }
    )
}

@Composable
private fun LoadingOverlay(
    visible: Boolean
) {
    val colors = AvalonTheme.colors

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.overlaySurface.copy(alpha = 0.85f))
                .clickable(enabled = false) {}, // блокируем клики
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = colors.neonAccent,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.buy_energy_loading_products),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textMain
                )
            }
        }
    }
}

@Composable
fun ConsumablePackRow(
    item: DisplayConsumableDto,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val colors = AvalonTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        label = "pack_row_scale",
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    LaunchedEffect(isPressed) {
        if (isPressed && enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceHigh,
        border = BorderStroke(1.dp, colors.strokeSubtle),
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.neonAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconsManager.Bolt,
                        contentDescription = null,
                        tint = colors.neonAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "+${item.energyAmount} ⚡",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textMain
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            val buttonShape = RoundedCornerShape(12.dp)

            Box(
                modifier = Modifier
                    .clip(buttonShape)
                    .background(colors.neonAccent.copy(alpha = 0.12f))
                    .border(1.dp, colors.neonAccent.copy(alpha = 0.35f), buttonShape)
            ) {
                Text(
                    text = item.priceLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = colors.neonAccent,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

