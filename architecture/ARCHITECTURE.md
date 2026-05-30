# D&D Helper — Architecture

## Project Overview

D&D Helper is a Kotlin Multiplatform (KMP) application for Dungeons & Dragons 5th Edition sessions. It supports **Android** (primary, player-only), **Desktop** (JVM, with Player and Admin/DM modes), and **Web Browser** (player-only, mobile-like view).

### Platform Modes

| Platform | Modes |
|----------|-------|
| Android | Player only |
| Desktop | Player copy + Admin (DM) |
| Web Browser | Player only (mobile-like view) |

### Admin (DM) Desktop Mode Sub-Modes
- **Preparation Mode** — create locations, characters, equipment, monsters, bosses
- **Game Mode** — live session: show players locations, manage combat, quick item creation

### Backend
- **Google Sheets** — single shared spreadsheet per campaign, editable by anyone with link
- **Google Apps Script** — deployed as a Web App that proxies read/write requests to Google Sheets
- **No OAuth** — Apps Script Web App handles all Google authentication server-side
- **No local database** — all data fetched from Google Sheets via Apps Script

## High-Level Architecture

We follow **Clean Architecture** with **MVVM** presentation pattern.

### Module Structure

```
:shared                     ← KMP shared module (UI, domain, data logic)
├── commonMain              ← Shared Compose UI, ViewModels, UseCases, Repositories
├── androidMain             ← Android-specific platform bindings
├── desktopMain             ← Desktop-specific platform bindings
└── wasmJsMain              ← Web-specific platform bindings

:app                        ← Android application module
├── MainActivity.kt         ← Entry point, calls shared App()
├── AndroidManifest.xml
└── Koin Android init

:desktop                    ← Desktop application module
├── main.kt                 ← Entry point, calls shared App()
└── Koin Desktop init

:web                        ← Web Browser application module (Kotlin/Wasm)
├── main.kt                 ← Entry point, calls shared App() inside CanvasBasedWindow
└── index.html              ← Mobile-like phone frame wrapper
```

**Why not feature-vertical modules yet?** The project is small. We keep everything in `:shared` initially. When it grows, extract `:feature:*` modules from `:shared/commonMain`.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose Multiplatform, Material 3 |
| State Management | ViewModel + StateFlow |
| DI | Koin (KMP-compatible) |
| Network | Ktor client + kotlinx.serialization |
| Backend | Google Apps Script Web App (Ktor POST) |
| Navigation | navigation-compose 2.8+ (type-safe routes) |
| Image Loading | Coil 3 (Compose Multiplatform) |
| Build | Gradle with version catalogs (.kts) |

## Data Flow

```
Compose Screen → ViewModel → Repository → GoogleAppsScriptDataSource (Ktor POST)
                    ↑                                              |
                    └──────── StateFlow ← Result ←──────────────────┘
```

## Module Dependencies

```
:app
└── :shared (androidMain artifacts)

:desktop
└── :shared (desktopMain artifacts)

:web
└── :shared (wasmJsMain artifacts)

:shared
├── commonMain: Koin, Compose, Navigation, Ktor, kotlinx.serialization, Coil 3
├── androidMain: Android-specific platform bindings
├── desktopMain: Desktop-specific platform bindings
└── wasmJsMain: Web-specific platform bindings (ktor-client-js)
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

### 6. Google Apps Script Backend
- **No OAuth in the app** — authentication is handled server-side by the Apps Script Web App.
- **Ktor POST requests** — the app sends JSON payloads to the Apps Script Web App URL with an `action` field (e.g., `getCharacters`, `saveCharacter`).
- **Apps Script handles Sheets API** — all Google Sheets read/write operations happen in the Apps Script code, not in the Kotlin app.
- **Simple deployment** — just paste the Web App URL into `GoogleAppsScriptConfig.WEB_APP_URL`.

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
| Network | Ktor client POST to Apps Script Web App | `:shared/commonMain` |
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
            App()
        }
    }
}
```

### Web Browser (`:web`)
```kotlin
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow("D&D Helper", canvasElementId = "ComposeTarget") {
        DndHelperTheme {
            App()
        }
    }
}
```

### Shared (`:shared`)
```kotlin
@Composable
fun App() {
    // Player mode for Android + Web, Player/DM mode selection for Desktop
    // Navigation host with type-safe routes
}
```

## Build Configuration

- **AGP**: 8.13.2
- **Kotlin**: 2.1.0
- **Compose Multiplatform**: 1.7.3
- **Min SDK**: 29
- **Target SDK**: 36
- **Java/Kotlin target**: 21
- **Web target**: Kotlin/Wasm (wasmJs) with Canvas-based rendering

## Open Questions / Future Decisions

- [ ] Whether to add iOS target (pending Apple Developer account)
- [ ] When to extract `:feature:*` modules from `:shared`
- [x] Backend approach: Google Apps Script Web App (no in-app OAuth)
- [ ] Desktop window management (single window vs multiple windows for DM)
- [ ] Image storage: Google Sheets cell images vs external hosting vs Google Drive
- [ ] Real-time sync: polling Google Sheets vs push notifications (not possible with Sheets API)
