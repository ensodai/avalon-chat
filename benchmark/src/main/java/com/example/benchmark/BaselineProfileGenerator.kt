package com.example.benchmark

import android.os.SystemClock
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TARGET_PACKAGE = "com.ensodai.avalon"
private const val WAIT_TIMEOUT_MS = 30_000L
private const val CHAT_KEYBOARD_SETTLE_MS = 2_000L
private const val SECOND_PAGE_SETTLE_MS = 4_000L
private const val SHORT_SETTLE_MS = 700L
private const val MEDIUM_SETTLE_MS = 1_200L
private const val EDIT_CARD_SETTLE_MS = 5_000L
private const val APP_ROOT_TAG = "benchmark_app_root"

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = TARGET_PACKAGE,
        maxIterations = 10,
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait()

        waitForTag(APP_ROOT_TAG)
        waitForTag("benchmark_main_pager")
        warmMainPager()
        waitForTag("benchmark_main_account_button")
        warmUserAccountAndPayment()
        warmUserChatsTab()

        repeat(3) { chatIndex ->
            warmChat(chatIndex)
        }
    }

    private fun MacrobenchmarkScope.warmUserAccountAndPayment() {
        clickTag("benchmark_main_account_button")
        waitForTag("benchmark_user_account_screen")
        device.waitForIdle()
        SystemClock.sleep(MEDIUM_SETTLE_MS)

        device.findObject(By.res("benchmark_user_account_screen"))?.apply {
            setGestureMargin(device.displayWidth / 5)
            swipe(Direction.UP, 0.75f)
            swipe(Direction.DOWN, 0.75f)
        }

        warmUserAccountCards()

        returnToMainScreenFromUserAccount()
    }

    private fun MacrobenchmarkScope.returnToMainScreenFromUserAccount() {
        repeat(4) {
            if (waitForOptionalTag("benchmark_main_account_button", 1_000L) != null) {
                return
            }

            device.pressBack()
            device.waitForIdle()
            SystemClock.sleep(SHORT_SETTLE_MS)
        }

        waitForTag("benchmark_main_account_button")
    }

    private fun MacrobenchmarkScope.warmMainPager() {
        openAllChatsPage()
        SystemClock.sleep(SECOND_PAGE_SETTLE_MS)

        openUserChatsPage()
        SystemClock.sleep(SHORT_SETTLE_MS)
    }

    private fun MacrobenchmarkScope.warmUserChatsTab() {
        clickTag("benchmark_main_tab_user_chats")
        waitForTag("benchmark_main_user_chats_list")
        device.waitForIdle()
        SystemClock.sleep(SHORT_SETTLE_MS)
    }

    private fun MacrobenchmarkScope.warmChat(chatIndex: Int) {
        clickTag("benchmark_user_chat_item_$chatIndex")
        waitForTag("benchmark_chat_screen")
        device.waitForIdle()

        SystemClock.sleep(CHAT_KEYBOARD_SETTLE_MS)
        clickTag("benchmark_chat_input_field")
        SystemClock.sleep(MEDIUM_SETTLE_MS)
        device.pressBack()
        SystemClock.sleep(SHORT_SETTLE_MS)

        longClickFirstExisting(
            "benchmark_chat_message_item_0",
            "benchmark_chat_message_item_1",
            "benchmark_chat_message_item_2",
            "benchmark_chat_message_item_3"
        )
        SystemClock.sleep(SHORT_SETTLE_MS)
        device.pressBack()
        SystemClock.sleep(SHORT_SETTLE_MS)

        clickTag("benchmark_chat_profile_button")
        waitForTag("benchmark_character_profile_screen")
        SystemClock.sleep(MEDIUM_SETTLE_MS)

        clickTag("benchmark_character_profile_menu_button")
        SystemClock.sleep(SHORT_SETTLE_MS)
        clickCharacterProfileEditMenuItem()
        waitForTag("benchmark_edit_character_screen")
        device.waitForIdle()
        SystemClock.sleep(MEDIUM_SETTLE_MS)
        warmEditCharacterCards()
        device.pressBack()

        waitForTag("benchmark_character_profile_screen")
        SystemClock.sleep(SHORT_SETTLE_MS)
        device.pressBack()

        waitForTag("benchmark_chat_screen")
        device.pressBack()
        waitForTag("benchmark_main_user_chats_list")
        SystemClock.sleep(SHORT_SETTLE_MS)
    }

    private fun MacrobenchmarkScope.warmEditCharacterCards() {
        val cardTags = listOf(
            "benchmark_edit_character_images_card",
            "benchmark_edit_character_name_card",
            "benchmark_edit_character_short_description_card",
            "benchmark_edit_character_full_description_card",
            "benchmark_edit_character_temperature_card",
            "benchmark_edit_character_first_message_card",
            "benchmark_edit_character_welcome_card",
            "benchmark_edit_character_privacy_card"
        )

        cardTags.forEach { tag ->
            scrollToTagInContainer("benchmark_edit_character_screen", tag)
            clickTag(tag)
            SystemClock.sleep(EDIT_CARD_SETTLE_MS)
            device.pressBack()
            device.waitForIdle()
            waitForTag("benchmark_edit_character_screen")
            SystemClock.sleep(SHORT_SETTLE_MS)
        }
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
            SystemClock.sleep(EDIT_CARD_SETTLE_MS)

            if (waitForOptionalTag("benchmark_payment_screen", 1_000L) != null) {
                device.waitForIdle()
                device.findObject(By.res("benchmark_payment_cards_pager"))?.apply {
                    setGestureMargin(device.displayWidth / 4)
                    swipe(Direction.LEFT, 0.85f)
                    swipe(Direction.RIGHT, 0.85f)
                }
            }

            returnToUserAccountScreen()
            SystemClock.sleep(SHORT_SETTLE_MS)
        }
    }

    private fun MacrobenchmarkScope.returnToUserAccountScreen() {
        repeat(4) {
            if (waitForOptionalTag("benchmark_user_account_screen", 1_000L) != null) {
                return
            }

            device.pressBack()
            device.waitForIdle()
            SystemClock.sleep(SHORT_SETTLE_MS)
        }

        waitForTag("benchmark_user_account_screen")
    }

    private fun MacrobenchmarkScope.clickTag(tag: String) {
        waitForTag(tag).click()
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.scrollToTagInContainer(
        containerTag: String,
        targetTag: String,
        maxAttempts: Int = 6
    ) {
        repeat(maxAttempts) {
            if (waitForOptionalTag(targetTag, 500L) != null) {
                return
            }

            swipeVerticalByTag(containerTag, fromTopToBottom = false, steps = 30)
            SystemClock.sleep(SHORT_SETTLE_MS)
        }

        waitForTag(targetTag)
    }

    private fun MacrobenchmarkScope.swipeVerticalByTag(
        tag: String,
        fromTopToBottom: Boolean,
        steps: Int = 20
    ) {
        val target = waitForTag(tag)
        val bounds = target.visibleBounds
        val centerX = bounds.centerX()
        val topY = bounds.top + (bounds.height() * 0.18f).toInt()
        val bottomY = bounds.bottom - (bounds.height() * 0.18f).toInt()
        val startY = if (fromTopToBottom) topY else bottomY
        val endY = if (fromTopToBottom) bottomY else topY

        device.swipe(centerX, startY, centerX, endY, steps)
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.longClickFirstExisting(vararg tags: String) {
        val target = tags.firstNotNullOfOrNull { tag ->
            waitForOptionalTag(tag, 2_500L)
        } ?: error("None of the benchmark tags were found: ${tags.joinToString()}")

        target.longClick()
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.clickCharacterProfileEditMenuItem() {
        waitForOptionalTag("benchmark_character_profile_edit_menu_item", 1_500L)?.let {
            it.click()
            device.waitForIdle()
            return
        }

        waitForAnyObject(
            By.text("Редактировать"),
            By.text("Edit"),
            timeoutMs = 5_000L
        ).click()
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.openUserChatsPage() {
        repeat(4) {
            if (waitForOptionalTag("benchmark_main_user_chats_list", 1_000L) != null) {
                return
            }

            clickTag("benchmark_main_tab_user_chats")
            SystemClock.sleep(SHORT_SETTLE_MS)
        }

        waitForTag("benchmark_main_user_chats_list")
    }

    private fun MacrobenchmarkScope.openAllChatsPage() {
        repeat(4) {
            if (waitForOptionalTag("benchmark_main_tab_all_chats", 1_000L) != null &&
                waitForOptionalTag("benchmark_main_account_button", 500L) == null
            ) {
                return
            }

            clickTag("benchmark_main_tab_all_chats")
            SystemClock.sleep(SHORT_SETTLE_MS)
        }

        waitUntilAccountButtonHidden()
    }

    private fun MacrobenchmarkScope.waitForTag(tag: String, timeoutMs: Long = WAIT_TIMEOUT_MS): UiObject2 {
        return waitForOptionalTag(tag, timeoutMs)
            ?: error("Unable to find benchmark tag: $tag")
    }

    private fun MacrobenchmarkScope.waitForAnyTag(
        vararg tags: String,
        timeoutMs: Long = WAIT_TIMEOUT_MS
    ): UiObject2 {
        val startedAt = SystemClock.uptimeMillis()
        while (SystemClock.uptimeMillis() - startedAt < timeoutMs) {
            tags.firstNotNullOfOrNull { tag ->
                waitForOptionalTag(tag, 1_000L)
            }?.let { return it }
        }

        error("Unable to find any benchmark tag: ${tags.joinToString()}")
    }

    private fun MacrobenchmarkScope.waitForAnyObject(
        vararg selectors: BySelector,
        timeoutMs: Long = WAIT_TIMEOUT_MS
    ): UiObject2 {
        val startedAt = SystemClock.uptimeMillis()
        while (SystemClock.uptimeMillis() - startedAt < timeoutMs) {
            selectors.firstNotNullOfOrNull { selector ->
                device.wait(Until.findObject(selector), 1_000L)
            }?.let { return it }
        }

        error("Unable to find any selector")
    }

    private fun MacrobenchmarkScope.waitForOptionalTag(
        tag: String,
        timeoutMs: Long = WAIT_TIMEOUT_MS
    ): UiObject2? {
        return device.wait(Until.findObject(By.res(tag)), timeoutMs)
    }

    private fun MacrobenchmarkScope.waitUntilAccountButtonHidden(timeoutMs: Long = WAIT_TIMEOUT_MS) {
        val startedAt = SystemClock.uptimeMillis()
        while (SystemClock.uptimeMillis() - startedAt < timeoutMs) {
            if (waitForOptionalTag("benchmark_main_account_button", 500L) == null) {
                return
            }
        }

        error("benchmark_main_account_button is still visible after switching to all chats tab")
    }
}
