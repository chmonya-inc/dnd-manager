# D&D Helper — Current State

## Project Status: Phase 1 — KMP Structure Setup (Mostly Complete)

### Context Update (2026-05-30)
User defined full project scope:
- **Android** — player-only app
- **Desktop** — player mode + admin (DM) mode with preparation and game sub-modes
- **Backend** — Google Sheets REST API with OAuth, no local database
- **First screen** — Character List

## What's Implemented

### Project Structure
- [x] `:shared` KMP module with `commonMain`, `androidMain`, `desktopMain` source sets
- [x] `:app` Android application module depending on `:shared`
- [x] `:desktop` Desktop application module depending on `:shared`
- [x] Gradle configuration with version catalogs for KMP, Ktor, Koin, Coil, Serialization
- [x] Compose Multiplatform setup in `:shared`

### Domain Layer
- [x] `Character` domain model (id, name, playerName, race, class, level, description, imageUrl, stats, hp)
- [x] `CharacterStats` data class (str, dex, con, int, wis, cha)

### Presentation Layer — Character List Screen
- [x] `CharacterListState` — UI state (characters, loading, error)
- [x] `CharacterListEvent` — sealed interface (Refresh, CharacterClicked)
- [x] `CharacterListViewModel` — loads characters, handles events, mock data for now
- [x] `CharacterListScreen` — Compose UI with LazyColumn, loading, error, empty states
- [x] `CharacterCard` — card composable with icon, name, race/class/level, player name

### Shared UI
- [x] `DndHelperTheme` in `:shared` — Material 3 theme for Android + Desktop
- [x] `App()` composable — entry point with Koin DI and CharacterListScreen

### Platform Entry Points
- [x] Android `MainActivity` — calls `App()`
- [x] Desktop `main.kt` — window with `App()`

### Architecture docs
- [x] `ARCHITECTURE.md` — high-level architecture, tech stack, decisions
- [x] `STATE.md` — current state tracking
- [x] `DESCRIPTION.md` — full project description with platforms, modes, features

## What's In Progress
- [ ] Gradle sync / build verification (not yet tested)

## Not Yet Implemented
- [ ] Google Sheets data source (Ktor + OAuth)
- [ ] Real repository implementations
- [ ] Navigation setup (NavHost with type-safe routes)
- [ ] Admin mode UI
- [ ] Game mode UI
- [ ] Location management
- [ ] Equipment/monster creation
- [ ] Image loading with Coil 3
- [ ] Platform-specific OAuth implementation

## Known Issues / Blockers
- Current Java target is 11; should migrate to 21 for better performance and AGP 9 compatibility. **(Done in Gradle files, needs verification)**
- AGP 9 KMP library plugin constraints: `:shared` uses `com.android.kotlin.multiplatform.library`.
- Google Sheets OAuth requires platform-specific implementation (Android vs Desktop).

## Next Immediate Tasks
1. Verify Gradle build succeeds
2. Fix any compilation errors
3. Add Google Sheets data source with OAuth
4. Add navigation to character detail screen
5. Build admin mode selection for Desktop

## Notes
- Skill file `.kimi/skills/dnd-kmp/SKILL.md` created on 2026-05-30.
- `ARCHITECTURE.md`, `STATE.md`, and `DESCRIPTION.md` are the single source of truth for project context.
- Always update `STATE.md` after each work session.
