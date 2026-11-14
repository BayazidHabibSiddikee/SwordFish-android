package com.example.data.blocker

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

/**
 * JavaScript Interface Bridge between web page scripts and the native Android host.
 * Exposed to in-page scripts via `window.SwordfishBridge`.
 * Handles adblocking telemetry as well as secure password manager autofill and form detection.
 */
class SwordfishAdBlockBridge(
    private val onAdRemoved: (selector: String, count: Int) -> Unit = { _, _ -> },
    private val onVideoAdSkipped: (durationSec: Double) -> Unit = { _ -> },
    private val onPopupBlocked: (url: String) -> Unit = { _ -> },
    private val onAntiAdblockNeutralized: (details: String) -> Unit = { _ -> },
    private val onLoginFormDetected: (domain: String, usernameSelector: String, passwordSelector: String) -> Unit = { _, _, _ -> },
    private val onPasswordFormSubmitted: (domain: String, username: String, pass: String) -> Unit = { _, _, _ -> },
    private val onReaderContentExtracted: (String) -> Unit = { _ -> },
    private val onReaderModeUnavailable: (String) -> Unit = { _ -> },
    private val onReaderAvailabilityChanged: (Boolean) -> Unit = { _ -> },
    private val onPageHtmlExtracted: (url: String, html: String) -> Unit = { _, _ -> },
    /** Provides the current page origin/URL for security gating. */
    private val currentUrlProvider: () -> String? = { null }
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun reportAdBlocked(selector: String, count: Int) {
        mainHandler.post {
            onAdRemoved(selector, count)
        }
    }

    @JavascriptInterface
    fun reportVideoAdSkipped(durationSec: Double) {
        mainHandler.post {
            onVideoAdSkipped(durationSec)
        }
    }

    @JavascriptInterface
    fun reportPopupBlocked(url: String) {
        mainHandler.post {
            onPopupBlocked(url)
        }
    }

    @JavascriptInterface
    fun reportAntiAdblockNeutralized(details: String) {
        mainHandler.post {
            onAntiAdblockNeutralized(details)
        }
    }

    @JavascriptInterface
    fun reportLoginFormDetected(domain: String, usernameSelector: String, passwordSelector: String) {
        if (!isSafeInput(domain, usernameSelector, passwordSelector)) return
        if (!originMatches(domain)) return
        mainHandler.post {
            onLoginFormDetected(domain, usernameSelector, passwordSelector)
        }
    }

    @JavascriptInterface
    fun reportPasswordFormSubmitted(domain: String, username: String, pass: String) {
        // Security: never accept oversized payloads; require origin match.
        if (pass.length > MAX_CREDENTIAL_LEN || username.length > MAX_CREDENTIAL_LEN || domain.length > MAX_DOMAIN_LEN) return
        if (!originMatches(domain)) return
        mainHandler.post {
            onPasswordFormSubmitted(domain, username, pass)
        }
    }

    @JavascriptInterface
    fun reportReaderContent(jsonStr: String) {
        mainHandler.post {
            onReaderContentExtracted(jsonStr)
        }
    }

    @JavascriptInterface
    fun reportReaderModeUnavailable(reason: String) {
        mainHandler.post {
            onReaderModeUnavailable(reason)
        }
    }

    @JavascriptInterface
    fun reportReaderAvailable(isAvailable: Boolean) {
        mainHandler.post {
            onReaderAvailabilityChanged(isAvailable)
        }
    }

    @JavascriptInterface
    fun reportPageHtml(url: String, html: String) {
        mainHandler.post {
            onPageHtmlExtracted(url, html)
        }
    }

    @JavascriptInterface
    fun getCustomBlockSelectors(): String {
        return """[
            ".adsbygoogle",
            "[id*='google_ads']",
            "[class*='ad-box']",
            "[class*='ad-banner']",
            "[class*='advertisement']",
            "[class*='ad-container']",
            "[class*='sponsor-box']",
            "iframe[src*='doubleclick']",
            "iframe[src*='adservice']",
            "iframe[src*='googlesyndication']",
            ".taboola",
            ".outbrain",
            ".trc_related_container",
            ".mgid-wrapper",
            "div[data-ad]",
            "div[data-ad-unit]",
            "div[id*='dfp-ad']",
            "div[class*='native-ad']",
            "ins.adsbygoogle",
            "aside[id*='ad']",
            "aside[class*='ad']",
            "div[class*='sponsor']",
            "div[id*='banner-ad']",
            "div[class*='banner-ad']",
            ".ad-slot",
            "[id*='ad-slot']",
            "[class*='adunit']",
            "[id*='adunit']"
        ]"""
    }

    companion object {
        private const val MAX_CREDENTIAL_LEN = 256
        private const val MAX_DOMAIN_LEN = 253
    }

    private fun originMatches(domain: String): Boolean {
        val current = currentUrlProvider() ?: return true
        return try {
            val host = java.net.URI(current).host ?: return true
            host.equals(domain, ignoreCase = true) || host.endsWith(".$domain", ignoreCase = true) || domain.endsWith(".$host", ignoreCase = true)
        } catch (e: Exception) {
            true
        }
    }

    private fun isSafeInput(vararg values: String): Boolean =
        values.all { it.length <= MAX_CREDENTIAL_LEN * 4 }

    @JavascriptInterface
    fun isShieldActive(): Boolean {
        return true
    }
}
