package com.tictactoe.config

import androidx.compose.ui.graphics.Color
import com.tictactoe.ui.theme.ThemeManager

object AppConfig {
    private var remoteConfig: RemoteConfig = RemoteConfig()

    fun update(config: RemoteConfig) {
        remoteConfig = config
    }

    val board get() = remoteConfig.board
    val features get() = remoteConfig.features
    val ui get() = remoteConfig.ui

    // Colors now come from ThemeManager (supports in-app theme switching)
    fun backgroundColor() = ThemeManager.getColors().background
    fun surfaceColor() = ThemeManager.getColors().surface
    fun primaryColor() = ThemeManager.getColors().primary
    fun secondaryColor() = ThemeManager.getColors().secondary
    fun accentColor() = ThemeManager.getColors().accent
    fun textPrimaryColor() = ThemeManager.getColors().textPrimary
    fun textSecondaryColor() = ThemeManager.getColors().textSecondary
}
