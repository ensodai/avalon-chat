package com.ensodai.avalon

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import com.ensodai.avalon.navigation.AvalonNavHost
import com.ensodai.avalon.navigation.AvalonSwipeLockEffect
import com.ensodai.avalon.navigation.DeepLinkManager
import com.ensodai.avalon.navigation.LocalAvalonNavController
import com.ensodai.avalon.navigation.Route
import com.ensodai.avalon.navigation.rememberAvalonNavController
import com.ensodai.avalon.presentation.addCharacterScreen.AddCharacterScreen
import com.ensodai.avalon.presentation.appUpgradeScreen.AppUpgradeScreen
import com.ensodai.avalon.presentation.archiveScreen.ArchiveScreen
import com.ensodai.avalon.presentation.characterProfileScreen.CharacterProfileScreen
import com.ensodai.avalon.presentation.chatScreen.ChatScreen
import com.ensodai.avalon.presentation.chatVariantScreen.ChatVariantScreen
import com.ensodai.avalon.presentation.commonComponents.TechnoUndoStack
import com.ensodai.avalon.presentation.commonComponents.snackbar.AppSnackbarHost
import com.ensodai.avalon.presentation.commonComponents.snackbar.LocalAppSnackbarController
import com.ensodai.avalon.presentation.commonComponents.snackbar.rememberAppSnackbarController
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.EditCharacterAvatarScreen
import com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.EditCharacterPrivatePhotosScreen
import com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.EditCharacterFullDescriptionScreen
import com.ensodai.avalon.presentation.editCharacterTextScreen.EditCharacterTextScreen
import com.ensodai.avalon.presentation.editCharacterScreen.EditCharacterScreen
import com.ensodai.avalon.presentation.editTemperatureScreen.EditTemperatureScreen
import com.ensodai.avalon.presentation.editUserProfileScreen.EditUserProfileScreen
import com.ensodai.avalon.presentation.editWelcomeCardScreen.EditWelcomeCardScreen
import com.ensodai.avalon.presentation.characterLorebooksScreen.CharacterLorebooksScreen
import com.ensodai.avalon.presentation.editLorebookScreen.EditLorebookScreen
import com.ensodai.avalon.presentation.lorebookDetailsScreen.LorebookDetailsScreen
import com.ensodai.avalon.presentation.importCharacterScreen.ImportCharacterScreen
import com.ensodai.avalon.presentation.loginScreen.LoginScreen

import com.ensodai.avalon.presentation.mainScreen.MainScreen
import com.ensodai.avalon.presentation.navigation.LocalSnackbarBottomPadding
import com.ensodai.avalon.presentation.navigation.MainViewModel
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.presentation.navigation.NavigationAvalon
import com.ensodai.avalon.presentation.paymentScreen.PaymentScreen
import com.ensodai.avalon.presentation.personaScreen.PersonaScreen
import com.ensodai.avalon.presentation.settingScreen.SettingScreen
import com.ensodai.avalon.presentation.splashScreen.SplashScreenContent
import com.ensodai.avalon.presentation.splashScreen.SplashViewModel
import com.ensodai.avalon.presentation.userAccountScreen.UserAccountScreen
import com.ensodai.avalon.presentation.userAiSettingsScreen.UserAiSettingsScreen
import com.ensodai.avalon.presentation.selectAiProviderScreen.SelectAiProviderScreen
import com.ensodai.avalon.presentation.summaryExplanationScreen.SummaryExplanationScreen
import com.ensodai.avalon.presentation.userCharactersScreen.UserCharacterScreen
import com.ensodai.avalon.presentation.liveCallScreen.LiveCallScreen
import com.ensodai.avalon.theme.GemTheme
import com.ensodai.avalon.theme.AvalonThemePresets
import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import com.ensodai.avalon.presentation.characterEventsScreen.CharacterEventsScreen
import com.ensodai.avalon.presentation.relationshipRoadScreen.RelationshipRoadScreen
import com.ensodai.avalon.presentation.customizationScreen.CustomizationScreen
import com.ensodai.avalon.presentation.customizationScreen.ChatPresetsScreen
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import com.ensodai.avalon.presentation.characterOutreachSettingsScreen.CharacterOutreachSettingsScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.toSize
import com.ensodai.avalon.navigation.PaneRole
import com.ensodai.avalon.navigation.getPaneRole
import com.ensodai.avalon.presentation.mainScreen.AdaptiveWorkspaceScreen

