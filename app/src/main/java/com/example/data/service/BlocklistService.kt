package com.example.data.service

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.data.model.BlockedRequestLog
import com.example.data.model.BlocklistFilterResult
import com.example.data.model.BlocklistRule
import com.example.data.model.BlocklistType
import com.example.data.model.FilterSettings
import com.example.data.model.MatchType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

class BlocklistService(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("swordfish_blocklist_prefs", Context.MODE_PRIVATE)

    private val _rules = MutableStateFlow<List<BlocklistRule>>(emptyList())
    val rules: StateFlow<List<BlocklistRule>> = _rules.asStateFlow()

    private val _blockedRequestsLog = MutableStateFlow<List<BlockedRequestLog>>(emptyList())
    val blockedRequestsLog: StateFlow<List<BlockedRequestLog>> = _blockedRequestsLog.asStateFlow()

    private val _totalAdsBlocked = MutableStateFlow(0)
    val totalAdsBlocked: StateFlow<Int> = _totalAdsBlocked.asStateFlow()

    private val _totalTrackersBlocked = MutableStateFlow(0)
    val totalTrackersBlocked: StateFlow<Int> = _totalTrackersBlocked.asStateFlow()

    init {
        loadRules()
    }

    private fun getDefaultRules(): List<BlocklistRule> {
        val list = mutableListOf<BlocklistRule>()

        // Common Advertising Domains (Domain Suffix Match)
        val defaultAdDomains = listOf(
            "doubleclick.net" to "Google DoubleClick ad server",
            "googleadservices.com" to "Google AdServices conversion tracker",
            "googlesyndication.com" to "Google AdSense syndication",
            "adservice.google.com" to "Google core ad delivery endpoint",
            "pagead2.googlesyndication.com" to "Google PageAd dynamic script",
            "adnxs.com" to "AppNexus programmatic ad exchange",
            "criteo.com" to "Criteo dynamic retargeting network",
            "criteo.net" to "Criteo dynamic ad asset host",
            "outbrain.com" to "Outbrain sponsored native content",
            "taboola.com" to "Taboola recommended content widget",
            "popads.net" to "PopAds aggressive popup ad network",
            "popcash.net" to "PopCash popunder ad server",
            "adcolony.com" to "AdColony mobile video ad SDK",
            "unityads.unity3d.com" to "Unity Ads game network",
            "applifier.com" to "Applifier video ads network",
            "vungle.com" to "Vungle mobile video advertisements",
            "chartboost.com" to "Chartboost in-app ads engine",
            "scorecardresearch.com" to "ScorecardResearch audience tracker",
            "moatads.com" to "Moat ad verification and metrics",
            "rubiconproject.com" to "Rubicon Project programmatic ad exchange",
            "openx.net" to "OpenX programmatic supply exchange",
            "pubmatic.com" to "PubMatic digital advertising platform",
            "casalemedia.com" to "Casale Media / Index Exchange ads",
            "bidswitch.net" to "BidSwitch media trading router",
            "advertising.com" to "AOL Advertising network",
            "adroll.com" to "AdRoll cross-device retargeting",
            "smartadserver.com" to "Smart AdServer media publisher platform",
            "adtechus.com" to "AdTech digital ad server",
            "serving-sys.com" to "Sizmek digital campaign delivery",
            "zedo.com" to "Zedo targeted popup and banner ad server",
            "revcontent.com" to "Revcontent content recommendation ads",
            "mgid.com" to "MGID native advertising widget",
            "exoclick.com" to "ExoClick display and popup ad network",
            "propellerads.com" to "PropellerAds push and popunder network",
            "trafficjunky.com" to "TrafficJunky banner network",
            "amazon-adsystem.com" to "Amazon Advertising server",
            "adform.net" to "Adform media buy exchange",
            "bidvertiser.com" to "BidVertiser PPC ad network",
            "infolinks.com" to "Infolinks in-text advertisement injector",
            "buysellads.com" to "BuySellAds banner marketplace",
            "yandexadexchange.net" to "Yandex direct advertising exchange",
            "aniview.com" to "Aniview video ad player platform",
            "applovin.com" to "AppLovin monetization network",
            "admob.com" to "Google AdMob mobile ad platform",
            "adblade.com" to "Adblade paid content placement",
            "adsterra.com" to "Adsterra popunder and banner network",
            "richaudience.com" to "RichAudience rich media ad server",
            "sharethrough.com" to "Sharethrough omnichannel native ads",
            "yieldmo.com" to "Yieldmo high-impact mobile ad exchange",
            "sovrn.com" to "Sovrn programmatic ad monetization",
            "sonobi.com" to "Sonobi media marketplace",
            "triplelift.com" to "TripleLift native ad exchange",
            "innovid.com" to "Innovid connected TV and video ads",
            "exponential.com" to "Exponential interactive display ads"
        )

        for ((domain, desc) in defaultAdDomains) {
            list.add(
                BlocklistRule(
                    id = "ad_domain_${domain.replace('.', '_')}",
                    pattern = domain,
                    type = BlocklistType.ADVERTISING,
                    matchType = MatchType.DOMAIN_SUFFIX,
                    isEnabled = true,
                    isCustom = false,
                    description = desc
                )
            )
        }

        // Common Advertising Keyword & Path Patterns
        val defaultAdKeywords = listOf(
            "/pagead/" to "Google PageAd path",
            "/ads/banner" to "Standard ad banner path",
            "/ads/display" to "Display ad serving path",
            "/adserver/" to "Generic adserver directory",
            "/adclick" to "Ad click conversion redirect",
            "ads.min.js" to "Common ad script asset",
            "show_ads.js" to "Legacy Google AdSense script",
            "/api/stats/ads" to "YouTube ad telemetry API",
            "/pagead/parallelverify" to "Google parallel verification beacon",
            "googleads.g.doubleclick.net" to "Google unified ad router"
        )

        for ((kw, desc) in defaultAdKeywords) {
            list.add(
                BlocklistRule(
                    id = "ad_kw_${kw.replace('/', '_').replace('.', '_')}",
                    pattern = kw,
                    type = BlocklistType.ADVERTISING,
                    matchType = MatchType.CONTAINS_KEYWORD,
                    isEnabled = true,
                    isCustom = false,
                    description = desc
                )
            )
        }

        // Common Tracking & Telemetry Domains (Domain Suffix Match)
        val defaultTrackingDomains = listOf(
            "google-analytics.com" to "Google Universal Analytics collection endpoint",
            "googletagmanager.com" to "Google Tag Manager container server",
            "googletagservices.com" to "Google Tag Services JavaScript host",
            "hotjar.com" to "Hotjar heatmaps and user session recording",
            "segment.io" to "Segment customer data platform telemetry",
            "mixpanel.com" to "Mixpanel user behavior tracking API",
            "connect.facebook.net" to "Meta Facebook Pixel & SDK telemetry",
            "flurry.com" to "Yahoo Flurry mobile analytics beacon",
            "branch.io" to "Branch deep-linking and attribution tracker",
            "appsflyer.com" to "AppsFlyer mobile attribution and tracking",
            "adjust.com" to "Adjust app install and event tracker",
            "quantserve.com" to "Quantcast demographic profiling beacon",
            "clarity.ms" to "Microsoft Clarity session recordings & telemetry",
            "statcounter.com" to "Statcounter web traffic tracker",
            "mouseflow.com" to "Mouseflow live session recording tracker",
            "crazyegg.com" to "Crazy Egg click heatmap tracker",
            "newrelic.com" to "New Relic browser performance & tracing beacon",
            "fullstory.com" to "FullStory session replay telemetry",
            "optimizely.com" to "Optimizely A/B testing and experimentation beacon"
        )

        for ((domain, desc) in defaultTrackingDomains) {
            list.add(
                BlocklistRule(
                    id = "track_domain_${domain.replace('.', '_')}",
                    pattern = domain,
                    type = BlocklistType.TRACKING,
                    matchType = MatchType.DOMAIN_SUFFIX,
                    isEnabled = true,
                    isCustom = false,
                    description = desc
                )
            )
        }

        // Common Tracking Keyword Patterns
        val defaultTrackingKeywords = listOf(
            "/gtag/js" to "Google Global Site Tag loader",
            "/fbevents.js" to "Meta Facebook Pixel event script",
            "analytics.js" to "Generic client analytics library",
            "/telemetry" to "Browser / app telemetry beacon",
            "visitor-pixel" to "Tracking conversion pixel"
        )

        for ((kw, desc) in defaultTrackingKeywords) {
            list.add(
                BlocklistRule(
                    id = "track_kw_${kw.replace('/', '_').replace('.', '_')}",
                    pattern = kw,
                    type = BlocklistType.TRACKING,
                    matchType = MatchType.CONTAINS_KEYWORD,
                    isEnabled = true,
                    isCustom = false,
                    description = desc
                )
            )
        }

        return list
    }

    private fun loadRules() {
        val defaultRules = getDefaultRules()
        val customRulesJson = prefs.getString("custom_rules_json", null)
        val disabledRuleIds = prefs.getStringSet("disabled_rule_ids", emptySet()) ?: emptySet()

        val merged = mutableListOf<BlocklistRule>()

        // Apply disabled state to defaults
        for (rule in defaultRules) {
            val isEnabled = !disabledRuleIds.contains(rule.id)
            merged.add(rule.copy(isEnabled = isEnabled))
        }

        // Parse custom user rules
        if (!customRulesJson.isNullOrBlank()) {
            try {
                val array = JSONArray(customRulesJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id", UUID.randomUUID().toString())
                    val pattern = obj.getString("pattern")
                    val type = BlocklistType.valueOf(obj.optString("type", BlocklistType.ADVERTISING.name))
                    val matchType = MatchType.valueOf(obj.optString("matchType", MatchType.DOMAIN_SUFFIX.name))
                    val isEnabled = obj.optBoolean("isEnabled", true) && !disabledRuleIds.contains(id)
                    val description = obj.optString("description", "Custom user-defined rule")
                    val hitCount = obj.optInt("hitCount", 0)

                    merged.add(
                        BlocklistRule(
                            id = id,
                            pattern = pattern,
                            type = type,
                            matchType = matchType,
                            isEnabled = isEnabled,
                            isCustom = true,
                            description = description,
                            hitCount = hitCount
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        _rules.value = merged
    }

    private fun saveCustomRules() {
        val customRules = _rules.value.filter { it.isCustom }
        val array = JSONArray()
        for (rule in customRules) {
            val obj = JSONObject().apply {
                put("id", rule.id)
                put("pattern", rule.pattern)
                put("type", rule.type.name)
                put("matchType", rule.matchType.name)
                put("isEnabled", rule.isEnabled)
                put("description", rule.description)
                put("hitCount", rule.hitCount)
            }
            array.put(obj)
        }
        prefs.edit().putString("custom_rules_json", array.toString()).apply()
    }

    private fun saveDisabledRuleIds() {
        val disabledIds = _rules.value.filter { !it.isEnabled }.map { it.id }.toSet()
        prefs.edit().putStringSet("disabled_rule_ids", disabledIds).apply()
    }

    /**
     * Intercepts a network request URI against the managed blocklist of common advertising and tracking URLs.
     */
    fun shouldInterceptUrl(uri: Uri, filterSettings: FilterSettings? = null): BlocklistFilterResult {
        if (filterSettings != null && !filterSettings.isMasterShieldEnabled) {
            return BlocklistFilterResult(isBlocked = false)
        }

        val host = uri.host?.lowercase(Locale.ROOT) ?: return BlocklistFilterResult(isBlocked = false)
        val path = uri.path?.lowercase(Locale.ROOT) ?: ""
        val fullUrl = uri.toString().lowercase(Locale.ROOT)

        // Check if YouTube Ads are handled
        val isYtAd = host.contains("youtube.com") && (
            path.contains("/api/stats/ads") ||
            path.contains("/pagead/") ||
            fullUrl.contains("adformat=") ||
            fullUrl.contains("ad_type=") ||
            fullUrl.contains("ptracking")
        )

        if (isYtAd) {
            val ytFilterActive = filterSettings?.adBlock?.youtubeAdBlocking ?: true
            if (ytFilterActive) {
                recordHit("ad_kw__api_stats_ads", BlocklistType.ADVERTISING, uri.toString(), host)
                return BlocklistFilterResult(
                    isBlocked = true,
                    type = BlocklistType.ADVERTISING,
                    reason = "YouTube Ad Stream Interception"
                )
            }
        }

        val currentRules = _rules.value

        for (rule in currentRules) {
            if (!rule.isEnabled) continue

            // Granular category filter checks
            if (rule.type == BlocklistType.TRACKING) {
                val trackerFilterActive = filterSettings?.tracking?.analyticsTelemetryBlocking ?: true
                if (!trackerFilterActive) continue
            } else if (rule.type == BlocklistType.ADVERTISING) {
                val adFilterActive = filterSettings?.adBlock?.networkAdInterception ?: true
                if (!adFilterActive) continue
            }

            val pattern = rule.pattern.lowercase(Locale.ROOT)
            val matches = when (rule.matchType) {
                MatchType.DOMAIN_SUFFIX -> {
                    host == pattern || host.endsWith(".$pattern")
                }
                MatchType.CONTAINS_KEYWORD -> {
                    path.contains(pattern) || host.contains(pattern) || fullUrl.contains(pattern)
                }
                MatchType.EXACT_MATCH -> {
                    host == pattern || fullUrl == pattern
                }
            }

            if (matches) {
                recordHit(rule.id, rule.type, uri.toString(), host)
                return BlocklistFilterResult(
                    isBlocked = true,
                    matchedRule = rule,
                    type = rule.type,
                    reason = rule.description.ifBlank { "Blocked matching rule: ${rule.pattern}" }
                )
            }
        }

        return BlocklistFilterResult(isBlocked = false)
    }

    private fun recordHit(ruleId: String, type: BlocklistType, url: String, host: String) {
        val now = System.currentTimeMillis()

        // Update rule hit counter
        _rules.update { list ->
            list.map { rule ->
                if (rule.id == ruleId) {
                    rule.copy(
                        hitCount = rule.hitCount + 1,
                        lastBlockedTimestamp = now
                    )
                } else {
                    rule
                }
            }
        }

        // Update total counter
        when (type) {
            BlocklistType.ADVERTISING -> _totalAdsBlocked.update { it + 1 }
            BlocklistType.TRACKING -> _totalTrackersBlocked.update { it + 1 }
        }

        // Add to audit log (capped at 100 entries for efficiency)
        val logEntry = BlockedRequestLog(
            url = url,
            host = host,
            timestamp = now,
            type = type,
            rulePattern = ruleId
        )
        _blockedRequestsLog.update { current ->
            (listOf(logEntry) + current).take(100)
        }
    }

    /**
     * Adds a custom user-defined blocked URL pattern or domain to the blocklist.
     */
    fun addCustomRule(
        pattern: String,
        type: BlocklistType,
        isKeyword: Boolean = false,
        description: String = ""
    ): Boolean {
        val clean = pattern.trim().lowercase(Locale.ROOT)
            .removePrefix("http://")
            .removePrefix("https://")
            .removeSuffix("/")
        if (clean.isBlank()) return false

        // Check if rule already exists
        if (_rules.value.any { it.pattern.equals(clean, ignoreCase = true) }) {
            return false
        }

        val matchType = if (isKeyword || clean.contains("/")) MatchType.CONTAINS_KEYWORD else MatchType.DOMAIN_SUFFIX
        val newRule = BlocklistRule(
            id = "custom_${UUID.randomUUID().toString().take(8)}",
            pattern = clean,
            type = type,
            matchType = matchType,
            isEnabled = true,
            isCustom = true,
            description = description.ifBlank { "User custom rule for $clean" }
        )

        _rules.update { listOf(newRule) + it }
        saveCustomRules()
        return true
    }

    /**
     * Removes a custom user-defined rule.
     */
    fun removeRule(ruleId: String): Boolean {
        val target = _rules.value.find { it.id == ruleId } ?: return false
        if (!target.isCustom) return false

        _rules.update { it.filterNot { rule -> rule.id == ruleId } }
        saveCustomRules()
        return true
    }

    /**
     * Toggles an individual rule's enabled status.
     */
    fun toggleRule(ruleId: String, isEnabled: Boolean) {
        _rules.update { list ->
            list.map { rule ->
                if (rule.id == ruleId) rule.copy(isEnabled = isEnabled) else rule
            }
        }
        saveDisabledRuleIds()
        saveCustomRules()
    }

    /**
     * Enables or disables an entire category of rules.
     */
    fun setCategoryEnabled(type: BlocklistType, enabled: Boolean) {
        _rules.update { list ->
            list.map { rule ->
                if (rule.type == type) rule.copy(isEnabled = enabled) else rule
            }
        }
        saveDisabledRuleIds()
        saveCustomRules()
    }

    /**
     * Resets all rules to factory defaults.
     */
    fun resetToDefaults() {
        prefs.edit().remove("custom_rules_json").remove("disabled_rule_ids").apply()
        loadRules()
    }

    /**
     * Clears the live intercepted requests log.
     */
    fun clearLog() {
        _blockedRequestsLog.value = emptyList()
    }

    companion object {
        @Volatile
        private var instance: BlocklistService? = null

        fun getInstance(context: Context): BlocklistService {
            return instance ?: synchronized(this) {
                instance ?: BlocklistService(context.applicationContext).also { instance = it }
            }
        }
    }
}
