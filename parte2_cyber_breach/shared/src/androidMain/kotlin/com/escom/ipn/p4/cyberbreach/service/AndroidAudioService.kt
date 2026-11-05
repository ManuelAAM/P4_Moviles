package com.escom.ipn.p4.cyberbreach.service

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class AndroidAudioService : IAudioService {

    override var isMuted: Boolean = false
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun playClick() {
        if (isMuted) return
        scope.launch {
            playTone(800.0, 30)
        }
    }

    override fun playHit() {
        if (isMuted) return
        scope.launch {
            playTone(520.0, 60)
            playTone(880.0, 100)
        }
    }

    override fun playMiss() {
        if (isMuted) return
        scope.launch {
            playTone(220.0, 70)
        }
    }

    override fun playSkill() {
        if (isMuted) return
        scope.launch {
            playChirp(1200.0, 400.0, 120)
        }
    }

    override fun playVictory() {
        if (isMuted) return
        scope.launch {
            playTone(523.25, 100) // C5
            playTone(659.25, 100) // E5
            playTone(783.99, 100) // G5
            playTone(1046.50, 250) // C6
        }
    }

    override fun playAlarm() {
        if (isMuted) return
        scope.launch {
            playTone(350.0, 100)
            playTone(280.0, 120)
        }
    }

    private fun playTone(frequencyHz: Double, durationMs: Int) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
            val generatedSnd = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val dVal = sin(2.0 * Math.PI * i / (sampleRate / frequencyHz))
                // Envelope fade-out at the end to prevent clicking
                val fade = if (i > numSamples - 100) (numSamples - i) / 100.0 else 1.0
                generatedSnd[i] = (dVal * 32767 * 0.4 * fade).toInt().toShort()
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(generatedSnd, 0, numSamples)
            track.play()
            Thread.sleep(durationMs.toLong() + 20)
            track.release()
        } catch (e: Exception) {
            // Ignore audio glitches if device audio is busy
        }
    }

    private fun playChirp(startFreq: Double, endFreq: Double, durationMs: Int) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
            val generatedSnd = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples.toDouble()
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val dVal = sin(2.0 * Math.PI * i / (sampleRate / currentFreq))
                generatedSnd[i] = (dVal * 32767 * 0.35).toInt().toShort()
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(generatedSnd, 0, numSamples)
            track.play()
            Thread.sleep(durationMs.toLong() + 20)
            track.release()
        } catch (e: Exception) {
        }
    }
}
