# D&D Helper — Google Apps Script Backend

This folder contains the Google Apps Script code that serves as the backend for the D&D Helper app.

## How it works

The Apps Script is deployed as a **Web App** that receives JSON POST requests from the Kotlin app, reads/writes data to a Google Spreadsheet, and returns JSON responses.

The Kotlin app never talks to Google Sheets or Google OAuth directly — everything goes through this script.

## Setup Instructions

### 1. Create a Google Spreadsheet

1. Go to [Google Sheets](https://sheets.new) and create a new spreadsheet.
2. Name it something like **"D&D Campaign"**.
3. **Copy the Spreadsheet ID** from the URL:
   ```
   https://docs.google.com/spreadsheets/d/SPREADSHEET_ID/edit
   ```
   The ID is the long random string between `/d/` and `/edit`.

### 2. Open the Apps Script editor

You have two options:

**Option A — Bound script (simplest):**
1. In your spreadsheet, click **Extensions → Apps Script**.
2. Paste the `Code.gs` content into the editor.
3. Leave `SPREADSHEET_ID = ""` at the top — the script will use the sheet it's bound to.

**Option B — Standalone script (if you already have a script project):**
1. Go to [script.google.com](https://script.google.com) and create a new project.
2. Paste the `Code.gs` content.
3. Set `SPREADSHEET_ID = "YOUR_ID_HERE"` at the top.

### 3. Deploy as a Web App

1. Click **Deploy → New deployment**.
2. Click the gear icon (⚙️) next to **Type** and select **Web app**.
3. Configure:
   - **Description**: `v1`
   - **Execute as**: `Me`
   - **Who has access**: **`Anyone`** (critical — otherwise you'll get HTTP 401)
4. Click **Deploy**.
5. You may need to **authorize** the script to access your Google Sheets — click through the permissions.
6. Copy the **Web App URL** (looks like `https://script.google.com/macros/s/.../exec`).

### 4. Connect the Kotlin app

1. Open **`local.properties`** in the project root.
2. Paste your Web App URL:
   ```properties
   apps.script.url=https://script.google.com/macros/s/YOUR_SCRIPT_ID/exec
   ```
3. **Sync Gradle** so the generated config picks up the URL.
4. Build and run the app.

## Troubleshooting

### HTTP 302 "Moved Temporarily"

Google Apps Script `/exec` URLs return a 302 redirect to `script.googleusercontent.com`. The Kotlin app now handles this automatically — no action needed.

If you still see errors, check Logcat for the `[AppsScript]` tag.

### HTTP 401 Unauthorized

Your Web App is not publicly accessible. Redeploy with **"Who has access: Anyone"**.

### "Cannot resolve hostname" / Network error

- Check that `apps.script.url` in `local.properties` is correct.
- Make sure your emulator/device has internet.
- Check that `AndroidManifest.xml` has `<uses-permission android:name="android.permission.INTERNET" />`.

## API Reference

### Request format

All requests are HTTP POST with `Content-Type: application/json`.

```json
{
  "action": "getCharacters" 
}
```

### Actions

| Action | Payload | Description |
|---|---|---|
| `getCharacters` | none | Returns all characters |
| `getCharacter` | `{ "id": "1" }` | Returns one character by ID |
| `saveCharacter` | `{ "character": { ... } }` | Inserts or updates a character |
| `deleteCharacter` | `{ "id": "1" }` | Deletes a character by ID |

### Response format

```json
// Success with data
{ "success": true, "data": [ ... ] }

// Success without data
{ "success": true }

// Error
{ "success": false, "error": "Character not found: 1" }
```

## Sheet structure

The script automatically creates a sheet named **"Characters"** with these columns:

| ID | Name | PlayerName | Race | Class | Level | Description | ImageUrl | MaxHP | CurrentHP | Strength | Dexterity | Constitution | Intelligence | Wisdom | Charisma |

You can manually edit the sheet at any time — the app will read the latest data on refresh.
