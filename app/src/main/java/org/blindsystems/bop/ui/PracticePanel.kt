package org.blindsystems.bop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.blindsystems.bop.core.PracticeSession
import org.blindsystems.bop.infra.I18n

/**
 * Practice session control panel.
 * Mirrors ui/practice_panel.py.
 */
@Composable
fun PracticePanel(
    isRunning: Boolean,
    loopCount: Int,
    elapsedMs: Long,
    currentTempo: Float,
    onStart: (PracticeSession.Config) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var targetLoops by remember { mutableStateOf("0") }
    var loopDelay   by remember { mutableStateOf("0") }
    var progressive by remember { mutableStateOf(false) }
    var tempoStart  by remember { mutableStateOf("50") }
    var tempoStep   by remember { mutableStateOf("5") }
    var tempoTarget by remember { mutableStateOf("100") }

    val colors = MaterialTheme.colorScheme
    val elapsedSec = elapsedMs / 1000

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                I18n["practice"],
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.primary
            )

            // ── Session stats (visible when running) ─────────────────────────
            if (isRunning) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatChip("${I18n["loops_done"]}: $loopCount")
                    StatChip("${"%02d:%02d".format(elapsedSec / 60, elapsedSec % 60)}")
                    StatChip("${(currentTempo * 100).toInt()} %")
                }
            }

            // ── Config inputs ─────────────────────────────────────────────────
            if (!isRunning) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = targetLoops,
                        onValueChange = { targetLoops = it },
                        label = { Text(I18n["loop_count"], style = MaterialTheme.typography.labelSmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).semantics {
                            contentDescription = "${I18n["loop_count"]}. Valeur: $targetLoops"
                        }
                    )
                    OutlinedTextField(
                        value = loopDelay,
                        onValueChange = { loopDelay = it },
                        label = { Text(I18n["loop_delay"], style = MaterialTheme.typography.labelSmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Progressive tempo toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = progressive,
                        onCheckedChange = { progressive = it },
                        modifier = Modifier.semantics {
                            contentDescription = "${I18n["progressive_tempo"]}: ${if (progressive) "activé" else "désactivé"}"
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(I18n["progressive_tempo"], color = colors.onSurface)
                }

                if (progressive) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tempoStart,
                            onValueChange = { tempoStart = it },
                            label = { Text("Départ %", style = MaterialTheme.typography.labelSmall) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = tempoStep,
                            onValueChange = { tempoStep = it },
                            label = { Text("Pas %", style = MaterialTheme.typography.labelSmall) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = tempoTarget,
                            onValueChange = { tempoTarget = it },
                            label = { Text("Cible %", style = MaterialTheme.typography.labelSmall) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Start / Stop ──────────────────────────────────────────────────
            if (!isRunning) {
                Button(
                    onClick = {
                        val cfg = PracticeSession.Config(
                            targetLoopCount  = targetLoops.toIntOrNull() ?: 0,
                            loopDelayMs      = (loopDelay.toLongOrNull() ?: 0L) * 1000L,
                            progressiveTempo = progressive,
                            tempoStart       = (tempoStart.toIntOrNull() ?: 50) / 100f,
                            tempoStep        = (tempoStep.toIntOrNull() ?: 5) / 100f,
                            tempoTarget      = (tempoTarget.toIntOrNull() ?: 100) / 100f
                        )
                        onStart(cfg)
                    },
                    modifier = Modifier.fillMaxWidth().semantics {
                        contentDescription = I18n["start_session"]
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondary)
                ) {
                    Text(I18n["start_session"], color = colors.onSecondary)
                }
            } else {
                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.fillMaxWidth().semantics {
                        contentDescription = I18n["stop_session"]
                    }
                ) {
                    Text(I18n["stop_session"])
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
