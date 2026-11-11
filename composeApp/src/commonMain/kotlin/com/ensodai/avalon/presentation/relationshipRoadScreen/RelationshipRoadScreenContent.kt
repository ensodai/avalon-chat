package com.ensodai.avalon.presentation.relationshipRoadScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.*
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.presentation.relationshipRoadScreen.action.RelationshipRoadScreenActions
import com.ensodai.avalon.presentation.relationshipRoadScreen.component.RelationshipRoadHeader
import com.ensodai.avalon.presentation.relationshipRoadScreen.component.RelationshipRoadItemCard
import com.ensodai.avalon.presentation.relationshipRoadScreen.viewState.RelationshipRoadScreenViewState
import com.ensodai.avalon.theme.AvalonTheme
import kotlin.uuid.ExperimentalUuidApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RelationshipRoadScreenContent(
    actions: RelationshipRoadScreenActions,
    viewState: RelationshipRoadScreenViewState,
    navigation: Navigation
) {
    val colors = AvalonTheme.colors
    val currentLevel = viewState.relationshipLevel
    val currentXp = viewState.currentExperience

    Scaffold(
        topBar = {
            GemIslandTopBar(
                title = {
                    Text(
                        text = stringResource(Res.string.relationship_road_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textMain
                    )
                },
                navigationIcon = {
                    NavigationBackButton(
                        onBackButtonClick = { navigation.navigateBack() }
                    )
                }
            )
        },
        containerColor = colors.background
    ) { innerPadding ->

            if (viewState.isLoading && viewState.roadmapItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.neonAccent)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding() + 12.dp,
                        bottom = innerPadding.calculateBottomPadding() + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Хедер с текущим статусом прогресса отношения
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        RelationshipRoadHeader(
                            currentLevel = currentLevel,
                            currentXp = currentXp
                        )
                    }

                    // Заголовок списка наград
                    item {
                        Text(
                            text = stringResource(Res.string.relationship_road_rewards_header),
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.5.sp
                            ),
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }

                    // Список из 65 уровней
                    items(viewState.roadmapItems) { item ->
                        RelationshipRoadItemCard(
                            item = item,
                            currentLevel = currentLevel
                        )
                    }
                }
            }

    }
}
