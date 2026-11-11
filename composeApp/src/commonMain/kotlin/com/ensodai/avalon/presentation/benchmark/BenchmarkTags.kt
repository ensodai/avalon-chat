package com.ensodai.avalon.presentation.benchmark

object BenchmarkTags {
    const val MainAccountButton = "benchmark_main_account_button"
    const val MainPager = "benchmark_main_pager"
    const val MainAllChatsTab = "benchmark_main_tab_all_chats"
    const val MainUserChatsTab = "benchmark_main_tab_user_chats"
    const val MainCharactersGrid = "benchmark_main_characters_grid"
    const val MainUserChatsList = "benchmark_main_user_chats_list"

    const val UserAccountScreen = "benchmark_user_account_screen"
    const val UserAccountProfileCard = "benchmark_user_account_profile_card"
    const val UserAccountResourcesCard = "benchmark_user_account_resources_card"
    const val UserAccountQuotaCard = "benchmark_user_account_quota_card"
    const val UserAccountBatteriesCard = "benchmark_user_account_batteries_card"
    const val UserAccountCharactersCard = "benchmark_user_account_characters_card"
    const val UserAccountPersonaCard = "benchmark_user_account_persona_card"

    const val PaymentScreen = "benchmark_payment_screen"
    const val PaymentCloseButton = "benchmark_payment_close_button"
    const val PaymentCardsPager = "benchmark_payment_cards_pager"

    const val ChatScreen = "benchmark_chat_screen"
    const val ChatMessagesList = "benchmark_chat_messages_list"
    const val ChatInputField = "benchmark_chat_input_field"
    const val ChatProfileButton = "benchmark_chat_profile_button"

    const val CharacterProfileScreen = "benchmark_character_profile_screen"
    const val CharacterProfileMenuButton = "benchmark_character_profile_menu_button"
    const val CharacterProfileEditMenuItem = "benchmark_character_profile_edit_menu_item"

    const val EditCharacterScreen = "benchmark_edit_character_screen"
    const val EditCharacterImagesCard = "benchmark_edit_character_images_card"
    const val EditCharacterNameCard = "benchmark_edit_character_name_card"
    const val EditCharacterShortDescriptionCard = "benchmark_edit_character_short_description_card"
    const val EditCharacterFullDescriptionCard = "benchmark_edit_character_full_description_card"
    const val EditCharacterScenarioCard = "benchmark_edit_character_scenario_card"
    const val EditCharacterMesExampleCard = "benchmark_edit_character_mes_example_card"
    const val EditCharacterTemperatureCard = "benchmark_edit_character_temperature_card"
    const val EditCharacterFirstMessageCard = "benchmark_edit_character_first_message_card"
    const val EditCharacterWelcomeCard = "benchmark_edit_character_welcome_card"
    const val EditCharacterLorebookCard = "benchmark_edit_character_lorebook_card"
    const val EditCharacterPrivacyCard = "benchmark_edit_character_privacy_card"

    fun userChatItem(index: Int): String = "benchmark_user_chat_item_$index"

    fun characterGridItem(index: Int): String = "benchmark_character_grid_item_$index"

    fun chatMessageItem(index: Int): String = "benchmark_chat_message_item_$index"
}
