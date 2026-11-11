package com.ensodai.avalon.presentation.summaryExplanationScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.summary_screen_benefits_section
import com.ensodai.avalon.generated.resources.summary_screen_header_desc
import com.ensodai.avalon.generated.resources.summary_screen_header_title
import com.ensodai.avalon.navigation.avalonCustomSwipe
import com.ensodai.avalon.presentation.summaryExplanationScreen.action.SummaryExplanationScreenActions
import com.ensodai.avalon.presentation.summaryExplanationScreen.component.SummaryBenefitCards
import com.ensodai.avalon.presentation.summaryExplanationScreen.component.SummaryTimelineSimulator
import com.ensodai.avalon.presentation.summaryExplanationScreen.component.TopBarSummaryExplanationScreen
import com.ensodai.avalon.presentation.summaryExplanationScreen.viewState.SummaryExplanationScreenViewState
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun SummaryExplanationScreenContent(
    state: SummaryExplanationScreenViewState,
    actions: SummaryExplanationScreenActions,
    onBack: () -> Unit
) {
    val colors = AvalonTheme.colors
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .avalonCustomSwipe { dragDeltaX -> dragDeltaX > 0 },
        topBar = {
            TopBarSummaryExplanationScreen(
                modifier = Modifier.fillMaxWidth(),
                onBackButtonClick = onBack
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(top = 16.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = stringResource(Res.string.summary_screen_header_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textMain
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(Res.string.summary_screen_header_desc),
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Simulator
            SummaryTimelineSimulator(
                currentStep = state.currentStep,
                isAutoPlaying = state.isAutoPlaying,
                onStepSelected = actions.onStepSelected,
                onNextStep = actions.onNextStep,
                onPreviousStep = actions.onPreviousStep,
                onToggleAutoPlay = actions.onToggleAutoPlay,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(Res.string.summary_screen_benefits_section),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textMain
            )

            Spacer(modifier = Modifier.height(12.dp))

            SummaryBenefitCards(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
