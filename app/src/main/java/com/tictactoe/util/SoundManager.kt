package com.tictactoe.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

object SoundManager {

    private lateinit var soundPool: SoundPool
    private var tapSound = 0
    private var winSound = 0
    private var drawSound = 0
    private var loaded = false

    fun init(context: Context) {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(attrs)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, _ -> loaded = true }

        // Load system-generated tones as placeholder
        // Replace R.raw.xxx with actual sound files when you add them
        tapSound = loadSystemSound(context)
        winSound = loadSystemSound(context)
        drawSound = loadSystemSound(context)
    }

    private fun loadSystemSound(context: Context): Int {
        // Uses Android notification sound as fallback
        val uri = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
        return soundPool.load(context, uri, 1)
    }

    fun playTap() {
        if (loaded && PrefsManager.soundEnabled) soundPool.play(tapSound, 0.7f, 0.7f, 1, 0, 1f)
    }

    fun playWin() {
        if (loaded && PrefsManager.soundEnabled) soundPool.play(winSound, 1f, 1f, 1, 0, 1f)
    }

    fun playDraw() {
        if (loaded && PrefsManager.soundEnabled) soundPool.play(drawSound, 0.5f, 0.5f, 1, 0, 1f)
    }

    fun release() {
        if (::soundPool.isInitialized) soundPool.release()
    }
}
