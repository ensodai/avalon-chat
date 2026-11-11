package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DividerLine() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp),
        thickness = 1.dp
    )
}
