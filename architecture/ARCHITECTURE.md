# D&D Helper — Architecture

## Project Overview

D&D Helper is a Kotlin Multiplatform (KMP) application for Dungeons & Dragons 5th Edition sessions. It supports **Android** (primary, player-only) and **Desktop** (JVM, with Player and Admin/DM modes).

### Platform Modes

| Platform | Modes |
|----------|-------|
| Android | Player only |
| Desktop | Player copy + Admin (DM) |

### Admin (DM) Desktop Mode Sub-Modes
- **Preparation Mode** — create locations, characters, equipment, monsters, bosses
- **Game Mode** — live session: show players locations, manage combat, quick item creation

### Backend
- **Google Sheets** — single shared spreadsheet per campaign, editable by anyone with link
- **Google Sheets REST API** — with OAuth for read/write access
- **No local database** — all data fetched from Google Sheets directly

## High-Level Architecture

We follow **Clean Architecture** with **MVVM** presentation pattern.

### Module Structure

```
:shared                     ← KMP shared module (UI, domain, data logic)
├── commonMain              ← Shared Compose UI, ViewModels, UseCases, Repositories
├── androidMain             ← Android-specific bindings (OAuth, platform APIs)
└── desktopMain             ← Desktop-specific bindings (OAuth, window state)

:app                        ← Android application module
├── MainActivity.kt         ← Entry point, calls shared App()
├── AndroidManifest.xml
└── Koin Android init

:desktop                    ← Desktop application module
├── main.kt                 ← Entry point, calls shared App()
└── Koin Desktop init
```

**Why not feature-vertical modules yet?** The project is small. We keep everything in `:shared` initially. When it grows, extract `:feature:*` modules from `:shared/commonMain`.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose Multiplatform, Material 3 |
| State Management | ViewModel + StateFlow |
| DI | Koin (KMP-compatible) |
| Network | Ktor client + kotlinx.serialization |
| Backend | Google Sheets REST API (OAuth 2.0) |
| Navigation | navigation-compose 2.8+ (type-safe routes) |
| Image Loading | Coil 3 (Compose Multiplatform) |
| Build | Gradle with version catalogs (.kts) |

## Data Flow

```
Compose Screen → ViewModel → UseCase → Repository → Google Sheets DataSource (Ktor + OAuth)
                    ↑                                              |
                    └──────── StateFlow ← Result ←──────────────────┘
```

## Module Dependencies

```
:app
└── :shared (androidMain artifacts)

:desktop
└── :shared (desktopMain artifacts)

:shared
├── commonMain: Koin, Compose, Navigation, Ktor, kotlinx.serialization, Coil 3
├── androidMain: Android-specific OAuth bindings
└── desktopMain: Desktop-specific OAuth bindings
```

## Key Architectural Decisions

### 1. Koin over Hilt
- Hilt is Android-only and requires kapt (incompatible with AGP 9 built-in Kotlin).
- Koin is pure Kotlin, works in `commonMain`, and supports KMP.

### 2. Google Sheets as Backend (no local DB)
- User explicitly chose **no SQLDelight** — all data lives in Google Sheets.
- Simpler stack, single source of truth.
- Trade-off: no offline support, network latency on every operation.

### 3. Ktor over Retrofit
- Ktor client is multiplatform by design.
- Retrofit is Android/JVM-only and cannot exist in shared modules.

### 4. Type-Safe Navigation
- Navigation Compose 2.8+ supports `@Serializable` route objects.
- No string-based routes — compile-time safety.

### 5. Single `:shared` Module (for now)
- All UI, domain, and data code starts in `:shared/commonMain`.
- When the codebase grows, extract `:feature:*` modules.
- This avoids premature modularization and speeds up initial development.

### 6. Platform-Specific OAuth
- Google Sign-In / OAuth 2.0 flow is platform-specific:
  - **Android**: Google Sign-In SDK or Custom Tabs
  - **Desktop**: System browser + local callback server
- Boundary: `interface AuthProvider` in `commonMain`, platform implementations in `androidMain`/`desktopMain`.

## Error Handling Strategy

We use a sealed `Result<T>` class across all layers:

- **Data sources**: Throw platform exceptions (`IOException`, `HttpException`, etc.).
- **Repositories**: Catch platform exceptions, map to domain error types (`AppError`).
- **Use cases**: Return `Result<T>` with domain errors.
- **ViewModels**: Handle `Result<T>`, map to UI state (loading/success/error).

```kotlin
// :shared/commonMain
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val error: AppError) : Result<Nothing>
}

sealed interface AppError {
    data object Network : AppError
    data object Unauthorized : AppError
    data object NotFound : AppError
    data class Unknown(val message: String) : AppError
}
```

## Platform Boundaries (KMP)

| Capability | Boundary Shape | Location |
|-----------|---------------|----------|
| Network | Ktor (common) | `:shared/commonMain` |
| OAuth / Auth | `interface AuthProvider` + platform binding | `:shared/commonMain` + `androidMain`/`desktopMain` |
| File I/O | `expect`/`actual` or interface | `:shared/commonMain` |
| Preferences | `expect`/`actual` or interface | `:shared/commonMain` |
| Platform UI (if any) | `expect` composable leaf | `:shared/commonMain` |

## App Entry Points

### Android (`:app`)
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DndHelperTheme {
                App(platform = Platform.ANDROID)
            }
        }
    }
}
```

### Desktop (`:desktop`)
```kotlin
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "D&D Helper") {
        DndHelperTheme {
            App(platform = Platform.DESKTOP)
        }
    }
}
```

### Shared (`:shared`)
```kotlin
@Composable
fun App(platform: Platform) {
    // Mode selection for Desktop, fixed Player for Android
    // Navigation host with type-safe routes
}
```

## Build Configuration

- **AGP**: 9.2.1
- **Kotlin**: 2.2.10
- **Compose BOM**: 2026.02.01
- **Min SDK**: 29
- **Target SDK**: 36
- **Java/Kotlin target**: 21 (migrating from 11)

## Open Questions / Future Decisions

- [ ] Whether to add iOS target (pending Apple Developer account)
- [ ] When to extract `:feature:*` modules from `:shared`
- [ ] Google Sheets OAuth implementation details (Google Sign-In SDK vs Custom Tabs vs generic OAuth)
- [ ] Desktop window management (single window vs multiple windows for DM)
- [ ] Image storage: Google Sheets cell images vs external hosting vs Google Drive
- [ ] Real-time sync: polling Google Sheets vs push notifications (not possible with Sheets API)
