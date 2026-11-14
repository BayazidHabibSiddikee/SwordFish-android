# Swordfish Browser

**Swordfish** is a fast, privacy-first Android web browser built with **Jetpack Compose** and **Kotlin**, styled with the official **Ubuntu Orange & Yaru** theme. It features a multi-tiered **AdBlocker Engine with a JavaScript Bridge**, a dedicated **YouTube AdBlocker**, an in-built **Distraction Blocker**, and a **Background Service** that tracks active usage times stored in a local **Room Database**.

---

## Key Features

### 1. Canonical Ubuntu Orange & Yaru Design System
- **Ubuntu Orange (`#E95420`)**: Brand accent on primary action buttons, loading progress bars, active status pills, and shield icons.
- **Ubuntu Dark Aubergine (`#2C001E`)**: Background gradients evoking the classic Ubuntu Linux desktop.
- **Yaru Dark Terminal (`#1E1E1E` & `#262626`)**: Clean dark surfaces with high-contrast text and crisp card borders.
- **Ubuntu Success Green (`#38B44A`)**: Terminal green for verified security indicators and active monitor states.

### 2. Multi-Tier AdBlocker Engine with JavaScript Bridge
- **Tier 1 (Network-Level Interception)**: Intercepts ad networks, tracking pixels, and telemetry servers directly in `WebViewClient.shouldInterceptRequest`, returning zero-byte responses to save mobile data.
- **Tier 2 (JavaScript Bridge Layer)**: Native `SwordfishAdBlockBridge` registered via `addJavascriptInterface` as `window.SwordfishBridge`. In-page scripts report blocked DOM selectors and popup traps back to the Android app in real-time.
- **Tier 3 (DOM MutationObserver)**: Continuously prunes injected ad elements (`ins.adsbygoogle`, `iframe[src*="ad"]`, `div[data-ad]`, Outbrain, Taboola), disables popups via `window.open`, and neutralizes anti-adblock modal overlays.

### 3. Dedicated YouTube AdBlocker
- **Video Pre-Roll & Mid-Roll Auto-Skip**: Detects YouTube video player ad status (`.ad-showing`), mutes audio, accelerates playback rate, and jumps to `video.duration`.
- **Automatic Skip Button Clicker**: Triggers `.ytp-ad-skip-button` and `.ytp-skip-ad-button` as soon as they appear.
- **Feed & Banner Suppression**: Hides promoted videos, statement banners, companion cards, and upscale dialogs (`ytd-promoted-video-renderer`, `ytm-promoted-sparkles-web-renderer`, `#masthead-ad`).
- **Endpoint Interception**: Blocks backend YouTube ad endpoints (`/api/stats/ads`, `/pagead/`, `googleads.g.doubleclick.net`).

### 4. Distraction Blocker & Background Usage Monitor Service
- **Room Database Storage**: Stores blacklisted app packages (e.g., `com.zhiliaoapp.musically`, `com.instagram.android`) and web domains (e.g., `tiktok.com`, `instagram.com`), their custom categories, and daily usage limits in minutes.
- **Active Usage Time Tracking**: Records daily active seconds and blocked attempts in the `usage_times` table.
- **Background & Foreground Service (`FocusUsageMonitorService`)**: Runs a persistent monitoring loop with an ongoing notification, keeping track of active time remaining and enforcing focus shields.
- **Pomodoro Focus Sessions**: Quick presets (15m, 25m, 45m, 60m) that trigger an interactive distraction-blocked overlay whenever a restricted target is accessed.

### 5. Screen Time Bar Chart & Usage Analytics Dashboard
- **Jetpack Compose Bar Chart (`ScreenTimeBarChart`)**: Custom canvas-rendered and card-backed bar chart visualizing daily active screen time spent on blacklisted sites vs their configured limits.
- **Dynamic Limit Threshold Styling**:
  - Normal usage: Canonical Ubuntu Orange (`#E95420`)
  - Warning zone (over 75% of limit): Warm Yellow (`#EFB73E`)
  - Limit exceeded (100%+): High-contrast Red (`#DF382C`) with a limit-exceeded warning indicator.
- **Room SQLite Aggregation**: Queries `usage_times` and `blocked_sites` to calculate total distraction minutes and block events.
- **Interactive Card Details**: Tap any bar to view exact minutes logged, daily limits, times blocked, and package/domain classification.

