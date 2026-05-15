package org.blindsystems.bop

import android.app.Application
import android.net.Uri
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.blindsystems.bop.core.CommandHistory
import org.blindsystems.bop.core.PracticeSession
import org.blindsystems.bop.core.Segment
import org.blindsystems.bop.core.SegmentCommand
import org.blindsystems.bop.core.SegmentManager
import org.blindsystems.bop.infra.I18n
import org.blindsystems.bop.infra.PracticeHistoryRepository
import org.blindsystems.bop.infra.SegmentRepository
import org.blindsystems.bop.infra.SettingsRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BopViewModelTest {

    private lateinit var viewModel: BopViewModel
    private val app = mockk<Application>(relaxed = true)
    private val player = mockk<AudioPlayerManager>(relaxed = true)
    private val segmentRepo = mockk<SegmentRepository>(relaxed = true)
    private val settingsRepo = mockk<SettingsRepository>(relaxed = true)
    private val historyRepo = mockk<PracticeHistoryRepository>(relaxed = true)
    private lateinit var segmentManager: SegmentManager
    private lateinit var commandHistory: CommandHistory
    private lateinit var practiceSession: PracticeSession

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        segmentManager = SegmentManager()
        commandHistory = CommandHistory()
        practiceSession = PracticeSession()

        every { app.applicationContext } returns app
        every { settingsRepo.theme } returns flowOf("DARK")
        every { settingsRepo.language } returns flowOf("fr")
        every { settingsRepo.announceInterval } returns flowOf(0)

        every { player.isPlaying } returns MutableStateFlow(false)
        every { player.currentPosition } returns MutableStateFlow(0L)
        every { player.duration } returns MutableStateFlow(0L)
        every { player.playbackSpeed } returns MutableStateFlow(1.0f)
        every { player.playbackPitch } returns MutableStateFlow(1.0f)

        viewModel = BopViewModel(
            app, player, segmentManager, commandHistory,
            segmentRepo, settingsRepo, historyRepo, practiceSession,
            startPollingAutomatically = false
        )
    }

    @AfterEach
    fun tearDown() {
        viewModel.stopPolling()
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadAudioFile() {
        val uri = mockk<Uri>(relaxed = true)
        val resolver = mockk<android.content.ContentResolver>()
        val cursor = mockk<android.database.Cursor>()
        
        every { app.contentResolver } returns resolver
        every { resolver.query(any(), any(), any(), any(), any()) } returns cursor
        every { cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME) } returns 0
        every { cursor.moveToFirst() } returns true
        every { cursor.getString(0) } returns "Test Audio"
        every { cursor.close() } returns Unit
        
        viewModel.loadAudioFile(uri)
        assertEquals("Test Audio", viewModel.uiState.value.audioFileName)
    }

    @Test
    fun testLoadAudioFileCursorNull() {
        val uri = mockk<Uri>(relaxed = true)
        val resolver = mockk<android.content.ContentResolver>()
        
        every { app.contentResolver } returns resolver
        every { resolver.query(any(), any(), any(), any(), any()) } returns null
        every { uri.lastPathSegment } returns "fallback.mp3"
        
        viewModel.loadAudioFile(uri)
        assertEquals("fallback.mp3", viewModel.uiState.value.audioFileName)
    }

    // ── Transport ──

    @Test
    fun testPlay() {
        viewModel.play()
        verify { player.play() }
    }

    @Test
    fun testPause() {
        viewModel.pause()
        verify { player.pause() }
    }

    @Test
    fun testStop() {
        viewModel.stop()
        verify { player.stop() }
    }

    @Test
    fun testSeekTo() {
        viewModel.seekTo(5000L)
        verify { player.seekTo(5000L) }
        assertEquals(5000L, viewModel.uiState.value.currentPositionMs)
    }

    @Test
    fun testSeek() {
        viewModel.seek(3000L)
        verify { player.seekTo(3000L) }
        assertEquals(3000L, viewModel.uiState.value.currentPositionMs)
    }

    // ── A-B Loop ──

    @Test
    fun testSetLoopA() {
        every { player.loopA } returns 100L
        viewModel.setLoopA()
        assertEquals(100L, viewModel.uiState.value.loopAMs)
        verify { player.setLoopA() }
    }

    @Test
    fun testSetLoopB() {
        every { player.loopB } returns 500L
        viewModel.setLoopB()
        assertEquals(500L, viewModel.uiState.value.loopBMs)
        verify { player.setLoopB() }
    }

    @Test
    fun testClearAB() {
        every { player.loopA } returns 100L
        every { player.loopB } returns 200L
        viewModel.setLoopA()
        viewModel.setLoopB()

        viewModel.clearAB()
        assertNull(viewModel.uiState.value.loopAMs)
        assertNull(viewModel.uiState.value.loopBMs)
        assertFalse(viewModel.uiState.value.isLoopingAB)
        verify { player.clearLoop() }
    }

    @Test
    fun testToggleLoopAB() {
        viewModel.toggleLoopAB(true)
        assertTrue(viewModel.uiState.value.isLoopingAB)
        verify { player.isLoopingAB = true }

        viewModel.toggleLoopAB(false)
        assertFalse(viewModel.uiState.value.isLoopingAB)
        verify { player.isLoopingAB = false }
    }

    // ── Audio Processing ──

    @Test
    fun testSetSpeed() {
        viewModel.setSpeed(1.5f)
        assertEquals(1.5f, viewModel.uiState.value.speedFactor)
        verify { player.setSpeedAndPitch(1.5f, any(), any()) }
    }

    @Test
    fun testSetSpeedClamped() {
        viewModel.setSpeed(0.1f) // Below min 0.5
        assertEquals(0.5f, viewModel.uiState.value.speedFactor)

        viewModel.setSpeed(5.0f) // Above max 2.0
        assertEquals(2.0f, viewModel.uiState.value.speedFactor)
    }

    @Test
    fun testSetPitch() {
        viewModel.setPitch(3.0f)
        assertEquals(3.0f, viewModel.uiState.value.pitchSemitones)
        verify { player.setSpeedAndPitch(any(), 3.0f, any()) }
    }

    @Test
    fun testSetPitchClamped() {
        viewModel.setPitch(-20f) // Below min -12
        assertEquals(-12f, viewModel.uiState.value.pitchSemitones)

        viewModel.setPitch(20f) // Above max 12
        assertEquals(12f, viewModel.uiState.value.pitchSemitones)
    }

    @Test
    fun testTogglePitchPreserving() {
        assertTrue(viewModel.uiState.value.pitchPreserving) // Default is true
        viewModel.togglePitchPreserving()
        assertFalse(viewModel.uiState.value.pitchPreserving)
        viewModel.togglePitchPreserving()
        assertTrue(viewModel.uiState.value.pitchPreserving)
    }

    @Test
    fun testSetVolume() {
        viewModel.setVolume(0.7f)
        assertEquals(0.7f, viewModel.uiState.value.volume)
        verify { player.setVolume(0.7f) }
    }

    // ── Segments ──

    @Test
    fun testSaveSegment() {
        every { player.loopA } returns 100L
        every { player.loopB } returns 200L
        viewModel.setLoopA()
        viewModel.setLoopB()

        viewModel.saveSegment("Test Segment")

        val segments = viewModel.uiState.value.segments
        assertEquals(1, segments.size)
        assertEquals("Test Segment", segments[0].name)
        assertEquals(100L, segments[0].start)
        assertEquals(200L, segments[0].end)
        assertTrue(viewModel.uiState.value.canUndo)
    }

    @Test
    fun testSaveSegmentWithCustomFields() {
        every { player.loopA } returns 0L
        every { player.loopB } returns 1000L
        viewModel.setLoopA()
        viewModel.setLoopB()

        viewModel.saveSegment("Custom", "Guitar", "#FF0000", "My notes")

        val seg = viewModel.uiState.value.segments[0]
        assertEquals("Custom", seg.name)
        assertEquals("Guitar", seg.category)
        assertEquals("#FF0000", seg.color)
        assertEquals("My notes", seg.notes)
    }

    @Test
    fun testSaveSegmentNoLoopA() {
        // No loopA set — should do nothing
        viewModel.saveSegment("NoLoop")
        assertEquals(0, viewModel.uiState.value.segments.size)
    }

    @Test
    fun testSaveSegmentNoLoopB() {
        every { player.loopA } returns 100L
        viewModel.setLoopA()
        // No loopB set — should do nothing
        viewModel.saveSegment("NoLoopB")
        assertEquals(0, viewModel.uiState.value.segments.size)
    }

    @Test
    fun testSaveSegmentAGreaterOrEqualB() {
        every { player.loopA } returns 200L
        every { player.loopB } returns 100L
        viewModel.setLoopA()
        viewModel.setLoopB()

        viewModel.saveSegment("InvalidRange")
        assertEquals(0, viewModel.uiState.value.segments.size)
        assertTrue(viewModel.uiState.value.statusMessage.isNotEmpty())
    }

    @Test
    fun testDeleteSegment() {
        every { player.loopA } returns 100L
        every { player.loopB } returns 200L
        viewModel.setLoopA()
        viewModel.setLoopB()
        viewModel.saveSegment("ToDelete")
        val segId = viewModel.uiState.value.segments[0].id

        viewModel.deleteSegment(segId)
        assertEquals(0, viewModel.uiState.value.segments.size)
        assertTrue(viewModel.uiState.value.canUndo)
    }

    @Test
    fun testDeleteNonExistentSegment() {
        viewModel.deleteSegment("non-existent")
        // Should not crash
        assertEquals(0, viewModel.uiState.value.segments.size)
    }

    @Test
    fun testJumpToSegment() {
        every { player.loopA } returns 100L
        every { player.loopB } returns 200L
        viewModel.setLoopA()
        viewModel.setLoopB()
        viewModel.saveSegment("JumpTo")
        val segId = viewModel.uiState.value.segments[0].id

        viewModel.jumpToSegment(segId)
        verify { player.seekTo(100L) }
        assertEquals(100L, viewModel.uiState.value.loopAMs)
        assertEquals(200L, viewModel.uiState.value.loopBMs)
    }

    @Test
    fun testJumpToNonExistentSegment() {
        viewModel.jumpToSegment("non-existent")
        // Should not crash
    }

    // ── Undo/Redo ──

    @Test
    fun testUndoRedo() {
        every { player.loopA } returns 100L
        every { player.loopB } returns 200L
        viewModel.setLoopA()
        viewModel.setLoopB()

        viewModel.saveSegment("UndoMe")
        assertEquals(1, viewModel.uiState.value.segments.size)

        viewModel.undo()
        assertEquals(0, viewModel.uiState.value.segments.size)

        viewModel.redo()
        assertEquals(1, viewModel.uiState.value.segments.size)
    }

    @Test
    fun testUndoEmpty() {
        viewModel.undo() // Should not crash
        assertEquals(0, viewModel.uiState.value.segments.size)
    }

    @Test
    fun testRedoEmpty() {
        viewModel.redo() // Should not crash
    }

    @Test
    fun testUndoDelete() {
        every { player.loopA } returns 100L
        every { player.loopB } returns 200L
        viewModel.setLoopA()
        viewModel.setLoopB()
        viewModel.saveSegment("Deleted")
        val segId = viewModel.uiState.value.segments[0].id

        viewModel.deleteSegment(segId)
        assertEquals(0, viewModel.uiState.value.segments.size)

        viewModel.undo() // Should restore the deleted segment
        assertEquals(1, viewModel.uiState.value.segments.size)
    }

    @Test
    fun testRedoDelete() {
        every { player.loopA } returns 100L
        every { player.loopB } returns 200L
        viewModel.setLoopA()
        viewModel.setLoopB()
        viewModel.saveSegment("Deleted")
        val segId = viewModel.uiState.value.segments[0].id

        viewModel.deleteSegment(segId)
        viewModel.undo()
        viewModel.redo() // Should re-delete
        assertEquals(0, viewModel.uiState.value.segments.size)
    }

    // ── Practice Session ──

    @Test
    fun testStartPracticeSession() {
        viewModel.startPracticeSession(
            PracticeSession.Config(targetLoopCount = 5, loopDelayMs = 1000L, progressiveTempo = true)
        )
        assertTrue(viewModel.uiState.value.practiceRunning)
    }

    @Test
    fun testStopPracticeSession() {
        viewModel.startPracticeSession(PracticeSession.Config())
        viewModel.stopPracticeSession()
        assertFalse(viewModel.uiState.value.practiceRunning)
        verify { historyRepo.logSession(any(), any(), any(), any()) }
    }

    @Test
    fun testLoadPracticeHistory() {
        every { historyRepo.loadAll() } returns listOf(
            PracticeHistoryRepository.SessionRecord("2026-01-01", "test.mp3", 60000, 10, 100)
        )
        val history = viewModel.loadPracticeHistory()
        assertEquals(1, history.size)
    }

    @Test
    fun testOnLoopCompletedNotInPractice() {
        viewModel.onLoopCompleted()
        // Should not crash; no practice session active
    }

    @Test
    fun testOnLoopCompletedInPractice() = runTest {
        viewModel.startPracticeSession(
            PracticeSession.Config(targetLoopCount = 3, progressiveTempo = false)
        )
        viewModel.onLoopCompleted()
        advanceTimeBy(600) // Trigger the tick in practiceTickJob
        assertEquals(1, viewModel.uiState.value.practiceLoopCount)
        viewModel.stopPracticeSession()
    }

    @Test
    fun testOnLoopCompletedSessionEnds() = runTest {
        viewModel.startPracticeSession(
            PracticeSession.Config(targetLoopCount = 1)
        )
        viewModel.onLoopCompleted()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.practiceRunning)
    }

    @Test
    fun testOnLoopCompletedWithDelay() = runTest {
        viewModel.startPracticeSession(
            PracticeSession.Config(targetLoopCount = 0, loopDelayMs = 500L)
        )
        viewModel.onLoopCompleted()
        verify { player.pause() }
        advanceTimeBy(600)
        verify { player.play() }
        viewModel.stopPracticeSession()
    }

    // ── Settings ──

    @Test
    fun testSetTheme() = runTest {
        viewModel.setTheme("LIGHT")
        advanceUntilIdle()
        assertEquals("LIGHT", viewModel.uiState.value.theme)
        coVerify { settingsRepo.saveTheme("LIGHT") }
    }

    @Test
    fun testSetLanguage() = runTest {
        viewModel.setLanguage("en")
        advanceUntilIdle()
        assertEquals("en", viewModel.uiState.value.language)
        assertEquals("en", I18n.getLanguage())
        coVerify { settingsRepo.saveLanguage("en") }
    }

    // ── Audio Loading ──

    @Test
    fun testLoadAudio() {
        val uri = mockk<Uri>()
        every { segmentRepo.load(uri) } returns listOf(
            Segment(name = "Loaded", start = 0, end = 1000)
        )
        viewModel.loadAudio(uri, "test.mp3")

        assertEquals(uri, viewModel.uiState.value.audioUri)
        assertEquals("test.mp3", viewModel.uiState.value.audioFileName)
        assertEquals(1, viewModel.uiState.value.segments.size)
        assertNull(viewModel.uiState.value.loopAMs)
        assertNull(viewModel.uiState.value.loopBMs)
        verify { player.loadUri(uri) }
    }

    // ── Helpers ──

    @Test
    fun testFormatTime() {
        assertEquals("00:00", viewModel.formatTime(0))
        assertEquals("00:05", viewModel.formatTime(5000))
        assertEquals("01:30", viewModel.formatTime(90000))
        assertEquals("10:00", viewModel.formatTime(600000))
        assertEquals("99:59", viewModel.formatTime(5999000))
    }

    // ── BopUiState ──

    @Test
    fun testBopUiStateDefaults() {
        val state = BopUiState()
        assertFalse(state.isPlaying)
        assertEquals(0L, state.currentPositionMs)
        assertEquals(0L, state.durationMs)
        assertEquals(1.0f, state.speedFactor)
        assertEquals(0.0f, state.pitchSemitones)
        assertTrue(state.pitchPreserving)
        assertEquals(1.0f, state.volume)
        assertNull(state.loopAMs)
        assertNull(state.loopBMs)
        assertFalse(state.isLoopingAB)
        assertNull(state.audioUri)
        assertEquals("", state.audioFileName)
        assertTrue(state.segments.isEmpty())
        assertFalse(state.canUndo)
        assertFalse(state.canRedo)
        assertEquals("", state.statusMessage)
        assertFalse(state.practiceRunning)
        assertEquals(0L, state.practiceElapsedMs)
        assertEquals(0, state.practiceLoopCount)
        assertEquals(1.0f, state.practiceCurrentTempo)
        assertEquals("dark", state.theme)
        assertEquals("fr", state.language)
    }

    @Test
    fun testBopUiStateCopy() {
        val state = BopUiState(isPlaying = true, speedFactor = 1.5f)
        val copied = state.copy(volume = 0.5f)
        assertTrue(copied.isPlaying)
        assertEquals(1.5f, copied.speedFactor)
        assertEquals(0.5f, copied.volume)
    }

    @Test
    fun testOnCleared() {
        // We use reflection to call protected onCleared
        val method = viewModel.javaClass.getDeclaredMethod("onCleared")
        method.isAccessible = true
        method.invoke(viewModel)
        
        verify { player.release() }
    }

    @Test
    fun testPollingAndAnnouncement() = runTest {
        every { settingsRepo.announceInterval } returns flowOf(1) // 1 second
        
        val vm = BopViewModel(
            app, player, segmentManager, commandHistory,
            segmentRepo, settingsRepo, historyRepo, practiceSession,
            startPollingAutomatically = true
        )
        
        every { player.currentPosition } returns MutableStateFlow(1000L)
        every { player.duration } returns MutableStateFlow(5000L)
        every { player.isPlaying } returns MutableStateFlow(true)
        
        // Wait for first poll tick (200ms)
        advanceTimeBy(300)
        runCurrent()
        assertEquals(1000L, vm.uiState.value.currentPositionMs)
        
        // Wait for announcement (1s)
        advanceTimeBy(1000)
        runCurrent()
        assertTrue(vm.uiState.value.statusMessage.contains("00:01"))
        
        vm.stopPolling()
    }

    @Test
    fun testDeleteSegmentNonExistent() {
        viewModel.deleteSegment("non-existent")
        verify(exactly = 0) { segmentRepo.save(any(), any()) }
    }

    @Test
    fun testRedoNullUri() {
        val segment = Segment(id="1", name="S1", start=0, end=100)
        // Manually push a command to history
        val cmd = SegmentCommand.Add(segment)
        val historyField = viewModel.javaClass.getDeclaredField("commandHistory")
        historyField.isAccessible = true
        (historyField.get(viewModel) as CommandHistory).push(cmd)
        
        viewModel.undo() // Now it's in redo stack
        viewModel.redo()
        verify(exactly = 0) { segmentRepo.save(any(), any()) }
    }

    @Test
    fun testUndoNullUri() {
        val segment = Segment(id="1", name="S1", start=0, end=100)
        val cmd = SegmentCommand.Add(segment)
        val historyField = viewModel.javaClass.getDeclaredField("commandHistory")
        historyField.isAccessible = true
        (historyField.get(viewModel) as CommandHistory).push(cmd)
        
        viewModel.undo()
        verify(exactly = 0) { segmentRepo.save(any(), any()) }
    }
    @Test
    fun testLoadAudioFileException() {
        val uri = mockk<Uri>(relaxed = true)
        every { app.contentResolver } throws RuntimeException("Boom")
        every { uri.lastPathSegment } returns "error.mp3"
        viewModel.loadAudioFile(uri)
        assertEquals("error.mp3", viewModel.uiState.value.audioFileName)
    }

    @Test
    fun testSaveSegmentEarlyReturns() {
        viewModel.clearAB()
        viewModel.saveSegment("Test") // AMs and BMs are null
        verify(exactly = 0) { segmentRepo.save(any(), any()) }
        
        every { player.loopA } returns 1000L
        every { player.loopB } returns 500L // A > B
        viewModel.setLoopA()
        viewModel.setLoopB()
        
        viewModel.saveSegment("Test")
        assertTrue(viewModel.uiState.value.statusMessage.contains("A must be before B") || 
                   viewModel.uiState.value.statusMessage.contains("A doit etre avant B"))
    }

    @Test
    fun testUndoRedoRemove() {
        val uri = mockk<Uri>()
        every { uri.lastPathSegment } returns "test.mp3"
        viewModel.loadAudio(uri, "test.mp3")

        val segment = Segment(name = "S1", start = 0, end = 100)
        segmentManager.add(segment) // Initial state
        
        viewModel.deleteSegment(segment.id)
        assertFalse(viewModel.uiState.value.segments.contains(segment))
        
        viewModel.undo()
        assertTrue(viewModel.uiState.value.segments.contains(segment))
        
        viewModel.redo()
        assertFalse(viewModel.uiState.value.segments.contains(segment))
    }
}
