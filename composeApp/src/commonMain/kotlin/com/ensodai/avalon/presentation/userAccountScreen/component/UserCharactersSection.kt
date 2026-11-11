package com.ensodai.avalon.presentation.userAccountScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.round
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.my_characters
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.shimmer.ShimmerContainer
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun UserCharactersSection(
    modifier: Modifier = Modifier,
    charactersCount: Int,
    unclaimedEnergy: Double,
    loadingState: LoadingState,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    GemCard(
        modifier = modifier,
        onClick = onClick,
        enabled = true,
        contentPadding = PaddingValues(16.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.textMain.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconsManager.RecentActors,
                        contentDescription = null,
                        tint = colors.textMain.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(Res.string.my_characters).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textMain.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {

//                if (unclaimedEnergy > 0.0 && loadingState != LoadingState.Loading) {
//                    Box(
//                        modifier = Modifier
//                            .clip(RoundedCornerShape(6.dp))
//                            .background(Color(0xFFE5A93B).copy(alpha = 0.15f))
//                            .padding(horizontal = 8.dp, vertical = 4.dp)
//                    ) {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Icon(
//                                imageVector = IconsManager.Bolt,
//                                contentDescription = null,
//                                tint = Color(0xFFE5A93B),
//                                modifier = Modifier.size(14.dp)
//                            )
//                            Spacer(modifier = Modifier.width(4.dp))
//                            Text(
//                                text = "+${(round(unclaimedEnergy * 10) / 10.0).toString().removeSuffix(".0")}",
//                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
//                                color = Color(0xFFE5A93B),
//                                fontWeight = FontWeight.Bold
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.width(12.dp))
//                }

                ShimmerContainer(
                    isLoading = loadingState == LoadingState.Loading,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = charactersCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textMain,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    modifier = Modifier
                        .size(30.dp),
                    imageVector = IconsManager.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.textMain.copy(alpha = 0.5f)
                )
            }
        }
    }
}
