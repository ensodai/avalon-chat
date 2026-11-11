package com.ensodai.avalon.presentation.chatScreen.component.explosionEffect

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class ReactionDisplayItem(
    val id: String,
    val model: ReactionUiModel
)

@Composable
fun ReactionsLayout(
    modifier: Modifier = Modifier,
    userReaction: String?,
    characterReaction: String?,
    userAvatarUrl: String?,
    characterAvatarUrl: String?,
    onReactionClick: (String) -> Unit
) {
    // 1. Формируем список
    val reactionItems = remember(userReaction, characterReaction) {
        val items = mutableListOf<ReactionDisplayItem>()
        val mergedReaction = userReaction?.takeIf { it == characterReaction }

        if (mergedReaction != null) {
            items.add(
                ReactionDisplayItem(
                    id = mergedReaction,
                    model = ReactionUiModel(
                        mergedReaction,
                        2,
                        listOf(characterAvatarUrl, userAvatarUrl)
                    )
                )
            )
        } else {
            if (characterReaction != null) {
                items.add(
                    ReactionDisplayItem(
                        id = characterReaction,
                        model = ReactionUiModel(characterReaction, 1, listOf(characterAvatarUrl))
                    )
                )
            }
            if (userReaction != null) {
                items.add(
                    ReactionDisplayItem(
                        id = userReaction,
                        model = ReactionUiModel(userReaction, 1, listOf(userAvatarUrl))
                    )
                )
            }
        }
        items
    }

    // --- ЛОГИКА ИСПРАВЛЕНИЯ ---

    // Получаем список ID текущих элементов
    val currentIds = reactionItems.map { it.id }.toSet()

    // 2. Инициализируем "прошлые" реакции ТЕКУЩИМИ значениями при первом создании.
    // Благодаря этому при первом рендере isNew будет false для всех существующих.
    var previousReactions by remember { mutableStateOf(currentIds) }

    // Обновляем "прошлые" реакции только ПОСЛЕ отрисовки кадра
    SideEffect {
        previousReactions = currentIds
    }

    Row(
        modifier = modifier
            .animateContentSize(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioNoBouncy
                )
            ),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        reactionItems.forEach { item ->
            key(item.id) {
                // Если элемент есть в previousReactions, значит он БЫЛ на прошлом кадре (или при инициализации).
                // Если его нет — значит он только что добавлен юзером.
                val isNew = item.id !in previousReactions

                ReactionSlot(
                    model = item.model,
                    isNewlyAdded = isNew, // Передаем флаг новизны
                    onClick = { onReactionClick(item.id) }
                )
            }
        }
    }
}
