# D&D Helper — Current State

## Project Status: Phase 2 — Player Screens & Navigation (Active)

### Context Update (2026-05-30)
User defined full project scope:
- **Android** — player-only app
- **Desktop** — player mode + admin (DM) mode with preparation and game sub-modes
- **Backend** — Google Sheets via Apps Script Web App, no local database
- **Screens** — Start (ID input) → Character List → Character Detail (view + edit) / Character Create (desktop)

## What's Implemented

### Project Structure
- [x] `:shared:core` KMP module (Domain models, Repositories, Theme, DI, Network)
- [x] `:shared:player` KMP module (Start, Character List, Character Detail screens)
- [x] `:shared:desktop` KMP module (DM tools, Library, Creator, Music Player)
- [x] `:app` Android application module depending on `:shared:player`
- [x] `:desktop` Desktop application module depending on `:shared:desktop`
- [x] `:web` Web Browser application module (Kotlin/Wasm) depending on `:shared:player`
- [x] Gradle configuration with version catalogs for KMP, Ktor, Koin, Coil, Serialization
- [x] Compose Multiplatform setup in shared modules (Android, Desktop, Web)

### Domain Layer
- [x] `Character` domain model with nested data classes:
  - Core: id, name, playerName, race, characterClass, subclass, background, level, experiencePoints, description, imageUrl
  - `CharacterStats` — str, dex, con, int, wis, cha
  - `CharacterAppearance` — age, gender, height, weight, eyes, hair, skin
  - `CharacterCombat` — armorClass, initiative, speed, proficiencyBonus, tempHp, hitDice, hitDiceCurrent, inspiration, exhaustion, conditions, deathSaveSuccesses, deathSaveFailures
  - `CharacterProficiencies` — savingThrows, skills, armor, weapons, tools, languages
  - `CharacterFeatures` — classFeatures, racialTraits, feats
  - `Weapon` — id, name, attackBonus, damage, damageType, notes
  - `Item` — id, name, slot, rarity, stats, description, equipped
  - `Skill` — id, name, description, iconName, damage, damageType, resourceCost, range, castingTime, duration, level, school, isPassive
- [x] `EquipmentSlot` / `ItemRarity` enums
- [x] `DndSkill` enum — all 18 D&D 5e skills with ability mapping
- [x] `CharacterStorage` interface — platform-agnostic key-value storage for character ID
- [x] `Result<T>` + `AppError` sealed classes for error handling across layers

### Data Layer
- [x] `GoogleAppsScriptDataSource` — Ktor GET to Apps Script Web App (`?request=` query param)
- [x] `CharacterRepository` + `CharacterRepositoryImpl` — clean architecture data layer
- [x] `GoogleAppsScriptConfig.WEB_APP_URL` — build-time injection from `local.properties`
- [x] Request/response models: `AppsScriptRequest`, `AppsScriptResponse<T>`
- [x] `apps-script/Code.gs` — complete server-side script with CRUD operations (GET-based)
  - **Per-character sheets** — each character has its own sheet (tab) named by character ID
  - Character data in rows 1-2, item table in rows 4+
  - `getCharacters` iterates all sheets; `getCharacter` reads both character + items; `saveCharacter` writes both sections
- [x] `apps-script/README.md` — deployment instructions

### Navigation
- [x] Type-safe routes with `@Serializable` objects/data classes
  - `Start` — character ID input screen
  - `CharacterList` — list of all characters
  - `CharacterCreate` — create new character (desktop/admin)
  - `CharacterDetail(id: String)` — single character sheet
- [x] `NavHost` in `App()` with four destinations
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
- [x] **Create Character button** — "+" icon in TopAppBar navigates to `CharacterCreate` (available on all platforms)

### Presentation Layer — Character Detail Screen
- [x] `CharacterDetailState` — UI state (character, editedCharacter, isEditing, isSaving, isLoading, error)
- [x] `CharacterDetailEvent` — sealed interface (Refresh, UpdateStat, UpdateHp, UpdateMaxHp, UpdateLevel, ToggleEdit, EditCharacter, SaveChanges)
- [x] `CharacterDetailViewModel` — loads single character, handles stat/HP/level updates, edit/save with optimistic updates + rollback
  - **Debounced saves** — rapid stat/HP/level clicks are batched locally; pushed to server after 5s of inactivity
  - `flushPendingSave()` — force-flushes pending changes when navigating away
  - `hasUnsavedChanges` flag + orange dot in TopAppBar for visual feedback
