package org.blindsystems.bop

import org.blindsystems.bop.media.AudioPlayerManager
import org.blindsystems.bop.media.BopPlaybackService

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.blindsystems.bop.core.*
import org.blindsystems.bop.infra.I18n
import org.blindsystems.bop.infra.PracticeHistoryRepository
import org.blindsystems.bop.infra.SegmentRepository
import org.blindsystems.bop.infra.SettingsRepository

/**
 * UI State for the Main Screen.
 */
data class BopUiState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val speedFactor: Float = 1.0f,
    val pitchSemitones: Float = 0.0f,
    val pitchPreserving: Boolean = true,
    val volume: Float = 1.0f,
    val loopAMs: Long? = null,
    val loopBMs: Long? = null,
    val isLoopingAB: Boolean = false,
    val audioUri: Uri? = null,
    val audioFileName: String = "",
    val segments: List<Segment> = emptyList(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val statusMessage: String = "",
    val practiceRunning: Boolean = false,
    val practiceElapsedMs: Long = 0L,
    val practiceLoopCount: Int = 0,
    val practiceCurrentTempo: Float = 1.0f,
    val theme: String = "dark",
    val language: String = "fr"
)

/**
 * ViewModel managing the application state and business logic.
 */
class BopViewModel(
    application: Application,
    private val player: AudioPlayerManager = AudioPlayerManager(application.applicationContext),
    private val segmentManager: SegmentManager = SegmentManager(),
    private val commandHistory: CommandHistory = CommandHistory(),
    private val segmentRepo: SegmentRepository = SegmentRepository(application.applicationContext),
    private val settingsRepo: SettingsRepository = SettingsRepository(application.applicationContext),
    private val historyRepo: PracticeHistoryRepository = PracticeHistoryRepository(application.applicationContext),
    private val practiceSession: PracticeSession = PracticeSession(),
    startPollingAutomatically: Boolean = true
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    
    // State
    private val _uiState = MutableStateFlow(BopUiState())
    val uiState: StateFlow<BopUiState> = _uiState.asStateFlow()

    private var positionPollingJob: Job? = null
    private var practiceTickJob: Job? = null

    init {
        // Load settings
        viewModelScope.launch {
            val theme = settingsRepo.theme.first()
            val lang = settingsRepo.language.first()
            I18n.setLanguage(lang)
            updateState { copy(theme = theme, language = lang) }
        }

        if (startPollingAutomatically) {
            startPositionPolling()
        }
        
        player.onLoopCompleted = {
            onLoopCompleted()
        }
    }

    fun stopPolling() {
        positionPollingJob?.cancel()
        practiceTickJob?.cancel()
    }

    // ── Audio loading ─────────────────────────────────────────────────────────

    fun loadAudioFile(uri: Uri) {
        val name = try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1 && it.moveToFirst()) it.getString(index) else null
            } ?: uri.lastPathSegment ?: "Unknown"
        } catch (e: Exception) {
            uri.lastPathSegment ?: "File"
        }
        loadAudio(uri, name)
    }

    fun loadAudio(uri: Uri, displayName: String) {
        player.loadUri(uri)
        val segments = segmentRepo.load(uri)
        segmentManager.setAll(segments)
        commandHistory.clear()

        val state = _uiState.value
        player.setSpeedAndPitch(state.speedFactor, state.pitchSemitones, state.pitchPreserving)

        updateState {
            copy(
                audioUri = uri,
                audioFileName = displayName,
                segments = segments,
                loopAMs = null,
                loopBMs = null,
                isLoopingAB = false,
                canUndo = false,
                canRedo = false,
                statusMessage = if (I18n.getLanguage() == "fr") "Fichier charge : $displayName" 
                               else "File loaded: $displayName"
            )
        }
    }

    // ── Transport ─────────────────────────────────────────────────────────────

    fun play() = player.play()
    fun pause() = player.pause()
    fun stop() = player.stop()

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        updateState { copy(currentPositionMs = positionMs) }
    }

    fun seek(positionMs: Long) = seekTo(positionMs)

    // ── A-B Loop ──────────────────────────────────────────────────────────────

    fun setLoopA() {
        player.setLoopA()
        updateState { copy(loopAMs = player.loopA) }
    }

    fun setLoopB() {
        player.setLoopB()
        updateState { copy(loopBMs = player.loopB) }
    }

    fun clearAB() {
        player.clearLoop()
        updateState { copy(loopAMs = null, loopBMs = null, isLoopingAB = false) }
    }

    fun toggleLoopAB(enabled: Boolean) {
        player.isLoopingAB = enabled
        updateState { copy(isLoopingAB = enabled) }
    }

    // ── Audio Processing ──────────────────────────────────────────────────────

    fun setSpeed(factor: Float) {
        val f = factor.coerceIn(0.5f, 2.0f)
        updateState { copy(speedFactor = f) }
        player.setSpeedAndPitch(f, _uiState.value.pitchSemitones, _uiState.value.pitchPreserving)
    }

    fun setPitch(semitones: Float) {
        val s = semitones.coerceIn(-12f, 12f)
        updateState { copy(pitchSemitones = s) }
        player.setSpeedAndPitch(_uiState.value.speedFactor, s, _uiState.value.pitchPreserving)
    }

    fun togglePitchPreserving() {
        val newValue = !_uiState.value.pitchPreserving
        updateState { copy(pitchPreserving = newValue) }
        player.setSpeedAndPitch(_uiState.value.speedFactor, _uiState.value.pitchSemitones, newValue)
    }

    fun setVolume(value: Float) {
        updateState { copy(volume = value) }
        player.setVolume(value)
    }

    // ── Segments ──────────────────────────────────────────────────────────────

    fun saveSegment(name: String, category: String = "", color: String = "#4FC3F7", notes: String = "") {
        val a = _uiState.value.loopAMs ?: return
        val b = _uiState.value.loopBMs ?: return
        if (a >= b) {
            updateState { copy(statusMessage = if (I18n.getLanguage() == "fr") "A doit etre avant B" else "A must be before B") }
            return
        }
        val seg = Segment(name = name, start = a, end = b, category = category, color = color, notes = notes)
        segmentManager.add(seg)
        commandHistory.push(SegmentCommand.Add(seg))

        val uri = _uiState.value.audioUri
        if (uri != null) segmentRepo.save(uri, segmentManager.segments)

        updateState {
            copy(
                segments = segmentManager.segments,
                canUndo = commandHistory.canUndo(),
                canRedo = commandHistory.canRedo(),
                statusMessage = if (I18n.getLanguage() == "fr") "Segment \"$name\" sauvegarde"
                               else "Segment \"$name\" saved"
            )
        }
    }

    fun deleteSegment(id: String) {
        val idx = segmentManager.segments.indexOfFirst { it.id == id }
        val seg = segmentManager.getById(id) ?: return
        segmentManager.remove(id)
        commandHistory.push(SegmentCommand.Remove(seg, idx))

        val uri = _uiState.value.audioUri
        if (uri != null) segmentRepo.save(uri, segmentManager.segments)

        updateState {
            copy(
                segments = segmentManager.segments,
                canUndo = commandHistory.canUndo(),
                canRedo = commandHistory.canRedo()
            )
        }
    }

    fun jumpToSegment(id: String) {
        val seg = segmentManager.getById(id) ?: return
        player.seekTo(seg.start)
        updateState {
            copy(
                loopAMs = seg.start,
                loopBMs = seg.end,
                statusMessage = "Segment: ${seg.name}"
            )
        }
        player.loopA = seg.start
        player.loopB = seg.end
    }

    fun undo() {
        when (val cmd = commandHistory.undo()) {
            is SegmentCommand.Add -> segmentManager.remove(cmd.segment.id)
            is SegmentCommand.Remove -> {
                val list = segmentManager.segments.toMutableList()
                list.add(cmd.originalIndex.coerceIn(0, list.size), cmd.segment)
                segmentManager.setAll(list)
            }
            null -> return
        }
        val uri = _uiState.value.audioUri
        if (uri != null) segmentRepo.save(uri, segmentManager.segments)
        updateState {
            copy(segments = segmentManager.segments,
                 canUndo = commandHistory.canUndo(),
                 canRedo = commandHistory.canRedo())
        }
    }

    fun redo() {
        when (val cmd = commandHistory.redo()) {
            is SegmentCommand.Add -> segmentManager.add(cmd.segment)
            is SegmentCommand.Remove -> segmentManager.remove(cmd.segment.id)
            null -> return
        }
        val uri = _uiState.value.audioUri
        if (uri != null) segmentRepo.save(uri, segmentManager.segments)
        updateState {
            copy(segments = segmentManager.segments,
                 canUndo = commandHistory.canUndo(),
                 canRedo = commandHistory.canRedo())
        }
    }

    // ── Practice Session ──────────────────────────────────────────────────────

    fun startPracticeSession(config: PracticeSession.Config) {
        practiceSession.start(config, _uiState.value.speedFactor)
        updateState { copy(practiceRunning = true) }
        practiceTickJob = viewModelScope.launch {
            while (_uiState.value.practiceRunning) {
                delay(500)
                practiceSession.tick()
                updateState {
                    copy(
                        practiceElapsedMs = practiceSession.elapsedMs.value,
                        practiceLoopCount = practiceSession.loopCount.value,
                        practiceCurrentTempo = practiceSession.currentTempo.value
                    )
                }
            }
        }
    }

    fun onLoopCompleted() {
        val delayMs = if (_uiState.value.practiceRunning) practiceSession.config.loopDelayMs else 0L
        
        if (delayMs > 0) {
            player.pause()
            viewModelScope.launch {
                delay(delayMs)
                if (_uiState.value.isPlaying || _uiState.value.practiceRunning) {
                    player.play()
                }
            }
        }

        if (!_uiState.value.practiceRunning) return
        val newTempo = practiceSession.onLoopCompleted()
        setSpeed(newTempo)
        if (practiceSession.shouldStop()) {
            stopPracticeSession()
            updateState { copy(statusMessage = if (I18n.getLanguage() == "fr") "Session terminee" else "Session completed") }
        }
    }

    fun stopPracticeSession() {
        practiceSession.stop()
        practiceTickJob?.cancel()
        updateState { copy(practiceRunning = false) }
        val state = _uiState.value
        historyRepo.logSession(
            audioFileName = state.audioFileName,
            durationMs = practiceSession.elapsedMs.value,
            loopCount = practiceSession.loopCount.value,
            tempo = state.speedFactor
        )
    }

    fun loadPracticeHistory() = historyRepo.loadAll()

    // ── Settings ──────────────────────────────────────────────────────────────

    fun setTheme(theme: String) {
        updateState { copy(theme = theme) }
        viewModelScope.launch { settingsRepo.saveTheme(theme) }
    }

    fun setLanguage(lang: String) {
        I18n.setLanguage(lang)
        updateState { copy(language = lang) }
        viewModelScope.launch { settingsRepo.saveLanguage(lang) }
    }

    // ── Position polling ──────────────────────────────────────────────────────

    private var lastAnnounceTimeMs = 0L
    private fun handlePeriodicAnnouncement(positionMs: Long) {
        viewModelScope.launch {
            settingsRepo.announceInterval.first().let { intervalS ->
                if (intervalS > 0) {
                    val now = System.currentTimeMillis()
                    if (now - lastAnnounceTimeMs >= intervalS * 1000L) {
                        lastAnnounceTimeMs = now
                        val posStr = formatTime(positionMs)
                        val durStr = formatTime(_uiState.value.durationMs)
                        val msg = "Position: $posStr / $durStr"
                        _uiState.update { it.copy(statusMessage = msg) }
                    }
                }
            }
        }
    }

    private fun startPositionPolling() {
        positionPollingJob = viewModelScope.launch {
            while (true) {
                delay(200)
                player.updatePosition()
                val pos = player.currentPosition.value
                val dur = player.duration.value
                val playing = player.isPlaying.value
                updateState { copy(currentPositionMs = pos, durationMs = dur, isPlaying = playing) }
                handlePeriodicAnnouncement(pos)
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private inline fun updateState(block: BopUiState.() -> BopUiState) {
        _uiState.update { it.block() }
    }

    fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        return "%02d:%02d".format(m, s)
    }

    override fun onCleared() {
        stopPolling()
        player.release()
        super.onCleared()
    }
}
