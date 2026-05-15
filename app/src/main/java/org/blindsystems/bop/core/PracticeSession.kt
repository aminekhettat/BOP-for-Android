package org.blindsystems.bop.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks a practice session: loop count, elapsed time, progressive tempo.
 * This class handles the logic for session state and tempo progression.
 */
class PracticeSession {

    /**
     * Configuration parameters for a practice session.
     * 
     * @property targetLoopCount Number of loops before stopping (0 for infinite).
     * @property loopDelayMs Delay between each loop iteration in milliseconds.
     * @property progressiveTempo Whether to increase speed at each iteration.
     * @property tempoStart Starting tempo (0.5 to 2.0).
     * @property tempoStep Speed increment per loop.
     * @property tempoTarget Maximum tempo to reach.
     */
    data class Config(
        val targetLoopCount: Int = 0,
        val loopDelayMs: Long = 0L,
        val progressiveTempo: Boolean = false,
        val tempoStart: Float = 0.5f,
        val tempoStep: Float = 0.05f,
        val tempoTarget: Float = 1.0f
    )

    private val _loopCount = MutableStateFlow(0)
    /** Current number of completed loops. */
    val loopCount: StateFlow<Int> = _loopCount.asStateFlow()

    private val _elapsedMs = MutableStateFlow(0L)
    /** Total elapsed time in the current session in milliseconds. */
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    private val _currentTempo = MutableStateFlow(1.0f)
    /** The tempo currently applied to the audio player. */
    val currentTempo: StateFlow<Float> = _currentTempo.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    /** Indicates if a session is currently active. */
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    /** The current active configuration. */
    var config = Config()
        private set
    
    private var startTimeMs = 0L

    /**
     * Starts a new practice session with the given configuration.
     * 
     * @param cfg The configuration to use.
     * @param initialTempo The current player tempo if not progressive.
     */
    fun start(cfg: Config, initialTempo: Float) {
        config = cfg
        _loopCount.value = 0
        _elapsedMs.value = 0L
        _currentTempo.value = if (cfg.progressiveTempo) cfg.tempoStart else initialTempo
        startTimeMs = System.currentTimeMillis()
        _isRunning.value = true
    }

    /**
     * Stops the current session and records the final elapsed time.
     */
    fun stop() {
        _isRunning.value = false
        _elapsedMs.value = System.currentTimeMillis() - startTimeMs
    }

    /**
     * Updates the elapsed time based on the system clock.
     */
    fun tick() {
        if (_isRunning.value) {
            _elapsedMs.value = System.currentTimeMillis() - startTimeMs
        }
    }

    /**
     * Called when a loop completes. Updates loop count and tempo if progressive.
     * 
     * @return The new tempo factor to apply to the player.
     */
    fun onLoopCompleted(): Float {
        _loopCount.value += 1
        if (config.progressiveTempo && _currentTempo.value < config.tempoTarget) {
            _currentTempo.value = (_currentTempo.value + config.tempoStep)
                .coerceAtMost(config.tempoTarget)
        }
        return _currentTempo.value
    }

    /**
     * Checks if the target loop count has been reached.
     */
    fun shouldStop(): Boolean {
        val target = config.targetLoopCount
        return target > 0 && _loopCount.value >= target
    }

    /**
     * Resets the session state to defaults.
     */
    fun reset() {
        _loopCount.value = 0
        _elapsedMs.value = 0L
        _isRunning.value = false
    }
}
