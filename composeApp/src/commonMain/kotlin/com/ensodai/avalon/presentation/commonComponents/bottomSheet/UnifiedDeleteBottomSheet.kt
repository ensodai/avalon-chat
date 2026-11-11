package com.ensodai.avalon.presentation.commonComponents.bottomSheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedDeleteBottomSheet(
    title: String,
    description: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "РЈР”РђР›РРўР¬",
    cancelText: String = "РћРўРњР•РќРђ"
) {
    val colors = AvalonTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.overlaySurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.textSecondary) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // РРєРѕРЅРєР°
            Icon(
                imageVector = IconsManager.WarningAmber,
                contentDescription = null,
                tint = colors.error,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // РўРµРєСЃС‚С‹
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textMain,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Р’РћРў РўРЈРў РњР•РќРЇР•Рњ РќРђ РЎРўРђРќР”РђР РўРќР«Р• РљРќРћРџРљР
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp) // РћС‚СЃС‚СѓРї РјРµР¶РґСѓ РєРЅРѕРїРєР°РјРё
            ) {
                // РљРЅРѕРїРєР° РћРўРњР•РќРђ (Р’С‚РѕСЂРѕСЃС‚РµРїРµРЅРЅР°СЏ РґРµС„РѕР»С‚РЅР°СЏ)
                TechnoButton(
                    text = cancelText,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    isPrimary = false, // РџСЂРѕР·СЂР°С‡РЅР°СЏ/РљРѕРЅС‚СѓСЂРЅР°СЏ
                    // РќР°СЃС‚СЂРѕРёРј С†РІРµС‚Р° РїРѕРґ СЃС‚РёР»СЊ "РћС‚РјРµРЅС‹"
                    customBorderColor = colors.strokeActive,
                    onSecondaryColor = colors.textMain
                )

                // РљРЅРѕРїРєР° РЈР”РђР›РРўР¬ (РџРµСЂРІРёС‡РЅР°СЏ РґРµС„РѕР»С‚РЅР°СЏ, РЅРѕ РљСЂР°СЃРЅР°СЏ)
                TechnoButton(
                    text = confirmText,
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    isPrimary = true,
                    // РџРµСЂРµРѕРїСЂРµРґРµР»СЏРµРј С†РІРµС‚ РЅР° РєСЂР°СЃРЅС‹Р№ (Danger)
                    primaryColor = colors.error,
                    onPrimaryColor = colors.textMain
                )
            }
        }
    }
}
