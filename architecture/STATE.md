# D&D Helper — Current State

## Project Status: Phase 2 — Player Screens & Navigation (Active)

### Context Update (2026-05-30)
User defined full project scope:
- **Android** — player-only app
- **Desktop** — player mode + admin (DM) mode with preparation and game sub-modes
- **Backend** — Google Sheets via Apps Script Web App, no local database
- **Screens** — Start (ID input) → Character List → Character Detail (view + edit)

## What's Implemented

### Project Structure
- [x] `:shared` KMP module with `commonMain`, `androidMain`, `desktopMain`, `wasmJsMain` source sets
- [x] `:app` Android application module depending on `:shared`
- [x] `:desktop` Desktop application module depending on `:shared`
- [x] `:web` Web Browser application module (Kotlin/Wasm) depending on `:shared`
- [x] Gradle configuration with version catalogs for KMP, Ktor, Koin, Coil, Serialization
- [x] Compose Multiplatform setup in `:shared` (Android, Desktop, Web)

### Domain Layer
- [x] `Character` domain model (id, name, playerName, race, class, level, description, imageUrl, stats, hp)
- [x] `CharacterStats` data class (str, dex, con, int, wis, cha)
- [x] `CharacterStorage` interface — platform-agnostic key-value storage for character ID
- [x] `Result<T>` + `AppError` sealed classes for error handling across layers

### Data Layer
- [x] `GoogleAppsScriptDataSource` — Ktor GET to Apps Script Web App (`?request=` query param)
- [x] `CharacterRepository` + `CharacterRepositoryImpl` — clean architecture data layer
- [x] `GoogleAppsScriptConfig.WEB_APP_URL` — build-time injection from `local.properties`
- [x] Request/response models: `AppsScriptRequest`, `AppsScriptResponse<T>`
- [x] `apps-script/Code.gs` — complete server-side script with CRUD operations (GET-based)
- [x] `apps-script/README.md` — deployment instructions

### Navigation
- [x] Type-safe routes with `@Serializable` objects/data classes
  - `Start` — character ID input screen
  - `CharacterList` — list of all characters
  - `CharacterDetail(id: String)` — single character sheet
- [x] `NavHost` in `App()` with three destinations
- [x] Platform-specific start destination: Desktop → `CharacterList`, Mobile/Web → `Start`

### Presentation Layer — Start Screen
- [x] `StartState` — UI state (characterId input)
- [x] `StartEvent` — sealed interface (`CharacterIdChanged`, `LoadCharacter`)
- [x] `StartViewModel` — manages input state, persists ID via `CharacterStorage`
- [x] `StartScreen` — branded input screen with "D&D Helper" title, text field, "Load Character" button

### Presentation Layer — Character List Screen
- [x] `CharacterListState` — UI state (characters, loading, error)
- [x] `CharacterListEvent` — sealed interface (`Refresh`, `CharacterClicked`)
- [x] `CharacterListViewModel` — loads characters from repository, handles refresh
- [x] `CharacterListScreen` — Compose UI with `LazyColumn`, loading, error, empty states
- [x] `CharacterCard` — card composable with icon, name, race/class/level, player name
- [x] Pull-to-refresh — `PullToRefreshBox` with Material 3 indicator (Android + Web)
- [x] Refresh button + F5 keyboard shortcut in `TopAppBar` (Desktop)

### Presentation Layer — Character Detail Screen
- [x] `CharacterDetailState` — UI state (character, editedCharacter, isEditing, isSaving, isLoading, error)
- [x] `CharacterDetailEvent` — sealed interface (Refresh, UpdateStat, UpdateHp, UpdateMaxHp, UpdateLevel, ToggleEdit, EditCharacter, SaveChanges)
- [x] `CharacterDetailViewModel` — loads single character, handles stat/HP/level updates, edit/save with optimistic updates + rollback
- [x] `CharacterDetailScreen` — full character sheet with:
  - Hero image via Coil 3 `AsyncImage` with gradient overlay
  - Name, race/class chip, level control (± with amount input)
  - HP card with progress bar, color-coded by health ratio, damage/heal controls
  - Stats grid (2×3) with color-coded icons and Russian descriptive tags
  - Biography section
  - Edit mode: full form with all editable fields (name, race, class, level, HP, stats, player name, image URL, description)
  - TopAppBar with Back, Edit ✓/✕, Refresh actions

