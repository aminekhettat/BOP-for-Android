package org.blindsystems.bop.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PracticeSessionTest {

    private lateinit var session: PracticeSession

    @BeforeEach
    fun setup() {
        session = PracticeSession()
    }

    @Test
    fun testInitialState() {
        assertFalse(session.isRunning.value)
        assertEquals(0, session.loopCount.value)
        assertEquals(0L, session.elapsedMs.value)
    }

    @Test
    fun testStartWithNonProgressiveTempo() {
        val config = PracticeSession.Config(targetLoopCount = 5)
        session.start(config, 1.2f)
        assertTrue(session.isRunning.value)
        assertEquals(1.2f, session.currentTempo.value)
        assertEquals(0, session.loopCount.value)
        assertEquals(0L, session.elapsedMs.value)
    }

    @Test
    fun testStartWithProgressiveTempo() {
        val config = PracticeSession.Config(
            progressiveTempo = true,
            tempoStart = 0.5f,
            tempoStep = 0.1f,
            tempoTarget = 1.0f
        )
        session.start(config, 1.0f)
        assertTrue(session.isRunning.value)
        assertEquals(0.5f, session.currentTempo.value) // Uses tempoStart, not initialTempo
    }

    @Test
    fun testStop() {
        session.start(PracticeSession.Config(), 1.0f)
        session.stop()
        assertFalse(session.isRunning.value)
        assertTrue(session.elapsedMs.value >= 0)
    }

    @Test
    fun testProgressiveTempo() {
        val config = PracticeSession.Config(
            progressiveTempo = true,
            tempoStart = 0.5f,
            tempoStep = 0.1f,
            tempoTarget = 0.7f
        )

        session.start(config, 1.0f)
        assertEquals(0.5f, session.currentTempo.value)

        val t1 = session.onLoopCompleted()
        assertEquals(0.6f, t1, 0.001f)

        val t2 = session.onLoopCompleted()
        assertEquals(0.7f, t2, 0.001f)

        // Should not exceed target
        val t3 = session.onLoopCompleted()
        assertEquals(0.7f, t3, 0.001f)
    }

    @Test
    fun testNonProgressiveTempoOnLoop() {
        val config = PracticeSession.Config(progressiveTempo = false)
        session.start(config, 1.5f)

        val tempo = session.onLoopCompleted()
        assertEquals(1.5f, tempo) // Stays the same
        assertEquals(1, session.loopCount.value)
    }

    @Test
    fun testShouldStopWithTarget() {
        val config = PracticeSession.Config(targetLoopCount = 2)
        session.start(config, 1.0f)
        assertFalse(session.shouldStop())

        session.onLoopCompleted()
        assertFalse(session.shouldStop())

        session.onLoopCompleted()
        assertTrue(session.shouldStop())
    }

    @Test
    fun testShouldStopWithZeroTarget() {
        val config = PracticeSession.Config(targetLoopCount = 0) // infinite
        session.start(config, 1.0f)
        session.onLoopCompleted()
        session.onLoopCompleted()
        session.onLoopCompleted()
        assertFalse(session.shouldStop()) // Never stops
    }

    @Test
    fun testReset() {
        session.start(PracticeSession.Config(), 1.0f)
        session.onLoopCompleted()

        session.reset()
        assertEquals(0, session.loopCount.value)
        assertEquals(0L, session.elapsedMs.value)
        assertFalse(session.isRunning.value)
    }

    @Test
    fun testTickWhenRunning() {
        session.start(PracticeSession.Config(), 1.0f)
        session.tick()
        assertTrue(session.elapsedMs.value >= 0)
    }

    @Test
    fun testTickWhenNotRunning() {
        session.tick() // Should not crash, elapsedMs stays 0
        assertEquals(0L, session.elapsedMs.value)
    }

    @Test
    fun testConfigDefaults() {
        val config = PracticeSession.Config()
        assertEquals(0, config.targetLoopCount)
        assertEquals(0L, config.loopDelayMs)
        assertFalse(config.progressiveTempo)
        assertEquals(0.5f, config.tempoStart)
        assertEquals(0.05f, config.tempoStep)
        assertEquals(1.0f, config.tempoTarget)
    }

    @Test
    fun testConfigAccessor() {
        val config = PracticeSession.Config(targetLoopCount = 10, loopDelayMs = 2000L)
        session.start(config, 1.0f)
        assertEquals(10, session.config.targetLoopCount)
        assertEquals(2000L, session.config.loopDelayMs)
    }

    @Test
    fun testLoopCountIncrements() {
        session.start(PracticeSession.Config(), 1.0f)
        assertEquals(0, session.loopCount.value)
        session.onLoopCompleted()
        assertEquals(1, session.loopCount.value)
        session.onLoopCompleted()
        assertEquals(2, session.loopCount.value)
        session.onLoopCompleted()
        assertEquals(3, session.loopCount.value)
    }
}
