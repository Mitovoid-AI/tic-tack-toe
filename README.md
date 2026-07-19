# Tic Tac Toe — Neon Edition

A modern Android Tic Tac Toe game with dark neon glassmorphism UI. Built with Kotlin and Jetpack Compose.

## Features

- **Player vs Player** — two players on the same device
- **Player vs AI** — three difficulty levels (Easy, Medium, Hard/Unbeatable)
- **Animated UI** — smooth X/O draw animations, glowing win highlights
- **Match History** — local stats tracking with Room database
- **Remote Config** — change theme, colors, board size, and game modes without rebuilding the APK

## Screenshots

| Home | Game | Stats |
|------|------|-------|
| Dark neon theme with mode selection | Animated 3x3 board with score tracking | Win/loss/draw history per mode |

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM |
| Local Storage | Room DB |
| Remote Config | OkHttp + kotlinx.serialization |
| Async | Coroutines + Flow |

## Download

Grab the latest APK from [Releases](https://github.com/Mitovoid-AI/tic-tack-toe/releases).

## Remote Config (No Rebuild Needed)

Edit [`config.json`](config.json) directly on GitHub to change the app's appearance and behavior:

```json
{
  "theme": {
    "background": "#1a0a2e",
    "primary": "#ff6b6b",
    "secondary": "#4ecdc4",
    "accent": "#ffd93d"
  },
  "board": {
    "size": 3,
    "win_length": 3
  },
  "features": {
    "pvp_enabled": true,
    "ai_enabled": true,
    "ai_difficulty": "hard"
  },
  "ui": {
    "app_title": "XO Arena",
    "subtitle": "Battle Mode"
  }
}
```

Changes take effect the next time the app is opened with an internet connection.

## Build from Source

**Prerequisites:** JDK 17 + Android SDK

```bash
git clone https://github.com/Mitovoid-AI/tic-tack-toe.git
cd tic-tac-toe
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Project Structure

```
app/src/main/java/com/tictactoe/
├── config/          # Remote config fetching and caching
├── data/            # Room database, DAO, repository
├── game/            # Game state, AI logic, ViewModel
├── ui/
│   ├── components/  # Reusable UI (GlassCard, NeonButton, GameBoard)
│   ├── screens/     # Home, Game, Stats, Settings
│   ├── theme/       # Colors, typography, theme
│   └── navigation/  # Nav graph
└── util/            # Extensions
```

## CI/CD

Every push to `main` triggers GitHub Actions which:
1. Builds the debug APK
2. Publishes it as a tagged release (e.g., `v1.5 (2026-07-19)`)

## License

MIT
