package com.ensodai.avalon.presentation.paymentScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.payment_service_disable
import com.ensodai.avalon.generated.resources.privacy_policy_url
import com.ensodai.avalon.generated.resources.terms_of_service_url
import com.ensodai.avalon.presentation.addCharacterScreen.component.loader.TesseractLoader
import com.ensodai.avalon.presentation.benchmark.BenchmarkTags
import com.ensodai.avalon.presentation.commonComponents.toastNotify
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.presentation.paymentScreen.action.PaymentScreenActions
import com.ensodai.avalon.presentation.paymentScreen.component.HexagonTechnoSuccessView
import com.ensodai.avalon.presentation.paymentScreen.component.MaxTariffView
import com.ensodai.avalon.presentation.paymentScreen.component.PaymentCardsPager
import com.ensodai.avalon.presentation.paymentScreen.component.TitlePaymentSection
import com.ensodai.avalon.presentation.paymentScreen.viewState.PaymentScreenViewState
import com.ensodai.avalon.presentation.paymentScreen.viewState.PaymentUiStatus
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.utils.rememberUrlOpener
import org.jetbrains.compose.resources.stringResource

@Composable
fun PaymentScreenContent(
    viewState: PaymentScreenViewState,
    actions: PaymentScreenActions,
    navigation: Navigation,
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {
    val colors = AvalonTheme.colors
    val toastNotify = toastNotify()
    val urlOpener = rememberUrlOpener()
    val notifyText = stringResource(Res.string.payment_service_disable)
    val termsUrl = stringResource(Res.string.terms_of_service_url)
    val privacyUrl = stringResource(Res.string.privacy_policy_url)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag(BenchmarkTags.PaymentScreen),
        containerColor = colors.background
    ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {

            AnimatedContent(
                targetState = viewState.paymentStatus,
                transitionSpec = {
                    if (targetState == PaymentUiStatus.SUCCESS) {
                        (fadeIn(animationSpec = tween(600)) +
                                scaleIn(initialScale = 0.9f, animationSpec = tween(600)))
                            .togetherWith(fadeOut(animationSpec = tween(400)))
                    } else {
                        fadeIn().togetherWith(fadeOut())
                    }
                },
                label = "PaymentScreenTransition"
            ) { status ->
                when (status) {
                    PaymentUiStatus.SUCCESS -> {

                        LaunchedEffect(Unit) { onDisableSwipeable() }

                        val plan = viewState.purchasedPlan
                        if (plan != null) {
                            HexagonTechnoSuccessView(
                                plan = plan,
                                onContinueClick = {
                                    onEnableSwipeable()
                                    navigation.navigateBack()
                                }
                            )
                        } else {
                            Box(Modifier.fillMaxSize()) { CircularProgressIndicator() }
                            LaunchedEffect(Unit) { navigation.navigateBack() }
                        }
                    }

                    PaymentUiStatus.PROCESSING -> {
                        Column {
                            TitlePaymentSection(
                                modifier = Modifier,
                                paymentStatus = viewState.paymentStatus,
                                currentTariff = viewState.currentTariff,
                                isMaxTariff = viewState.isMaxTariff,
                                onCloseClick = { navigation.navigateBack() }
                            )
                            TesseractLoader(
                                modifier = Modifier.fillMaxSize(),
                                glitchText = false
                            )
                        }
                    }

                    else -> {
                        if (viewState.isMaxTariff) {
                            MaxTariffView(
                                onBackClick = { navigation.navigateBack() }
                            )
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                TitlePaymentSection(
                                    modifier = Modifier,
                                    paymentStatus = viewState.paymentStatus,
                                    currentTariff = viewState.currentTariff,
                                    isMaxTariff = viewState.isMaxTariff,
                                    onCloseClick = { navigation.navigateBack() }
                                )

                                PaymentCardsPager(
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag(BenchmarkTags.PaymentCardsPager),
                                    products = viewState.products,
                                    selectedPlanIndex = viewState.selectedPlanIndex,
                                    isEnablePaymentService = viewState.isEnablePaymentService,
                                    onPurchase = { plan ->
                                        if (viewState.isEnablePaymentService) {
                                            actions.onPurchase(plan)
                                        } else {
                                            toastNotify(notifyText)
                                        }
                                    },
                                    onSelectPlan = actions.onSelectPlan,
                                    onRestorePurchase = actions.onRestorePurchase,
                                    onOpenTerms = {
                                        urlOpener.open(termsUrl)
                                    },
                                    onOpenPrivacy = {
                                        urlOpener.open(privacyUrl)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
