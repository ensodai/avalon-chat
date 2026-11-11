package com.ensodai.avalon.presentation.characterEventsScreen.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.character.models.CharacterEventDto
import com.ensodai.avalon.generated.resources.*
import com.ensodai.avalon.presentation.commonComponents.AnimateDropdownMenu
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.textFields.TechnoTextField
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun CharacterEventItemCard(
    event: CharacterEventDto,
    index: Int,
    allEvents: List<CharacterEventDto>,
    onDeleteEvent: (Int) -> Unit,
    onRequiredLevelChanged: (Int, Int) -> Unit,
    onEventNameChanged: (Int, String) -> Unit,
    onTriggerScenarioChanged: (Int, String) -> Unit,
    onPermanentTraitChanged: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors

    GemCard(
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.character_events_item_title, index + 1),
                style = MaterialTheme.typography.titleMedium,
                color = colors.neonAccent
            )
            IconButton(
                onClick = { onDeleteEvent(index) }
            ) {
                Icon(
                    imageVector = IconsManager.Delete,
                    contentDescription = stringResource(Res.string.character_events_delete_action),
                    tint = colors.error
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Выбор уровня
        Text(
            text = stringResource(Res.string.character_events_required_level),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        val allPossibleLevels = listOf(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65)
        val selectedInOtherEvents = allEvents
            .filterIndexed { idx, _ -> idx != index }
            .map { it.requiredLevel }
        val availableLevels = allPossibleLevels
            .filter { it !in selectedInOtherEvents }
            .map { it.toString() }
            
        AnimateDropdownMenu(
            selectedItem = event.requiredLevel.toString(),
            itemsList = availableLevels,
            onChangeItem = { onRequiredLevelChanged(index, it.toIntOrNull() ?: event.requiredLevel) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Название события
        Text(
            text = stringResource(Res.string.character_events_name_label),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        TechnoTextField(
            value = event.eventName,
            onValueChange = { onEventNameChanged(index, it) },
            placeholder = stringResource(Res.string.character_events_name_placeholder),
            maxLength = 100,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Сценарий
        Text(
            text = stringResource(Res.string.character_events_scenario_label),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        TechnoTextField(
            value = event.eventTriggerScenario,
            onValueChange = { onTriggerScenarioChanged(index, it) },
            placeholder = stringResource(Res.string.character_events_scenario_placeholder),
            maxLength = 500,
            singleLine = false,
            minLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Изменение черты
        Text(
            text = stringResource(Res.string.character_events_trait_label),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        TechnoTextField(
            value = event.eventPermanentTrait,
            onValueChange = { onPermanentTraitChanged(index, it) },
            placeholder = stringResource(Res.string.character_events_trait_placeholder),
            maxLength = 500,
            singleLine = false,
            minLines = 3
        )
    }
}
