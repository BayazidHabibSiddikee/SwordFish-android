# Swordfish AdBlocking & JavaScript Bridge Engine

Swordfish implements a multi-layered ad blocking pipeline designed to block ads, scripts, analytics trackers, and video pre-rolls before they can compromise speed or privacy, integrated with a bidirectional **JavaScript Bridge Layer**.

---

## 1. Multi-Tier Architecture with JavaScript Bridge

```
                    Incoming HTTP / HTTPS Web Requests
                                    │
                                    ▼
       ┌────────────────────────────────────────────────────────┐
       │   Tier 1: Network-Layer Interception                   │
       │   `shouldInterceptRequest(view, request)`              │
       │   - Suffix Domain Lookup in AdBlockEngine.AD_DOMAINS   │
       │   - Path Heuristics (/pagead/, /ads/, /gtag/js)        │
       │   - Returns: WebResourceResponse (0-byte text/plain)   │
       └────────────────────────────┬───────────────────────────┘
                                    │
                                    ▼
       ┌────────────────────────────────────────────────────────┐
       │   Tier 2: JavaScript Bridge Layer                      │
       │   `SwordfishAdBlockBridge` (exposed as window.SwordfishBridge) │
       │   - `@JavascriptInterface` bi-directional contract    │
       │   - `getCustomBlockSelectors()`: injects custom rules  │
       │   - `reportAdBlocked(selector, count)`                 │
       │   - `reportVideoAdSkipped(durationSec)`                │
       │   - `reportPopupBlocked(url)`                          │
       │   - `reportAntiAdblockNeutralized(details)`            │
       └────────────────────────────┬───────────────────────────┘
                                    │
                                    ▼
       ┌────────────────────────────────────────────────────────┐
       │   Tier 3: In-Page DOM Filtering & YouTube Blocker      │
       │   - MutationObserver actively detaches ad nodes        │
       │   - Overrides `window.open` to trap click-jack popups  │
       │   - Auto-skips YouTube pre-rolls & suppresses feeds    │
       │   - Real-time event notifications sent to Bridge       │
       └────────────────────────────────────────────────────────┘
```

---

## 2. JavaScript Bridge Layer Details

### 2.1 The Native Bridge (`SwordfishAdBlockBridge.kt`)
The bridge is registered on the WebView instance:
```kotlin
webView.addJavascriptInterface(adBlockBridge, "SwordfishBridge")
```

When in-page scripts filter out ad elements or skip video ads, they call the bridge methods:
- **`reportAdBlocked(selector: String, count: Int)`**: Updates the active tab's blocked ads badge and adds to the bandwidth saved estimate on the Android UI thread.
- **`reportVideoAdSkipped(durationSec: Double)`**: Notifies the browser that a video ad segment was jumped over, adding ~3.5MB to bandwidth saved.
- **`reportPopupBlocked(url: String)`**: Records intercepted popup windows.
- **`reportAntiAdblockNeutralized(details: String)`**: Neutralizes anti-adblock overlay modals and restores page scrollability.
- **`getCustomBlockSelectors()`**: Exposes dynamic CSS selector blocklists to in-page JavaScript.

### 2.2 Lifecycle & Memory Safety
When the Composable tab is destroyed or switched, `BrowserWebView` safely removes the bridge:
```kotlin
onDispose {
    webView.removeJavascriptInterface("SwordfishBridge")
}
```

---

## 3. Tier 1: Low-Level Network Interception

Intercepts all HTTP/HTTPS requests directly in `WebViewClient.shouldInterceptRequest`:
```kotlin
override fun shouldInterceptRequest(
    view: WebView?,
    request: WebResourceRequest?
): WebResourceResponse? {
    val uri = request?.url ?: return super.shouldInterceptRequest(view, request)
    if (viewModel.shieldStats.value.isShieldEnabled && AdBlockEngine.isAdOrTracker(uri)) {
        mainHandler.post { viewModel.onAdOrTrackerBlocked(isTracker) }
        return AdBlockEngine.createEmptyResponse()
    }
    return super.shouldInterceptRequest(view, request)
}
```

---

## 4. Tier 3: Dedicated YouTube Ad Blocker

Optimized specifically for mobile and desktop YouTube (`m.youtube.com`, `www.youtube.com`):

1. **Video Pre-Roll & Mid-Roll Ad Skipping**:
   When `.ad-showing` or `.ytp-ad-player-overlay` appears on the video player, Swordfish mutes audio, accelerates playback rate, and jumps to `video.duration`, notifying `window.SwordfishBridge.reportVideoAdSkipped(...)`.
2. **Automated Skip Button Clicking**:
   Monitors `.ytp-ad-skip-button`, `.ytp-skip-ad-button`, and `.videoAdUiSkipButton` and clicks them immediately upon rendering.
3. **Feed & Banner Ads Suppression**:
   Removes `ytd-promoted-video-renderer`, `ytm-promoted-sparkles-web-renderer`, `#masthead-ad`, and dialog upsells.
