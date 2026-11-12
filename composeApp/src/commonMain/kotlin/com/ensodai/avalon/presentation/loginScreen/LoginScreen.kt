package com.ensodai.avalon.presentation.loginScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.config.AppBuildConfig
import com.ensodai.avalon.presentation.commonComponents.SmartGoogleSignInButton
import com.ensodai.avalon.presentation.commonComponents.drawHexagonPath
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.utils.L
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginScreenViewModel = koinViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()
    val actions = viewModel.actions
    val versionName = AppBuildConfig.VERSION_NAME
    val colors = AvalonTheme.colors
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 28.dp, vertical = 32.dp)
            ) {
                AvalonTitle()

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "OPEN AI MESSENGER",
                    color = colors.textSecondary.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    letterSpacing = 4.sp,
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Server URL input
                OutlinedTextField(
                    value = viewState.serverUrl,
                    onValueChange = actions.onServerUrlChanged,
                    label = { Text("Адрес сервера") },
                    singleLine = true,
                    enabled = !viewState.isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.neonAccent,
                        unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.3f),
                        focusedLabelColor = colors.neonAccent,
                        unfocusedLabelColor = colors.textSecondary.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Username input
                OutlinedTextField(
                    value = viewState.username,
                    onValueChange = actions.onUsernameChanged,
                    label = { Text("Имя пользователя") },
                    singleLine = true,
                    enabled = !viewState.isLoading,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.neonAccent,
                        unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.3f),
                        focusedLabelColor = colors.neonAccent,
                        unfocusedLabelColor = colors.textSecondary.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password input
                OutlinedTextField(
                    value = viewState.password,
                    onValueChange = actions.onPasswordChanged,
                    label = { Text("Пароль") },
                    singleLine = true,
                    enabled = !viewState.isLoading,
                    visualTransformation = if (viewState.isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        TextButton(onClick = actions.onTogglePasswordVisibility) {
                            Text(
                                text = if (viewState.isPasswordVisible) "Скрыть" else "Показать",
                                color = colors.neonAccent,
                                fontSize = 11.sp
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { actions.onSubmit() }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.neonAccent,
                        unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.3f),
                        focusedLabelColor = colors.neonAccent,
                        unfocusedLabelColor = colors.textSecondary.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                AnimatedContent(
                    targetState = viewState.isLoading,
                    label = "LoginLoading"
                ) { isLoading ->
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = colors.neonAccent,
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Button(
                            onClick = actions.onSubmit,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.neonAccent,
                                contentColor = colors.background
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = if (viewState.isRegisterMode) "Создать аккаунт" else "Войти",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mode switcher (Login / Register)
                TextButton(
                    onClick = actions.onToggleRegisterMode,
                    enabled = !viewState.isLoading
                ) {
                    Text(
                        text = if (viewState.isRegisterMode) {
                            "Уже есть аккаунт? Войти"
                        } else {
                            "Нет аккаунта? Зарегистрироваться"
                        },
                        color = colors.neonAccent,
                        fontSize = 13.sp
                    )
                }

                viewState.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        color = colors.error.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Avalon Avatars v$versionName (Self-Hosted)",
                    color = colors.textMain.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun AvalonTitle() {
    val colors = AvalonTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(72.dp)) {
            drawHexagonPath(
                color = colors.textMain,
                style = Stroke(width = 4.dp.toPx())
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "AVALON",
            color = colors.textMain,
            fontSize = 42.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 6.sp,
            style = MaterialTheme.typography.displayLarge
        )
    }
}

@Composable
fun AtmosphericBackground() {
    val colors = AvalonTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")

    val radiusRatio by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "GradientRadius"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxRadius = size.minDimension / 1.2f
        val currentRadius = maxRadius * radiusRatio

        val brush = Brush.radialGradient(
            colors = listOf(
                colors.neonAccent,
                Color.Transparent
            ),
            center = center,
            radius = currentRadius
        )

        drawCircle(
            brush = brush,
            radius = currentRadius,
            center = center,
            alpha = alpha
        )
    }
}
