package com.ensodai.avalon.presentation.chatScreen.component.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.shared.avalon.chat.models.SystemEventType
import com.ensodai.shared.avalon.chat.models.SystemEventType.*
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun ActionButton(
    onActionButtonClick: () -> Unit,
    actionButtonColor: Color?,
    actionButtonText: String
) {
    val accentColor = actionButtonColor ?: AvalonTheme.colors.premiumAccent

    Surface(
        onClick = onActionButtonClick,
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = BorderStroke(1.5.dp, accentColor)
    ) {
        Text(
            text = actionButtonText.uppercase(),
            modifier = Modifier.padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = accentColor
        )
    }
}

fun actionButtonClickHandler(systemEventType: SystemEventType?, navigation: Navigation) {
    when (systemEventType) {
        QUOTA_EXCEEDED,
        CUSTOM_MODEL_MISSING,
        CUSTOM_KEY_INVALID,
        CUSTOM_INSUFFICIENT_CREDITS,
        CUSTOM_RATE_LIMIT,
        CUSTOM_MODEL_NOT_FOUND,
        CUSTOM_CONTEXT_LENGTH_EXCEEDED,
        CUSTOM_GUARDRAILS_RESTRICTED,
        CUSTOM_PROVIDER_UNAVAILABLE -> {
            navigation.navigateToUserAiSettingsScreen()
        }
        MESSAGE_BLOCKED -> {}
        SERVER_ERROR -> {}
        USER_BANNED -> {}
        null -> {}
        IMAGE_PERSON_BLOCKED -> {}
        IMAGE_SAFETY_BLOCKED -> {}
    }
}
