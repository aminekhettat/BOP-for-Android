package org.blindsystems.bop.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blindsystems.bop.infra.I18n
import org.blindsystems.bop.infra.PracticeHistoryRepository

/**
 * Read-only practice history dialog.
 * Mirrors ui/history_dialog.py.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDialog(
    sessions: List<PracticeHistoryRepository.SessionRecord>,
    onDismiss: () -> Unit,
    onExportCsv: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                I18n["history"],
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column {
                if (sessions.isEmpty()) {
                    Text("Aucune session enregistrée", color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                } else {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Date", "Fichier", "Durée", "Boucles", "Tempo").forEach { header ->
                            Text(
                                header,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    HorizontalDivider()
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(sessions.reversed()) { record ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(record.date.substringBefore(" "), fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text(record.audioFile.take(12), fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text("${record.durationMs / 1000}s", fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text("${record.loopCount}", fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text("${record.tempoPercent}%", fontSize = 11.sp, modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onExportCsv) { Text(I18n["export_csv"]) }
                Button(onClick = onDismiss) { Text(I18n["confirm"]) }
            }
        }
    )
}