- [x] `CharacterDetailScreen` — full character sheet with:
  - Hero image via Coil 3 `AsyncImage` with gradient overlay
  - Name, race/class chip, level control (± with amount input)
  - HP card with progress bar, color-coded by health ratio, damage/heal controls
  - Stats grid (2×3) with color-coded icons and Russian descriptive tags
  - Biography section
  - **Bottom navigation** — 6 tabs:
    - **Overview** — Header (name, race, class, subclass, background, level, XP), appearance chips (age, gender, height, etc.), AC/HP/hit dice/death saves, status bar (initiative, speed, passive perception, proficiency), inspiration, exhaustion, conditions
    - **Stats** — 6 ability score cards with modifiers, saving throws with proficiency dots, 18 skills grouped by ability
    - **Inventory** — Diablo-style equipment panel + item grid
    - **Combat** — Weapons & attacks table
    - **Features** — Background, subclass, proficiencies (armor/weapons/tools/languages) as chips, class features, racial traits, feats, biography
    - **Skills** — Grid of skill/spell cards with icon, name, level, and resource cost; tap opens floating detail dialog with full stats (damage, range, casting time, duration, school, description)
  - Edit mode: full form with all editable fields
  - TopAppBar with Back, Edit ✓/✕, Refresh actions + unsaved-changes dot
  - Auto-refresh polling via `DisposableEffect` (start/stop with screen lifecycle)

### Presentation Layer — Character Create Screen (Desktop)
- [x] `CharacterCreateState` — UI state for creation form (all character fields + item list + weapon list + skill list)
- [x] `CharacterCreateEvent` — sealed interface for every field change, item/weapon/skill add/remove/edit, save
- [x] `CharacterCreateViewModel` — validates input, builds `Character` with `items`, `weapons`, `skills`, saves via repository
- [x] `CharacterCreateScreen` — compact scrollable form with styled sections:
  - Section headers with icons and accent-colored dividers
  - Basic info: name, race, class, subclass, background, level, XP, image URL (2-column layout)
  - Appearance: age, gender, height, weight, eyes, hair, skin (2-column layout)
  - Ability scores: color-coded cards (STR=red, DEX=green, CON=orange, INT=blue, WIS=purple, CHA=pink)
  - Combat: HP, AC, initiative, speed, prof. bonus, hit dice (2-column layout)
  - Status: inspiration switch, exhaustion, death saves, conditions
  - Proficiencies: saving throws, skills, armor, weapons, tools, languages
  - **Item editor** — add/remove items with name, slot dropdown, rarity dropdown, equipped checkbox, description
  - **Weapon editor** — add/remove weapons with name, attack bonus, damage, damage type, notes
  - **Skill editor** — add/remove skills with name, damage, damage type, resource cost, range, casting time, duration, level, school, icon name, passive toggle, description
  - Features: class features, racial traits, feats (multi-line)
  - Save button with loading state
- [x] Navigation from `CharacterListScreen` via "+" button in TopAppBar

### Auto-Update / Real-Time Sync
- [x] Apps Script `Metadata` sheet — stores global `lastModified` timestamp
- [x] Timestamp updated on every `saveCharacter` and `deleteCharacter`
- [x] `getLastModified` action — lightweight poll endpoint (single ISO timestamp)
- [x] `CharacterRepository.getLastModified()` — repository interface method
- [x] `CharacterListViewModel` — polls every 4s, auto-reloads list when timestamp changes; smooth auto-refresh (no `isLoading` flicker)
- [x] `CharacterDetailViewModel` — polls every 4s, auto-reloads character when timestamp changes; **skips poll while `isEditing`** to avoid overwriting user edits; **skips reload for self-initiated changes** (`hasPendingLocalChange` flag) so the app that performed the update doesn't redundantly reload itself
- [x] `DisposableEffect(viewModel)` in both screens — starts polling on enter, stops on leave

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
- [ ] **Desktop UI Overhaul** — transitioning to sidebar-based split-pane layout for DMs
- [ ] **Master Mode Implementation** — unified library management and enhanced character editing

## Not Yet Implemented
- [ ] Game mode UI (Desktop live session)
- [ ] Location management
- [ ] Equipment/monster creation
- [ ] DM screen with combat tools
- [ ] Presentation Mode — secondary window for player-facing content

## Recently Implemented
- [x] **New Desktop UI Plan** — documented sidebar + split-pane navigation in `PLAN.md` and `ARCHITECTURE.md`
- [x] **Navigation** — `NavHost` with type-safe `@Serializable` routes
- [x] **Start Screen** — character ID input with platform storage persistence
- [x] **Character Detail Screen** — full character sheet with view/edit modes, stat/HP/level controls, Coil 3 image loading
- [x] **Platform Storage** — `expect`/`actual` `CharacterStorage` (SharedPreferences / java.util.prefs / localStorage)
- [x] **Platform start destination** — Desktop opens `CharacterList`, Mobile/Web opens `Start`
- [x] **Web UI simplified** — removed phone frame, full-screen canvas fills browser viewport
- [x] **Optimistic updates** — CharacterDetailViewModel updates UI immediately, rolls back on save failure
- [x] **Auto-update / real-time sync** — When DM updates a character from Desktop, Android/Web auto-reloads
  - Apps Script `Metadata` sheet stores a global `lastModified` timestamp
  - `handleSaveCharacter` / `handleDeleteCharacter` update the timestamp after every write
  - New `getLastModified` action — lightweight poll endpoint (just a timestamp string)
  - `CharacterRepository.getLastModified()` + `GoogleAppsScriptDataSource.getLastModified()`
  - `CharacterListViewModel` & `CharacterDetailViewModel` poll every 4s via `viewModelScope`
  - `DisposableEffect` in Compose screens starts/stops polling with screen lifecycle
  - Auto-refresh is **paused while editing** in Character Detail to avoid overwriting user changes