### 6. Media Hub & Python yt-dlp Video/Audio Downloader
- **Integrated CLI Tool Engine**:
  - **`yt-dlp` (v2026.10.26)**: Universal stream extraction and high-speed downloading.
  - **`ffmpeg` (v4.4.2)**: High-performance audio extraction (MP3/M4A), muxing, and video transcoding.
  - **`aria2c` (v1.36.0)**: Multi-connection download accelerator integrated via `yt-dlp --downloader aria2c`.
  - **`mediainfo` (v23.04)**: Codec, bitrate, frame rate, and container analyzer.
  - **`AtomicParsley`**: ID3, AAC, and MP4 metadata tagging engine.
- **Built-in Hardware Media Player**:
  - Supports video playback via Android `VideoView` and audio playback via Android `MediaPlayer`.
  - Play, Pause, -10s Rewind, +10s Fast-Forward, dynamic seek slider, and loop repeat toggles.
  - Variable playback speed controls (`0.5x`, `0.75x`, `1.0x`, `1.25x`, `1.5x`, `2.0x`).
  - Animated ambient audio visualizer wave graphics during playback.
  - Bundled offline sample assets: `focus_ambience.m4a` and `sample_video.mp4`.
- **Active Web Stream Detection**: Automatically detects YouTube, Vimeo, and direct media URLs in the active browser tab for 1-tap download in MP4 video or MP3 audio formats.
- **Python CLI Tool Pipeline**: `scripts/swordfish_media_engine.py` provides command-line testing and status inspection.

### 7. Room Database Bookmarking & Favorite Quick Access
- **Room SQLite Persistence**: Saves favorite URLs, page titles, category tags, optional notes, visit counters, and pin states in the local `bookmarks` table.
- **New Tab Page Quick Access Grid**:
  - Dynamically renders favorite bookmarks stored in the local Room DB as interactive Ubuntu tiles with category-based gradients.
  - **"+ Add Favorite" Tile**: Dedicated tile allowing users to add custom URLs or select from quick presets (Ubuntu, GitHub, YouTube, DDG, Wikipedia) directly on the New Tab page.
  - **Category Filter Pills**: Real-time filtering by category ("All", "Favorites", "Ubuntu", "Dev", "Media", "News", "Social") directly on the home screen.
  - **Pin to New Tab**: Users can pin/unpin favorites from quick access or manage them via the full Bookmarks Manager.
  - **3-Dots Quick Actions**: Open in New Tab, Copy URL, Edit Favorite, Toggle Pin, or Remove from Room DB.
  - **1-Tap Star in Address Bar**: Instant bookmarking of the currently loaded web page with Toast confirmation and filled star feedback.
- **Most Visited Sites (Room Database Analytics)**:
  - Dynamically computes and ranks most visited websites from browsing history (`COUNT(*) as visitCount GROUP BY url`).
  - Displays visit count badges (e.g. "⭐ 15 visits") and 1-tap navigation.
  - Quick "+ Save to Favorites" action if a frequently visited site is not yet bookmarked.
- **Ubuntu Yaru Bookmarks Manager**:
  - Full modal bottom sheet with live search, category filtering, multi-criteria sorting (Newest, Oldest, Title A-Z, Category), and Ubuntu Starter Pack seeder.
  - Authentic Ubuntu font typography powered by local `.ttf` font integration.

### 8. Secure Local Password Manager & In-Browser Autofill
- **Zero-Plaintext Keystore Encryption**:
  - Stored user credentials are encrypted at rest using **Android Jetpack Security (`EncryptedSharedPreferences`)**.
  - Employs **AES256-SIV** for preference keys and **AES256-GCM** for credential values, managed by the Android Keystore via `MasterKey`.
- **Automated Login Form Detection & JavaScript Bridge**:
  - In-page script (`LOGIN_DETECTOR_JS`) continuously inspects DOM forms for password fields, email/username inputs, and submit buttons.
  - Reports detected login forms back to the native Android host via `window.SwordfishBridge.reportLoginFormDetected`.
- **1-Tap In-Browser Autofill**:
  - Floating Ubuntu Yaru prompt (`AutofillPromptBanner`) appears above the bottom toolbar whenever a login form is detected on a matching domain.
  - Supports multiple accounts per domain with quick account switching.
  - Injects credentials via `PasswordAutofillEngine` and dispatches synthetic `input` and `change` events with `{ bubbles: true }` so modern reactive frameworks (React, Vue, Angular) immediately recognize the entered values.
