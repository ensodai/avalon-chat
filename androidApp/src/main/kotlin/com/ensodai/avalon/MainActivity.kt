package com.ensodai.avalon

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ensodai.avalon.navigation.DeepLinkManager
import com.ensodai.avalon.presentation.splashScreen.SplashViewModel
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {
    private val splashViewModel: SplashViewModel by inject()
    private val deepLinkManager: DeepLinkManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            splashViewModel.isAuthorized.value == null || !splashViewModel.isThemeLoaded.value
        }

        enableEdgeToEdge()
        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { testTagsAsResourceId = true }
                    .testTag("benchmark_app_root")
            ) {
                App()
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return
        val characterIdStr = intent.getStringExtra("character_id")
        if (characterIdStr != null) {
            try {
                val characterId = kotlin.uuid.Uuid.parse(characterIdStr)
                deepLinkManager.dispatchChatDeepLink(characterId)
            } catch (e: Exception) {
                // Игнорируем некорректные UUID
            }
        }
    }
}
