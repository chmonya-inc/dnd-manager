# D&D Helper — Desktop DM Mode Implementation Plan

## 1. Desktop UI Restructuring (Split-Pane)
Currently, Desktop follows the mobile-like linear navigation. We will move to a sidebar-based multi-pane layout.

- **MainDesktopScreen**: A new container for Desktop that includes:
    - **Navigation Rail / Sidebar**: Icons for Characters, Library, Creation Hub, and Presentation.
    - **Content Area**: Dynamic content based on sidebar selection.
- **Split-Pane Characters View**:
    - **Left Panel (30%)**: The `CharacterListScreen` (modified to be a narrow list).
    - **Right Panel (70%)**: The `CharacterDetailScreen` (Master Version) or `CharacterEditor`.
    - Selection in the left panel updates the right panel via a shared `CharacterDetailViewModel`.

## 2. Master Mode Features
DM mode requires more "destructive" and "creative" power than the Player mode.

- **Enhanced Character Editor (DM Mode)**:
    - Add/Delete skills and proficiencies.
    - Add/Delete/Edit items in inventory directly.
    - Full override of all stats without constraints.
    - UI toggle to switch between "Player View" and "Master Editor".
- **Master Creation Hub**:
    - A unified screen to create:
        - New Characters.
        - New Items / Equipment templates.
        - New Monsters / Mobs with stat blocks.
        - New Locations with descriptions and images.
- **Library Management (The "Folder" System)**:
    - Navigation sections for Items, Mobs, and Locations.
    - CRUD operations (Create, Read, Update, Delete) for all game entities stored in Google Sheets.

## 3. Presentation Mode
A feature for DMs to share information with players on a separate screen (e.g., a TV or Projector).

- **Secondary Window**: Use Compose `Window` to open a "Player View" window.
- **Presentation Controller**:
    - In the DM's main window, a panel to "Send to Screen".
    - Ability to send images (Maps, NPC portraits).
    - Ability to send "Card Blocks" (Item stats, Spell descriptions).
    - DM can arrange, resize, and clear items on the presentation screen.

## 4. Architectural Changes
- **Navigation**:
    - Introduce `DesktopMain` route as the entry point for `isDesktop == true`.
    - Use nested navigation or simple state switching for the right panel in the split-pane.
- **ViewModels**:
    - `MasterLibraryViewModel` to manage non-character entities (Items, Mobs).
    - `PresentationViewModel` to sync state between the DM window and the Presentation window.
- **Data Layer**:
    - Extend `GoogleAppsScriptDataSource` and `CharacterRepository` to handle new entities (Monsters, Items, etc.) by targeting different Sheets/Tabs in the Google Spreadsheet.

## 5. Implementation Steps
1.  **Phase 1**: Update `App.kt` to show `MainDesktopScreen` on desktop.
2.  **Phase 2**: Implement the Sidebar and the Split-Pane Character List + Detail.
3.  **Phase 3**: Create the "Master Library" for Items/Mobs/Locations.
4.  **Phase 4**: Implement the "Creation Hub" for creating new templates.
5.  **Phase 5**: Build the Presentation Window and the "Send to Screen" logic.
