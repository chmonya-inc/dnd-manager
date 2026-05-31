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
├── androidMain             ← Android-specific platform bindings (SharedPreferences)
├── desktopMain             ← Desktop-specific platform bindings (java.util.prefs)
└── wasmJsMain              ← Web-specific platform bindings (localStorage)

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

**Why not feature-vertical modules yet?** The project is small. We keep everything in `:shared` initially. When it grows, extract `:feature:*` modules from `:shared/commonMain`.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose Multiplatform, Material 3 |
| State Management | ViewModel + StateFlow |
| DI | Koin (KMP-compatible) |
| Network | Ktor client + kotlinx.serialization |
| Backend | Google Apps Script Web App (Ktor GET with query parameter) |
| Navigation | navigation-compose 2.8+ (type-safe routes via `@Serializable`) |
| Image Loading | Coil 3 (Compose Multiplatform) |
| Platform Storage | `expect`/`actual` `CharacterStorage` interface |
| Build | Gradle with version catalogs (.kts) |

## Navigation

Type-safe navigation using `@Serializable` route objects (Navigation Compose 2.8+):

```kotlin
@Serializable object Start
@Serializable object CharacterList
@Serializable data class CharacterDetail(val id: String)
```

### Navigation Graph

```
Start (mobile/web) ──Load Character──→ CharacterDetail(id)
                                         ↑
CharacterList (desktop default) ──Click──┘
```

- **Android & Web**: Start at `Start` screen (character ID input)
- **Desktop**: Start at `CharacterList` (skips ID input screen)
- `CharacterDetail` supports viewing, inline stat/HP/level editing, and full character edit mode

## Data Flow

```
Compose Screen → ViewModel → Repository → GoogleAppsScriptDataSource (Ktor GET)
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
├── androidMain: Android-specific platform bindings, SharedPreferences storage
├── desktopMain: Desktop-specific platform bindings, java.util.prefs storage
└── wasmJsMain: Web-specific platform bindings, localStorage, ktor-client-js
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
- Three routes: `Start` (object), `CharacterList` (object), `CharacterDetail(id: String)` (data class).

### 5. Single `:shared` Module (for now)
- All UI, domain, and data code starts in `:shared/commonMain`.
- When the codebase grows, extract `:feature:*` modules.
- This avoids premature modularization and speeds up initial development.

### 6. Google Apps Script Backend
- **No OAuth in the app** — authentication is handled server-side by the Apps Script Web App.
- **Ktor GET requests** — the app sends JSON-encoded requests as a `?request=` query parameter to the Apps Script Web App URL (GET avoids 302 redirect issues with POST).
- **Apps Script handles Sheets API** — all Google Sheets read/write operations happen in the Apps Script code, not in the Kotlin app.
- **Build-time URL injection** — the Web App URL is read from `local.properties` (`apps.script.url`) and injected at compile time into `GeneratedConfig.kt`. Never committed to Git.

### 7. Platform-Specific Storage (`expect`/`actual`)
- `CharacterStorage` interface provides `saveCharacterId(id)` and `getCharacterId()`.
- **Android actual**: `SharedPreferences` via `androidContext()`.
- **Desktop actual**: `java.util.prefs.Preferences`.
- **Web actual**: `kotlinx.browser.localStorage`.
- Used by `StartViewModel` to persist the last entered character ID.

### 8. Platform-Specific Start Destination
- Desktop (`isDesktop = true`) skips the `Start` screen and opens directly to `CharacterList`.
- Android and Web (`isDesktop = false`) show the `Start` screen first for character ID input.

### 9. Auto-Update / Real-Time Sync (Polling with Timestamp)
- **No WebSockets** — Google Apps Script doesn't support persistent connections.
- **Global timestamp** — A `Metadata` sheet stores a single `lastModified` ISO timestamp. Every write (`saveCharacter`, `deleteCharacter`) updates it.
- **Lightweight polling** — `getLastModified()` returns just the timestamp string. ViewModels poll every 4s via `viewModelScope` + `delay()`.
- **Screen-lifecycle bound** — `DisposableEffect` in Compose starts polling when the screen is visible and stops when the user navigates away.
- **Edit-aware** — `CharacterDetailViewModel` skips auto-refresh while `isEditing` to avoid overwriting the user's in-progress changes.
- **Trade-off**: polling consumes quota (2,880 requests/day per active screen at 30s intervals). Acceptable for a small-party D&D app.

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
| Network | Ktor client GET to Apps Script Web App | `:shared/commonMain` |
| Preferences | `expect`/`actual` `CharacterStorage` | `:shared/commonMain` + platform source sets |
| Platform UI (if any) | `expect` composable leaf | `:shared/commonMain` |

## App Entry Points

### Android (`:app`)
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}
```

### Desktop (`:desktop`)
```kotlin
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "D&D Helper") {
        App()
    }
}
```

### Web Browser (`:web`)
```kotlin
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow("D&D Helper", canvasElementId = "ComposeTarget") {
        App()
    }
}
```

### Shared (`:shared`)
```kotlin
@Composable
fun App(koinConfiguration: KoinAppDeclaration = {}) {
    // KoinApplication with appModule + platformModule
    // NavHost with Start / CharacterList / CharacterDetail routes
    // Desktop starts at CharacterList; Mobile/Web starts at Start
}
```

## Presentation Screens

### Start Screen
- Character ID input with persistence via `CharacterStorage`
- "Load Character" button navigates to `CharacterDetail(id)`
- Shown on Android and Web; skipped on Desktop

### Character List Screen
- LazyColumn of character cards
- Pull-to-refresh (`PullToRefreshBox`) on Android
- Refresh button + F5 keyboard shortcut in `TopAppBar` on Desktop
- Click navigates to `CharacterDetail(id)`

### Character Detail Screen
- Full character sheet: image (Coil 3 `AsyncImage`), name, race/class/level, HP bar, stats grid (2×3), biography
- **View mode**: displays all fields, stat/HP/level increment/decrement with inline amount input
- **Edit mode**: full form editing (name, race, class, level, HP, all stats, player name, image URL, description)
- Optimistic updates with rollback on save failure
- TopAppBar actions: Edit ✓/✕, Refresh ↻

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
