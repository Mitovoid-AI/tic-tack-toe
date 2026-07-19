package com.tictactoe.config

import kotlinx.serialization.Serializable

@Serializable
data class RemoteConfig(
    val version: Int = 1,
    val theme: ThemeConfig = ThemeConfig(),
    val board: BoardConfig = BoardConfig(),
    val features: FeaturesConfig = FeaturesConfig(),
    val ui: UiConfig = UiConfig()
)

@Serializable
data class ThemeConfig(
    val background: String = "#0a0a1a",
    val surface: String = "#1a1a2e",
    val primary: String = "#00d4ff",
    val secondary: String = "#ff006e",
    val accent: String = "#7b2ff7",
    val text_primary: String = "#e0e0e0",
    val text_secondary: String = "#a0a0a0"
)

@Serializable
data class BoardConfig(
    val size: Int = 3,
    val win_length: Int = 3
)

@Serializable
data class FeaturesConfig(
    val pvp_enabled: Boolean = true,
    val ai_enabled: Boolean = true,
    val ai_difficulty: String = "medium",
    val show_stats: Boolean = true,
    val show_history: Boolean = true
)

@Serializable
data class UiConfig(
    val app_title: String = "Tic Tac Toe",
    val subtitle: String = "Neon Edition"
)
