package com.ensodai.avalon.presentation.userAccountScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.in_development
import com.ensodai.avalon.navigation.avalonCustomSwipe
import com.ensodai.avalon.presentation.benchmark.BenchmarkTags
import com.ensodai.avalon.presentation.commonComponents.toastNotify
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.presentation.paymentScreen.viewState.UserTariff
import com.ensodai.avalon.presentation.userAccountScreen.actions.UserAccountScreenActions
import com.ensodai.avalon.presentation.userAccountScreen.component.PersonaSection
import com.ensodai.avalon.presentation.userAccountScreen.component.ResourcesSection
import com.ensodai.avalon.presentation.userAccountScreen.component.SupporterBadgeCard
import com.ensodai.avalon.presentation.userAccountScreen.component.TariffPromoCard
import com.ensodai.avalon.presentation.userAccountScreen.component.TopBarUserAccountScreen
import com.ensodai.avalon.presentation.userAccountScreen.component.UserCharactersSection
import com.ensodai.avalon.presentation.userAccountScreen.component.bottomSheet.BottomSheetUserAccountScreen
import com.ensodai.avalon.presentation.userAccountScreen.viewState.UserAccountScreenViewState
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.presentation.commonComponents.bottomSheet.BuyEnergyBottomSheet

@Composable
fun UserAccountScreenContent(
    navigation: Navigation,
    viewState: UserAccountScreenViewState,
    actions: UserAccountScreenActions
) {
    val toastNotify = toastNotify()
    val message = stringResource(Res.string.in_development)
    var showBuyEnergySheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag(BenchmarkTags.UserAccountScreen)
            .avalonCustomSwipe { dragDeltaX ->
                dragDeltaX > 0
            },
        topBar = {
            TopBarUserAccountScreen(
                modifier = Modifier
                    .fillMaxWidth(),
                userNickName = viewState.userNickName,
                premiumStatus = viewState.premiumStatus,
                isShowEditButton = viewState.isAuthorized && viewState.loadingState == LoadingState.Success,
                onBackButtonClick = { navigation.navigateBack() },
                onOpenSettingsScreen = { navigation.navigateToSettingScreen() },
                onEditButtonClick = { navigation.navigateToEditUserProfileScreen(viewState.userNickName) }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (viewState.isEarlySupporter) {
                item {
                    SupporterBadgeCard(
                        modifier = Modifier.testTag(BenchmarkTags.UserAccountProfileCard),
                        onClick = {
                            actions.bottomSheetActions.onOpenSupporterGratitude()
                        }
                    )
                }
            }

            if (viewState.premiumStatus != UserTariff.Free || viewState.purchasedBatteries != 0) {
                item {
                    ResourcesSection(
                        modifier = Modifier.testTag(BenchmarkTags.UserAccountResourcesCard),
                        loadingState = viewState.loadingState,
                        dailyQuotaCurrent = viewState.currentQuota,
                        dailyQuotaMax = viewState.maxQuota,
                        purchasedBatteries = if (
                            viewState.loadingState == LoadingState.Loading
                        )
                            0
                        else viewState.purchasedBatteries,
                        onQuotaClick = { actions.bottomSheetActions.onOpenManageEnergy() },
                        onBatteriesClick = { actions.bottomSheetActions.onOpenManageReservedEnergy() }
                    )
                }
            }


            item {
                UserCharactersSection(
                    modifier = Modifier.testTag(BenchmarkTags.UserAccountCharactersCard),
                    charactersCount = viewState.charactersCount,
                    unclaimedEnergy = viewState.totalUnclaimedCreatorEnergy,
                    loadingState = viewState.loadingState,
                    onClick = {
                        navigation.navigateToUserCharacterScreen()
                    }
                )
            }

            item {
                PersonaSection(
                    modifier = Modifier.testTag(BenchmarkTags.UserAccountPersonaCard),
                    premiumStatus = viewState.premiumStatus,
                    loadingState = viewState.loadingState,
                    selectedPersona = viewState.selectedPersona,
                    personaInfoList = viewState.personas,
                    onPersonaClick = { actions.onSelectPersonaAsDefault(it) },
                    onCreateNewPersonaClick = { navigation.navigateToCreatePersonaScreen() },
                    onEditPersonaClick = { navigation.navigateToEditPersonaScreen(id = it.id) },
                    onPremiumClick = { navigation.navigateToPaymentScreen() }
                )
            }
        }
    }

    BottomSheetUserAccountScreen(
        state = viewState.bottomSheetState,
        onUpgradeClick = {
            actions.bottomSheetActions.onCloseBottomSheet()
            navigation.navigateToPaymentScreen()
        },
        onBuyEnergyClick = {
            actions.bottomSheetActions.onCloseBottomSheet()
            showBuyEnergySheet = true
        },
        onCloseBottomSheet = { actions.bottomSheetActions.onCloseBottomSheet() }
    )

    if (showBuyEnergySheet) {
        BuyEnergyBottomSheet(
            onDismiss = { showBuyEnergySheet = false }
        )
    }
}
