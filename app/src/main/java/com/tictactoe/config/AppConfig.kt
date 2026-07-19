package com.tictactoe.config

import androidx.compose.ui.graphics.Color

object AppConfig {
    private var remoteConfig: RemoteConfig = RemoteConfig()

    fun update(config: RemoteConfig) {
        remoteConfig = config
    }

    val theme get() = remoteConfig.theme
    val board get() = remoteConfig.board
    val features get() = remoteConfig.features
    val ui get() = remoteConfig.ui

    fun backgroundColor() = Color(android.graphics.Color.parseColor(theme.background))
    fun surfaceColor() = Color(android.graphics.Color.parseColor(theme.surface))
    fun primaryColor() = Color(android.graphics.Color.parseColor(theme.primary))
    fun secondaryColor() = Color(android.graphics.Color.parseColor(theme.secondary))
    fun accentColor() = Color(android.graphics.Color.parseColor(theme.accent))
    fun textPrimaryColor() = Color(android.graphics.Color.parseColor(theme.text_primary))
    fun textSecondaryColor() = Color(android.graphics.Color.parseColor(theme.text_secondary))
}
