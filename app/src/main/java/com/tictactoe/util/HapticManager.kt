package com.tictactoe.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticManager {

    private var vibrator: Vibrator? = null

    fun init(context: Context) {
        vibrator = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    fun lightTap() {
        if (!PrefsManager.vibrateEnabled) return
        vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun mediumTap() {
        if (!PrefsManager.vibrateEnabled) return
        vibrator?.vibrate(VibrationEffect.createOneShot(50, 180))
    }

    fun winVibration() {
        if (!PrefsManager.vibrateEnabled) return
        val pattern = longArrayOf(0, 50, 50, 50, 50, 100)
        val amplitudes = intArrayOf(0, 100, 0, 180, 0, 255)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
    }

    fun drawVibration() {
        if (!PrefsManager.vibrateEnabled) return
        vibrator?.vibrate(VibrationEffect.createOneShot(100, 120))
    }
}
