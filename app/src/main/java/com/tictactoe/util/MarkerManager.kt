package com.tictactoe.util

import android.content.Context
import android.content.SharedPreferences

object MarkerManager {

    private const val PREFS_NAME = "marker_prefs"
    private const val KEY_MARKER_X = "marker_x"
    private const val KEY_MARKER_O = "marker_o"

    private lateinit var prefs: SharedPreferences

    val defaultX = "X"
    val defaultO = "O"

    val emojiOptions = listOf(
        "X", "O",
        "\uD83D\uDD34", "\uD83D\uDD35",  // Red/Blue circles
        "\u274C", "\u2B55",                // Cross/Circle
        "\u2764\uFE0F", "\uD83D\uDCA2",   // Heart/Anger
        "\uD83D\uDE08", "\uD83D\uDE07",   // Devil/Angel
        "\uD83C\uDFAE", "\uD83D\uDCAF",   // Game/100
        "\uD83D\uDD25", "\u2744\uFE0F",   // Fire/Snowflake
        "\uD83C\uDF1F", "\u26A1",         // Star/Lightning
        "\uD83D\uDC80", "\uD83C\uDF08",   // Skull/Rainbow
        "\uD83E\uDD16", "\uD83E\uDD84",   // Robot/Unicorn
    )

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var markerX: String
        get() = prefs.getString(KEY_MARKER_X, defaultX) ?: defaultX
        set(value) = prefs.edit().putString(KEY_MARKER_X, value).apply()

    var markerO: String
        get() = prefs.getString(KEY_MARKER_O, defaultO) ?: defaultO
        set(value) = prefs.edit().putString(KEY_MARKER_O, value).apply()
}