@Composable
fun App() {
    val splashViewModel: SplashViewModel = koinViewModel()
    val appSettingsRepository: AppSettingsRepository = koinInject()
    val customColors by appSettingsRepository.getAvalonColorsFlow().collectAsState(AvalonThemePresets.fallback)
    val showAiWarning by appSettingsRepository.getShowAiWarningFlow().collectAsState(true)
    val chatRoleplayPreset by appSettingsRepository.getChatRoleplayPresetFlow().collectAsState(ChatRoleplayPreset.STANDARD_BUBBLE)

    val windowSizeClass = calculateWindowSizeClassCommon()
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    val configuration = remember {
        SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Route.SplashScreen::class)
                    subclass(Route.AppUpgradeScreen::class)
                    subclass(Route.MainScreen::class)
                    subclass(Route.LoginScreen::class)
                    subclass(Route.SettingsScreen::class)
                    subclass(Route.ChatScreen::class)
                    subclass(Route.CharacterProfileScreen::class)
                    subclass(Route.AddCharacterScreen::class)
                    subclass(Route.ImportCharacterScreen::class)
                    subclass(Route.UserAccountScreen::class)
                    subclass(Route.UserAiSettingsScreen::class)
                    subclass(Route.PersonaScreen::class)
                    subclass(Route.ArchiveScreen::class)
                    subclass(Route.ChatVariantScreen::class)
                    subclass(Route.EditUserProfileScreen::class)
                    subclass(Route.EditCharacterScreen::class)
                    subclass(Route.EditCharacterFullDescriptionScreen::class)
                    subclass(Route.EditCharacterTextScreen::class)
                    subclass(Route.EditCharacterAvatarScreen::class)
                    subclass(Route.EditTemperatureScreen::class)
                    subclass(Route.EditWelcomeCardScreen::class)
                    subclass(Route.EditLorebookScreen::class)
                    subclass(Route.LorebookDetailsScreen::class)
                    subclass(Route.CharacterLorebooksScreen::class)
                    subclass(Route.PaymentScreen::class)
                    subclass(Route.UserCharacterScreen::class)
                    subclass(Route.LiveCallScreen::class)
                    subclass(Route.CustomizationScreen::class)
                    subclass(Route.CharacterEventsScreen::class)
                    subclass(Route.RelationshipRoadScreen::class)
                    subclass(Route.CharacterOutreachSettingsScreen::class)
                    subclass(Route.EditCharacterPrivatePhotosScreen::class)
                    subclass(Route.ChatPresetsScreen::class)
                    subclass(Route.SelectAiProviderScreen::class)
                    subclass(Route.SummaryExplanationScreen::class)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val backStack = rememberNavBackStack(configuration, Route.SplashScreen) as NavBackStack<Route>
    val controller = rememberAvalonNavController(backStack)
    val isAuthorized by splashViewModel.isAuthorized.collectAsState()
    val requiredVersion by splashViewModel.requiredVersion.collectAsState()
    val snackbarBottomPadding = remember { mutableStateOf(0.dp) }
    val animatedSnackbarPadding by animateDpAsState(
        targetValue = snackbarBottomPadding.value,
        label = "SnackbarPaddingAnimation"
    )
    val appSnackbarController = rememberAppSnackbarController()
    val mainViewModel: MainViewModel = koinViewModel()
    val mainViewState by mainViewModel.viewState.collectAsState()

    val deepLinkManager: DeepLinkManager = koinInject()
    val pendingRoute by deepLinkManager.pendingRoute.collectAsState()

    val platformMetadata: com.ensodai.avalon.domain.model.PlatformMetadata = koinInject()
    val isWeb = platformMetadata.platform == "Web"
    val isGlobalSwipeEnabled = !(isWeb && !isCompact)

    LaunchedEffect(isAuthorized, requiredVersion) {
        val currentRequiredVersion = requiredVersion
        val targetRoute = when {
            currentRequiredVersion != null -> Route.AppUpgradeScreen(currentRequiredVersion)
            isAuthorized == null -> Route.SplashScreen
            isAuthorized == true -> Route.MainScreen
            else -> Route.LoginScreen
        }

        if (controller.backStack.size != 1 || controller.backStack.last() != targetRoute) {
            controller.resetTo(targetRoute)
        }
    }

    LaunchedEffect(isAuthorized, pendingRoute, controller.backStack.lastOrNull()) {
        if (isAuthorized == true && pendingRoute != null && controller.backStack.lastOrNull() == Route.MainScreen) {
            val route = pendingRoute!!
            deepLinkManager.clearPendingRoute()
            controller.navigate(route)
        }
    }

    CompositionLocalProvider(
        LocalAvalonNavController provides controller,
        LocalSnackbarBottomPadding provides snackbarBottomPadding,
        LocalAppSnackbarController provides appSnackbarController
    ) {
        GemTheme(customColors = customColors!!) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                if (!constraints.hasBoundedWidth || !constraints.hasBoundedHeight) {
                    return@BoxWithConstraints
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        AppSnackbarHost(
                            hostState = appSnackbarController.hostState
                        )
                    }
                ) { _ ->
                    AvalonNavHost<Route>(
                        controller = controller,
                        groupStackTransformer = { logicalStack ->
                            if (isCompact) return@AvalonNavHost logicalStack
                            
                            val result = mutableListOf<Route>()
                            var hasWorkspace = false
                            for (route in logicalStack) {
                                if (route.getPaneRole() == PaneRole.FULLSCREEN) {
                                    result.add(route)
                                    hasWorkspace = false
                                } else {
                                    if (!hasWorkspace) {
                                        result.add(route) // Базовый экран воркспейса
                                        hasWorkspace = true
                                    }
                                }
                            }
                            result
                        },
                        isRouteSwipeable = { route -> 
                            if (!isGlobalSwipeEnabled) false 
                            else if (isCompact) true 
                            else route.getPaneRole() == PaneRole.FULLSCREEN 
                        },
                        enableTransitions = isCompact
                    ) { route ->
                        val navigation = remember(controller, showAiWarning) {
                            NavigationAvalon(controller, showAiWarning)
                        }

                        val role = route.getPaneRole()
                        if (!isCompact && role != PaneRole.FULLSCREEN) {
                            AdaptiveWorkspaceScreen(
                                parentController = controller,
                                baseNavigation = navigation,
                                windowSizeClass = windowSizeClass,
                                showAiWarning = showAiWarning,
                                chatRoleplayPreset = chatRoleplayPreset
                            )
                        } else {
                            when (route) {
                                is Route.SplashScreen -> SplashScreenContent()
                                is Route.AppUpgradeScreen -> AppUpgradeScreen(requiredVersion = route.requiredVersion)
                                is Route.LoginScreen -> LoginScreen()
                                is Route.MainScreen -> MainScreen(navigation = navigation)
                            is Route.SettingsScreen -> SettingScreen(
                                navigation = navigation,
                                argumentGuide = route.guide
                            )

                            is Route.ChatScreen -> ChatScreen(
                                navigation = navigation,
                                characterPublicId = route.characterPublicId,
                                showAiWarning = route.showAiWarning,
                                chatRoleplayPreset = chatRoleplayPreset
                            )

                            is Route.CharacterProfileScreen -> CharacterProfileScreen(
                                navigation = navigation,
                                characterId = route.characterId,
                                fullScreenImage = route.fullScreenImage,
                                characterHasAvatars = route.characterHasAvatars
                            )

                            is Route.AddCharacterScreen -> AvalonLegacySwipeHost(navigation) { enable, disable, nav ->
                                AddCharacterScreen(
                                    navigation = nav,
                                    onEnableSwipeable = enable,
                                    onDisableSwipeable = disable
                                )
                            }

                            is Route.ImportCharacterScreen -> AvalonLegacySwipeHost(navigation) { enable, disable, nav ->
                                ImportCharacterScreen(
                                    navigation = nav,
                                    onEnableSwipeable = enable,
                                    onDisableSwipeable = disable
                                )
                            }

                            is Route.UserAccountScreen -> UserAccountScreen(navigation = navigation)
                            is Route.UserAiSettingsScreen -> UserAiSettingsScreen(navigation = navigation)
                            is Route.PersonaScreen -> AvalonLegacySwipeHost(navigation) { enable, disable, nav ->
                                PersonaScreen(
                                    navigation = nav,
                                    id = route.id,
                                    isUpdatePersona = route.isUpdatePersona,
                                    onEnableSwipeable = enable,
                                    onDisableSwipeable = disable
                                )
                            }

                            is Route.ArchiveScreen -> ArchiveScreen(navigation = navigation)
                            is Route.ChatVariantScreen -> ChatVariantScreen(
                                navigation = navigation,
                                characterId = route.characterId
                            )

                            is Route.EditUserProfileScreen -> AvalonLegacySwipeHost(navigation) { enable, disable, nav ->
                                EditUserProfileScreen(
                                    navigation = nav,
                                    nickname = route.nickname,
                                    onEnableSwipeable = enable,
                                    onDisableSwipeable = disable
                                )
                            }

                            is Route.EditCharacterScreen -> EditCharacterScreen(
                                navigation = navigation,
                                characterId = route.characterId
                            )

                            is Route.EditCharacterFullDescriptionScreen -> AvalonLegacySwipeHost(
                                navigation
                            ) { enable, disable, nav ->
                                EditCharacterFullDescriptionScreen(
                                    navigation = nav,
                                    characterId = route.characterId,
                                    onEnableSwipeable = enable,
                                    onDisableSwipeable = disable
                                )
                            }

                            is Route.EditCharacterTextScreen -> AvalonLegacySwipeHost(
                                navigation
                            ) { enable, disable, nav ->
                                EditCharacterTextScreen(
                                    navigation = nav,
                                    characterId = route.characterId,
                                    textType = route.textType,
                                    onEnableSwipeable = enable,
                                    onDisableSwipeable = disable
                                )
                            }

                            is Route.EditCharacterAvatarScreen -> AvalonLegacySwipeHost(navigation) { enable, disable, nav ->
                                EditCharacterAvatarScreen(
                                    navigation = nav,
                                    characterId = route.characterId,
                                    onEnableSwipeable = enable,
                                    onDisableSwipeable = disable
                                )
                            }

                            is Route.EditCharacterPrivatePhotosScreen -> AvalonLegacySwipeHost(navigation) { enable, disable, nav ->
                                EditCharacterPrivatePhotosScreen(
                                    navigation = nav,
                                    characterId = route.characterId,
                                    onEnableSwipeable = enable,
                                    onDisableSwipeable = disable
                                )
                            }

                            is Route.EditTemperatureScreen -> AvalonLegacySwipeHost(navigation) { enable, disable, nav ->
                                EditTemperatureScreen(
                                    navigation = nav,
                                    characterId = route.characterId,
                                    onEnableSwipeable = enable,
                                    onDisableSwipeable = disable
                                )
                            }

                            is Route.EditWelcomeCardScreen -> AvalonLegacySwipeHost(navigation) { enable, disable, nav ->
                                EditWelcomeCardScreen(
                                    navigation = nav,
                                    characterId = route.characterId,
                                    onEnableSwipeable = enable,
                                    onDisableSwipeable = disable
                                )
                            }

                            is Route.EditLorebookScreen -> AvalonLegacySwipeHost(navigation) { enable, disable, nav ->
                                EditLorebookScreen(
                                    navigation = nav,
                                    lorebookId = route.lorebookId,
                                    onEnableSwipeable = enable,
                                    onDisableSwipeable = disable
                                )
                            }

                            is Route.LorebookDetailsScreen -> AvalonLegacySwipeHost(navigation) { enable, disable, nav ->
                                LorebookDetailsScreen(
                                    navigation = nav,
                                    lorebookId = route.lorebookId,
                                    onEnableSwipeable = enable,
                                    onDisableSwipeable = disable
                                )
                            }

                            is Route.CharacterLorebooksScreen -> AvalonLegacySwipeHost(navigation) { enable, disable, nav ->
                                CharacterLorebooksScreen(
                                    navigation = nav,
                                    characterId = route.characterId,
                                    onEnableSwipeable = enable,
                                    onDisableSwipeable = disable
                                )
                            }

                            is Route.PaymentScreen -> AvalonLegacySwipeHost(navigation) { enable, disable, nav ->
                                PaymentScreen(
                                    navigation = nav,
                                    onEnableSwipeable = enable,
                                    onDisableSwipeable = disable
                                )
                            }

                            is Route.UserCharacterScreen -> UserCharacterScreen(navigation = navigation)
                            is Route.LiveCallScreen -> LiveCallScreen(
                                navigation = navigation,
                                characterId = route.characterId
                            )

                            Route.CustomizationScreen -> CustomizationScreen(navigation = navigation)

                            Route.ChatPresetsScreen -> ChatPresetsScreen(navigation = navigation)

                            is Route.CharacterEventsScreen -> CharacterEventsScreen(
                                navigation = navigation,
                                characterId = route.characterId
                            )

                            is Route.RelationshipRoadScreen -> RelationshipRoadScreen(
                                navigation = navigation,
                                characterId = route.characterId
                            )

                            is Route.CharacterOutreachSettingsScreen -> CharacterOutreachSettingsScreen(
                                navigation = navigation,
                                characterId = route.characterId
                            )

                            is Route.SelectAiProviderScreen -> SelectAiProviderScreen(
                                navigation = navigation
                            )

                            is Route.SummaryExplanationScreen -> SummaryExplanationScreen(
                                navigation = navigation
                            )
                        }
                    }
                }
                TechnoUndoStack(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = animatedSnackbarPadding),
                    pendingDeletions = mainViewState.pendingDeletions,
                    onUndo = { item -> mainViewModel.action.undoDeletion(item) },
                    onForceDelete = { item -> mainViewModel.action.forceDelete(item) }
                )
            }
        }
    }
}
}
}

@Composable
internal fun AvalonLegacySwipeHost(
    navigation: Navigation,
    content: @Composable (
        onEnableSwipeable: () -> Unit,
        onDisableSwipeable: () -> Unit,
        navigation: Navigation
    ) -> Unit
) {
    val isSwipeLocked = remember { mutableStateOf(false) }
    val currentNavigation = rememberUpdatedState(navigation)

    AvalonSwipeLockEffect(locked = isSwipeLocked.value)

    content(
        { isSwipeLocked.value = false },
        { isSwipeLocked.value = true },
        currentNavigation.value
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun calculateWindowSizeClassCommon(): WindowSizeClass {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val size = with(density) { windowInfo.containerSize.toSize().toDpSize() }
    return WindowSizeClass.calculateFromSize(size)
}