### Platform Storage
- [x] `expect`/`actual` `CharacterStorage` interface
  - Android actual: `SharedPreferences` (`AndroidCharacterStorage`)
  - Desktop actual: `java.util.prefs.Preferences` (`DesktopCharacterStorage`)
  - Web actual: `kotlinx.browser.localStorage` (`WasmCharacterStorage`)

### Shared UI
- [x] `DndHelperTheme` in `:shared` — Material 3 dark theme for all platforms
  - Arcane Violet (`#B794F6`) primary, Mystic Teal (`#82DBC4`) secondary
  - Deep dungeon surface (`#141218`) with full M3 tonal palette
  - Full `darkColorScheme` with primary/secondary/error containers and inverse colors
- [x] `App()` composable — entry point with Koin DI, NavHost, platform-specific start destination

### Platform Entry Points
- [x] Android `MainActivity` — calls `App()`
- [x] Desktop `main.kt` — window with `App()`
- [x] Web `main.kt` — `CanvasBasedWindow` with `App()`, full-screen canvas (no phone frame)

### Architecture docs
- [x] `ARCHITECTURE.md` — high-level architecture, tech stack, decisions
- [x] `STATE.md` — current state tracking
- [x] `DESCRIPTION.md` — full project description with platforms, modes, features

## What's In Progress
- [ ] Gradle sync / build verification (not yet tested)

## Not Yet Implemented
- [ ] Admin mode UI (Desktop DM mode)
- [ ] Game mode UI (Desktop live session)
- [ ] Location management
- [ ] Equipment/monster creation
- [ ] DM screen with combat tools

## Recently Implemented
- [x] **Navigation** — `NavHost` with type-safe `@Serializable` routes
- [x] **Start Screen** — character ID input with platform storage persistence
- [x] **Character Detail Screen** — full character sheet with view/edit modes, stat/HP/level controls, Coil 3 image loading
- [x] **Platform Storage** — `expect`/`actual` `CharacterStorage` (SharedPreferences / java.util.prefs / localStorage)
- [x] **Platform start destination** — Desktop opens `CharacterList`, Mobile/Web opens `Start`
- [x] **Web UI simplified** — removed phone frame, full-screen canvas fills browser viewport
- [x] **Optimistic updates** — CharacterDetailViewModel updates UI immediately, rolls back on save failure

## Known Issues / Blockers
- Gradle build not yet verified (`JAVA_HOME` not set in current environment)
- Node.js required for `:web` module (`:kotlinNodeJsSetup`)
- Ktor Logging plugin commented out in `App.kt` (can re-enable for debugging)
- Web images depend on CORS headers from the image host (fundamental browser limitation; Coil uses `fetch` which enforces CORS)

## Recently Fixed
- [x] **Web CSP + eval issue** — Webpack `devtool` changed from `eval-source-map` to `source-map`; added CSP meta tag with `unsafe-eval` + `wasm-unsafe-eval`
- [x] **Web mobile viewport** — `index.html` CSS updated with `100dvh`, `position: fixed`, `touch-action: manipulation`, `overscroll-behavior: none`, `-webkit-tap-highlight-color: transparent`
- [x] **Coil ImageLoader initialization** — `SingletonImageLoader.setSafe` with `KtorNetworkFetcherFactory` added to all platform entry points (Android, Desktop, Web). Without this, `AsyncImage` had no network fetcher on Web.

## Next Immediate Tasks
1. Verify Gradle build succeeds across all targets
2. Build admin mode selection for Desktop
3. Add Location management (preparation mode)
4. Add Equipment/Monster creation screens
5. Implement Game Mode (DM live session)

## Notes
- Skill file `.kimi/skills/dnd-kmp/SKILL.md` created on 2026-05-30.
- `ARCHITECTURE.md`, `STATE.md`, and `DESCRIPTION.md` are the single source of truth for project context.
- Always update `STATE.md` after each work session.
