package com.ensodai.avalon.navigation

/**
 * Определяет семантическую роль экрана в адаптивном интерфейсе.
 */
enum class PaneRole {
    /** Основная навигация, список элементов (например, список чатов) */
    PRIMARY,
    
    /** Главный контент выбранного элемента (например, сам чат) */
    DETAIL,
    
    /** Дополнительная информация или редактирование (например, профиль персонажа) */
    EXTRA,
    
    /** Полноэкранные состояния (логин, сплеш, звонок, оплата) */
    FULLSCREEN
}

/**
 * Возвращает роль экрана для автоматического распределения по панелям в Desktop/Tablet.
 */
fun Route.getPaneRole(): PaneRole = when (this) {
    // PRIMARY
    is Route.MainScreen,
    is Route.ArchiveScreen,
    is Route.UserCharacterScreen,
    is Route.SettingsScreen -> PaneRole.PRIMARY

    // DETAIL
    is Route.ChatScreen,
    is Route.AddCharacterScreen,
    is Route.ImportCharacterScreen,
    is Route.UserAccountScreen,
    is Route.CustomizationScreen,
    is Route.EditCharacterScreen,
    is Route.UserAiSettingsScreen -> PaneRole.DETAIL

    // EXTRA
    is Route.CharacterProfileScreen,
    is Route.ChatVariantScreen,
    is Route.EditUserProfileScreen,
    is Route.ChatPresetsScreen,
    is Route.PersonaScreen,
    is Route.EditCharacterFullDescriptionScreen,
    is Route.EditCharacterTextScreen,
    is Route.EditCharacterAvatarScreen,
    is Route.EditCharacterPrivatePhotosScreen,
    is Route.EditTemperatureScreen,
    is Route.EditWelcomeCardScreen,
    is Route.CharacterLorebooksScreen,
    is Route.EditLorebookScreen,
    is Route.LorebookDetailsScreen,
    is Route.CharacterEventsScreen,
    is Route.RelationshipRoadScreen,
    is Route.CharacterOutreachSettingsScreen,
    is Route.SelectAiProviderScreen,
    is Route.SummaryExplanationScreen -> PaneRole.EXTRA

    // FULLSCREEN
    is Route.SplashScreen,
    is Route.LoginScreen,
    is Route.AppUpgradeScreen,
    is Route.PaymentScreen,
    is Route.LiveCallScreen -> PaneRole.FULLSCREEN
}
