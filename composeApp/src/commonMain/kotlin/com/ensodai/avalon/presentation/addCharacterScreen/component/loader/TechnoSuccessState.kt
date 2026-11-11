package com.ensodai.avalon.presentation.addCharacterScreen.component.loader

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.add_character_success
import com.ensodai.avalon.generated.resources.start_chat
import com.ensodai.avalon.theme.AvalonTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun TechnoSuccessState(
    avatarUrl: String?,
    characterName: String,
    onDoneClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        launch { alphaAnim.animateTo(1f, animationSpec = tween(800)) }
        launch { scaleAnim.animateTo(1f, animationSpec = tween(800, easing = BackOutEasing)) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = alphaAnim.value
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(colors.cardBackground)
                .border(
                    width = 0.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(colors.neonAccent, colors.accentSecondary)
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    colors.cardBackground,
                                    colors.surfaceHigh
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = characterName.take(1).uppercase(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.textMain.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, colors.neonAccent.copy(alpha = 0.2f))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = characterName,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = colors.textMain,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(Res.string.add_character_success),
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = colors.neonAccent,
                letterSpacing = 2.sp
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        TechnoGhostButton(
            text = stringResource(Res.string.start_chat),
            onClick = onDoneClick
        )
    }
}

@Composable
fun TechnoGhostButton(
    text: String,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = colors.textMain
        ),
        border = BorderStroke(1.dp, colors.textMain.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
        modifier = Modifier.height(56.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        )
    }
}

val BackOutEasing = Easing { x ->
    val c1 = 1.70158f
    val c3 = c1 + 1
    1 + c3 * (x - 1).let { it * it * it } + c1 * (x - 1).let { it * it }
}
