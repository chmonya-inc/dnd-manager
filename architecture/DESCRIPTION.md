# D&D Helper — Project Description

## Overview

D&D Helper is a Kotlin Multiplatform application for Dungeons & Dragons 5th Edition sessions. It supports two platforms:
- **Android** — primary platform for players
- **Desktop (JVM)** — two modes: Player copy and Admin (Dungeon Master)

## Platforms & Modes

### Android App
- Player-only mode
- Character management
- Location viewing (controlled by DM)

### Desktop App
The desktop app has two modes:

#### 1. Player Mode
- Exact copy of Android player experience
- Character sheet viewing
- Location and description viewing

#### 2. Admin Mode (Dungeon Master)
Two sub-modes:

**Preparation Mode:**
- Create and manage locations
- Manage characters (NPCs and player characters)
- Create weapons, armor, and other equipment
- Create monsters and bosses
- World-building tools

**Game Mode:**
- Focused on live game process
- Show players their current location and description
- Combat and fight management
- Quick creation of items on-the-fly (secondary feature)
- DM screen with all necessary game info

## Backend

**Google Sheets** is used as the server/database.
- No security needed — the spreadsheet is editable by anyone with the link
- OAuth is used for Google Sheets API access (read/write)
- Single shared spreadsheet per campaign/world
- All game data (characters, locations, items, monsters) stored in sheets

## Tech Stack

| Component | Technology |
|-----------|-----------|
| UI Framework | Jetpack Compose Multiplatform |
| Architecture | MVVM + Clean Architecture |
| DI | Koin |
| Network | Ktor client + kotlinx.serialization |
| Backend | Google Sheets REST API (OAuth) |
| Navigation | navigation-compose 2.8+ (type-safe routes) |
| Build | Gradle with version catalogs (.kts) |

## Module Structure

```
:shared                     ← KMP shared module (UI, domain, data logic)
├── commonMain              ← Shared Compose UI, ViewModels, UseCases, Repositories
├── androidMain             ← Android-specific bindings (if any)
└── desktopMain             ← Desktop-specific bindings (if any)

:app                        ← Android application module
├── MainActivity.kt         ← Entry point, calls shared App()
├── AndroidManifest.xml
└── Koin Android init

:desktop                    ← Desktop application module
├── main.kt                 ← Entry point, calls shared App()
└── Koin Desktop init
```

## Features

### Player Features
- Character sheet management (view/edit stats, description, image)
- View current location and description (DM-controlled)

### Admin Features
- **Preparation:** Create locations, characters, equipment, monsters, bosses
- **Game:** Live session management, location display for players, combat tools

## Data Model (Initial)

### Character
- id: String
- name: String
- playerName: String
- race: String
- characterClass: String
- level: Int
- description: String
- imageUrl: String?
- stats: CharacterStats (str, dex, con, int, wis, cha)
- maxHp: Int
- currentHp: Int

### Location
- id: String
- name: String
- description: String
- imageUrl: String?
- isCurrent: Boolean

## Notes
- Desktop app mode selection (Player/Admin) happens at startup
- Android app always starts in Player mode
- Google Sheets acts as both database and sync mechanism
- No local database — all data is fetched from Google Sheets
