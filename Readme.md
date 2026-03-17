# VidagdhanAds SDK

A plug-and-play Android Ad SDK for all Vidagdhan apps.
Fetches, filters, stores and renders banner and popup ads
from a centralised backend — with zero screen-level hardcoding.

---

## How It Works
```
App initialises SDK with API URL
        ↓
SDK calls https://vidagdhan.com/ads.php?app_id=YOUR_PACKAGE
        ↓
Backend returns ads filtered by app_id
        ↓
SDK further filters by date (start_date / end_date)
        ↓
Valid ads stored in Room DB (offline support)
        ↓
App calls showBanner() or showPopup() with a screenId
        ↓
SDK checks frequency gate → shows ad or skips
```

---

## Backend API

**Endpoint:** `https://vidagdhan.com/ads.php?app_id=YOUR_PACKAGE_NAME`

**Response format:**
```json
{
  "status": true,
  "data": [
    {
      "app_id":     "com.vidagdhan.yourapp",
      "screen_id":  "home_screen",
      "type":       "banner",
      "image_url":  "https://vidagdhan.com/app_ads/320x100.png",
      "frequency":  3,
      "start_date": "2026-03-16 00:00:00",
      "end_date":   "2026-03-31 23:59:00",
      "cta":        "https://vidagdhan.com"
    }
  ]
}
```

| Field        | Description                                              |
|--------------|----------------------------------------------------------|
| `app_id`     | Must match the app's `applicationId` exactly             |
| `screen_id`  | Any string — the app decides placement names             |
| `type`       | `"banner"` or `"popup"`                                  |
| `frequency`  | Show every Nth visit — `3` means show on visit 3, 6, 9… |
| `start_date` | Ad goes live at this datetime                            |
| `end_date`   | Ad expires at this datetime                              |
| `cta`        | Click-through URL — can be empty string `""`             |

---

## Integration Steps

### 1. Add the module to your project

**`settings.gradle.kts`**
```kotlin
include(":ads-sdk")
project(":ads-sdk").projectDir = File("ads-sdk/ads-sdk")
// Adjust path if you placed the folder differently
```

**`app/build.gradle.kts`**
```kotlin
dependencies {
    implementation(project(":ads-sdk"))
}
```

---

### 2. Set your applicationId

**`app/build.gradle.kts`**
```kotlin
android {
    defaultConfig {
        // Must match app_id registered in the backend
        applicationId = "com.vidagdhan.yourapp"
    }
}
```

---

### 3. Initialise in Application class

Create `MyApp.kt`:
```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        VidagdhanAds.initialize(
            context       = this,
            apiUrl        = "https://vidagdhan.com/",
            enableLogging = BuildConfig.DEBUG
        )
    }
}
```

Register in `AndroidManifest.xml`:
```xml
<application android:name=".MyApp" ...>
```

---

### 4. Show a Banner Ad

**Option A — XML (auto-loads)**
```xml
<com.vidagdhan.ad_sdk_lib.ui.banner.BannerAdView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:screenId="home_screen" />
```

**Option B — Programmatic**
```kotlin
// In Activity or Fragment
VidagdhanAds.syncAds { success ->
    VidagdhanAds.showBanner(
        activity  = this,
        container = binding.adContainer,  // any FrameLayout/ViewGroup
        screenId  = "home_screen"
    )
}
```

---

### 5. Show a Popup Ad
```kotlin
// Trigger from anywhere — button click, back press, timer, etc.
VidagdhanAds.showPopup(
    activity = this,
    screenId = "exit_screen"
)
```

---

### 6. Sync ads manually (optional)

By default you sync inside `syncAds {}` callback before showing.
For pre-warming on app start call without a callback:
```kotlin
VidagdhanAds.syncAds()
```

---

## Frequency Logic

| Visit | frequency=1 | frequency=3 | frequency=5 |
|-------|-------------|-------------|-------------|
| 1     | ✅ Show      | ❌ Skip      | ❌ Skip      |
| 2     | ❌ Skip      | ❌ Skip      | ❌ Skip      |
| 3     | ✅ Show      | ✅ Show      | ❌ Skip      |
| 4     | ❌ Skip      | ❌ Skip      | ❌ Skip      |
| 5     | ✅ Show      | ❌ Skip      | ✅ Show      |
| 6     | ❌ Skip      | ✅ Show      | ❌ Skip      |

Frequency counters persist across app restarts via `SharedPreferences`.

---

## Adding a New App to the SDK

1. Ask backend team to add the app's ads in the database with correct `app_id`
2. Set `applicationId` in the new app's `build.gradle.kts`
3. Add the SDK module dependency
4. Initialize in `Application` class
5. Call `showBanner()` / `showPopup()` wherever needed

No SDK code changes required — the backend controls everything.

---

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `syncAds → success=false` | Network error or wrong URL | Check `apiUrl` ends with `/` |
| `dateValid=false` | Ad expired in backend | Update `end_date` in backend DB |
| `appMatch=false` | `app_id` mismatch | Ensure `applicationId` matches backend exactly |
| `frequency gate: SKIP` | Not Nth visit yet | Expected — ad will show on Nth visit |
| Banner visible in XML but not programmatic | `syncAds()` not awaited | Always show banner inside `syncAds {}` callback |
| Popup not showing | Activity finishing | Check `!activity.isFinishing` timing |

---

## Architecture
```
VidagdhanAds          ← Public API (only class the app touches)
    │
    ├── AdManager     ← Internal singleton, wires everything
    │       │
    │       ├── AdRepository    ← Source of truth
    │       │       │
    │       │       ├── AdFetcher    ← Network (Retrofit + OkHttp)
    │       │       └── AdStorage    ← Room DB + SharedPrefs
    │       │
    │       └── AdStorage       ← Frequency gate
    │
    ├── BannerAdView  ← Custom View (XML + programmatic)
    └── PopupAdDialog ← Dialog UI
```

---

## Tech Stack

| Library    | Version | Purpose          |
|------------|---------|------------------|
| Retrofit   | 2.11.0  | HTTP client      |
| OkHttp     | 4.12.0  | Network layer    |
| Gson       | 2.11.0  | JSON parsing     |
| Glide      | 4.16.0  | Image loading    |
| Room       | 2.6.1   | Local cache      |
| Coroutines | 1.8.1   | Async operations |

---

## Screen ID Convention (Recommended)

Although the SDK never enforces screen names, it is recommended
to use a consistent naming convention across all apps:

| Screen          | Recommended screenId  |
|-----------------|-----------------------|
| Home / Dashboard | `home_screen`        |
| Exit / Back press | `exit_screen`       |
| After purchase  | `post_purchase_screen`|
| Settings        | `settings_screen`     |
| Splash          | `splash_screen`       |

---

## Version History

| Version | Changes            |
|---------|--------------------|
| 1.0.0   | Initial release    |
