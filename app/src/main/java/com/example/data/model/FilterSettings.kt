package com.example.data.model

import org.json.JSONObject

/**
 * Granular individual filter configurations for Ad-Blocking, Tracking Protection,
 * and Distraction Management in the Swordfish browser.
 */
data class AdBlockFilters(
    val networkAdInterception: Boolean = true,
    val cosmeticDomHiding: Boolean = true,
    val youtubeAdBlocking: Boolean = true,
    val popupBlocking: Boolean = true,
    val antiAdblockNeutralizer: Boolean = true,
    val socialFloatingWidgets: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("networkAdInterception", networkAdInterception)
        put("cosmeticDomHiding", cosmeticDomHiding)
        put("youtubeAdBlocking", youtubeAdBlocking)
        put("popupBlocking", popupBlocking)
        put("antiAdblockNeutralizer", antiAdblockNeutralizer)
        put("socialFloatingWidgets", socialFloatingWidgets)
    }

    fun activeCount(): Int = listOf(
        networkAdInterception,
        cosmeticDomHiding,
        youtubeAdBlocking,
        popupBlocking,
        antiAdblockNeutralizer,
        socialFloatingWidgets
    ).count { it }

    companion object {
        fun fromJson(json: JSONObject): AdBlockFilters = AdBlockFilters(
            networkAdInterception = json.optBoolean("networkAdInterception", true),
            cosmeticDomHiding = json.optBoolean("cosmeticDomHiding", true),
            youtubeAdBlocking = json.optBoolean("youtubeAdBlocking", true),
            popupBlocking = json.optBoolean("popupBlocking", true),
            antiAdblockNeutralizer = json.optBoolean("antiAdblockNeutralizer", true),
            socialFloatingWidgets = json.optBoolean("socialFloatingWidgets", true)
        )
    }
}

data class TrackingFilters(
    val analyticsTelemetryBlocking: Boolean = true,
    val crossSiteCookieShield: Boolean = true,
    val urlTrackingParamStripping: Boolean = true,
    val cryptominerBlocking: Boolean = true,
    val fingerprintingProtection: Boolean = true,
    val referrerTrimming: Boolean = true
) {
    val analyticsTelemetryBlocker: Boolean get() = analyticsTelemetryBlocking
    val stripTrackingParameters: Boolean get() = urlTrackingParamStripping

    fun activeCount(): Int = listOf(
        analyticsTelemetryBlocking,
        crossSiteCookieShield,
        urlTrackingParamStripping,
        cryptominerBlocking,
        fingerprintingProtection,
        referrerTrimming
    ).count { it }

    fun toJson(): JSONObject = JSONObject().apply {
        put("analyticsTelemetryBlocking", analyticsTelemetryBlocking)
        put("crossSiteCookieShield", crossSiteCookieShield)
        put("urlTrackingParamStripping", urlTrackingParamStripping)
        put("cryptominerBlocking", cryptominerBlocking)
        put("fingerprintingProtection", fingerprintingProtection)
        put("referrerTrimming", referrerTrimming)
    }

    companion object {
        fun fromJson(json: JSONObject): TrackingFilters = TrackingFilters(
            analyticsTelemetryBlocking = json.optBoolean("analyticsTelemetryBlocking", true),
            crossSiteCookieShield = json.optBoolean("crossSiteCookieShield", true),
            urlTrackingParamStripping = json.optBoolean("urlTrackingParamStripping", true),
            cryptominerBlocking = json.optBoolean("cryptominerBlocking", true),
            fingerprintingProtection = json.optBoolean("fingerprintingProtection", true),
            referrerTrimming = json.optBoolean("referrerTrimming", true)
        )
    }
}

data class DistractionFilters(
    val focusShieldEnforcement: Boolean = true,
    val shortFormVideoBlocker: Boolean = true,
    val socialMediaDailyLimits: Boolean = true,
    val nearLimitChimes: Boolean = true,
    val breakReminders: Boolean = true
) {
    fun activeCount(): Int = listOf(
        focusShieldEnforcement,
        shortFormVideoBlocker,
        socialMediaDailyLimits,
        nearLimitChimes,
        breakReminders
    ).count { it }

    fun toJson(): JSONObject = JSONObject().apply {
        put("focusShieldEnforcement", focusShieldEnforcement)
        put("shortFormVideoBlocker", shortFormVideoBlocker)
        put("socialMediaDailyLimits", socialMediaDailyLimits)
        put("nearLimitChimes", nearLimitChimes)
        put("breakReminders", breakReminders)
    }

    companion object {
        fun fromJson(json: JSONObject): DistractionFilters = DistractionFilters(
            focusShieldEnforcement = json.optBoolean("focusShieldEnforcement", true),
            shortFormVideoBlocker = json.optBoolean("shortFormVideoBlocker", true),
            socialMediaDailyLimits = json.optBoolean("socialMediaDailyLimits", true),
            nearLimitChimes = json.optBoolean("nearLimitChimes", true),
            breakReminders = json.optBoolean("breakReminders", true)
        )
    }
}

