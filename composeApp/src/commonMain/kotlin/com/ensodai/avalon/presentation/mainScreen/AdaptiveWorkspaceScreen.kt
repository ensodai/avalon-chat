package com.ensodai.avalon.presentation.mainScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.compose.runtime.Stable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.ensodai.avalon.navigation.AvalonNavController
import com.ensodai.avalon.navigation.PaneRole
import com.ensodai.avalon.navigation.Route
import com.ensodai.avalon.navigation.WorkspaceNavigation
import com.ensodai.avalon.navigation.getPaneRole
import com.ensodai.avalon.navigation.paneSwipeBack

import com.ensodai.avalon.presentation.addCharacterScreen.AddCharacterScreen
import com.ensodai.avalon.presentation.archiveScreen.ArchiveScreen
import com.ensodai.avalon.presentation.importCharacterScreen.ImportCharacterScreen
import com.ensodai.avalon.presentation.characterEventsScreen.CharacterEventsScreen
import com.ensodai.avalon.presentation.characterProfileScreen.CharacterProfileScreen
import com.ensodai.avalon.presentation.chatScreen.ChatScreen
import com.ensodai.avalon.presentation.chatVariantScreen.ChatVariantScreen
import com.ensodai.avalon.presentation.customizationScreen.ChatPresetsScreen
import com.ensodai.avalon.presentation.customizationScreen.CustomizationScreen
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.EditCharacterAvatarScreen
import com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.EditCharacterFullDescriptionScreen
import com.ensodai.avalon.presentation.editCharacterTextScreen.EditCharacterTextScreen
import com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.EditCharacterPrivatePhotosScreen
import com.ensodai.avalon.presentation.editCharacterScreen.EditCharacterScreen
import com.ensodai.avalon.presentation.editTemperatureScreen.EditTemperatureScreen
import com.ensodai.avalon.presentation.editUserProfileScreen.EditUserProfileScreen
import com.ensodai.avalon.presentation.editWelcomeCardScreen.EditWelcomeCardScreen
import com.ensodai.avalon.presentation.characterLorebooksScreen.CharacterLorebooksScreen
import com.ensodai.avalon.presentation.editLorebookScreen.EditLorebookScreen
import com.ensodai.avalon.presentation.lorebookDetailsScreen.LorebookDetailsScreen
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.koinInject
import com.ensodai.avalon.domain.model.PlatformMetadata
import com.ensodai.avalon.presentation.personaScreen.PersonaScreen
import com.ensodai.avalon.presentation.relationshipRoadScreen.RelationshipRoadScreen
import com.ensodai.avalon.presentation.settingScreen.SettingScreen
import com.ensodai.avalon.presentation.userAccountScreen.UserAccountScreen
import com.ensodai.avalon.presentation.userAiSettingsScreen.UserAiSettingsScreen
import com.ensodai.avalon.presentation.selectAiProviderScreen.SelectAiProviderScreen
import com.ensodai.avalon.presentation.summaryExplanationScreen.SummaryExplanationScreen
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import com.ensodai.avalon.presentation.characterOutreachSettingsScreen.CharacterOutreachSettingsScreen
import com.ensodai.avalon.presentation.userCharactersScreen.*

/**
 * Изолированное хранилище состояния (Lifecycle + ViewModelStore) для каждой панели.
 */
@Stable
class PaneEntry(val id: String) : LifecycleOwner, ViewModelStoreOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun resume() {
        if (lifecycleRegistry.currentState != Lifecycle.State.DESTROYED) {
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        }
    }

    fun pause() {
        if (lifecycleRegistry.currentState != Lifecycle.State.DESTROYED) {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }
    }

    fun dispose() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        store.clear()
    }
}

@Composable
fun rememberPaneEntries(logicalStack: List<Route>): Map<Route, PaneEntry> {
    val entries = remember { mutableMapOf<Route, PaneEntry>() }

    // Убиваем ViewModels экранов, которые исчезли из логического стека
    LaunchedEffect(logicalStack) {
        val currentRoutes = logicalStack.toSet()
        val it = entries.iterator()
        while (it.hasNext()) {
            val (route, entry) = it.next()
            if (!currentRoutes.contains(route)) {
                entry.dispose()
                it.remove()
            }
        }
    }

    // Создаем записи для новых экранов
    for (route in logicalStack) {
        if (!entries.containsKey(route)) {
            entries[route] = PaneEntry(route.toString())
        }
    }

    return entries
}

