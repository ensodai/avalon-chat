package com.ensodai.avalon.presentation.userAccountScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ensodai.avalon.presentation.commonComponents.copyToClipboardAndShowToast
import com.ensodai.avalon.presentation.commonComponents.shimmer.technoShimmer
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun UserInfoHeaderSection(
    isLoading: Boolean,
    userAvatar: String,
    userName: String,
    userNickName: String
) {
    val copyToClipboard = copyToClipboardAndShowToast()
    val colors = AvalonTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .technoShimmer(visible = true, shape = CircleShape)
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceHigh)
            )
        } else {
            AsyncImage(
                model = userAvatar,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .technoShimmer(visible = true, shape = RoundedCornerShape(8.dp))
                        .width(180.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceHigh)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .technoShimmer(visible = true, shape = RoundedCornerShape(8.dp))
                        .width(120.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceHigh)
                )
            } else {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    modifier = Modifier.clickable {
                        copyToClipboard("@$userNickName")
                    },
                    text = "@$userNickName",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.infoAccent
                )
            }
        }
    }
}
