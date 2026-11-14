# Swordfish Browser Architecture

Swordfish is a high-performance Android web browser built using **Jetpack Compose**, **Kotlin Coroutines**, and **Android WebView**, styled with the official **Ubuntu Orange & Yaru** design system.

---

## 1. High-Level System Architecture

The application is structured following modern **Android Clean Architecture (MVVM)** principles:

```
┌─────────────────────────────────────────────────────────────┐
│                       Jetpack Compose UI                    │
│  BrowserApp ── BrowserTopBar ── BrowserBottomBar ── Sheets  │
│         ▲                                                   │
│         │ StateFlow / UI Events                             │
│         ▼                                                   │
│                 BrowserViewModel                            │
│  - Tab Management (_tabs, _activeTabId)                     │
│  - Focus Session Manager (Pomodoro Timer)                   │
│  - Shield Stats & Privacy Configuration                     │
│         ▲                                                   │
│         │ Repository Pattern                                │
│         ▼                                                   │
│                BrowserRepository                            │
│         ▲                    ▲                  ▲           │
│         │                    │                  │           │
│         ▼                    ▼                  ▼           │
│   BrowserDatabase    FocusUsageMonitorService  Blocker      │
│   - Bookmarks        (Background/Foreground)   Engines      │
│   - History          - Active usage seconds    - AdBlock    │
│   - BlockedSites     - Ongoing notification    - AppBlock   │
│   - UsageTimes       - Focus countdown         - JS Bridge  │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Core Modules & Responsibilities

### 2.1 UI Layer (`com.example.ui`)
- **`MainActivity.kt`**: Single Activity entry point with edge-to-edge system insets handling and integrated Android back navigation handler.
- **`NewTabPage.kt`**: Ubuntu-themed home page displaying speed dial shortcuts, omnibox search input, search engine switcher chips, live shield stats, and quick focus launcher.
- **`BrowserTopBar.kt`**: Modern Omnibox supporting search query resolution, SSL lock indicator, live blocked-ads counter badge, bookmark star, and animated loading bar.
- **`BrowserBottomBar.kt`**: Bottom navigation compliant with Android navigation bar insets, providing back, forward, home, Focus Shield, bookmarks/history, and tabs switcher.
- **`BrowserWebView.kt`**: Custom AndroidView wrapping `WebView` with attached `WebViewClient`, `WebChromeClient`, and `SwordfishAdBlockBridge` for JavaScript interception.
- **`ShieldModalSheet.kt`**: Privacy Dashboard showing real-time page metrics, all-time statistics, master switches, and search engine preference.
- **`AppBlockerSheet.kt`**: Distraction & App Blocker configuration, active usage progress bars from Room, background service controls, and custom package/site management.
- **`TabsManagerSheet.kt`**: Multi-tab management grid supporting tab creation, switching, closing, and private Incognito browsing.
- **`BookmarksHistorySheet.kt`**: Ubuntu-styled bookmark management interface with live search filtering, category pills, add/edit modal dialogs, URL copy, delete confirmation, and browsing history.
- **`BlockedOverlayView.kt`**: Interactive blocker screen shown when navigating to restricted domains during focus sessions.
- **`ScreenTimeBarChart.kt`**: Jetpack Compose custom canvas & card bar chart with animated bar heights, warning/danger threshold indicators, and selectable site detail cards.
- **`ScreenTimeDashboardSheet.kt`**: Analytics dashboard querying Room DB `usage_times` and `blocked_sites` to show daily screen time, distraction counts, and active monitoring state.
- **`MediaHubSheet.kt`**: Full media suite featuring built-in VideoView & MediaPlayer, yt-dlp downloader interface, media library, and CLI engine status inspector.

### 2.2 Data Layer (`com.example.data`)
- **`BrowserRepository.kt`**: Abstraction over Room DAO queries and usage time tracking, exposing reactive `Flow<List<T>>` to ViewModels.
- **`BrowserDatabase.kt`**: Room database configured with schema `version = 3`, featuring pre-populated default distraction packages and rich Ubuntu/developer bookmarks.
- **`BrowserDao.kt`**: Reactive queries for Bookmarks, History, Blocked Sites, and Usage Times.
- **`FocusUsageMonitorService.kt`**: Background/Foreground Service monitoring active usage seconds and daily limits for blacklisted targets.
- **`MediaModels.kt`**: Data classes for media playback items, download queue tasks, and CLI tool diagnostic records.

### 2.3 Blocker & Bridge Layer (`com.example.data.blocker`)
- **`AdBlockEngine.kt`**:
  - Hash-set domain matching and URL path heuristic detection.
  - Returns zero-byte responses to prevent unwanted bandwidth consumption.
  - Comprehensive JavaScript MutationObserver injection for dynamic ad DOM removal.
  - Dedicated YouTube video ad skipping and banner suppression.
- **`SwordfishAdBlockBridge.kt`**:
  - JavaScript Interface (`@JavascriptInterface`) enabling bidirectional in-page JS communication with native Android host.
  - Dispatches ad-block events, video ad skip metrics, pop-up closures, login form detections, and credential submission captures.
- **`AppBlockerEngine.kt`**:
  - Domain suffix matching against enabled blocklists.
  - Pomodoro focus session state and time formatting utilities.

### 2.4 Secure Password Manager & Autofill Layer (`com.example.data.local` & `ui/components`)
- **`SecurePasswordRepository.kt`**:
  - Encrypted credential storage backed by **Android Jetpack Security (`EncryptedSharedPreferences`)**.
  - Dual-layer hardware cryptography: **AES256-SIV** for keys and **AES256-GCM** for values using Android Keystore `MasterKey`.
  - Zero plaintext persistence with automatic domain matching (including subdomains and multi-account support).
  - Built-in cryptographically secure password generator (`SecureRandom`) with customizable constraints (length 8-64, uppercase, digits, symbols) and entropy/strength analyzer.
- **`PasswordAutofillEngine.kt`**:
  - Injected JavaScript observer (`LOGIN_DETECTOR_JS`) identifying password inputs and correlated username/email fields on web pages.
  - Dynamic MutationObserver listener capturing form submissions to trigger native "Save Password" prompts.
  - Synthetic event dispatcher (`input` and `change` with `{ bubbles: true }`) ensuring compatibility with modern SPA frameworks (React, Angular, Vue).
- **`AutofillPromptBanner.kt`**:
  - Floating Ubuntu Yaru bottom banner presenting matching credentials when visiting known login domains with 1-tap autofill.
- **`SavePasswordDialog.kt`**:
  - Native confirmation dialog prompting users to encrypt and save newly entered credentials with visibility toggle and "Never for this site" options.
- **`PasswordManagerSheet.kt`**:
  - Master vault interface for searching, copying, adding, editing, and deleting saved credentials, with integrated password generator modal.

### 2.4 CLI & Media Processing Pipeline (`scripts/`)
- **`scripts/swordfish_media_engine.py`**: Python CLI utility bridging `yt-dlp`, `ffmpeg`, `aria2c`, `mediainfo`, and `AtomicParsley` for stream extraction, high-speed downloads, format conversions, and metadata inspection.

---

## 3. Ubuntu Orange & Yaru Design System

Swordfish implements the canonical Ubuntu visual identity:
- **Ubuntu Orange (`#E95420`)**: Primary interactive brand color used on active pills, buttons, and badges.
- **Ubuntu Dark Aubergine (`#2C001E`)**: Background gradient tone evoking the classic Ubuntu desktop shell.
- **Ubuntu Dark Surface (`#1E1E1E` & `#262626`)**: Yaru dark terminal background for sleek, readable contrast.
- **Ubuntu Success Green (`#38B44A`)**: Terminal green for verified security and active service states.
