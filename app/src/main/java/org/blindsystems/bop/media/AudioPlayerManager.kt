package org.blindsystems.bop.media

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class AudioPlayerManager(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _playbackPitch = MutableStateFlow(1.0f)
    val playbackPitch: StateFlow<Float> = _playbackPitch.asStateFlow()

    // A-B Loop State
    var loopA: Long? = null
    var loopB: Long? = null
    var isLoopingAB: Boolean = false

    init {
        initPlayer()
    }

    private fun initPlayer() {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }
                
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        _duration.value = duration.coerceAtLeast(0L)
                    }
                }
            })
        }
    }

    fun loadUri(uri: Uri) {
        exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = false
        }
        clearLoop()
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun stop() {
        exoPlayer?.stop()
        exoPlayer?.seekTo(0)
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    fun setSpeedAndPitch(speed: Float, pitchSemitones: Float, preserving: Boolean) {
        _playbackSpeed.value = speed
        _playbackPitch.value = pitchSemitones
        
        val pitchFactor = Math.pow(2.0, pitchSemitones / 12.0).toFloat()
        
        if (preserving) {
            exoPlayer?.playbackParameters = PlaybackParameters(speed, pitchFactor)
        } else {
            // Tape mode: speed and pitch are linked. 
            // Setting both to the same value in Media3 (Sonic) triggers simple resampling.
            val combined = speed * pitchFactor
            exoPlayer?.playbackParameters = PlaybackParameters(combined, combined)
        }
    }

    var onLoopCompleted: (() -> Unit)? = null

    fun updatePosition() {
        exoPlayer?.let {
            val pos = it.currentPosition
            _currentPosition.value = pos
            checkABLoop(pos)
        }
    }

    fun setLoopA() {
        loopA = exoPlayer?.currentPosition
    }

    fun setLoopB() {
        loopB = exoPlayer?.currentPosition
    }

    fun clearLoop() {
        loopA = null
        loopB = null
        isLoopingAB = false
    }

    private fun checkABLoop(currentPos: Long) {
        if (isLoopingAB) {
            val a = loopA ?: return
            val b = loopB ?: return
            if (a < b && currentPos >= b) {
                seekTo(a)
                onLoopCompleted?.invoke()
            }
        }
    }

    fun setVolume(value: Float) {
        exoPlayer?.volume = value.coerceIn(0f, 1f)
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
