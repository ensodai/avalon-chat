package com.ensodai.avalon.presentation.userAccountScreen.component.bottomSheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.ensodai.avalon.presentation.userAccountScreen.viewState.UserAccountScreenBottomSheetState
import com.ensodai.avalon.utils.rememberSubscriptionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetUserAccountScreen(
    state: UserAccountScreenBottomSheetState,
    onUpgradeClick: () -> Unit,
    onBuyEnergyClick: () -> Unit,
    onCloseBottomSheet: () -> Unit,
) {
    val subscriptionManager = rememberSubscriptionManager()

    when (state) {
        UserAccountScreenBottomSheetState.Idle -> {}
        UserAccountScreenBottomSheetState.SupporterGratitude -> {
            SupporterGratitudeBottomSheet(
                onDismiss = onCloseBottomSheet
            )
        }
        is UserAccountScreenBottomSheetState.ManageEnergy -> {
            EnergyContentBottomSheet(
                current = state.currentQuota,
                max = state.maxQuota,
                onUpgradeClick = onBuyEnergyClick, // Перенаправляем на покупку энергии!
                onDismiss = onCloseBottomSheet,
                isShowUpgradeButton = state.isShowUpgradeButton
            )
        }

        is UserAccountScreenBottomSheetState.ManageReservedEnergy -> {
            ReservedEnergyContentBottomSheet(
                onDismiss = onCloseBottomSheet,
                reserveBalance = state.reserveBalance,
                maxReserveBalance = state.maxReserveBalance,
                onBuyEnergyClick = onBuyEnergyClick
            )
        }

        is UserAccountScreenBottomSheetState.ManageSubscription -> {
            SubscriptionStatusSheet(
                tariff = state.tariff,
                nextBillingDate = state.nextBillingDate,
                onDismiss = { onCloseBottomSheet() },
                onManageClick = {
                    subscriptionManager.manage(state.provider)
                },
                onUpgradeClick = {
                    onUpgradeClick()
                }
            )
        }
    }
}
