package org.blindsystems.bop.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.blindsystems.bop.BopViewModel
import org.blindsystems.bop.core.PracticeSession
import org.blindsystems.bop.infra.I18n

/**
 * Main application screen - mirrors ui/main_window.py.
 * Contains transport, A-B loop, waveform, tempo/pitch, segments, practice.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: BopViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Dialogs visibility
    var showSaveSegmentDialog by remember { mutableStateOf(false) }
    var showHistoryDialog     by remember { mutableStateOf(false) }
    var showSettingsDialog    by remember { mutableStateOf(false) }

    // File picker
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            vm.loadAudioFile(uri)
        }
    }

    // Root scaffold
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Announce status messages via Snackbar
    LaunchedEffect(state.statusMessage) {
        if (state.statusMessage.isNotBlank()) {
            snackbarHostState.showSnackbar(state.statusMessage)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "BOP",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (state.audioFileName.isNotBlank()) {
                            Text(
                                state.audioFileName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showHistoryDialog = true }) { Icon(Icons.Default.History, null) }
                    IconButton(onClick = { showSettingsDialog = true }) { Icon(Icons.Default.Settings, null) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Transport & Waveform Section
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        TransportPanel(
                            isPlaying = state.isPlaying,
                            onOpenFile = { filePicker.launch(arrayOf("audio/*")) },
                            onPlay     = { vm.play() },
                            onPause    = { vm.pause() },
                            onStop     = { vm.stop() }
                        )
                        
                        Spacer(Modifier.height(16.dp))

                        WaveformWidget(
                            durationMs = state.durationMs,
                            positionMs = state.currentPositionMs,
                            loopAMs = state.loopAMs,
                            loopBMs = state.loopBMs,
                            segments = state.segments,
                            onSeek = { vm.seek(it) }
                        )

                        Text(
                            text = "${formatTime(state.currentPositionMs)} / ${formatTime(state.durationMs)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // A-B Loop Section
                ABLoopPanel(
                    loopAMs      = state.loopAMs,
                    loopBMs      = state.loopBMs,
                    isLooping    = state.isLoopingAB,
                    onSetA       = { vm.setLoopA() },
                    onSetB       = { vm.setLoopB() },
                    onClearAB    = { vm.clearAB() },
                    onToggleLoop = { vm.toggleLoopAB(it) }
                )

                // Processing Section (Tempo/Pitch/Volume)
                TempoAndPitchPanel(
                    speedFactor        = state.speedFactor,
                    pitchSemitones     = state.pitchSemitones,
                    pitchPreserving    = state.pitchPreserving,
                    volume             = state.volume,
                    onSpeedChange      = { vm.setSpeed(it) },
                    onPitchChange      = { vm.setPitch(it) },
                    onVolumeChange     = { vm.setVolume(it) },
                    onTogglePreserving = { vm.togglePitchPreserving() }
                )

                // Segments Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(I18n["segments"], style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { showSaveSegmentDialog = true },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                    
                    SegmentsPanel(
                        segments = state.segments,
                        onJump   = { vm.jumpToSegment(it.id) },
                        onDelete = { vm.deleteSegment(it.id) }
                    )
                }

                // Practice Section
                PracticePanel(
                    practiceRunning = state.practiceRunning,
                    practiceLoops   = state.practiceLoopCount,
                    practiceElapsed = state.practiceElapsedMs,
                    onStart = { count, delayS, prog, start, step, target ->
                        vm.startPracticeSession(
                            org.blindsystems.bop.core.PracticeSession.Config(
                                targetLoopCount = count,
                                loopDelayMs = (delayS * 1000).toLong(),
                                progressiveTempo = prog,
                                tempoStart = start,
                                tempoStep = step,
                                tempoTarget = target
                            )
                        )
                    },
                    onStop  = { vm.stopPracticeSession() }
                )

                // Undo/Redo FABs or Bottom Bar could go here, but for now we keep it in the top bar or a floating row.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    val accent = MaterialTheme.colorScheme.primary
                    IconButton(onClick = { vm.undo() }, enabled = state.canUndo) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = I18n["undo"], tint = accent)
                    }
                    IconButton(onClick = { vm.redo() }, enabled = state.canRedo) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = I18n["redo"], tint = accent)
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }

    // Dialogs
    if (showSaveSegmentDialog) {
        SaveSegmentDialog(
            onConfirm = { name, category, color, notes ->
                vm.saveSegment(name, category, color, notes)
                showSaveSegmentDialog = false
            },
            onDismiss = { showSaveSegmentDialog = false }
        )
    }
}

@Composable
private fun TransportPanel(
    isPlaying: Boolean,
    onOpenFile: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Open
        FilledTonalIconButton(
            onClick = onOpenFile,
            modifier = Modifier.semantics { contentDescription = I18n["open_file"] }
        ) { Icon(Icons.Default.FileOpen, null) }

        Spacer(Modifier.weight(1f))

        // Stop
        IconButton(
            onClick = onStop,
            modifier = Modifier.semantics { contentDescription = I18n["stop"] }
        ) { Icon(Icons.Default.Stop, null) }

        // Play/Pause
        FloatingActionButton(
            onClick = if (isPlaying) onPause else onPlay,
            modifier = Modifier.semantics { contentDescription = if (isPlaying) I18n["pause"] else I18n["play"] },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun ABLoopPanel(
    loopAMs: Long?,
    loopBMs: Long?,
    isLooping: Boolean,
    onSetA: () -> Unit,
    onSetB: () -> Unit,
    onClearAB: () -> Unit,
    onToggleLoop: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, colors.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Set A
                OutlinedButton(
                    onClick = onSetA,
                    modifier = Modifier.weight(1f).semantics { contentDescription = I18n["set_a"] },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF81C784))
                ) {
                    Text("A${loopAMs?.let { " ${formatTime(it)}" } ?: ""}")
                }
                // Set B
                OutlinedButton(
                    onClick = onSetB,
                    modifier = Modifier.weight(1f).semantics { contentDescription = I18n["set_b"] },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB74D))
                ) {
                    Text("B${loopBMs?.let { " ${formatTime(it)}" } ?: ""}")
                }
                // Clear
                OutlinedButton(
                    onClick = onClearAB,
                    modifier = Modifier.weight(1f).semantics { contentDescription = I18n["clear_ab"] },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error)
                ) {
                    Text("X A/B")
                }
            }
            // Loop A-B toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = isLooping,
                    onCheckedChange = { onToggleLoop(it) },
                    enabled = loopAMs != null && loopBMs != null,
                    modifier = Modifier.semantics {
                        contentDescription = "${I18n["loop_ab"]}: ${if (isLooping) "actif" else "desactif"}"
                    }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    I18n["loop_ab"],
                    color = if (isLooping) colors.primary else colors.onSurface.copy(0.6f),
                    fontWeight = if (isLooping) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun TempoAndPitchPanel(
    speedFactor: Float,
    pitchSemitones: Float,
    pitchPreserving: Boolean,
    volume: Float,
    onSpeedChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onTogglePreserving: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Tempo
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${I18n["tempo"]}:", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(72.dp))
                Slider(
                    value = speedFactor,
                    onValueChange = onSpeedChange,
                    valueRange = 0.5f..2.0f,
                    steps = 29,
                    modifier = Modifier.weight(1f).semantics {
                        contentDescription = "${I18n["tempo"]} : ${(speedFactor * 100).toInt()} %"
                    }
                )
                Text("${(speedFactor * 100).toInt()} %", modifier = Modifier.width(52.dp).padding(start = 8.dp), fontWeight = FontWeight.Bold, color = colors.primary)
            }

            // Pitch
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${I18n["pitch"]}:", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(72.dp))
                Slider(
                    value = pitchSemitones,
                    onValueChange = onPitchChange,
                    valueRange = -12f..12f,
                    steps = 23,
                    modifier = Modifier.weight(1f).semantics {
                        val sign = if (pitchSemitones >= 0) "+" else ""
                        contentDescription = "${I18n["pitch"]} : $sign${pitchSemitones.toInt()} st"
                    }
                )
                val sign = if (pitchSemitones >= 0) "+" else ""
                Text("$sign${pitchSemitones.toInt()} st", modifier = Modifier.width(52.dp).padding(start = 8.dp), fontWeight = FontWeight.Bold, color = colors.primary)
            }

            // Volume
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${I18n["volume"]}:", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(72.dp))
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1.0f,
                    modifier = Modifier.weight(1f).semantics {
                        contentDescription = "${I18n["volume"]} : ${(volume * 100).toInt()} %"
                    }
                )
                Text("${(volume * 100).toInt()} %", modifier = Modifier.width(52.dp).padding(start = 8.dp), fontWeight = FontWeight.Bold, color = colors.primary)
            }

            // Pitch Preserving Toggle
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onTogglePreserving() }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(I18n["preserve_pitch"], style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = pitchPreserving,
                    onCheckedChange = { onTogglePreserving() },
                    modifier = Modifier.semantics {
                        contentDescription = "${I18n["preserve_pitch"]} : ${if (pitchPreserving) "actif" else "desactif"}"
                    }
                )
            }
        }
    }
}

@Composable
private fun SegmentsPanel(
    segments: List<org.blindsystems.bop.core.Segment>,
    onJump: (org.blindsystems.bop.core.Segment) -> Unit,
    onDelete: (org.blindsystems.bop.core.Segment) -> Unit
) {
    if (segments.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(I18n["segments"], style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        segments.forEach { seg ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onJump(seg) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(12.dp).background(Color(android.graphics.Color.parseColor(seg.color)), CircleShape))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(seg.name, fontWeight = FontWeight.SemiBold)
                        Text(formatTime(seg.start), style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = { onDelete(seg) }) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticePanel(
    practiceRunning: Boolean,
    practiceLoops: Int,
    practiceElapsed: Long,
    onStart: (count: Int, delayS: Float, prog: Boolean, start: Float, step: Float, target: Float) -> Unit,
    onStop: () -> Unit
) {
    var count by remember { mutableStateOf("0") }
    var delay by remember { mutableStateOf("0") }
    var prog  by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(I18n["practice"], style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            if (!practiceRunning) {
                OutlinedTextField(value = count, onValueChange = { count = it }, label = { Text(I18n["loop_count"]) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = delay, onValueChange = { delay = it }, label = { Text(I18n["loop_delay"]) }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = prog, onCheckedChange = { prog = it })
                    Text(I18n["progressive_tempo"])
                }
                Button(onClick = { 
                    onStart(count.toIntOrNull() ?: 0, delay.toFloatOrNull() ?: 0f, prog, 0.5f, 0.05f, 1.0f)
                }, modifier = Modifier.fillMaxWidth()) { Text(I18n["start_session"]) }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${I18n["loops_done"]}: $practiceLoops")
                    Text(formatTime(practiceElapsed))
                }
                Button(onClick = onStop, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text(I18n["stop_session"])
                }
            }
        }
    }
}

@Composable
private fun SaveSegmentDialog(
    onConfirm: (name: String, category: String, color: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val colors = listOf("#4FC3F7", "#81C784", "#FFB74D", "#E57373", "#CE93D8", "#80CBC4")
    var selectedColor by remember { mutableStateOf(colors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(I18n["save_segment"]) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(I18n["segment_name"]) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categorie") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { hex ->
                        Box(Modifier.size(32.dp).background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                            .border(if (selectedColor == hex) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            .clickable { selectedColor = hex })
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(name, category, selectedColor, notes) }, enabled = name.isNotBlank()) { Text(I18n["confirm"]) } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text(I18n["cancel"]) } }
    )
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) String.format("%02d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}
