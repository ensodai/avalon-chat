package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.button_login_google
import com.ensodai.avalon.generated.resources.ic_google_button
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GoogleSignInButtonContent(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val colors = AvalonTheme.colors
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .alpha(if (enabled) 1f else 0.5f)
            .height(50.dp)
            .fillMaxWidth(0.7f),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.surfaceHigh,
            contentColor = colors.textMain
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_google_button),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = stringResource(Res.string.button_login_google),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
expect fun SmartGoogleSignInButton(
    modifier: Modifier = Modifier,
    onSignInGoogleToken: (String) -> Unit,
    onDismiss: () -> Unit,
    onError: (Exception) -> Unit
)
