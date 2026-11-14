package com.example.data.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebViewDatabase

/**
 * IncognitoSessionManager handles private browsing session isolation and cleanup.
 *
 * When an Incognito tab is closed, or when the user exits Incognito mode:
 * 1. Session cookies are removed via CookieManager.removeSessionCookies.
 * 2. HTML5 WebStorage (localStorage, sessionStorage) is purged via WebStorage.deleteAllData().
 * 3. Saved passwords and form autofill data are purged via WebViewDatabase.
 * 4. In-memory cookie cache is flushed to prevent leaks.
 */
object IncognitoSessionManager {

    var sessionClearCount: Int = 0
        private set

    var lastSessionClearedTimestamp: Long = 0L
        private set

    /**
     * Clears all session cookies, DOM storage, and credentials associated with
     * the private browsing session.
     */
    fun clearSessionCookiesAndStorage(context: Context, onComplete: (() -> Unit)? = null) {
        sessionClearCount++
        lastSessionClearedTimestamp = System.currentTimeMillis()

        try {
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeSessionCookies {
                try {
                    cookieManager.flush()
                } catch (_: Exception) {}
                Handler(Looper.getMainLooper()).post {
                    onComplete?.invoke()
                }
            }
        } catch (_: Exception) {
            onComplete?.invoke()
        }

        try {
            WebStorage.getInstance().deleteAllData()
        } catch (_: Exception) {}

        try {
            val db = WebViewDatabase.getInstance(context)
            db.clearFormData()
            db.clearHttpAuthUsernamePassword()
        } catch (_: Exception) {}
    }

    /**
     * Helper for tests or resetting state.
     */
    fun resetStatsForTesting() {
        sessionClearCount = 0
        lastSessionClearedTimestamp = 0L
    }
}
