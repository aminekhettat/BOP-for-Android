package org.blindsystems.bop.ui.theme

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class BopThemeTest {

    @Test
    fun testColorSchemeForDark() {
        val scheme = colorSchemeFor("DARK")
        assertNotNull(scheme)
        assertNotNull(scheme.primary)
        assertNotNull(scheme.background)
    }

    @Test
    fun testColorSchemeForLight() {
        val scheme = colorSchemeFor("LIGHT")
        assertNotNull(scheme)
        assertNotNull(scheme.primary)
        assertNotNull(scheme.background)
    }

    @Test
    fun testColorSchemeForHighContrast() {
        val scheme = colorSchemeFor("HIGH_CONTRAST")
        assertNotNull(scheme)
        assertNotNull(scheme.primary)
        assertNotNull(scheme.background)
    }

    @Test
    fun testColorSchemeForUnknownDefaultsToDark() {
        val dark = colorSchemeFor("DARK")
        val unknown = colorSchemeFor("UNKNOWN_THEME")
        // Unknown should fall back to dark
        assertNotNull(unknown)
        // Both should return the same dark color scheme
        assert(dark.primary == unknown.primary)
        assert(dark.background == unknown.background)
    }

    @Test
    fun testColorSchemesAreDifferent() {
        val dark = colorSchemeFor("DARK")
        val light = colorSchemeFor("LIGHT")
        val hc = colorSchemeFor("HIGH_CONTRAST")
        // Each theme should have different primary colors
        assert(dark.primary != light.primary)
        assert(dark.primary != hc.primary)
        assert(light.primary != hc.primary)
    }
}
