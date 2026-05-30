---
name: dnd-kmp
description: |
  ALWAYS use this skill when working on the D&D Helper project — a Kotlin Multiplatform (KMP) app for Dungeons & Dragons players.
  This is the top-level project skill. It governs all code generation, architecture decisions, and task execution for this project.
  Load this skill first, then delegate to specific sub-skills (android-skills:android-dev, android-skills:compose, android-skills:kotlin-coroutines, 
  android-skills:kotlin-flows, android-skills:android-data-layer, android-skills:android-retrofit, android-skills:kmp-boundaries, 
  android-skills:kmp-ktor, android-skills:coil-compose) for implementation details.
  This skill defines the project context loop: read ARCHITECTURE.md and STATE.md before work, write decisions back after work.
---

# D&D Helper — Project Skill

## Project Context

**D&D Helper** is a Kotlin Multiplatform application for Dungeons & Dragons players. It targets Android (primary), with shared code in KMP for future iOS/Desktop expansion.

### Domain
The app helps D&D players with:
- Character sheet management (create, edit, view D&D 5e characters)
- Dice rolling with history
- Spell lookup and bookmarking
- Initiative tracker for combat encounters
- Notes and campaign management

## Work Loop — MANDATORY

Every time you start working on this project, follow this loop exactly:

### Before writing any code:
1. **Read `architecture/ARCHITECTURE.md`** — understand the high-level architecture, tech stack, module boundaries, and key decisions.
2. **Read `architecture/STATE.md`** — understand the current state: what's implemented, what's in progress, what's planned, known issues.
3. **Check existing code patterns** — use `Grep` or `Read` to verify how existing ViewModels, repositories, and composables are structured. Follow established patterns.

### After writing code:
1. **Update `architecture/ARCHITECTURE.md`** — if you made architectural decisions (new modules, new patterns, new dependencies), document them.
2. **Update `architecture/STATE.md`** — update the status of what you implemented, mark tasks done/in-progress, add new known issues or next steps.
3. **Never skip the write-back** — these files are the project's memory. Future sessions depend on them.

## Skill Delegation

This skill is the orchestrator. For implementation details, delegate to specific skills using fully-qualified names:

| Concern | Delegate to |
|---|---|
| Android/KMP baseline architecture | `android-skills:android-dev` |
| Compose UI | `android-skills:compose` |
| Kotlin Coroutines | `android-skills:kotlin-coroutines` |
| Kotlin Flows | `android-skills:kotlin-flows` |
| Data layer (repositories, data sources) | `android-skills:android-data-layer` |
| Network (Ktor) | `android-skills:kmp-ktor` |
| KMP boundary design | `android-skills:kmp-boundaries` |
| Image loading | `android-skills:coil-compose` |
| Retrofit (Android-only if needed) | `android-skills:android-retrofit` |
| Gradle/module setup | `android-skills:android-gradle-logic` |
| Testing | `android-skills:android-tdd` |
| UX/Accessibility | `android-skills:android-ux` |

**Always load `android-skills:android-dev` alongside any other Android skill** — it provides the architectural baseline.

## Architecture Overview

### Module Structure (Target)

```
:app                        ← Android entry point (MainActivity, nav host, DI setup)
:core:model                 ← Shared domain models (Character, Spell, DiceRoll, etc.)
:core:domain                ← Use cases, repository interfaces (pure Kotlin)
:core:data                  ← Repository implementations, Ktor client, local DB
:core:ui                    ← Shared theme, design system, common composables
:core:common                ← Shared utilities, Result sealed class, extensions
:feature:charactersheet     ← Character creation, editing, viewing
:feature:diceroller         ← Dice roller with history
:feature:spellbook          ← Spell lookup and bookmarks
:feature:initiative         ← Combat initiative tracker
:feature:campaign           ← Notes and campaign management
```

### Tech Stack