data class SecurityFilters(
    val httpsOnlyMode: Boolean = true,
    val maliciousSiteShield: Boolean = true,
    val strictThirdPartyCookies: Boolean = true,
    val antiPhishingGuard: Boolean = true,
    val mixedContentBlocking: Boolean = true,
    val certificateSecurityVerification: Boolean = true
) {
    fun activeCount(): Int = listOf(
        httpsOnlyMode,
        maliciousSiteShield,
        strictThirdPartyCookies,
        antiPhishingGuard,
        mixedContentBlocking,
        certificateSecurityVerification
    ).count { it }

    fun toJson(): JSONObject = JSONObject().apply {
        put("httpsOnlyMode", httpsOnlyMode)
        put("maliciousSiteShield", maliciousSiteShield)
        put("strictThirdPartyCookies", strictThirdPartyCookies)
        put("antiPhishingGuard", antiPhishingGuard)
        put("mixedContentBlocking", mixedContentBlocking)
        put("certificateSecurityVerification", certificateSecurityVerification)
    }

    companion object {
        fun fromJson(json: JSONObject): SecurityFilters = SecurityFilters(
            httpsOnlyMode = json.optBoolean("httpsOnlyMode", true),
            maliciousSiteShield = json.optBoolean("maliciousSiteShield", true),
            strictThirdPartyCookies = json.optBoolean("strictThirdPartyCookies", true),
            antiPhishingGuard = json.optBoolean("antiPhishingGuard", true),
            mixedContentBlocking = json.optBoolean("mixedContentBlocking", true),
            certificateSecurityVerification = json.optBoolean("certificateSecurityVerification", true)
        )
    }
}

enum class FilterPreset(val displayName: String, val description: String) {
    STRICT("Strict", "Enforces all 23 security, ad-blocking, tracking, and focus filters for maximum privacy"),
    BALANCED("Balanced", "Default Ubuntu recommended profile balancing seamless browsing and protection"),
    PERMISSIVE("Permissive", "Minimal filtering allowing non-intrusive elements while maintaining core shields"),
    CUSTOM("Custom", "User customized filter configuration");

    fun createFilters(): FilterSettings = FilterSettings.createPreset(this)
}