- **Smart Save Password Prompts**:
  - When users submit a login form or click a sign-in button, `SavePasswordDialog` prompts to store the credentials in the encrypted vault with password preview toggles and "Never for this site" options.
- **Ubuntu Password Vault Interface**:
  - Dedicated **Password Manager Sheet (`PasswordManagerSheet`)** accessible via the browser bottom bar and New Tab page.
  - Domain search, copy username, copy password (with visual clipboard toast), visibility toggle, and manual add/edit/delete.
  - **Cryptographically Secure Password Generator**: Configurable length slider (8 to 32 characters), character set toggles (uppercase, digits, symbols), and dynamic entropy progress bar with strength classification (Very Weak to Very Strong).

### 9. Central Protection & Filter Settings UI
- **Granular 17-Filter Matrix**:
  - **Ad-Blocking (6 Filters)**: Network Ad-Request Interception, Cosmetic DOM Hiding, YouTube Video Ad Blocker, Pop-up & Tab Trap Blocker, Anti-Adblocker Script Neutralizer, Social Widgets & Sticky Bars.
  - **Tracking Protection (6 Filters)**: Web Analytics & Telemetry Blocker, Cross-Site Cookie Shield, URL Tracking Parameter Stripping (`utm_*`, `gclid`, `fbclid`), Cryptominer Script Blocker, Fingerprinting/Canvas Defense, HTTP Referrer Trimming.
  - **Distraction Management (5 Filters + Room Blacklist Toggles)**: Focus Shield Master Mode, Short-Form Video Loop Blocker (TikTok, Reels, Shorts), Social Media Daily Limits, Near-Limit Warning Chimes (75%/100%), Pomodoro Break Reminders.
- **Preset Profiles**: 1-tap switching between `Strict`, `Balanced` (Default Ubuntu profile), and `Permissive`.
- **Live Search Field**: Real-time filtering across all 17 filters by title, keyword, or description.
- **Reactive Persistence**: Backed by `FilterSettingsRepository` with JSON SharedPreferences storage.

### 10. Distraction-Free Reader Mode
- **Intelligent DOM Content Extractor (`ReaderModeEngine`)**:
  - Automatically strips away distracting advertisements, sidebars, navigation bars, cookie banners, newsletter slide-ins, and social clutter.
  - Retains and formats the core article title, author byline, publication date, site name, estimated reading time, and clean article paragraphs/headings.
- **Ubuntu Theming & Typography Customization (`ReaderSettings`)**:
  - **5 Reading Themes**: Ubuntu Dark, Ubuntu Aubergine, Paper Light, Sepia Warm, and AMOLED Black.
  - **Font Styles**: Sans-Serif, Serif, and Monospace.
  - **Font Size Stepper**: Adjustable from 12sp to 28sp with live scaling (`A-` and `A+`).
  - **Line Spacing & Width Controls**: Tight, Normal, Relaxed line height; Compact, Balanced, and Full reading widths.
- **Integrated Text-to-Speech (TTS) Read Aloud**:
  - Native hands-free listening using Android's `TextToSpeech` engine with play, stop, and sentence tracking.
- **Top Bar Integration**:
  - 1-tap Reader Mode toggle icon in the Omnibox whenever an article is detected, with a subtle glowing indicator when reading is ready.
  - Prominent "Exit Reader" button and seamless system Back button handling returning to the original webpage.
- **Share & Clean Text Export**: 1-tap copying of the cleaned, advertisement-free article text to clipboard or system share sheet.

---

## Architecture Overview