- **UI**: Jetpack Compose (Material 3), Compose Multiplatform for shared UI
- **Architecture**: MVVM with ViewModel + StateFlow
- **DI**: Koin (KMP-friendly, no kapt)
- **Network**: Ktor client (shared) + kotlinx.serialization
- **Database**: SQLDelight (KMP-friendly, type-safe SQL)
- **Navigation**: navigation-compose 2.8+ with type-safe routes
- **Image Loading**: Coil 3 (Compose Multiplatform)
- **Build**: Gradle with version catalogs, Kotlin script (.kts)

### Key Decisions

1. **Koin over Hilt** — Hilt is Android-only. Koin works in KMP shared modules.
2. **SQLDelight over Room KMP** — SQLDelight has mature KMP support and type-safe queries.
3. **Ktor over Retrofit** — Ktor client is multiplatform; Retrofit is Android-only.
4. **Type-safe navigation** — `@Serializable` route objects, no string routes.
5. **Feature-vertical modules** — Each feature is self-contained with its own UI, VM, and navigation entry point.

## Code Patterns

### ViewModel Pattern
```kotlin
// Sealed event class
sealed interface CharacterSheetEvent {
    data class NameChanged(val name: String) : CharacterSheetEvent
    data object SaveClicked : CharacterSheetEvent
}

// UI State
@Immutable
data class CharacterSheetState(
    val character: Character? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

// ViewModel
class CharacterSheetViewModel(
    private val getCharacterUseCase: GetCharacterUseCase,
    private val saveCharacterUseCase: SaveCharacterUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CharacterSheetState())
    val state: StateFlow<CharacterSheetState> = _state.asStateFlow()

    fun onEvent(event: CharacterSheetEvent) {
        when (event) {
            is CharacterSheetEvent.NameChanged -> { /* ... */ }
            CharacterSheetEvent.SaveClicked -> { /* ... */ }
        }
    }
}
```

### Screen Composable Pattern
```kotlin
@Composable
fun CharacterSheetScreen(
    viewModel: CharacterSheetViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    CharacterSheetContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun CharacterSheetContent(
    state: CharacterSheetState,
    onEvent: (CharacterSheetEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Stateless composable — pure UI
}
```

### Error Handling Pattern
```kotlin
// core:common
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val error: AppError) : Result<Nothing>
}

sealed interface AppError {
    data object Network : AppError
    data object NotFound : AppError
    data class Unknown(val message: String) : AppError
}
```

## Rules

1. **Always read ARCHITECTURE.md and STATE.md before work.**
2. **Always write back to ARCHITECTURE.md and STATE.md after work.**
3. **Follow existing patterns** — if the project already uses a specific pattern for ViewModels, events, navigation, or state, match it exactly.
4. **No Android types in commonMain** — `Context`, `Activity`, `Uri`, `Bundle` must not appear in `:core:*` modules.
5. **Use Koin for DI** — never introduce Hilt or Dagger into shared modules.
6. **Use Ktor for network** — never introduce Retrofit into shared modules.
7. **Use SQLDelight for DB** — never introduce Room into shared modules.
8. **Prefer StateFlow + collectAsStateWithLifecycle** — no LiveData.
9. **Screen composables connect to ViewModel** — child composables are stateless and receive events via callbacks.
10. **Navigation from UI layer only** — ViewModels emit effects (Channel/SharedFlow), UI calls NavController.
11. **Document KMP boundaries** — when adding platform-specific code, document the boundary shape in ARCHITECTURE.md.
12. **Feature modules don't depend on each other** — they depend only on `:core:domain`, `:core:ui`, `:core:common`.
13. **Keep UI models separate from domain models** — map in ViewModels.

## Migration Path (Current → Target)

The project started as a single Android module (`:app`). The migration to KMP multi-module is incremental:

1. **Phase 1**: Convert `:app` to KMP module with `commonMain`, `androidMain` source sets. Keep UI in `commonMain`.
2. **Phase 2**: Extract `:core:model`, `:core:domain`, `:core:data`, `:core:ui`, `:core:common` modules.
3. **Phase 3**: Extract feature modules one by one.
4. **Phase 4**: Add iOS/Desktop targets.

Current phase is tracked in `architecture/STATE.md`.
