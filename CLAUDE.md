# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Workflow rules (mandatory)

These apply to every task in this repo. Do not skip them.

1. **Use the ponytail skill for any code change.** Before writing code, invoke the `ponytail/ponytail` skill (via the Skill tool) so you produce only the code actually needed — no speculative edits, no drive-by refactors, no extra "while I'm here" changes.
2. **Run tests after implementing a feature.** Run the relevant suite (`./gradlew test`, or `:server:test` for server changes — see Commands) and fix anything that breaks before considering the work done.
3. **Run detekt after implementing a feature and fix issues for real.** Run `./gradlew detekt`. It auto-fixes formatting, but for any remaining findings, fix the root cause by hand — refactor, clean up imports, rename, restructure. **Never suppress issues with a detekt baseline** (do not run `detektBaseline` / do not add to a baseline file). `maxIssues: 0` is intentional; detekt must pass clean.

## Project

D&D Helper — a Kotlin Multiplatform (KMP) companion app for D&D 5e sessions. Clients talk to a self-hosted **Ktor + PostgreSQL** server over REST + WebSockets. Targets: Android (`:app`, player-only), Desktop (`:desktop`, player + DM/admin, JVM), Web (`:web`, player-only, Kotlin/Wasm canvas).

Toolchain: Kotlin 2.1.21, JDK/JVM 21, AGP 8.13.2, Compose Multiplatform 1.7.3, Ktor 3.1.0, Exposed 0.59.0. Versions live in `gradle/libs.versions.toml`.

## Commands

All via the Gradle wrapper (`./gradlew` on Unix, `gradlew.bat` on Windows).

```bash
# Per-target run
./gradlew :app:installDebug              # Android (debug, applicationIdSuffix ".qa")
./gradlew :desktop:run                   # Desktop JVM
./gradlew :web:wasmJsBrowserRun          # Web (browser; port set in web module config)
./gradlew :server:run                    # Ktor server on :9090 (needs Postgres + JWT_SECRET)

# Build / package
./gradlew build                          # All targets — also runs detekt (see Lint below)
./gradlew :app:assembleDebug             # Android APK
./gradlew :desktop:packageMsi            # Desktop release MSI (used by CI)
./gradlew :web:wasmJsBrowserDistribution # Web distribution

# Lint (detekt) — applied to every module from root build.gradle.kts
./gradlew detekt                         # ALSO auto-fixes formatting (autoCorrect=true)

# Tests
./gradlew test                           # All unit tests
./gradlew :server:test                   # Server tests
./gradlew :server:test --tests "com.dnd.helper.server.ServerTest"   # single test class

# Local Postgres for the server
docker compose up -d
```

## Lint — detekt is enforced and self-formatting

`build.gradle.kts` applies detekt to **all projects** (including `:server`). Two things make this non-obvious:

- **`autoCorrect = true`** — running detekt (including indirectly via `build`/`check`) **rewrites source files** to fix formatting. Expect modified files after a build.
- **`maxIssues: 0`** in `config/detekt/detekt.yml` — the build **fails on any remaining issue**. Max line length is **180**. The `detekt-formatting` plugin is active.

If a build fails on detekt, run `./gradlew detekt` to auto-fix, then re-run.

## Local setup & secrets

- **JDK 21** required.
- **`local.properties`** (gitignored) holds local secrets, read by `:shared:core`'s `generateAppConfig` Gradle task:
  - `apps.script.url.android`, `apps.script.url.desktop` — base URL of the server (despite the legacy name). For the Android emulator use `http://10.0.2.2:9090/exec`; for desktop/web use `http://localhost:9090/exec`.
  - `imgbb.api.key` — image upload key.
  - `sdk.dir` — Android SDK path.