data class FilterSettings(
    val isMasterShieldEnabled: Boolean = true,
    val preset: FilterPreset = FilterPreset.BALANCED,
    val adBlock: AdBlockFilters = AdBlockFilters(),
    val tracking: TrackingFilters = TrackingFilters(),
    val distraction: DistractionFilters = DistractionFilters(),
    val security: SecurityFilters = SecurityFilters()
) {
    val totalActiveFilters: Int
        get() = adBlock.activeCount() + tracking.activeCount() + distraction.activeCount()

    val totalFiltersCount: Int = 17

    val totalActiveWithSecurity: Int
        get() = totalActiveFilters + security.activeCount()

    fun totalActiveCount(): Int = totalActiveFilters

    fun toJsonString(): String = JSONObject().apply {
        put("isMasterShieldEnabled", isMasterShieldEnabled)
        put("preset", preset.name)
        put("adBlock", adBlock.toJson())
        put("tracking", tracking.toJson())
        put("distraction", distraction.toJson())
        put("security", security.toJson())
    }.toString()

    companion object {
        fun fromJsonString(jsonStr: String): FilterSettings {
            return try {
                val json = JSONObject(jsonStr)
                val presetName = json.optString("preset", FilterPreset.BALANCED.name)
                val parsedPreset = try { FilterPreset.valueOf(presetName) } catch (_: Exception) { FilterPreset.BALANCED }
                FilterSettings(
                    isMasterShieldEnabled = json.optBoolean("isMasterShieldEnabled", true),
                    preset = parsedPreset,
                    adBlock = json.optJSONObject("adBlock")?.let { AdBlockFilters.fromJson(it) } ?: AdBlockFilters(),
                    tracking = json.optJSONObject("tracking")?.let { TrackingFilters.fromJson(it) } ?: TrackingFilters(),
                    distraction = json.optJSONObject("distraction")?.let { DistractionFilters.fromJson(it) } ?: DistractionFilters(),
                    security = json.optJSONObject("security")?.let { SecurityFilters.fromJson(it) } ?: SecurityFilters()
                )
            } catch (e: Exception) {
                FilterSettings()
            }
        }

        fun createPreset(preset: FilterPreset): FilterSettings = when (preset) {
            FilterPreset.STRICT -> FilterSettings(
                isMasterShieldEnabled = true,
                preset = FilterPreset.STRICT,
                adBlock = AdBlockFilters(
                    networkAdInterception = true,
                    cosmeticDomHiding = true,
                    youtubeAdBlocking = true,
                    popupBlocking = true,
                    antiAdblockNeutralizer = true,
                    socialFloatingWidgets = true
                ),
                tracking = TrackingFilters(
                    analyticsTelemetryBlocking = true,
                    crossSiteCookieShield = true,
                    urlTrackingParamStripping = true,
                    cryptominerBlocking = true,
                    fingerprintingProtection = true,
                    referrerTrimming = true
                ),
                distraction = DistractionFilters(
                    focusShieldEnforcement = true,
                    shortFormVideoBlocker = true,
                    socialMediaDailyLimits = true,
                    nearLimitChimes = true,
                    breakReminders = true
                ),
                security = SecurityFilters(
                    httpsOnlyMode = true,
                    maliciousSiteShield = true,
                    strictThirdPartyCookies = true,
                    antiPhishingGuard = true,
                    mixedContentBlocking = true,
                    certificateSecurityVerification = true
                )
            )
            FilterPreset.BALANCED -> FilterSettings(
                isMasterShieldEnabled = true,
                preset = FilterPreset.BALANCED,
                adBlock = AdBlockFilters(
                    networkAdInterception = true,
                    cosmeticDomHiding = true,
                    youtubeAdBlocking = true,
                    popupBlocking = true,
                    antiAdblockNeutralizer = true,
                    socialFloatingWidgets = true
                ),
                tracking = TrackingFilters(
                    analyticsTelemetryBlocking = true,
                    crossSiteCookieShield = true,
                    urlTrackingParamStripping = true,
                    cryptominerBlocking = true,
                    fingerprintingProtection = false,
                    referrerTrimming = true
                ),
                distraction = DistractionFilters(
                    focusShieldEnforcement = true,
                    shortFormVideoBlocker = true,
                    socialMediaDailyLimits = true,
                    nearLimitChimes = true,
                    breakReminders = false
                ),
                security = SecurityFilters(
                    httpsOnlyMode = true,
                    maliciousSiteShield = true,
                    strictThirdPartyCookies = true,
                    antiPhishingGuard = true,
                    mixedContentBlocking = true,
                    certificateSecurityVerification = true
                )
            )
            FilterPreset.PERMISSIVE -> FilterSettings(
                isMasterShieldEnabled = true,
                preset = FilterPreset.PERMISSIVE,
                adBlock = AdBlockFilters(
                    networkAdInterception = true,
                    cosmeticDomHiding = false,
                    youtubeAdBlocking = true,
                    popupBlocking = true,
                    antiAdblockNeutralizer = false,
                    socialFloatingWidgets = false
                ),
                tracking = TrackingFilters(
                    analyticsTelemetryBlocking = true,
                    crossSiteCookieShield = false,
                    urlTrackingParamStripping = false,
                    cryptominerBlocking = true,
                    fingerprintingProtection = false,
                    referrerTrimming = false
                ),
                distraction = DistractionFilters(
                    focusShieldEnforcement = false,
                    shortFormVideoBlocker = false,
                    socialMediaDailyLimits = false,
                    nearLimitChimes = false,
                    breakReminders = false
                ),
                security = SecurityFilters(
                    httpsOnlyMode = false,
                    maliciousSiteShield = true,
                    strictThirdPartyCookies = false,
                    antiPhishingGuard = true,
                    mixedContentBlocking = false,
                    certificateSecurityVerification = true
                )
            )
            FilterPreset.CUSTOM -> FilterSettings(
                isMasterShieldEnabled = true,
                preset = FilterPreset.CUSTOM
            )
        }
    }
}
