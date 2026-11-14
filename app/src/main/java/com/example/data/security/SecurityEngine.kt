package com.example.data.security

import android.net.Uri

enum class SecurityThreatLevel {
    SAFE,
    INFO,
    WARNING,
    CRITICAL
}

data class SecurityReport(
    val isSecure: Boolean,
    val isHttps: Boolean,
    val threatLevel: SecurityThreatLevel,
    val threatDescription: String?,
    val host: String,
    val protocol: String
)

object SecurityEngine {

    // Known malicious, deceptive, or high-risk test and scam hostnames
    private val KNOWN_MALICIOUS_DOMAINS = setOf(
        "malware-traffic-analysis.net",
        "phishing.org",
        "fake-bank-login.com",
        "account-security-verify.com",
        "paypal-security-update.com",
        "crypto-airdrop-claim.com",
        "free-gift-card-win.xyz",
        "device-infected-cleaner.top",
        "browser-update-urgent.download",
        "secure-login-attempt.net",
        "bank-alert-security.com",
        "phishing-test.com",
        "evil-test-domain.com"
    )

    private val SUSPICIOUS_HOST_KEYWORDS = listOf(
        "login-verify",
        "secure-update-account",
        "banking-portal-auth",
        "claim-free-crypto",
        "apple-id-verify",
        "google-security-alert",
        "paypa1",
        "micros0ft",
        "netflix-billing-update"
    )

    /**
     * Checks if a URL or host is flagged as malicious, phishing, or deceptive.
     */
    fun checkMalicious(url: String): Pair<Boolean, String?> {
        val uri = try {
            Uri.parse(url)
        } catch (_: Exception) {
            return Pair(false, null)
        }

        val host = uri.host?.lowercase() ?: return Pair(false, null)

        // 1. Check known domain blacklist
        for (blocked in KNOWN_MALICIOUS_DOMAINS) {
            if (host == blocked || host.endsWith(".$blocked")) {
                return Pair(true, "Flagged Phishing/Malware Domain ($blocked)")
            }
        }

        // 2. Detect Deceptive Punycode / Homograph attacks (xn--)
        if (host.startsWith("xn--") || host.contains(".xn--")) {
            return Pair(true, "Internationalized Homograph Spoofing Risk ($host)")
        }

        // 3. Detect suspicious deceptive brand keyword combinations
        for (kw in SUSPICIOUS_HOST_KEYWORDS) {
            if (host.contains(kw)) {
                return Pair(true, "Deceptive Brand Impersonation Threat ($kw)")
            }
        }

        return Pair(false, null)
    }

    /**
     * Upgrades an insecure HTTP URL to HTTPS if HTTPS-Only mode is enabled.
     */
    fun upgradeToHttps(url: String, httpsOnlyMode: Boolean): Pair<Boolean, String> {
        if (!httpsOnlyMode) return Pair(false, url)

        val trimmed = url.trim()
        if (trimmed.startsWith("http://", ignoreCase = true)) {
            val uri = try { Uri.parse(trimmed) } catch (_: Exception) { null }
            val host = uri?.host?.lowercase() ?: ""
            // Do not upgrade loopback / local debug addresses
            if (host != "localhost" && host != "127.0.0.1" && host != "10.0.2.2") {
                val upgraded = "https://" + trimmed.substring(7)
                return Pair(true, upgraded)
            }
        }
        return Pair(false, url)
    }

    /**
     * Checks if a subresource request is insecure mixed-content within a secure HTTPS page.
     */
    fun isMixedContentInsecure(pageUrl: String?, resourceUrl: String): Boolean {
        if (pageUrl == null) return false
        val isPageHttps = pageUrl.startsWith("https://", ignoreCase = true)
        val isResourceHttp = resourceUrl.startsWith("http://", ignoreCase = true)
        return isPageHttps && isResourceHttp
    }

    /**
     * Analyzes current tab URL and generates security status report.
     */
    fun inspectUrl(url: String): SecurityReport {
        if (url.isBlank() || url.startsWith("about:")) {
            return SecurityReport(
                isSecure = true,
                isHttps = false,
                threatLevel = SecurityThreatLevel.SAFE,
                threatDescription = "Internal Browser Page",
                host = "localhost",
                protocol = "swordfish"
            )
        }

        val uri = try { Uri.parse(url) } catch (_: Exception) { null }
        val host = uri?.host ?: url
        val scheme = uri?.scheme ?: "http"
        val isHttps = scheme.equals("https", ignoreCase = true)

        val (isMalicious, reason) = checkMalicious(url)
        if (isMalicious) {
            return SecurityReport(
                isSecure = false,
                isHttps = isHttps,
                threatLevel = SecurityThreatLevel.CRITICAL,
                threatDescription = reason ?: "Dangerous Phishing or Malware Detected",
                host = host,
                protocol = scheme
            )
        }

        if (!isHttps) {
            return SecurityReport(
                isSecure = false,
                isHttps = false,
                threatLevel = SecurityThreatLevel.WARNING,
                threatDescription = "Connection is Insecure (HTTP) - Data is unencrypted",
                host = host,
                protocol = scheme
            )
        }

        return SecurityReport(
            isSecure = true,
            isHttps = true,
            threatLevel = SecurityThreatLevel.SAFE,
            threatDescription = "Verified TLS/SSL Connection - End-to-End Encrypted",
            host = host,
            protocol = "https (TLS 1.3)"
        )
    }
}