/**
 * Оркестратор адаптивного рабочего пространства.
 * Полностью заменяет старый DesktopWorkspaceScreen.
 * Читает единый logicalStack, определяет активные экраны для 3 ролей (PRIMARY, DETAIL, EXTRA),
 * и распределяет их по физическим слотам (Row + AnimatedVisibility) в зависимости от ширины экрана.
 */
@Composable
fun AdaptiveWorkspaceScreen(
    parentController: AvalonNavController<Route>,
    baseNavigation: Navigation,
    windowSizeClass: WindowSizeClass,
    showAiWarning: Boolean,
    chatRoleplayPreset: ChatRoleplayPreset
) {
    val saveableStateHolder = androidx.compose.runtime.saveable.rememberSaveableStateHolder()

    // Читаем единый логический стек как стейт
    val logicalStack = parentController.backStack.toList()
    
    // Парсим стек с конца (LIFO), чтобы найти самые актуальные экраны для каждой роли
    var primaryRoute: Route? = null
    var detailRoute: Route? = null
    var extraRoute: Route? = null
    
    val platformMetadata: PlatformMetadata = koinInject()
    val isWeb = platformMetadata.platform == "Web"
    val isGlobalSwipeEnabled = !isWeb
    
    // 1. Ищем явно открытые экраны в стеке
    for (route in logicalStack.reversed()) {
        val role = route.getPaneRole()
        if (role == PaneRole.EXTRA && extraRoute == null) extraRoute = route
        if (role == PaneRole.DETAIL && detailRoute == null) detailRoute = route
        if (role == PaneRole.PRIMARY && primaryRoute == null) primaryRoute = route
    }

    // 2. Умная контекстная панель: если юзер явно не открыл EXTRA-экран,
    // спрашиваем центральную панель (DETAIL), есть ли у нее дефолтный "сосед".
    if (extraRoute == null && detailRoute != null) {
        extraRoute = detailRoute.getDefaultExtraRoute()
    }

    // Создаем декоратор (Shallow Stack Extractor), который гарантирует, что стек не разрастается
    val workspaceNav = remember(parentController, baseNavigation) { 
        WorkspaceNavigation(parentController, baseNavigation) 
    }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val windowInfo = androidx.compose.ui.platform.LocalWindowInfo.current
    
    // Защита от крашей Compose Wasm при непроинициализированных размерах окна
    val rawWidth = windowInfo.containerSize.width
    if (rawWidth == 0 || rawWidth >= 16777215) {
        return // Ждем нормального размера
    }
    
    val screenWidth = with(density) { rawWidth.toDp() }

    // 1. Определяем, какие панели ДОЛЖНЫ быть видимы (логика)
    val topRole = logicalStack.lastOrNull()?.getPaneRole()

    val isPrimaryVisible: Boolean
    val isDetailVisible: Boolean
    val isExtraVisible: Boolean

    when {
        screenWidth < 600.dp -> {
            // На мобилке (Compact) помещается только 1 панель
            isPrimaryVisible = topRole == PaneRole.PRIMARY || topRole == null
            isDetailVisible = topRole == PaneRole.DETAIL
            isExtraVisible = topRole == PaneRole.EXTRA
        }
        screenWidth < 1050.dp -> {
            // Планшет (Medium): помещается ровно 2 панели
            if (topRole == PaneRole.EXTRA) {
                // Если юзер явно зашел в профиль (EXTRA) - показываем Чат (DETAIL) и Профиль (EXTRA)
                isPrimaryVisible = false
                isDetailVisible = detailRoute != null
                isExtraVisible = true
            } else {
                // Иначе показываем Список (PRIMARY) и Чат (DETAIL)
                isPrimaryVisible = true
                isDetailVisible = detailRoute != null
                isExtraVisible = false // Скрываем EXTRA, места нет
            }
        }
        else -> { 
            // Десктоп (Expanded): Помещаются все 3 панели
            isPrimaryVisible = true
            isDetailVisible = detailRoute != null
            isExtraVisible = extraRoute != null
        }
    }

    // 3. Собираем активные экраны, чтобы сгенерировать для них ViewModel и стейты.
    // Обязательно добавляем дефолтный extraRoute, если он физически показывается!
    val activeRoutes = remember(logicalStack, extraRoute, isExtraVisible) {
        val routes = logicalStack.toMutableList()
        if (isExtraVisible && extraRoute != null && !routes.contains(extraRoute)) {
            routes.add(extraRoute!!)
        }
        routes
    }
    val paneEntries = rememberPaneEntries(activeRoutes)

    // Избегаем крашей с бесконечной шириной благодаря нашей защите сверху

    Row(modifier = Modifier.fillMaxSize()) {
            // --- 1. ЛЕВАЯ ПАНЕЛЬ (PRIMARY) ---
            AnimatedVisibility(
                visible = isPrimaryVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                       .width(320.dp)
                       .fillMaxHeight()
                ) {
                    if (primaryRoute != null) {
                        val entry = paneEntries[primaryRoute]
                        if (entry != null) {
                            saveableStateHolder.SaveableStateProvider(entry.id) {
                                RenderRoute(
                                    route = primaryRoute, 
                                    nav = workspaceNav, 
                                    showAiWarning = showAiWarning, 
                                    chatRoleplayPreset = chatRoleplayPreset,
                                    paneEntry = entry
                                )
                            }
                        }
                    }
                }
            }

            // Вертикальный разделитель
            if (isPrimaryVisible && isDetailVisible) {
                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            // --- 2. ЦЕНТРАЛЬНАЯ ПАНЕЛЬ (DETAIL) ---
            AnimatedVisibility(
                visible = isDetailVisible,
                modifier = Modifier.weight(1f), 
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .paneSwipeBack(
                            enabled = isGlobalSwipeEnabled && !isExtraVisible,
                            onPopRequest = { _ -> workspaceNav.navigateBack() }
                        )
                ) {
                    if (detailRoute != null) {
                        val entry = paneEntries[detailRoute]
                        if (entry != null) {
                            saveableStateHolder.SaveableStateProvider(entry.id) {
                                RenderRoute(
                                    route = detailRoute, 
                                    nav = workspaceNav, 
                                    showAiWarning = showAiWarning, 
                                    chatRoleplayPreset = chatRoleplayPreset,
                                    paneEntry = entry
                                )
                            }
                        }
                    } else {
                        DesktopEmptyChatPlaceholder()
                    }
                }
            }

            // Вертикальный разделитель
            if (isExtraVisible && isDetailVisible) {
                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            // --- 3. ПРАВАЯ ПАНЕЛЬ (EXTRA) ---
            AnimatedVisibility(
                visible = isExtraVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                       .width(320.dp)
                       .fillMaxHeight()
                       .paneSwipeBack(
                           enabled = isGlobalSwipeEnabled,
                           onPopRequest = { _ -> workspaceNav.navigateBack() }
                       )
                ) {
                    if (extraRoute != null) {
                        val entry = paneEntries[extraRoute]
                        if (entry != null) {
                            saveableStateHolder.SaveableStateProvider(entry.id) {
                                RenderRoute(
                                    route = extraRoute, 
                                    nav = workspaceNav, 
                                    showAiWarning = showAiWarning, 
                                    chatRoleplayPreset = chatRoleplayPreset,
                                    paneEntry = entry
                                )
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun RenderRoute(
    route: Route, 
    nav: WorkspaceNavigation,
    showAiWarning: Boolean,
    chatRoleplayPreset: ChatRoleplayPreset,
    paneEntry: PaneEntry
) {
    DisposableEffect(paneEntry) {
        paneEntry.resume()
        onDispose {
            paneEntry.pause()
        }
    }

    CompositionLocalProvider(
        LocalLifecycleOwner provides paneEntry,
        LocalViewModelStoreOwner provides paneEntry
    ) {
        when (route) {
            // --- PRIMARY ---
            is Route.MainScreen -> MainScreen(navigation = nav)
            is Route.ArchiveScreen -> ArchiveScreen(navigation = nav)
            is Route.UserCharacterScreen -> UserCharacterScreen(navigation = nav)
            is Route.SettingsScreen -> SettingScreen(navigation = nav, argumentGuide = route.guide)
            
            // --- DETAIL ---
            is Route.ChatScreen -> ChatScreen(
                navigation = nav,
                characterPublicId = route.characterPublicId,
                showAiWarning = showAiWarning,
                chatRoleplayPreset = chatRoleplayPreset
            )
            is Route.AddCharacterScreen -> AddCharacterScreen(navigation = nav, onEnableSwipeable = {}, onDisableSwipeable = {})
            is Route.ImportCharacterScreen -> ImportCharacterScreen(navigation = nav, onEnableSwipeable = {}, onDisableSwipeable = {})
            is Route.UserAccountScreen -> UserAccountScreen(navigation = nav)
            is Route.UserAiSettingsScreen -> UserAiSettingsScreen(navigation = nav)
            is Route.CustomizationScreen -> CustomizationScreen(navigation = nav)
            is Route.EditCharacterScreen -> EditCharacterScreen(navigation = nav, characterId = route.characterId)

            // --- EXTRA ---
            is Route.CharacterProfileScreen -> CharacterProfileScreen(
                navigation = nav,
                characterId = route.characterId,
                fullScreenImage = route.fullScreenImage,
                characterHasAvatars = route.characterHasAvatars
            )
            is Route.ChatVariantScreen -> ChatVariantScreen(navigation = nav, characterId = route.characterId)
            is Route.EditUserProfileScreen -> EditUserProfileScreen(navigation = nav, nickname = route.nickname, onEnableSwipeable = {}, onDisableSwipeable = {})
            is Route.ChatPresetsScreen -> ChatPresetsScreen(navigation = nav)
            is Route.PersonaScreen -> PersonaScreen(navigation = nav, id = route.id, isUpdatePersona = route.isUpdatePersona, onEnableSwipeable = {}, onDisableSwipeable = {})
            is Route.EditCharacterFullDescriptionScreen -> EditCharacterFullDescriptionScreen(navigation = nav, characterId = route.characterId, onEnableSwipeable = {}, onDisableSwipeable = {})
            is Route.EditCharacterTextScreen -> EditCharacterTextScreen(navigation = nav, characterId = route.characterId, textType = route.textType, onEnableSwipeable = {}, onDisableSwipeable = {})
            is Route.EditCharacterAvatarScreen -> EditCharacterAvatarScreen(navigation = nav, characterId = route.characterId, onEnableSwipeable = {}, onDisableSwipeable = {})
            is Route.EditCharacterPrivatePhotosScreen -> EditCharacterPrivatePhotosScreen(navigation = nav, characterId = route.characterId, onEnableSwipeable = {}, onDisableSwipeable = {})
            is Route.EditTemperatureScreen -> EditTemperatureScreen(navigation = nav, characterId = route.characterId, onEnableSwipeable = {}, onDisableSwipeable = {})
            is Route.EditWelcomeCardScreen -> EditWelcomeCardScreen(navigation = nav, characterId = route.characterId, onEnableSwipeable = {}, onDisableSwipeable = {})
            is Route.EditLorebookScreen -> EditLorebookScreen(navigation = nav, lorebookId = route.lorebookId, onEnableSwipeable = {}, onDisableSwipeable = {})
            is Route.LorebookDetailsScreen -> LorebookDetailsScreen(navigation = nav, lorebookId = route.lorebookId, onEnableSwipeable = {}, onDisableSwipeable = {})
            is Route.CharacterLorebooksScreen -> CharacterLorebooksScreen(navigation = nav, characterId = route.characterId, onEnableSwipeable = {}, onDisableSwipeable = {})
            is Route.CharacterEventsScreen -> CharacterEventsScreen(navigation = nav, characterId = route.characterId)
            is Route.RelationshipRoadScreen -> RelationshipRoadScreen(navigation = nav, characterId = route.characterId)
            is Route.CharacterOutreachSettingsScreen -> CharacterOutreachSettingsScreen(navigation = nav, characterId = route.characterId)
            is Route.SelectAiProviderScreen -> SelectAiProviderScreen(navigation = nav)
            is Route.SummaryExplanationScreen -> SummaryExplanationScreen(navigation = nav)

            else -> Unit
        }
    }
}

@Composable
private fun DesktopEmptyChatPlaceholder() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "Выберите чат",
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
