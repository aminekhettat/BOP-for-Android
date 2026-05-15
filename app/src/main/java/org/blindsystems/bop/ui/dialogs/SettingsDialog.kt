package org.blindsystems.bop.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.blindsystems.bop.infra.I18n

/**
 * Settings / preferences dialog.
 * Mirrors ui/settings_dialog.py.
 */
@Composable
fun SettingsDialog(
    currentTheme: String,
    currentLanguage: String,
    onThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showAbout by remember { mutableStateOf(false) }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }

    val themes = listOf("DARK" to "Sombre", "LIGHT" to "Clair", "HIGH_CONTRAST" to "Contraste élevé")
    val languages = listOf("fr" to "Français", "en" to "English")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                I18n["settings"],
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // ── Theme ──────────────────────────────────────────────────────
                Text(I18n["theme"], style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Column {
                    themes.forEach { (key, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == key,
                                onClick = { onThemeChange(key) },
                                modifier = Modifier.semantics { contentDescription = label }
                            )
                            Text(label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }

                Divider()

                // ── Language ───────────────────────────────────────────────────
                Text(I18n["language"], style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Column {
                    languages.forEach { (key, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentLanguage == key,
                                onClick = { onLanguageChange(key) },
                                modifier = Modifier.semantics { contentDescription = label }
                            )
                            Text(label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showAbout = true }) {
                    Text(I18n["about"])
                }
                Button(onClick = onDismiss) {
                    Text(I18n["confirm"])
                }
            }
        }
    )
}
