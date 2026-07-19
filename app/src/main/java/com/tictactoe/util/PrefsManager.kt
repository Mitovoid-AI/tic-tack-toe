package com.tictactoe.util

import android.content.Context
import android.content.SharedPreferences

object PrefsManager {

    private lateinit var prefs: SharedPreferences

    private const val KEY_SOUND = "sound_enabled"
    private const val KEY_VIBRATE = "vibrate_enabled"
    private const val KEY_BOARD_SIZE = "board_size"
    private const val KEY_FIRST_PLAYER = "first_player"

    fun init(context: Context) {
        prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var vibrateEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATE, value).apply()

    var boardSize: Int
        get() = prefs.getInt(KEY_BOARD_SIZE, 3)
        set(value) = prefs.edit().putInt(KEY_BOARD_SIZE, value).apply()

    var firstPlayer: String
        get() = prefs.getString(KEY_FIRST_PLAYER, "X") ?: "X"
        set(value) = prefs.edit().putString(KEY_FIRST_PLAYER, value).apply()
}
