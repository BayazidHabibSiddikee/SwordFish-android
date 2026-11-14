# Swordfish Focus Shield (In-Built App & Website Blocker)

Swordfish includes a built-in distraction blocker to help users maintain deep focus and eliminate endless digital scrolling, backed by a **Room SQLite Database** and a **Background Usage Monitor Service**.

---

## 1. Feature Highlights

- **Background & Foreground Monitor Service (`FocusUsageMonitorService`)**:
  - Runs in the background with an ongoing low-priority notification showing active focus time remaining.
  - Automatically records active usage times (in seconds) for blacklisted apps and websites into the Room database.
- **Daily Usage Limits & Progress Tracking**:
  - Configurable daily limit (e.g., 20m for TikTok, 30m for Instagram, 45m for Reddit).
  - Dynamic progress bars visualize current usage versus the daily cap.
- **Categorized Blacklist (Apps & Websites)**:
  - **App Packages**: `com.zhiliaoapp.musically` (TikTok), `com.instagram.android`, `com.twitter.android`, `com.reddit.frontpage`.
  - **Web Domains**: `tiktok.com`, `instagram.com`, `x.com`, `reddit.com`, `twitch.tv`, `netflix.com`.
  - **Custom Packages & Domains**: Add any package or website with custom limits.
- **Pomodoro Focus Sessions**: Quick presets for 15, 25, 45, or 60-minute deep work sprints.
- **Active Navigation Interception**: Automatically blocks redirects and background navigation to restricted domains during active focus intervals.
- **Encouraging Interception Overlay**: Displays an inspiring productivity screen with real-time session countdown and safe return actions.

---

## 2. Request Interception Pipeline

When a user initiates navigation, the target URL is evaluated against the user's enabled blocklist:

```kotlin
val (isBlocked, domain) = AppBlockerEngine.checkBlocked(
    url = targetUrl,
    blockedDomains = enabledBlockedDomains,
    isFocusModeActive = isFocusModeActive
)

if (isBlocked) {
    // Records the blocked attempt in the Room database
    repository.recordBlockedOccurrence(domain ?: targetUrl)
    // Renders the BlockedOverlayView instead of loading webpage content
    updateActiveTab { it.copy(isBlockedByFocus = true, blockedDomain = domain) }
}
```

---

## 3. Background Usage Monitor Service (`FocusUsageMonitorService`)

The service runs continuously during active sessions to track target engagement:

```kotlin
class FocusUsageMonitorService : Service() {
    private fun startMonitoringLoop(focusMinutes: Int) {
        serviceScope.launch {
            var remainingSeconds = focusMinutes * 60
            while (isActive && _isServiceRunning.value) {
                delay(1000)
                _currentSessionActiveSeconds.value += 1
                if (remainingSeconds > 0) remainingSeconds--

                val currentTarget = _currentActiveTarget.value
                if (!currentTarget.isNullOrBlank()) {
                    repository.recordActiveUsage(currentTarget, 1)
                }

                if (_currentSessionActiveSeconds.value % 5 == 0L) {
                    val notification = buildNotification(remainingSeconds)
                    manager.notify(NOTIFICATION_ID, notification)
                }
            }
        }
    }
}
```

---

## 4. Room Database Storage & Persistence

All blacklisted packages and active usage records are persisted device-locally in SQLite:
- **`blocked_sites`**: Stores package names and web domains, display names, categories, and daily limits in minutes.
- **`usage_times`**: Stores date-stamped active usage duration (seconds) and daily block counts.
