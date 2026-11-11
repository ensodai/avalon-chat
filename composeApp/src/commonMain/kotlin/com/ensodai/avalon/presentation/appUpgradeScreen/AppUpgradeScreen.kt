package com.ensodai.avalon.presentation.appUpgradeScreen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.config.AppBuildConfig
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.presentation.commonComponents.drawHexagonPath
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.utils.rememberPlatformAppActions
import com.ensodai.avalon.utils.rememberUrlOpener

import com.ensodai.avalon.domain.model.PlatformMetadata
import org.koin.compose.koinInject

@Composable
fun AppUpgradeScreen(requiredVersion: String) {
    val colors = AvalonTheme.colors
    val urlOpener = rememberUrlOpener()
    val platformAppActions = rememberPlatformAppActions()
    val platformMetadata: PlatformMetadata = koinInject()
    val isWeb = platformMetadata.platform == "Web"
    val updateUrl = "https://github.com/your-username/avalon-chat/releases"

    // Блокируем кнопку назад
    AvalonBackHandler {
        platformAppActions.closeApp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(colors.cardBackground, colors.background, Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        TechnoBackgroundPattern()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {

            PulsingHexagonLogo()

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "SYSTEM UPDATE",
                color = colors.textMain.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 4.sp,
                    fontFamily = FontFamily.Monospace
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Critical Patch\nRequired",
                color = colors.textMain,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            VersionInfoCard(
                currentVersion = AppBuildConfig.VERSION_NAME,
                requiredVersion = requiredVersion
            )

            Spacer(modifier = Modifier.height(48.dp))

            CyberButton(
                text = if (isWeb) "REFRESH APPLICATION" else "INITIALIZE UPDATE",
                onClick = {
                    if (isWeb) {
                        platformAppActions.reloadApp()
                    } else {
                        urlOpener.open(updateUrl)
                    }
                }
            )
        }

        Text(
            text = "AVALON SYSTEMS // SECURE CONNECTION",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(0.3f),
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = colors.textMain
        )
    }
}

@Composable
fun VersionInfoCard(currentVersion: String, requiredVersion: String) {
    val colors = AvalonTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.textMain.copy(alpha = 0.05f))
            .border(1.dp, colors.textMain.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text("CURRENT", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
            Text(
                currentVersion,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .rotate(-90f)
                .size(16.dp)
                .background(colors.textMain, RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(horizontalAlignment = Alignment.Start) {
            Text("TARGET", style = MaterialTheme.typography.labelSmall, color = colors.textMain)
            Text(
                requiredVersion,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textMain,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun CyberButton(text: String, onClick: () -> Unit) {
    val colors = AvalonTheme.colors
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp),
        shape = RoundedCornerShape(2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.textMain,
            contentColor = colors.background
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        )
    }
}

@Composable
fun PulsingHexagonLogo() {
    val colors = AvalonTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    Box(contentAlignment = Alignment.Center) {
        // "Эхо" логотипа (расходится волной)
        Canvas(modifier = Modifier.size(100.dp).scale(scale).alpha(alpha)) {
            drawHexagonPath(color = colors.textMain, style = Stroke(width = 2.dp.toPx()))
        }

        // Основной логотип (Статика или легкое дыхание)
        Canvas(modifier = Modifier.size(100.dp)) {
            drawHexagonPath(color = colors.textMain, style = Stroke(width = 6.dp.toPx()))
        }
    }
}

@Composable
fun TechnoBackgroundPattern() {
    val colors = AvalonTheme.colors
    Canvas(modifier = Modifier.fillMaxSize().alpha(0.03f)) {
        val step = 40.dp.toPx()
        // Рисуем сетку
        for (x in 0..size.width.toInt() step step.toInt()) {
            drawLine(
                color = colors.textMain,
                start = Offset(x.toFloat(), 0f),
                end = Offset(x.toFloat(), size.height),
                strokeWidth = 1f
            )
        }
        for (y in 0..size.height.toInt() step step.toInt()) {
            drawLine(
                color = colors.textMain,
                start = Offset(0f, y.toFloat()),
                end = Offset(size.width, y.toFloat()),
                strokeWidth = 1f
            )
        }
    }
}
