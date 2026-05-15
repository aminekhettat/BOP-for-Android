package org.blindsystems.bop.infra

/**
 * Lightweight i18n engine - French / English.
 */
object I18n {

    private var lang = "fr"

    fun setLanguage(language: String) { lang = language }
    fun getLanguage(): String = lang

    private val strings = mapOf(
        "app_name" to mapOf("fr" to "Back-Office Player", "en" to "Back-Office Player"),
        "open_file" to mapOf("fr" to "Ouvrir un fichier audio", "en" to "Open audio file"),
        "play" to mapOf("fr" to "Lecture", "en" to "Play"),
        "pause" to mapOf("fr" to "Pause", "en" to "Pause"),
        "stop" to mapOf("fr" to "Arret", "en" to "Stop"),
        "set_a" to mapOf("fr" to "Definir A", "en" to "Set A"),
        "set_b" to mapOf("fr" to "Definir B", "en" to "Set B"),
        "clear_ab" to mapOf("fr" to "Effacer A/B", "en" to "Clear A/B"),
        "loop_ab" to mapOf("fr" to "Boucle A-B", "en" to "Loop A-B"),
        "segments" to mapOf("fr" to "Segments", "en" to "Segments"),
        "save_segment" to mapOf("fr" to "Sauvegarder segment", "en" to "Save segment"),
        "jump_to_segment" to mapOf("fr" to "Aller au segment", "en" to "Jump to segment"),
        "delete_segment" to mapOf("fr" to "Supprimer", "en" to "Delete"),
        "segment_name" to mapOf("fr" to "Nom du segment", "en" to "Segment name"),
        "no_segments" to mapOf("fr" to "Aucun segment", "en" to "No segments"),
        "tempo" to mapOf("fr" to "Tempo", "en" to "Tempo"),
        "pitch" to mapOf("fr" to "Hauteur", "en" to "Pitch"),
        "volume" to mapOf("fr" to "Volume", "en" to "Volume"),
        "preserve_pitch" to mapOf("fr" to "Preserver la hauteur", "en" to "Preserve pitch"),
        "practice" to mapOf("fr" to "Session de pratique", "en" to "Practice session"),
        "loop_count" to mapOf("fr" to "Nombre de boucles (0=inf)", "en" to "Loop count (0=inf)"),
        "loop_delay" to mapOf("fr" to "Delai entre boucles (s)", "en" to "Loop delay (s)"),
        "progressive_tempo" to mapOf("fr" to "Tempo progressif", "en" to "Progressive tempo"),
        "start_session" to mapOf("fr" to "Demarrer", "en" to "Start"),
        "stop_session" to mapOf("fr" to "Arreter", "en" to "Stop"),
        "loops_done" to mapOf("fr" to "Boucles effectuees", "en" to "Loops done"),
        "history" to mapOf("fr" to "Historique", "en" to "History"),
        "export_csv" to mapOf("fr" to "Exporter CSV", "en" to "Export CSV"),
        "settings" to mapOf("fr" to "Parametres", "en" to "Settings"),
        "theme" to mapOf("fr" to "Theme", "en" to "Theme"),
        "language" to mapOf("fr" to "Langue", "en" to "Language"),
        "dark" to mapOf("fr" to "Sombre", "en" to "Dark"),
        "light" to mapOf("fr" to "Clair", "en" to "Light"),
        "high_contrast" to mapOf("fr" to "Contraste eleve", "en" to "High contrast"),
        "undo" to mapOf("fr" to "Annuler", "en" to "Undo"),
        "redo" to mapOf("fr" to "Retablir", "en" to "Redo"),
        "cancel" to mapOf("fr" to "Annuler", "en" to "Cancel"),
        "confirm" to mapOf("fr" to "Confirmer", "en" to "Confirm"),
        "no_file" to mapOf("fr" to "Aucun fichier charge", "en" to "No file loaded"),
        "about" to mapOf("fr" to "A propos", "en" to "About"),
        "about_title" to mapOf("fr" to "A propos de BOP", "en" to "About BOP"),
        "by_blind_systems" to mapOf("fr" to "Développé par BLIND SYSTEMS", "en" to "Developed by BLIND SYSTEMS"),
        "developed_for_culture_musique" to mapOf("fr" to "Conçu pour l'association Culture Musique", "en" to "Designed for Culture Musique association"),
        "license_apache" to mapOf("fr" to "Licence Apache 2.0", "en" to "Apache 2.0 License"),
        "free_access" to mapOf("fr" to "Application libre d'accès", "en" to "Free access application"),
        "contribute_invite" to mapOf("fr" to "Contribuez au projet !", "en" to "Contribute to the project!")
    )

    operator fun get(key: String): String =
        strings[key]?.get(lang) ?: strings[key]?.get("en") ?: key
}
