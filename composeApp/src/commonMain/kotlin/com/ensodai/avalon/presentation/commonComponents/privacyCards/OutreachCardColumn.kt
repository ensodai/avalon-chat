package com.ensodai.avalon.presentation.commonComponents.privacyCards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.outreach_setting_allowed_title
import com.ensodai.avalon.generated.resources.outreach_setting_allowed_description
import com.ensodai.avalon.generated.resources.outreach_setting_blocked_title
import com.ensodai.avalon.generated.resources.outreach_setting_blocked_description
import org.jetbrains.compose.resources.stringResource

@Composable
fun OutreachCardColumn(
    modifier: Modifier = Modifier,
    isOutreachEnabled: Boolean,
    onOptionSelected: (Boolean) -> Unit
) {
    Column(modifier) {
        AvailabilityCard(
            title = stringResource(Res.string.outreach_setting_allowed_title),
            description = stringResource(Res.string.outreach_setting_allowed_description),
            isSelected = isOutreachEnabled,
            onClick = { onOptionSelected(true) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        AvailabilityCard(
            title = stringResource(Res.string.outreach_setting_blocked_title),
            description = stringResource(Res.string.outreach_setting_blocked_description),
            isSelected = !isOutreachEnabled,
            onClick = { onOptionSelected(false) }
        )
    }
}
