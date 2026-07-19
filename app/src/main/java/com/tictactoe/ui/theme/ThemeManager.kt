package com.tictactoe.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class AppTheme(val label: String) {
    NEON("Neon"),
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System")
}

data class ThemeColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color
)

object ThemeManager {

    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "selected_theme"

    private lateinit var prefs: SharedPreferences

    var currentTheme by mutableStateOf(AppTheme.NEON)
        private set

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_THEME, AppTheme.NEON.name) ?: AppTheme.NEON.name
        currentTheme = try { AppTheme.valueOf(saved) } catch (e: Exception) { AppTheme.NEON }
    }

    fun setTheme(theme: AppTheme) {
        currentTheme = theme
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    fun getColors(): ThemeColors = when (currentTheme) {
        AppTheme.NEON -> ThemeColors(
            background = Color(0xFF0A0A1A),
            surface = Color(0xFF1A1A2E),
            primary = Color(0xFF00D4FF),
            secondary = Color(0xFFFF006E),
            accent = Color(0xFF7B2FF7),
            textPrimary = Color(0xFFE0E0E0),
            textSecondary = Color(0xFFA0A0A0)
        )
        AppTheme.LIGHT -> ThemeColors(
            background = Color(0xFFF5F5F5),
            surface = Color(0xFFFFFFFF),
            primary = Color(0xFF1A73E8),
            secondary = Color(0xFFE91E63),
            accent = Color(0xFF6200EE),
            textPrimary = Color(0xFF1A1A1A),
            textSecondary = Color(0xFF666666)
        )
        AppTheme.DARK -> ThemeColors(
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC6),
            accent = Color(0xFFFFAB40),
            textPrimary = Color(0xFFE0E0E0),
            textSecondary = Color(0xFF9E9E9E)
        )
        AppTheme.SYSTEM -> ThemeColors(
            background = Color(0xFF0A0A1A),
            surface = Color(0xFF1A1A2E),
            primary = Color(0xFF00D4FF),
            secondary = Color(0xFFFF006E),
            accent = Color(0xFF7B2FF7),
            textPrimary = Color(0xFFE0E0E0),
            textSecondary = Color(0xFFA0A0A0)
        )
    }
}
