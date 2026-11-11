package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble

//@Composable
//fun MessageActionButtons(
//    modifier: Modifier = Modifier,
//    actionButtons: List<MessageActionEntity>?,
//    onSignIn: (String) -> Unit,
//    onDismissSignIn: () -> Unit,
//    onErrorSignIn: (Exception) -> Unit
//) {
//    val context = LocalContext.current
//    if (actionButtons == null) return
//    for (button in actionButtons) {
//        when (button.action) {
//            ChatMessageAction.REQUEST_AUTHORIZATION -> {
//                GoogleSignInButton(
//                    modifier = modifier,
//                    onSignInGoogleToken = { token -> onSignIn(token) },
//                    onDismiss = { onDismissSignIn() },
//                    onError = { onErrorSignIn(it) },
//                    context = context
//                )
//            }
//            ChatMessageAction.SUCCESS_AUTHORIZATION -> {}
//            ChatMessageAction.BAD_AUTHORIZATION -> {}
//            ChatMessageAction.DAILY_LIMIT_EXCEEDED -> {}
//            ChatMessageAction.API_KEYS_ARE_OUT -> {}
//            ChatMessageAction.GUIDE_GET_API_KEY_1 -> {}
//            ChatMessageAction.GUIDE_GET_API_KEY_2 -> {}
//            ChatMessageAction.QUOTA_LIMIT -> {}
//            ChatMessageAction.GET_SYSTEM_API_KEY_FAIL -> {}
//            ChatMessageAction.UPGRADE_REQUIRED -> {}
//        }
//    }
//}