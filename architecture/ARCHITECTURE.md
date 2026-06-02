# D&D Helper — Architecture

## Project Overview

D&D Helper is a Kotlin Multiplatform (KMP) application for Dungeons & Dragons 5th Edition sessions. It supports **Android** (primary, player-only), **Desktop** (JVM, with Player and Admin/DM modes), and **Web Browser** (player-only, full-screen canvas).

### Platform Modes

| Platform | Modes |
|----------|-------|
| Android | Player only |
| Desktop | Player copy + Admin (DM) |
| Web Browser | Player only (full-screen canvas) |

### Admin (DM) Desktop Mode Sub-Modes
- **Preparation Mode** — create locations, characters, equipment, monsters, bosses
- **Game Mode** — live session: show players locations, manage combat, quick item creation

### Backend
- **Ktor Server** — Kotlin-based backend running Netty.
- **SQLite Database** — local persistent storage using Exposed ORM.
- **REST API** — resource-based API for all D&D entities.
- **WebSockets** — real-time updates across all connected clients in a session.

## High-Level Architecture

We follow **Clean Architecture** with **MVVM** presentation pattern.

### Module Structure

```
:shared                     ← KMP shared module (UI, domain, data logic)
├── commonMain              ← Shared Compose UI, ViewModels, UseCases, Repositories
├── androidMain             ← Android-specific platform bindings (SharedPreferences)
├── desktopMain             ← Desktop-specific platform bindings (java.util.prefs)
└── wasmJsMain              ← Web-specific platform bindings (localStorage)

:server                     ← Ktor server module (JVM)
├── database                ← Exposed tables and factory
└── routing                 ← REST and WebSocket endpoints

:app                        ← Android application module
├── MainActivity.kt         ← Entry point, calls shared App()
├── AndroidManifest.xml
└── Koin Android init

:desktop                    ← Desktop application module
├── main.kt                 ← Entry point, calls shared App()
└── Koin Desktop init

:web                        ← Web Browser application module (Kotlin/Wasm)
├── main.kt                 ← Entry point, calls shared App() inside CanvasBasedWindow
└── index.html              ← Full-screen canvas, no phone frame
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose Multiplatform, Material 3 |
| State Management | ViewModel + StateFlow |
| DI | Koin (KMP-compatible) |
| Network | Ktor client + kotlinx.serialization |
| Real-time | Ktor WebSockets |
| Backend | Ktor server + Exposed + SQLite |
| Navigation | navigation-compose 2.8+ (type-safe routes via `@Serializable`) |
| Image Loading | Coil 3 (Compose Multiplatform) |
| Platform Storage | `expect`/`actual` `CharacterStorage` interface |
| Build | Gradle with version catalogs (.kts) |

## Key Architectural Decisions

### 1. Koin over Hilt
- Koin is pure Kotlin, works in `commonMain`, and supports KMP.

### 2. Ktor over Retrofit
- Ktor client is multiplatform by design.

### 3. Type-Safe Navigation
- Navigation Compose 2.8+ supports `@Serializable` route objects.

### 4. Real-Time Sync (WebSockets)
- **No more polling** — clients connect to a WebSocket endpoint `/api/{sessionId}/ws`.
- **Session isolation** — the server tracks connections by `sessionId` and broadcasts updates only to relevant clients.
- **Instant updates** — whenever data is modified via POST/DELETE, the server sends a tiny notification frame (e.g., `update:characters`), triggering clients to reload specific data.

### 5. Platform-Specific Start Destination
- Desktop (`isDesktop = true`) skips the `Start` screen and opens directly to `CharacterList`.
- Android and Web (`isDesktop = false`) show the `Start` screen first for character ID input.

## Data Flow

```
Compose Screen → ViewModel → Repository → KtorRemoteDataSource (HTTP / WS)
                    ↑                                              |
                    └──────── StateFlow ← Result ←──────────────────┘
```

## Error Handling Strategy

We use a sealed `Result<T>` class across all layers:

```kotlin
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val error: AppError) : Result<Nothing>
}
```