- [x] **Debounced saves** — rapid stat/HP/level clicks batch locally, flush after 5s inactivity
- [x] **Network optimizations** — Apps Script reads no longer acquire `ScriptLock` or call `flush()`; `handleGetCharacter` uses `createTextFinder()` instead of O(n) scan
- [x] **Self-initiated change skip** — App that performed an update skips its own auto-reload to avoid redundant flicker
- [x] **Per-character sheets** — Apps Script backend migrated from single "Characters" sheet to one sheet per character; each sheet stores character info (rows 1-2) and items (rows 4+)
- [x] **Item domain model** — `Item`, `EquipmentSlot`, `ItemRarity` with `@Serializable`; added `items: List<Item>` to `Character`
- [x] **Inventory tab** — Diablo-style equipment panel (top) + item grid (bottom) with rarity-colored borders; loads real items from server
- [x] **Character Create screen** — Full creation flow with all stats, description, and inline item editor; navigates from Character List "+" button
- [x] **Expanded character sheet** — 5-tab layout (Overview, Stats, Inventory, Combat, Features) matching D&D 5e character sheet reference
- [x] **Ability modifiers & skills** — Automatic modifier calculation `(score - 10) / 2`, passive perception, saving throws, all 18 skills with proficiency tracking
- [x] **Backend JSON columns** — Apps Script stores nested character data (appearance, combat, proficiencies, weapons, features, skills) as JSON strings in dedicated columns (Q-Y) alongside flat fields (A-P)
- [x] **Skill domain model** — `Skill` with icon, damage, damageType, resourceCost, range, castingTime, duration, level, school, isPassive
- [x] **Skills tab** — 6th bottom nav tab in Character Detail with adaptive grid of skill cards; floating detail dialog on tap
- [x] **Dice roller** — Floating action button on Character List opens dice dialog; 7 dice types (d4,d6,d8,d10,d12,d20,d%); single tap rolls one, batch roll with count selectors
- [x] **Enhanced Character Create screen** — Compact 2-column layout, color-coded ability scores with themed cards, section headers with icons and accent dividers, skill creation editor

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
6. Add item editing inside Character Detail screen (equip/unequip, add/remove items)

## Notes
- Skill file `.kimi/skills/dnd-kmp/SKILL.md` created on 2026-05-30.
- `ARCHITECTURE.md`, `STATE.md`, and `DESCRIPTION.md` are the single source of truth for project context.
- Always update `STATE.md` after each work session.

## Implemented 2026-07-11 — Player Character Creation & Campaign Joining (CHAR.md plan)

### Server
- `POST /api/my-characters` — player creates own character; stored in personal session `user-{userId}`. No schema changes needed (Characters table already had `userId` nullable and `sessionId` as string).
- `POST /api/my-characters/{id}/join` — player links their character to a campaign by providing `gameId` (sessionId). Upserts character into the game session, removes from personal session.
- `JoinCampaignRequest` DTO added to `AuthModels.kt` in `:models`.
- Master path (Master creates char → assigns to user) is unchanged.

### Client (shared/player + shared/core)
- `KtorRemoteDataSource.createMyCharacter()` — POSTs to `/api/my-characters`; client generates UUID so no ID parsing needed.
- `KtorRemoteDataSource.joinCampaign()` — POSTs to `/api/my-characters/{id}/join` with `JoinCampaignRequest`.
- `PlayerCharacterCreateState/Event/ViewModel/Screen` — minimal creation form (name, race, class, subclass, background, level, HP, 6 ability scores).
- `StartState` — added `isJoiningCampaign`, `joinError`.
- `StartEvent` — added `JoinCampaign`, `DismissJoinError`.
- `StartViewModel.joinCampaign()` — calls data source, saves tableId on success, reloads my-characters.
- `StartScreen` — added `+ Create` button in My Characters header; per-character `Join Campaign` button (shown when `campaignName == null`); Join Campaign dialog with gameId input.
- `PlayerApp` — added `PlayerCharacterCreate` route and `factory { PlayerCharacterCreateViewModel(get()) }`.

### ponytail decisions
- Personal session = `user-{userId}` as sessionId (no new DB column needed; ceiling: chars in personal session aren't visible to WS sessions).
- Client-generated UUID for new chars (no server-to-client ID round-trip needed).
- Minimal create form (9 fields); power users use Desktop for full creation.
- 15s polling refresh after create (no lifecycle wiring needed for acceptable UX).