```
app/src/main/java/com/example/
├── MainActivity.kt                     # Single Activity, Compose entry point & BackHandler
├── data/
│   ├── BrowserRepository.kt            # Repository abstraction for DAO & usage monitoring
│   ├── blocker/
│   │   ├── AdBlockEngine.kt            # Network rules, JS Mutation script, YouTube blocker
│   │   ├── AppBlockerEngine.kt         # Distraction matching & time formatting
│   │   └── SwordfishAdBlockBridge.kt   # Bidirectional @JavascriptInterface bridge
│   ├── local/
│   │   ├── BlockedSiteEntity.kt        # Room entity for blacklisted apps & sites
│   │   ├── BookmarkEntity.kt           # Room entity for saved bookmarks
│   │   ├── BrowserDao.kt               # Room Data Access Object
│   │   ├── BrowserDatabase.kt          # Room SQLite Database holder
│   │   ├── HistoryEntity.kt            # Room entity for browsing history
│   │   └── UsageTimeEntity.kt          # Room entity for active monitored usage times
│   ├── model/
│   │   ├── BrowserTab.kt               # Tab state (URL, title, loading, blocker counts)
│   │   ├── MediaModels.kt              # Media item, download task, and CLI tool models
│   │   ├── SearchEngine.kt             # Search engine endpoints
│   │   └── ShieldStats.kt              # Cumulative blocker metrics
│   └── service/
│       └── FocusUsageMonitorService.kt # Background/Foreground service for usage monitoring
├── ui/
│   ├── components/
│   │   ├── AppBlockerSheet.kt          # Distraction blocker UI & usage progress bars
│   │   ├── BlockedOverlayView.kt       # Inspiring distraction-blocked overlay screen
│   │   ├── BookmarksHistorySheet.kt    # Bookmarks and history management
│   │   ├── BrowserBottomBar.kt         # Edge-to-edge navigation bottom toolbar
│   │   ├── BrowserTopBar.kt            # Omnibox with shield badge and SSL lock
│   │   ├── BrowserWebView.kt           # Custom WebView with clients and JS Bridge
│   │   ├── MediaHubSheet.kt            # Video/Audio player & yt-dlp downloader modal
│   │   ├── NewTabPage.kt               # Ubuntu-themed home start page with speed dial & cards
│   │   ├── ScreenTimeBarChart.kt       # Custom Compose Bar Chart with threshold limits
│   │   ├── ScreenTimeDashboardSheet.kt # Daily screen time analytics dashboard
│   │   ├── ShieldModalSheet.kt         # Privacy dashboard and master toggles
│   │   └── TabsManagerSheet.kt         # Visual multi-tab grid manager
│   └── theme/
│       ├── Color.kt                    # Official Ubuntu Orange & Yaru palette
│       ├── Theme.kt                    # Jetpack Compose Material 3 theme configuration
│       └── Type.kt                     # Typography definitions
└── viewmodel/
    └── BrowserViewModel.kt             # Central state manager (tabs, shields, timer, Room data, media player)
```

---

## Room Database Schemas

### `blocked_sites` Table
| Column | Type | Description |
|---|---|---|
| `id` | INTEGER (PK) | Auto-generated ID |
| `domain` | TEXT | Package name or domain (e.g. `com.instagram.android` or `tiktok.com`) |
| `displayName` | TEXT | Human-readable title (e.g. `Instagram (App)`) |
| `category` | TEXT | Category tag (`Social Media`, `Entertainment`, `Games`, `Custom`) |
| `dailyLimitMinutes` | INTEGER | Configured daily limit in minutes |
| `isAppPackage` | INTEGER (Boolean) | True for Android app packages, False for web domains |
| `isEnabled` | INTEGER (Boolean) | Blacklist active toggle |

### `usage_times` Table
| Column | Type | Description |
|---|---|---|
| `id` | INTEGER (PK) | Auto-generated ID |
| `targetIdentifier` | TEXT | Target package name or domain |
| `dateString` | TEXT | Date string (`YYYY-MM-DD`) |
| `activeSecondsToday` | INTEGER | Active monitored seconds today |
| `lastActiveTime` | INTEGER | Epoch timestamp of last active interaction |
| `timesBlockedToday` | INTEGER | Count of navigation attempts blocked today |

---

## Python & CLI Media Tools

Verify installed media tools directly:
```bash
python3 scripts/swordfish_media_engine.py status
```

Output:
```json
{
  "yt-dlp": { "installed": true, "path": "/usr/local/bin/yt-dlp", "version": "2026.10.26" },
  "ffmpeg": { "installed": true, "path": "/usr/bin/ffmpeg", "version": "4.4.2" },
  "aria2c": { "installed": true, "path": "/usr/bin/aria2c", "version": "1.36.0" },
  "mediainfo": { "installed": true, "path": "/usr/bin/mediainfo", "version": "23.04" },
  "atomicparsley": { "installed": true, "path": "/usr/bin/AtomicParsley", "version": "utf8" }
}
```

---

## Building and Testing

### Compile the Application
```bash
gradle assembleDebug
```

### Run Unit and Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```
Tests cover:
- Resource string integrity (`app_name = Swordfish`)
- Network-level ad & tracker domain interception
- YouTube ad endpoint and URL detection
- JavaScript Bridge layer callback dispatching
- App and domain distraction blocking logic in focus mode
- Room DB daily usage aggregation and threshold checks
- Time formatting calculations

# SwordFish-android
