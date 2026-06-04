# D&D Helper

## Images
<p align="center">
  <img src="images/288shots_so.png" alt="App Preview 1" height="400" />
  <img src="images/766shots_so.png" alt="App Preview 2" height="400" />
</p>

A Kotlin Multiplatform (KMP) companion application for Dungeons & Dragons 5th Edition sessions. Manage your characters, items, and world library across Android, Desktop, and Web with real-time synchronization via Google Sheets.

## 🚀 Key Features

- **Multi-Platform Support**: Use the app on your Android phone, your DM's laptop (Desktop), or any browser (Web).
- **Character Management**: Track stats, HP, level, skills, and biography.
- **Inventory System**: Add, delete, and move items between characters (Admin mode).
- **World Library**: Store and manage Monsters, NPCs, and Locations.
- **Dice Roller**: Integrated dice rolling for quick checks.
- **Real-Time Sync**: Changes made on one device are automatically polled and updated on others using a Google Sheets backend.
- **Optimistic UI**: Instant UI updates with background synchronization for a lag-free experience.
- **Admin/DM Mode**: Extended capabilities on Desktop for game preparation and session management.

## 📱 Platform Availability

| Platform | Mode | Target |
|----------|------|--------|
| **Android** | Player | Android 10+ (API 29) |
| **Desktop** | Player + Admin (DM) | Windows/macOS/Linux (JVM 21) |
| **Web** | Player | Browser (Kotlin/Wasm) |

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| **UI** | [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) (Material 3) |
| **Networking** | [Ktor](https://ktor.io/) + [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) |
| **DI** | [Koin](https://insert-koin.io/) |
| **Navigation** | [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) (Type-safe) |
| **Image Loading** | [Coil 3](https://coil-kt.github.io/coil/) |
| **Concurrency** | Kotlin Coroutines & Flow |
| **Backend** | Google Apps Script + Google Sheets |

## 🏗️ Project Structure

The project follows a Clean Architecture pattern with MVVM:

- `:shared`: Core logic, domain models, and shared Compose UI.
- `:app`: Android-specific entry point and configuration.
- `:desktop`: Desktop-specific entry point using Compose for Desktop.
- `:web`: Web entry point using Kotlin/Wasm and Canvas-based rendering.
- `:server`: Ktor server with PostgreSQL database for advanced syncing and data management.
- `apps-script`: Backend code for Google Apps Script.

## 🛠️ Server & Database Setup

The project includes a standalone Ktor server (`:server`) that uses **PostgreSQL**.

### 1. Database Configuration
The server uses environment variables for configuration:
- `DB_HOST`: Database host (default: `localhost`)
- `DB_PORT`: Database port (default: `5432`)
- `DB_NAME`: Database name (default: `dndhelper`)
- `DB_USER`: Database user (default: `postgres`)
- `DB_PASSWORD`: Database password (default: `postgres`)

### 2. Running with Docker
A `docker-compose.yml` is provided for convenience. To start the database:
```bash
docker compose up -d
```

### 3. Running the Server
```bash
./gradlew :server:run
```

## 🛠️ Pre-conditions & Setup

### 1. Requirements
- **JDK 21** or higher.
- **Android Studio** (Hedgehog or newer) or **IntelliJ IDEA**.
- A **Google Account** (for the Google Sheets backend).

### 2. Backend Setup (Google Apps Script)
The app uses Google Sheets as its database. You must deploy the backend script first:
1. Follow the [Google Apps Script Setup Guide](apps-script/README.md).
2. Once deployed, copy your **Web App URL**.
3. Create a `local.properties` file in the project root if it doesn't exist.
4. Add the following line:
   ```properties
   apps.script.url=https://script.google.com/macros/s/YOUR_DEPLOYMENT_ID/exec
   ```

## 🏃 How to Run

### Android App
1. Open the project in Android Studio.
2. Select the `app` run configuration.
3. Click **Run** or use the terminal:
   ```bash
   ./gradlew :app:installDebug
   ```

### Desktop App
1. Use the `desktop` run configuration in your IDE.
2. Or run via terminal:
   ```bash
   ./gradlew :desktop:run
   ```

### Web Browser
1. Run the following command in the terminal:
   ```bash
   ./gradlew :web:wasmJsBrowserRun
   ```
2. The app will open in your default browser (usually at `http://localhost:8081`).

## 📚 Architecture

Detailed architectural decisions, tech stack information, and data flow diagrams can be found in the [ARCHITECTURE.md](architecture/ARCHITECTURE.md) file.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
