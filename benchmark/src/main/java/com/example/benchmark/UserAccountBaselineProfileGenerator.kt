package com.example.benchmark

import android.os.SystemClock
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val USER_ACCOUNT_TARGET_PACKAGE = "com.ensodai.avalon"
private const val USER_ACCOUNT_WAIT_TIMEOUT_MS = 30_000L
private const val USER_ACCOUNT_SHORT_SETTLE_MS = 700L
private const val USER_ACCOUNT_MEDIUM_SETTLE_MS = 1_200L
private const val USER_ACCOUNT_LONG_SETTLE_MS = 3_000L
private const val USER_ACCOUNT_APP_ROOT_TAG = "benchmark_app_root"

@RunWith(AndroidJUnit4::class)
class UserAccountBaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateUserAccountProfile() = baselineProfileRule.collect(
        packageName = USER_ACCOUNT_TARGET_PACKAGE,
        maxIterations = 10,
        includeInStartupProfile = false
    ) {
        pressHome()
        startActivityAndWait()

        waitForTag(USER_ACCOUNT_APP_ROOT_TAG)
        waitForTag("benchmark_main_account_button")

        repeat(3) {
            warmUserAccountOpenClose()
        }
    }

    private fun MacrobenchmarkScope.warmUserAccountOpenClose() {
        clickTag("benchmark_main_account_button")
        waitForTag("benchmark_user_account_screen")
        device.waitForIdle()
        SystemClock.sleep(USER_ACCOUNT_MEDIUM_SETTLE_MS)

        // Let the screen replace cached values with fresh ones before we leave it.
        SystemClock.sleep(USER_ACCOUNT_LONG_SETTLE_MS)

        device.findObject(By.res("benchmark_user_account_screen"))?.apply {
            setGestureMargin(device.displayWidth / 5)
            swipe(Direction.UP, 0.75f)
            swipe(Direction.DOWN, 0.75f)
        }

        warmUserAccountCards()
        togglePersonaSectionTwice()
        returnToMainScreen()
        SystemClock.sleep(USER_ACCOUNT_SHORT_SETTLE_MS)
    }

    private fun MacrobenchmarkScope.warmUserAccountCards() {
        val cardTags = listOf(
            "benchmark_user_account_profile_card",
            "benchmark_user_account_quota_card",
            "benchmark_user_account_batteries_card",
            "benchmark_user_account_characters_card",
            "benchmark_user_account_persona_card"
        )

        cardTags.forEach { tag ->
            clickTag(tag)
            SystemClock.sleep(USER_ACCOUNT_MEDIUM_SETTLE_MS)
            returnToUserAccountScreen()
            SystemClock.sleep(USER_ACCOUNT_SHORT_SETTLE_MS)
        }
    }

    private fun MacrobenchmarkScope.returnToUserAccountScreen() {
        repeat(4) {
            if (waitForOptionalTag("benchmark_user_account_screen", 1_000L) != null) {
                return
            }

            device.pressBack()
            device.waitForIdle()
            SystemClock.sleep(USER_ACCOUNT_SHORT_SETTLE_MS)
        }

        waitForTag("benchmark_user_account_screen")
    }

    private fun MacrobenchmarkScope.returnToMainScreen() {
        repeat(4) {
            if (waitForOptionalTag("benchmark_main_account_button", 1_000L) != null) {
                return
            }

            device.pressBack()
            device.waitForIdle()
            SystemClock.sleep(USER_ACCOUNT_SHORT_SETTLE_MS)
        }

        waitForTag("benchmark_main_account_button")
    }

    private fun MacrobenchmarkScope.togglePersonaSectionTwice() {
        clickTag("benchmark_user_account_persona_card")
        SystemClock.sleep(USER_ACCOUNT_MEDIUM_SETTLE_MS)
        clickTag("benchmark_user_account_persona_card")
        SystemClock.sleep(USER_ACCOUNT_MEDIUM_SETTLE_MS)
    }

    private fun MacrobenchmarkScope.clickTag(tag: String) {
        waitForTag(tag).click()
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.waitForTag(tag: String) =
        device.wait(
            androidx.test.uiautomator.Until.findObject(By.res(tag)),
            USER_ACCOUNT_WAIT_TIMEOUT_MS
        ) ?: error("Benchmark tag not found: $tag")

    private fun MacrobenchmarkScope.waitForOptionalTag(tag: String, timeoutMs: Long) =
        device.wait(
            androidx.test.uiautomator.Until.findObject(By.res(tag)),
            timeoutMs
        )
}
