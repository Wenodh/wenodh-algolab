package com.wenodh.zen.focus

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

/**
 * Generates non-musical noise locally. No audio files, network access or copyrighted tracks.
 */
object AmbientSoundEngine {
    enum class Sound { WHITE, PINK, BROWN }

    private const val SAMPLE_RATE = 22_050
    private val running = AtomicBoolean(false)
    @Volatile private var track: AudioTrack? = null
    @Volatile private var writer: Thread? = null
    @Volatile private var audioManager: AudioManager? = null
    @Volatile private var focusRequest: AudioFocusRequest? = null

    @Synchronized
    fun start(sound: Sound) {
        stop()
        val minBuffer = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val manager = appAudioManager ?: return
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attributes)
            .setOnAudioFocusChangeListener { change ->
                if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) stop()
            }
            .build()
        if (manager.requestAudioFocus(request) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return
        audioManager = manager
        focusRequest = request
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(minBuffer * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        track = audioTrack
        running.set(true)
        audioTrack.play()
        writer = Thread({
            val samples = ShortArray(2048)
            var brown = 0.0
            var pinkB0 = 0.0
            var pinkB1 = 0.0
            var pinkB2 = 0.0
            while (running.get()) {
                for (i in samples.indices) {
                    val white = Random.nextDouble(-1.0, 1.0)
                    val value = when (sound) {
                        Sound.WHITE -> white * 0.12
                        Sound.BROWN -> {
                            brown = ((brown + 0.02 * white) / 1.02).coerceIn(-1.0, 1.0)
                            brown * 2.8
                        }
                        Sound.PINK -> {
                            pinkB0 = 0.99765 * pinkB0 + white * 0.0990460
                            pinkB1 = 0.96300 * pinkB1 + white * 0.2965164
                            pinkB2 = 0.57000 * pinkB2 + white * 1.0526913
                            (pinkB0 + pinkB1 + pinkB2 + white * 0.1848) * 0.035
                        }
                    }
                    samples[i] = (value.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                }
                if (running.get()) audioTrack.write(samples, 0, samples.size)
            }
        }, "WenodhZenAmbient").apply { isDaemon = true; start() }
    }

    @Synchronized
    fun stop() {
        running.set(false)
        writer?.join(250)
        writer = null
        track?.let { audio -> runCatching { audio.pause(); audio.flush(); audio.release() } }
        track = null
        val manager = audioManager
        val request = focusRequest
        if (manager != null && request != null) runCatching { manager.abandonAudioFocusRequest(request) }
        audioManager = null
        focusRequest = null
    }

    // AudioManager is resolved lazily from the application context supplied by the service.
    @Volatile private var appAudioManager: AudioManager? = null

    fun initialize(context: android.content.Context) {
        appAudioManager = context.applicationContext.getSystemService(AudioManager::class.java)
    }
}
