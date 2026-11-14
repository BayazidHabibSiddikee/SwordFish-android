package com.example.data.blocker

import android.net.Uri
import com.example.data.model.FilterSettings

object AppBlockerEngine {

    /**
     * Checks if a target URL matches any blocked domain from the user's blacklist
     * or is filtered by active distraction settings (such as Short-Form Video Blocker).
     * Returns Pair(isBlocked, matchedDomain)
     */
    fun checkBlocked(
        url: String,
        blockedDomains: List<String>,
        isFocusModeActive: Boolean,
        filterSettings: FilterSettings? = null
    ): Pair<Boolean, String?> {
        val uri = try {
            Uri.parse(url)
        } catch (_: Exception) {
            return Pair(false, null)
        }

        val host = uri.host?.lowercase() ?: return Pair(false, null)

        // 1. Short-Form Video Filter check
        val shortFormBlocked = filterSettings?.distraction?.shortFormVideoBlocker ?: false
        if (shortFormBlocked && isShortFormVideo(url)) {
            return Pair(true, "Short-Form Video Filter")
        }

        // 2. Focus Shield / Domain Blacklist check
        val focusEnforcementActive = filterSettings?.distraction?.focusShieldEnforcement ?: true
        if (!isFocusModeActive || !focusEnforcementActive) {
            if (blockedDomains.isEmpty() || !isFocusModeActive) {
                return Pair(false, null)
            }
        }

        for (domain in blockedDomains) {
            val cleanDomain = domain.trim().lowercase().removePrefix("https://").removePrefix("http://").removePrefix("www.")
            if (cleanDomain.isBlank()) continue

            if (host == cleanDomain || host.endsWith(".$cleanDomain")) {
                return Pair(true, cleanDomain)
            }
        }

        return Pair(false, null)
    }

    /**
     * Detects addictive short-form video paths (TikTok, Instagram Reels, YouTube Shorts).
     */
    fun isShortFormVideo(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("tiktok.com") ||
                lower.contains("/shorts") ||
                lower.contains("/reels") ||
                lower.contains("/reel/")
    }

    fun formatSecondsToTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
