package com.tictactoe.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

object SoundManager {

    private var soundPool: SoundPool? = null
    private var tapSound = 0
    private var winSound = 0
    private var drawSound = 0
    private var loaded = false

    fun init(context: Context) {
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(attrs)
                .build()

            soundPool?.setOnLoadCompleteListener { _, _, _ -> loaded = true }

            val uri = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
            if (uri != null) {
                var afd: android.content.res.AssetFileDescriptor? = null
                try {
                    afd = context.contentResolver.openAssetFileDescriptor(uri, "r")
                    afd?.let {
                        tapSound = soundPool?.load(it, 1) ?: 0
                        winSound = soundPool?.load(it, 1) ?: 0
                        drawSound = soundPool?.load(it, 1) ?: 0
                    }
                } catch (_: Exception) {
                    // Devices/CI may not have audio
                } finally {
                    afd?.close()
                }
            }
        } catch (_: Exception) {
            // Silently fail on devices/CI without audio
        }
    }

    fun playTap() {
        if (loaded && PrefsManager.soundEnabled) soundPool?.play(tapSound, 0.7f, 0.7f, 1, 0, 1f)
    }

    fun playWin() {
        if (loaded && PrefsManager.soundEnabled) soundPool?.play(winSound, 1f, 1f, 1, 0, 1f)
    }

    fun playDraw() {
        if (loaded && PrefsManager.soundEnabled) soundPool?.play(drawSound, 0.5f, 0.5f, 1, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
