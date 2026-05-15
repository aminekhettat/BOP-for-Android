package org.blindsystems.bop.infra

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class I18nTest {

    @BeforeEach
    fun setup() {
        I18n.setLanguage("fr") // Reset to default
    }

    @Test
    fun testFrenchStrings() {
        I18n.setLanguage("fr")
        assertEquals("Ouvrir un fichier audio", I18n["open_file"])
        assertEquals("Lecture", I18n["play"])
        assertEquals("Pause", I18n["pause"])
        assertEquals("Arret", I18n["stop"])
    }

    @Test
    fun testEnglishStrings() {
        I18n.setLanguage("en")
        assertEquals("Open audio file", I18n["open_file"])
        assertEquals("Play", I18n["play"])
        assertEquals("Pause", I18n["pause"])
        assertEquals("Stop", I18n["stop"])
    }

    @Test
    fun testLanguageToggle() {
        I18n.setLanguage("en")
        assertEquals("Open audio file", I18n["open_file"])
        I18n.setLanguage("fr")
        assertEquals("Ouvrir un fichier audio", I18n["open_file"])
    }

    @Test
    fun testGetLanguage() {
        I18n.setLanguage("en")
        assertEquals("en", I18n.getLanguage())
        I18n.setLanguage("fr")
        assertEquals("fr", I18n.getLanguage())
    }

    @Test
    fun testUnknownKey() {
        assertEquals("unknown_key_xyz", I18n["unknown_key_xyz"])
    }

    @Test
    fun testFallbackToEnglish() {
        I18n.setLanguage("de") // Not supported
        assertEquals("Open audio file", I18n["open_file"])
    }

    @Test
    fun testAllKeysExist() {
        val keys = listOf(
            "app_name", "open_file", "play", "pause", "stop",
            "set_a", "set_b", "clear_ab", "loop_ab",
            "segments", "save_segment", "jump_to_segment", "delete_segment",
            "segment_name", "no_segments",
            "tempo", "pitch", "volume", "preserve_pitch",
            "practice", "loop_count", "loop_delay", "progressive_tempo",
            "start_session", "stop_session", "loops_done",
            "history", "export_csv", "settings", "theme", "language",
            "dark", "light", "high_contrast",
            "undo", "redo", "cancel", "confirm", "no_file"
        )

        I18n.setLanguage("fr")
        for (key in keys) {
            val value = I18n[key]
            // Should not return the key itself (means it was found)
            assert(value != key) { "Key '$key' not found in French translations" }
        }

        I18n.setLanguage("en")
        for (key in keys) {
            val value = I18n[key]
            assert(value != key) { "Key '$key' not found in English translations" }
        }
    }

    @Test
    fun testSegmentRelatedStrings() {
        I18n.setLanguage("fr")
        assertEquals("Segments", I18n["segments"])
        assertEquals("Sauvegarder segment", I18n["save_segment"])
        assertEquals("Aller au segment", I18n["jump_to_segment"])
        assertEquals("Supprimer", I18n["delete_segment"])
        assertEquals("Nom du segment", I18n["segment_name"])
        assertEquals("Aucun segment", I18n["no_segments"])
    }

    @Test
    fun testSettingsStrings() {
        I18n.setLanguage("en")
        assertEquals("Settings", I18n["settings"])
        assertEquals("Theme", I18n["theme"])
        assertEquals("Language", I18n["language"])
        assertEquals("Dark", I18n["dark"])
        assertEquals("Light", I18n["light"])
        assertEquals("High contrast", I18n["high_contrast"])
    }

    @Test
    fun testPracticeStrings() {
        I18n.setLanguage("en")
        assertEquals("Practice session", I18n["practice"])
        assertEquals("Loop count (0=inf)", I18n["loop_count"])
        assertEquals("Loop delay (s)", I18n["loop_delay"])
        assertEquals("Progressive tempo", I18n["progressive_tempo"])
        assertEquals("Start", I18n["start_session"])
        assertEquals("Stop", I18n["stop_session"])
        assertEquals("Loops done", I18n["loops_done"])
    }
}