- These can also come from **environment variables** (dots→`_`, uppercased): `APPS_SCRIPT_URL_DESKTOP`, `APPS_SCRIPT_URL_ANDROID`, `IMGBB_API_KEY`. CI injects them this way. Env vars take precedence over `local.properties`.
- `generateAppConfig` emits `shared/core/build/generated/.../GeneratedConfig.kt` (auto-generated, never edit) before any Kotlin compile.
- **Server env vars**: `JWT_SECRET` is **required** (server won't boot without it). DB defaults to local Postgres (`localhost:5432/dndhelper/postgres/postgres`); override via `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASSWORD`.

## Architecture

### Modules (`settings.gradle.kts`)

```
:models          — shared @Serializable domain models + DTOs (KMP, no deps). Server and clients both use it.
:shared:core     — domain (Result/AppError, repository interfaces), data (KtorRemoteDataSource, repo impls),
                   DI (coreModule/platformModule), theme, networking. KMP targets: android, desktop(jvm), wasmJs.
:shared:player   — player screens (auth, start, character list/detail/create). depends on :shared:core.
:shared:desktop  — DM/admin screens (library, creator, music, presenter, sessions). depends on :shared:core.
:app             — Android entry (MainActivity) → uses :shared:player.
:desktop         — Desktop entry (main.kt) → uses :shared:desktop.
:web             — Web entry (CanvasBasedWindow) → uses :shared:player.
:server          — Ktor server (JVM). depends on :models.
```

Dependency direction: `models ← core ← {player, desktop} ← {app, desktop, web}`. Client modules never depend on `:server`; both sides depend on `:models` for the wire format.

### Client data flow & state

```
Compose Screen → ViewModel (StateFlow) → Repository → KtorRemoteDataSource (HTTP + WebSocket) → Server
                  sealed State / Event / onEvent pattern; Result<T> + AppError across layers
```

- Each feature = `State` (data class) + `Event` (sealed interface) + `ViewModel` (`MutableStateFlow`, `onEvent`). Screens get the VM via `koinViewModel()`, collect with `collectAsStateWithLifecycle`. Child composables are stateless, receive events via callbacks.
- Navigation is **type-safe**: `@Serializable` route objects/data classes, declared per app in `PlayerApp` / `DesktopApp`. Player start destination branches on whether a refresh token exists (→ `AuthRoute` or `Start`).
- `KtorRemoteDataSource` derives the server URL from `CharacterStorage` (runtime override) falling back to `GeneratedConfig`. It **strips a trailing `/exec` and `/`** from the base URL — this is the backwards-compat shim that lets the legacy `apps.script.url.*` config keys now point at the Ktor server.

### Real-time sync

- Server: `SessionManager` (in-memory `ConcurrentHashMap`) tracks WebSocket sessions per `sessionId`. On every POST/DELETE it broadcasts `update:<type>` or `update:<type>:<id>` frames to that session's clients (`ApiRouting.kt`).
- Client: `KtorRemoteDataSource.observeUpdates()` opens `/api/{sessionId}/ws`, sends a `"check"` ping each second, and emits `update:...` events as a `Flow`. ViewModels collect this to reload the affected data. (The 4-second `lastModified` polling described in `STATE.md` is the **legacy** Apps-Script path; WebSocket is current.)

### Dependency injection (Koin)

`CoreApp` composable wraps everything in a `KoinApplication` loading `[coreModule, platformModule] + appModules` (`playerModule` or `desktopModule`). `coreModule` owns the `HttpClient` (bearer auth + refresh-on-401), repos, and data sources as `single`s; ViewModels are `factory` (some take `parametersOf`). Coil's `ImageLoader` is registered via `SingletonImageLoader.setSafe` using a separate `imageClient` HttpClient (avoids CORS preflight on image fetches). `platformModule` is `expect`/`actual` per target.

`CharacterStorage` is `expect`/`actual`: Android = `SharedPreferences`, Desktop = `java.util.prefs.Preferences`, Web = `localStorage`. Holds auth tokens, selected table/session id, server address.

### Auth

- Server (`AuthRouting.kt`): Argon2 password hashing via `password4j` (with legacy bcrypt-hash verification fallback). Access tokens 15 min, refresh tokens 7 days — **rotated**, raw refresh JWT stored SHA-256-hashed in the `refresh_tokens` table and validated on use; logout revokes. `JWT_SECRET` env var signs HMAC256 tokens.
- Client: bearer token attached in `DefaultRequest` (reactive to login/logout). On 401, the Ktor `Auth` plugin's `refreshTokens` calls `AuthRepository.refresh()`; on failure it clears tokens so the user is routed back to `AuthRoute`.

## Backend specifics (`:server`)

- `Main.kt` boots Netty on `0.0.0.0:9090`. Rate limiting is applied **only to auth endpoints** (no global limit — it would break WS upgrades and legitimate client polling). CORS is `anyHost()`.
- All `/api/{sessionId}/**` routes are behind `authenticate("auth-jwt")`. Access control helpers in `ApiRouting.kt`:
  - `ensureSessionAccess` — Masters must own the campaign for that `sessionId`; Players are allowed if they hold the session id.
  - `ensureMasterRole` — gates destructive writes (locations, monsters, npc, music, events) to Masters.
  - `ensureCharacterOwnership` — Masters can delete any character in their session; Players only their own.
- **DB**: PostgreSQL via Exposed. `DatabaseFactory.init()` runs `SchemaUtils.createMissingTablesAndColumns` on startup (adds tables/columns, never drops) plus a manual `skills → spells` rename migration block. Most entity tables use a **composite PK `(id, sessionId)`** — entities are scoped per session. Nested/complex fields (appearance, combat, weapons, spells, items, etc.) are stored as **JSON columns** via Exposed's `.json<T>()` using the shared `com.dnd.helper.domain.model.*` classes from `:models`. All DB access goes through `DatabaseFactory.dbQuery { }` (suspended on `Dispatchers.IO`).
- Routing files: `ApiRouting` (CRUD + WS, session-scoped), `AuthRouting`, `CampaignRouting`, `AssignmentRouting` (Master→Player character assignment requests), `HealthRouting`.

## Conventions

- **No Android types in `commonMain`** (`Context`, `Activity`, `Uri`, `Bundle`, …). Put platform code behind `expect`/`actual`.
- Package root is `com.dnd.helper` everywhere; server is `com.dnd.helper.server`.
- Git: `master` is the main branch (PR target). `develop` triggers CI (`develop.yml` builds api/web/desktop/android + detekt and deploys to the test env). Feature branches (e.g. `new-char-owner`) branch off and merge via PR.
- Commit style is short imperative ("Add detekt", "Fix secrets").

## ⚠️ Stale documentation — verify against code

Several docs predate the Ktor/Postgres migration and contradict the current code. Trust the code, not these:

- **`README.md`** describes the backend as **Google Sheets via Apps Script** with 4s polling. That is **legacy**. The real backend is the Ktor server + PostgreSQL + WebSockets. `apps-script/Code.gs` still exists but is not the active backend.
- **`architecture/ARCHITECTURE.md`** says the DB is **SQLite**; it is **PostgreSQL**.
- **`architecture/STATE.md`** mixes legacy Apps-Script polling notes with newer server work — read critically.
- **`.agents/skills/dnd-kmp/SKILL.md`** is significantly out of date: it references SQLDelight/Room, a `:core:model/domain/data/ui/common` module layout, a single-`:app` migration, and iOS targets — none of which match the current module graph. Do not rely on it for structure.

## Keeping docs in sync

The project treats `architecture/ARCHITECTURE.md` and `architecture/STATE.md` as the source of truth for intent and progress (see the `dnd-kmp` work loop). When you make an architectural change, update those files — but note the staleness caveats above and fix the backend description (Postgres/Ktor/WebSocket) if you touch it.
