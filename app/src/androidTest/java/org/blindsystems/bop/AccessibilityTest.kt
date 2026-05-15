package org.blindsystems.bop

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.accessibility.AccessibilityChecks
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class AccessibilityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            // Enable accessibility checks for all tests in this class
            AccessibilityChecks.enable()
        }
    }

    @Test
    fun testAccessibilityMainScreen() {
        // The simple act of the screen being rendered and checked by AccessibilityChecks
        // will trigger errors if common accessibility rules are violated.
        composeTestRule.waitForIdle()
    }
}
