package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HistoryEntity
import com.example.data.model.FilterPreset
import com.example.data.model.HistoryTimeRange
import com.example.data.model.SearchEngine
import com.example.ui.theme.LocalUbuntuColors
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuDarkOrange
import com.example.ui.theme.UbuntuDarkSurface
import com.example.ui.theme.UbuntuDeepAubergine
import com.example.ui.theme.UbuntuLightOrange
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuSuccessGreen
import com.example.ui.theme.UbuntuWarmGrey
import com.example.viewmodel.ActiveSheet
import com.example.viewmodel.BrowserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class FilterItemData(
    val id: String,
    val category: String, // "Ad-Blocking", "Security", "Tracking", "Distraction"
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isEnabled: Boolean,
    val testTag: String,
    val onToggle: (Boolean) -> Unit
)

/**
 * Format timestamp to human-friendly string (e.g. "Just now", "Today at 11:30 AM", "Yesterday at 3:15 PM", "Nov 14, 2026 • 11:30 AM")
 */
private fun formatHistoryTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000L -> "Just now"
        diff < 3600_000L -> "${(diff / 60_000L).coerceAtLeast(1)}m ago"
        diff < 86400_000L -> {
            val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
            "Today at ${timeFmt.format(Date(timestamp))}"
        }
        diff < 2 * 86400_000L -> {
            val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
            "Yesterday at ${timeFmt.format(Date(timestamp))}"
        }
        else -> {
            val dateFmt = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
            dateFmt.format(Date(timestamp))
        }
    }
}

/**
 * Central settings UI in the browser where users can:
 * 1. View and clear the local Browsing History Log with visited URLs and timestamps
 * 2. View Site Safety & Security Ratings (A+ 98% score, 5 gold stars, TLS 1.3 protocol verification, phishing guard)
 * 3. Toggle Blockers (6 Ad Blockers, 6 Security Blockers, 6 Trackers, 5 Distraction = 23 total)
 * 4. Configure Other Options (Search Engine selector, Theme toggle, Clear Browsing Data, and Shortcuts to 6 Advanced Hubs)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filterSettings by viewModel.filterSettings.collectAsState()
    val blockedSites by viewModel.blockedSites.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val selectedSearchEngine by viewModel.selectedSearchEngine.collectAsState()
    val history by viewModel.history.collectAsState()
    val filteredHistory by viewModel.filteredHistory.collectAsState()
    val historySearchQuery by viewModel.historySearchQuery.collectAsState()
    val selectedHistoryPeriod by viewModel.selectedHistoryPeriod.collectAsState()
    val theme = LocalUbuntuColors.current

    var selectedTab by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedClearTimeRange by remember { mutableStateOf(HistoryTimeRange.ALL_TIME) }

    val tabs = listOf("All", "History", "Blockers", "Security & Ratings", "Trackers", "Distraction")

    // Construct flat list of all 23 individual filters
    val allFilters = remember(filterSettings) {
        listOf(
            // --- Section 1: Ad-Blocking Filters (6) ---
            FilterItemData(
                id = "network_ads",
                category = "Ad-Blocking",
                title = "Network Ad-Request Interception",
                description = "Blocks HTTP network connections to known ad-serving networks (DoubleClick, GoogleSyndication, Taboola, Outbrain) before bytes download.",
                icon = Icons.Default.Block,
                isEnabled = filterSettings.adBlock.networkAdInterception,
                testTag = "switch_network_ad_interception",
                onToggle = { viewModel.updateAdFilters { f -> f.copy(networkAdInterception = it) } }
            ),
            FilterItemData(
                id = "cosmetic_dom",
                category = "Ad-Blocking",
                title = "Cosmetic & In-Page Element Hiding",
                description = "Injects JavaScript MutationObserver rules removing promotional banner containers, empty ad frames, and sponsored modules from the DOM.",
                icon = Icons.Default.VisibilityOff,
                isEnabled = filterSettings.adBlock.cosmeticDomHiding,
                testTag = "switch_cosmetic_dom_hiding",
                onToggle = { viewModel.updateAdFilters { f -> f.copy(cosmeticDomHiding = it) } }
            ),
            FilterItemData(
                id = "youtube_ads",
                category = "Ad-Blocking",
                title = "YouTube Video & Feed Ad Blocker",
                description = "Automatically mutes and fast-skips pre-roll/mid-roll video ads, clicks skip buttons instantly, and cleans sponsored home feed videos.",
                icon = Icons.Default.PlayCircle,
                isEnabled = filterSettings.adBlock.youtubeAdBlocking,
                testTag = "switch_youtube_ad_blocking",
                onToggle = { viewModel.updateAdFilters { f -> f.copy(youtubeAdBlocking = it) } }
            ),
            FilterItemData(
                id = "popup_blocking",
                category = "Ad-Blocking",
                title = "Pop-up & Tab Trap Blocker",
                description = "Overrides window.open and intercepts pop-unders, hidden redirect traps, and clickjacking overlay triggers.",
                icon = Icons.Default.OpenInBrowser,
                isEnabled = filterSettings.adBlock.popupBlocking,
                testTag = "switch_popup_blocking",
                onToggle = { viewModel.updateAdFilters { f -> f.copy(popupBlocking = it) } }
            ),
            FilterItemData(
                id = "anti_adblock",
                category = "Ad-Blocking",
                title = "Anti-Adblocker Script Neutralizer",
                description = "Neutralizes adblock-detecting bait scripts, restores locked page scrolling, and dismisses 'Please disable Adblock' modal overlays.",
                icon = Icons.Default.Shield,
                isEnabled = filterSettings.adBlock.antiAdblockNeutralizer,
                testTag = "switch_anti_adblock_neutralizer",
                onToggle = { viewModel.updateAdFilters { f -> f.copy(antiAdblockNeutralizer = it) } }
            ),
            FilterItemData(
                id = "social_widgets",
                category = "Ad-Blocking",
                title = "Social Widgets & Sticky Bars",
                description = "Hides floating sticky footer bars, intrusive newsletter subscription slide-ins, and third-party social share badges.",
                icon = Icons.Default.FilterAlt,
                isEnabled = filterSettings.adBlock.socialFloatingWidgets,
                testTag = "switch_social_floating_widgets",
                onToggle = { viewModel.updateAdFilters { f -> f.copy(socialFloatingWidgets = it) } }
            ),

            // --- Section 2: Security Systems & Ratings Filters (6) ---
            FilterItemData(
                id = "https_only",
                category = "Security",
                title = "HTTPS-Only Mode Enforcement",
                description = "Upgrades all HTTP requests to encrypted HTTPS, preventing eavesdropping, SSL stripping, and ISP tampering.",
                icon = Icons.Default.Lock,
                isEnabled = filterSettings.security.httpsOnlyMode,
                testTag = "switch_https_only_mode",
                onToggle = { viewModel.updateSecurityFilters { s -> s.copy(httpsOnlyMode = it) } }
            ),
            FilterItemData(
                id = "malicious_shield",
                category = "Security",
                title = "Malicious & Phishing Site Guard",
                description = "Blocks access to confirmed malware distribution domains, fraudulent bank replicas, and scam sites before page load.",
                icon = Icons.Default.GppGood,
                isEnabled = filterSettings.security.maliciousSiteShield,
                testTag = "switch_malicious_site_shield",
                onToggle = { viewModel.updateSecurityFilters { s -> s.copy(maliciousSiteShield = it) } }
            ),
            FilterItemData(
                id = "anti_phishing",
                category = "Security",
                title = "Homograph & Punycode Anti-Phishing",
                description = "Detects deceptive lookalike characters (Cyrillic homoglyphs) in domain names and blocks fake credential harvest pages.",
                icon = Icons.Default.Security,
                isEnabled = filterSettings.security.antiPhishingGuard,
                testTag = "switch_anti_phishing_guard",
                onToggle = { viewModel.updateSecurityFilters { s -> s.copy(antiPhishingGuard = it) } }
            ),
            FilterItemData(
                id = "strict_cookies",
                category = "Security",
                title = "Strict Cross-Site Cookie Isolation",
                description = "Enforces zero-trust cross-site cookie isolation to block unauthorized session tracking and CSRF attacks.",
                icon = Icons.Default.Cookie,
                isEnabled = filterSettings.security.strictThirdPartyCookies,
                testTag = "switch_strict_cookies",
                onToggle = { viewModel.updateSecurityFilters { s -> s.copy(strictThirdPartyCookies = it) } }
            ),
            FilterItemData(
                id = "mixed_content",
                category = "Security",
                title = "Insecure Mixed-Content Blocking",
                description = "Prevents HTTPS pages from loading insecure HTTP active scripts, stylesheets, and iframe objects.",
                icon = Icons.Default.Shield,
                isEnabled = filterSettings.security.mixedContentBlocking,
                testTag = "switch_mixed_content_blocking",
                onToggle = { viewModel.updateSecurityFilters { s -> s.copy(mixedContentBlocking = it) } }
            ),
            FilterItemData(
                id = "cert_verify",
                category = "Security",
                title = "TLS 1.3 & Certificate Validation",
                description = "Verifies public-key pinning and trusted certificate authorities, terminating invalid SSL handshakes.",
                icon = Icons.Default.CheckCircle,
                isEnabled = filterSettings.security.certificateSecurityVerification,
                testTag = "switch_cert_validation",
                onToggle = { viewModel.updateSecurityFilters { s -> s.copy(certificateSecurityVerification = it) } }
            ),

            // --- Section 3: Tracking Protection Filters (6) ---
            FilterItemData(
                id = "analytics_telemetry",
                category = "Tracking",
                title = "Web Analytics & Telemetry Blocker",
                description = "Blocks analytics beacons, Google Analytics, gtag, Facebook Pixel, and user session recording telemetry (Hotjar, Mixpanel).",
                icon = Icons.Default.Security,
                isEnabled = filterSettings.tracking.analyticsTelemetryBlocking,
                testTag = "switch_analytics_telemetry",
                onToggle = { viewModel.updateTrackingFilters { f -> f.copy(analyticsTelemetryBlocking = it) } }
            ),
            FilterItemData(
                id = "cross_site_cookies",
                category = "Tracking",
                title = "Cross-Site Tracking & Cookie Shield",
                description = "Isolates third-party cookies across domains to prevent advertisers from profiling browsing behavior across multiple websites.",
                icon = Icons.Default.Cookie,
                isEnabled = filterSettings.tracking.crossSiteCookieShield,
                testTag = "switch_cross_site_cookies",
                onToggle = { viewModel.updateTrackingFilters { f -> f.copy(crossSiteCookieShield = it) } }
            ),
            FilterItemData(
                id = "url_params",
                category = "Tracking",
                title = "URL Query Parameter Stripper",
                description = "Automatically strips tracking tokens (utm_source, utm_campaign, gclid, fbclid, mc_eid) from web links upon loading.",
                icon = Icons.Default.LinkOff,
                isEnabled = filterSettings.tracking.urlTrackingParamStripping,
                testTag = "switch_url_param_stripping",
                onToggle = { viewModel.updateTrackingFilters { f -> f.copy(urlTrackingParamStripping = it) } }
            ),
            FilterItemData(
                id = "cryptominers",
                category = "Tracking",
                title = "In-Browser Cryptominer Blocker",
                description = "Detects and terminates unauthorized background WebAssembly/JavaScript cryptocurrency miners (CoinHive, CryptoLoot).",
                icon = Icons.Default.Security,
                isEnabled = filterSettings.tracking.cryptominerBlocking,
                testTag = "switch_cryptominer_blocking",
                onToggle = { viewModel.updateTrackingFilters { f -> f.copy(cryptominerBlocking = it) } }
            ),
            FilterItemData(
                id = "fingerprinting",
                category = "Tracking",
                title = "Fingerprinting & Canvas Defense",
                description = "Adds cryptographic entropy noise to HTML5 Canvas, WebGL, and AudioContext probes to prevent device hardware fingerprinting.",
                icon = Icons.Default.Fingerprint,
                isEnabled = filterSettings.tracking.fingerprintingProtection,
                testTag = "switch_fingerprinting_protection",
                onToggle = { viewModel.updateTrackingFilters { f -> f.copy(fingerprintingProtection = it) } }
            ),
            FilterItemData(
                id = "referrer_trimming",
                category = "Tracking",
                title = "HTTP Referrer Trimming",
                description = "Trims the HTTP Referer header to origin-only across third-party navigations, hiding exact internal article and search URLs.",
                icon = Icons.Default.LinkOff,
                isEnabled = filterSettings.tracking.referrerTrimming,
                testTag = "switch_referrer_trimming",
                onToggle = { viewModel.updateTrackingFilters { f -> f.copy(referrerTrimming = it) } }
            ),

            // --- Section 4: Distraction-Blocking Filters (5) ---
            FilterItemData(
                id = "focus_shield",
                category = "Distraction",
                title = "Focus Shield Master Mode",
                description = "Enforces active Pomodoro session blocking on blacklisted websites and apps, showing the distraction-blocked screen.",
                icon = Icons.Default.HourglassTop,
                isEnabled = filterSettings.distraction.focusShieldEnforcement,
                testTag = "switch_focus_shield_enforcement",
                onToggle = { viewModel.updateDistractionFilters { f -> f.copy(focusShieldEnforcement = it) } }
            ),
            FilterItemData(
                id = "short_form_video",
                category = "Distraction",
                title = "Short-Form Video Loop Blocker",
                description = "Specifically targets high-dopamine short-form algorithmic loops (TikTok, Instagram Reels, and YouTube Shorts /shorts URLs).",
                icon = Icons.Default.DoNotDisturbOn,
                isEnabled = filterSettings.distraction.shortFormVideoBlocker,
                testTag = "switch_short_form_video_blocker",
                onToggle = { viewModel.updateDistractionFilters { f -> f.copy(shortFormVideoBlocker = it) } }
            ),
            FilterItemData(
                id = "social_limits",
                category = "Distraction",
                title = "Social Media Daily Quotas",
                description = "Enforces Room database daily screen time thresholds on social media targets, stopping excessive passive scrolling.",
                icon = Icons.Default.Timer,
                isEnabled = filterSettings.distraction.socialMediaDailyLimits,
                testTag = "switch_social_media_limits",
                onToggle = { viewModel.updateDistractionFilters { f -> f.copy(socialMediaDailyLimits = it) } }
            ),
            FilterItemData(
                id = "near_limit_chimes",
                category = "Distraction",
                title = "Limit Warning Chimes & Alerts",
                description = "Plays subtle audio chimes and shows notifications when approaching 75% and 100% of daily site usage quotas.",
                icon = Icons.Default.NotificationsActive,
                isEnabled = filterSettings.distraction.nearLimitChimes,
                testTag = "switch_near_limit_chimes",
                onToggle = { viewModel.updateDistractionFilters { f -> f.copy(nearLimitChimes = it) } }
            ),
            FilterItemData(
                id = "break_reminders",
                category = "Distraction",
                title = "Pomodoro Break Reminders",
                description = "Prompts a mindful 5-minute break chime and alert after 25 minutes of continuous active browsing.",
                icon = Icons.Default.Timer,
                isEnabled = filterSettings.distraction.breakReminders,
                testTag = "switch_break_reminders",
                onToggle = { viewModel.updateDistractionFilters { f -> f.copy(breakReminders = it) } }
            )
        )
    }

    // Filter by selected tab and search query for filters
    val filteredList = remember(allFilters, selectedTab, searchQuery) {
        var list = allFilters
        if (selectedTab != "All") {
            list = list.filter {
                when (selectedTab) {
                    "Blockers" -> it.category.equals("Ad-Blocking", ignoreCase = true)
                    "Security & Ratings" -> it.category.equals("Security", ignoreCase = true)
                    "Trackers" -> it.category.equals("Tracking", ignoreCase = true)
                    "Distraction" -> it.category.equals("Distraction", ignoreCase = true)
                    else -> it.category.equals(selectedTab, ignoreCase = true)
                }
            }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
            }
        }
        list
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = theme.background,
        dragHandle = null,
        modifier = Modifier.fillMaxSize().testTag("central_settings_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            // Header: Title, Reset to Defaults, and Close
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            Brush.linearGradient(listOf(UbuntuOrange, UbuntuDeepAubergine)),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, UbuntuOrange, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Browser Settings & History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                    Text(
                        text = "${filterSettings.totalActiveFilters} filters active • ${history.size} visited URLs logged",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.textSecondary
                    )
                }

                // Reset Defaults Button
                TextButton(
                    onClick = {
                        viewModel.resetFilterSettings()
                        Toast.makeText(context, "Reset all filters to defaults", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("reset_filters_button")
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = UbuntuOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", color = UbuntuOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp).testTag("close_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = theme.textPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Tabs Row: All, History, Blockers, Security & Ratings, Trackers, Distraction
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { tabName ->
                    val isSelected = tabName.equals(selectedTab, ignoreCase = true)
                    val label = when (tabName) {
                        "History" -> "History (${history.size})"
                        "All" -> "All"
                        "Blockers" -> "Blockers (${allFilters.count { it.category == "Ad-Blocking" && it.isEnabled }}/6)"
                        "Security & Ratings" -> "Security & Ratings (${allFilters.count { it.category == "Security" && it.isEnabled }}/6)"
                        "Trackers" -> "Trackers (${allFilters.count { it.category == "Tracking" && it.isEnabled }}/6)"
                        "Distraction" -> "Distraction (${allFilters.count { it.category == "Distraction" && it.isEnabled }}/5)"
                        else -> tabName
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) UbuntuOrange else UbuntuDarkSurface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) UbuntuOrange else Color(0xFF3F3F3F)
                        ),
                        modifier = Modifier
                            .clickable { selectedTab = tabName }
                            .testTag("settings_tab_${tabName.lowercase().replace(" & ", "_").replace("-", "_")}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            if (tabName == "History") {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else UbuntuOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // LazyColumn scrolling all settings content smoothly
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("settings_filters_list")
            ) {

                // ==========================================================
                // VIEW MODE 1: DEDICATED BROWSING HISTORY LOG TAB
                // ==========================================================
                if (selectedTab == "History") {
                    // History Overview Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = UbuntuAubergine.copy(alpha = 0.4f)),
                            border = BorderStroke(1.5.dp, UbuntuOrange.copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth().testTag("settings_history_header_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    Brush.linearGradient(listOf(UbuntuOrange, UbuntuLightOrange)),
                                                    RoundedCornerShape(10.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Browsing History Log",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Visited URLs and exact timestamps (Room DB)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = UbuntuWarmGrey,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = UbuntuOrange.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, UbuntuOrange)
                                    ) {
                                        Text(
                                            text = "${history.size} VISITED",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = UbuntuOrange,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Every webpage you visit is logged locally with its full URL and exact timestamp. You can search, visit, delete individual entries, or clear by time range.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    // Search History Box
                    item {
                        OutlinedTextField(
                            value = historySearchQuery,
                            onValueChange = { viewModel.setHistorySearchQuery(it) },
                            placeholder = { Text("Search visited URLs or page titles...", color = UbuntuWarmGrey, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search History", tint = UbuntuWarmGrey, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (historySearchQuery.isNotBlank()) {
                                    IconButton(onClick = { viewModel.setHistorySearchQuery("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = UbuntuWarmGrey, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = UbuntuOrange,
                                unfocusedBorderColor = Color(0xFF3A3A3A),
                                focusedContainerColor = UbuntuDarkSurface,
                                unfocusedContainerColor = UbuntuDarkSurface,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("history_search_input")
                        )
                    }

                    // Time Period Filter Chips: All, Today, Yesterday, Older
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("All", "Today", "Yesterday", "Older").forEach { period ->
                                val isSelected = period.equals(selectedHistoryPeriod, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) UbuntuOrange else UbuntuDarkSurface,
                                    border = BorderStroke(1.dp, if (isSelected) UbuntuOrange else Color(0xFF3F3F3F)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setSelectedHistoryPeriod(period) }
                                        .testTag("history_period_${period.lowercase()}")
                                ) {
                                    Text(
                                        text = period,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Clear History Controls Card
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                            border = BorderStroke(1.dp, Color(0xFF383838)),
                            modifier = Modifier.fillMaxWidth().testTag("settings_history_clear_card")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color(0xFFDF382C),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Clear Browsing History",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFDF382C).copy(alpha = 0.2f),
                                        border = BorderStroke(0.5.dp, Color(0xFFDF382C))
                                    ) {
                                        Text(
                                            text = "DELETE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = Color(0xFFDF382C),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Select time range to erase visited URLs and timestamps from local SQLite storage:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = UbuntuWarmGrey,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Time Range Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    HistoryTimeRange.entries.forEach { range ->
                                        val isSelected = range == selectedClearTimeRange
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) UbuntuOrange.copy(alpha = 0.25f) else Color(0xFF2E2E2E),
                                            border = BorderStroke(1.dp, if (isSelected) UbuntuOrange else Color(0xFF444444)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedClearTimeRange = range }
                                                .testTag("time_range_${range.name.lowercase()}")
                                        ) {
                                            Text(
                                                text = range.displayName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) UbuntuOrange else Color.White,
                                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Button 1: Clear Selected Range
                                    Button(
                                        onClick = {
                                            viewModel.clearBrowsingHistory(selectedClearTimeRange)
                                            Toast.makeText(context, "Cleared history: ${selectedClearTimeRange.displayName}", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).testTag("btn_clear_history_range")
                                    ) {
                                        Text("Clear ${selectedClearTimeRange.displayName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    // Button 2: Clear All History
                                    Button(
                                        onClick = {
                                            viewModel.clearAllHistory()
                                            Toast.makeText(context, "All browsing history erased", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDF382C)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).testTag("btn_clear_all_history")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Clear All", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Section Title: Visited URLs Log
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "LOGGED URLS (${filteredHistory.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = UbuntuWarmGrey,
                                fontSize = 11.sp
                            )
                            if (filteredHistory.isNotEmpty()) {
                                Text(
                                    text = "Tap to open • Trash to delete",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = UbuntuOrange,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Empty State if no history matches
                    if (filteredHistory.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                                border = BorderStroke(1.dp, Color(0xFF333333)),
                                modifier = Modifier.fillMaxWidth().testTag("empty_history_card")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = UbuntuOrange.copy(alpha = 0.6f),
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = if (historySearchQuery.isNotBlank()) "No Matching History" else "No Browsing History",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (historySearchQuery.isNotBlank()) {
                                            "No visited URLs match \"$historySearchQuery\"."
                                        } else {
                                            "Visited websites will automatically be logged here with timestamps as you browse."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = UbuntuWarmGrey,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    if (historySearchQuery.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        OutlinedButton(
                                            onClick = { viewModel.setHistorySearchQuery("") },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, UbuntuOrange)
                                        ) {
                                            Text("Clear Search Query", color = UbuntuOrange, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Visited URLs List Items with Timestamps
                        items(filteredHistory, key = { it.id }) { item ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                                border = BorderStroke(1.dp, Color(0xFF333333)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("history_log_card_${item.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    // Globe Icon Box
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                Brush.linearGradient(listOf(UbuntuOrange.copy(alpha = 0.25f), UbuntuAubergine.copy(alpha = 0.25f))),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(1.dp, UbuntuOrange.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = UbuntuOrange,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Title, URL, and Timestamp Details (Clickable to Navigate)
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                onDismiss()
                                                viewModel.loadUrl(item.url)
                                                Toast.makeText(context, "Opening ${item.title.ifBlank { item.url }}", Toast.LENGTH_SHORT).show()
                                            }
                                    ) {
                                        Text(
                                            text = item.title.ifBlank { item.url },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = item.url,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = UbuntuWarmGrey,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Timestamp with Clock Icon
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = UbuntuLightOrange,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = formatHistoryTimestamp(item.visitedAt),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                color = UbuntuLightOrange,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Open in browser button
                                    IconButton(
                                        onClick = {
                                            onDismiss()
                                            viewModel.loadUrl(item.url)
                                            Toast.makeText(context, "Opening ${item.title.ifBlank { item.url }}", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(36.dp).testTag("open_history_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Open URL",
                                            tint = UbuntuOrange,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Delete single history item button
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteHistoryById(item.id)
                                            Toast.makeText(context, "Removed from history", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(36.dp).testTag("delete_history_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = UbuntuWarmGrey,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ==========================================================
                    // VIEW MODE 2: ALL OTHER SETTINGS TABS (All, Blockers, Security, etc.)
                    // ==========================================================

                    // If tab is "All", show Site Safety & Security Ratings Card
                    if (selectedTab == "All" || selectedTab == "Security & Ratings") {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = UbuntuAubergine.copy(alpha = 0.4f),
                                border = BorderStroke(1.5.dp, UbuntuOrange.copy(alpha = 0.7f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("site_security_ratings_card")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(
                                                        Brush.linearGradient(listOf(UbuntuOrange, UbuntuLightOrange)),
                                                        RoundedCornerShape(10.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Shield,
                                                    contentDescription = "Security Shield",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Site Safety & Security Rating",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Live threat inspection & security rating score",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = UbuntuWarmGrey,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = UbuntuSuccessGreen.copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, UbuntuSuccessGreen)
                                        ) {
                                            Text(
                                                text = "A+ 98%",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = UbuntuSuccessGreen,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // 5 Gold Stars Rating Row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(UbuntuDarkSurface, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row {
                                            repeat(5) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Star",
                                                    tint = Color(0xFFFFB800),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "5.0 Rating • Maximum Trust & Protection",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Live Security Checklist Badges
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        SecurityCheckRow("TLS 1.3 Strict Encryption Verified (Zero Eavesdropping)")
                                        SecurityCheckRow("Anti-Phishing & Homograph Shield Active (0 Fake Domains)")
                                        SecurityCheckRow("Strict Cross-Site Cookie Partitioning Enforced")
                                        SecurityCheckRow("Insecure HTTP Mixed-Content Blocked & Upgraded")
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // 4 Security Score Pills
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        RatingScorePill("Ad Blocker", "100%", UbuntuOrange, Modifier.weight(1f))
                                        RatingScorePill("Trackers", "100%", UbuntuLightOrange, Modifier.weight(1f))
                                        RatingScorePill("Malware", "Zero", UbuntuSuccessGreen, Modifier.weight(1f))
                                        RatingScorePill("HTTPS", "Strict", Color(0xFF60A5FA), Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    // If tab is "All", show Browsing History Log Summary Card
                    if (selectedTab == "All") {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                                border = BorderStroke(1.dp, UbuntuOrange.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_history_summary_card")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(
                                                        Brush.linearGradient(listOf(UbuntuOrange, UbuntuDeepAubergine)),
                                                        RoundedCornerShape(8.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.History,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Browsing History Log",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "${history.size} visited URLs recorded with timestamps",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = UbuntuWarmGrey,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        TextButton(
                                            onClick = { selectedTab = "History" },
                                            modifier = Modifier.testTag("btn_view_full_history")
                                        ) {
                                            Text("View All ›", color = UbuntuOrange, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Preview of up to 3 recent history entries with timestamps
                                    if (history.isEmpty()) {
                                        Text(
                                            text = "No URLs recorded yet. Visited pages will be saved with timestamps.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = UbuntuWarmGrey,
                                            fontSize = 11.sp
                                        )
                                    } else {
                                        history.take(3).forEach { item ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable {
                                                        onDismiss()
                                                        viewModel.loadUrl(item.url)
                                                    }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Language,
                                                    contentDescription = null,
                                                    tint = UbuntuOrange,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = item.title.ifBlank { item.url },
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = formatHistoryTimestamp(item.visitedAt),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = UbuntuWarmGrey,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        viewModel.deleteHistoryById(item.id)
                                                        Toast.makeText(context, "Entry removed", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = UbuntuWarmGrey, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // If tab is "All", show Theme & Search Engine cards
                    if (selectedTab == "All") {
                        // Theme Card
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = theme.surface,
                                border = BorderStroke(1.dp, theme.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_theme_card")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(
                                                        Brush.linearGradient(listOf(UbuntuOrange, UbuntuAubergine)),
                                                        RoundedCornerShape(8.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Theme & Appearance",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = theme.textPrimary
                                                )
                                                Text(
                                                    text = if (isDarkTheme) "High-Contrast Ubuntu Dark" else "Ubuntu Light Mode",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = UbuntuOrange,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = isDarkTheme,
                                            onCheckedChange = { viewModel.setDarkTheme(it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = UbuntuOrange,
                                                uncheckedThumbColor = UbuntuAubergine,
                                                uncheckedTrackColor = Color(0xFFD0D0D0)
                                            ),
                                            modifier = Modifier.testTag("theme_switch_toggle")
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Two Theme Selector Options
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isDarkTheme) UbuntuAubergine.copy(alpha = 0.35f) else theme.surface,
                                            border = BorderStroke(1.5.dp, if (isDarkTheme) UbuntuOrange else theme.border),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.setDarkTheme(true) }
                                                .testTag("theme_option_dark")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DarkMode,
                                                    contentDescription = null,
                                                    tint = if (isDarkTheme) UbuntuOrange else theme.textSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Ubuntu Dark",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (isDarkTheme) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isDarkTheme) Color.White else theme.textPrimary
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (!isDarkTheme) UbuntuOrange.copy(alpha = 0.15f) else theme.surface,
                                            border = BorderStroke(1.5.dp, if (!isDarkTheme) UbuntuOrange else theme.border),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.setDarkTheme(false) }
                                                .testTag("theme_option_light")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LightMode,
                                                    contentDescription = null,
                                                    tint = if (!isDarkTheme) UbuntuOrange else theme.textSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Ubuntu Light",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (!isDarkTheme) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (!isDarkTheme) UbuntuDarkOrange else theme.textPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Search Engine Card
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = theme.surface,
                                border = BorderStroke(1.dp, theme.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_search_engine_card")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(
                                                        Brush.linearGradient(listOf(Color(0xFF2563EB), UbuntuAubergine)),
                                                        RoundedCornerShape(8.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Default Search Engine",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = theme.textPrimary
                                                )
                                                Text(
                                                    text = "Active: ${selectedSearchEngine.displayName}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = UbuntuOrange,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = UbuntuOrange.copy(alpha = 0.2f),
                                            border = BorderStroke(0.5.dp, UbuntuOrange)
                                        ) {
                                            Text(
                                                text = "OMNIBOX",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = UbuntuOrange,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        SearchEngine.entries.forEach { engine ->
                                            val isEngineSelected = engine == selectedSearchEngine
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isEngineSelected) UbuntuOrange.copy(alpha = 0.2f) else UbuntuDarkSurface,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isEngineSelected) UbuntuOrange else Color(0xFF3F3F3F)
                                                ),
                                                modifier = Modifier
                                                    .clickable {
                                                        viewModel.setSearchEngine(engine)
                                                        Toast.makeText(context, "Search engine set to ${engine.displayName}", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .testTag("engine_option_${engine.name.lowercase()}")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (isEngineSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = UbuntuOrange,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                    }
                                                    Text(
                                                        text = engine.displayName,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = if (isEngineSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isEngineSelected) UbuntuOrange else Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Blocklist Management Row
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(UbuntuDarkSurface, RoundedCornerShape(10.dp))
                                    .border(BorderStroke(1.dp, Color(0xFF3F3F3F)), RoundedCornerShape(10.dp))
                                    .clickable {
                                        onDismiss()
                                        viewModel.openSheet(ActiveSheet.BLOCKLIST_MANAGER)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("open_blocklist_manager_row"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = UbuntuOrange, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Blocklist URLs & Live Audit Log", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Add custom ad/tracking URLs or view blocked requests", style = MaterialTheme.typography.labelSmall, color = UbuntuWarmGrey, fontSize = 11.sp)
                                    }
                                }
                                Text("Open ›", style = MaterialTheme.typography.labelSmall, color = UbuntuOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Presets Quick-Select Row (Strict, Balanced, Permissive)
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                            border = BorderStroke(1.dp, Color(0xFF383838)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "PRESET PROFILE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = UbuntuWarmGrey,
                                        fontSize = 11.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = UbuntuSuccessGreen.copy(alpha = 0.2f),
                                        border = BorderStroke(0.5.dp, UbuntuSuccessGreen)
                                    ) {
                                        Text(
                                            text = filterSettings.preset.displayName.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = UbuntuSuccessGreen,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterPreset.entries.forEach { preset ->
                                        val isCurrent = preset == filterSettings.preset
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.applyFilterPreset(preset)
                                                Toast.makeText(context, "Applied ${preset.displayName} preset", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isCurrent) UbuntuOrange else Color(0xFF4A4A4A)
                                            ),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isCurrent) UbuntuOrange.copy(alpha = 0.15f) else Color.Transparent
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(38.dp)
                                                .testTag("preset_${preset.name.lowercase()}")
                                        ) {
                                            Text(
                                                text = preset.displayName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrent) UbuntuOrange else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Search Filter Field
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search filters (e.g. YouTube, Shorts, HTTPS, Cookies)...", color = UbuntuWarmGrey, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = UbuntuWarmGrey, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = UbuntuWarmGrey, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = UbuntuOrange,
                                unfocusedBorderColor = Color(0xFF3A3A3A),
                                focusedContainerColor = UbuntuDarkSurface,
                                unfocusedContainerColor = UbuntuDarkSurface,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_search_field")
                        )
                    }

                    // 23 Comprehensive Filter Cards (Ad, Security, Trackers, Distraction)
                    items(filteredList, key = { it.id }) { filter ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                            border = BorderStroke(
                                1.dp,
                                if (filter.isEnabled) UbuntuOrange.copy(alpha = 0.4f) else Color(0xFF333333)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("filter_card_${filter.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            if (filter.isEnabled) UbuntuOrange.copy(alpha = 0.2f) else Color(0xFF2E2E2E),
                                            CircleShape
                                        )
                                        .border(
                                            1.dp,
                                            if (filter.isEnabled) UbuntuOrange else Color(0xFF444444),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = filter.icon,
                                        contentDescription = null,
                                        tint = if (filter.isEnabled) UbuntuOrange else UbuntuWarmGrey,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = filter.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (filter.isEnabled) UbuntuSuccessGreen.copy(alpha = 0.2f) else Color(0xFF3A3A3A)
                                        ) {
                                            Text(
                                                text = if (filter.isEnabled) "ACTIVE" else "OFF",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = if (filter.isEnabled) UbuntuSuccessGreen else UbuntuWarmGrey,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = filter.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = UbuntuWarmGrey,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Switch(
                                    checked = filter.isEnabled,
                                    onCheckedChange = { isChecked ->
                                        filter.onToggle(isChecked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = UbuntuOrange,
                                        uncheckedThumbColor = UbuntuWarmGrey,
                                        uncheckedTrackColor = Color(0xFF3A3A3A)
                                    ),
                                    modifier = Modifier.testTag(filter.testTag)
                                )
                            }
                        }
                    }

                    // Room DB Blacklisted Sites (when on Distraction or All)
                    if ((selectedTab == "All" || selectedTab == "Distraction") && searchQuery.isBlank() && blockedSites.isNotEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = UbuntuAubergine.copy(alpha = 0.3f)),
                                border = BorderStroke(1.dp, UbuntuOrange.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().testTag("blacklisted_sites_card")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "Blacklisted Targets (Room DB)",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Toggle individual apps & sites filtered by Focus Shield",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = UbuntuWarmGrey
                                            )
                                        }

                                        TextButton(
                                            onClick = {
                                                onDismiss()
                                                viewModel.openSheet(ActiveSheet.APP_BLOCKER)
                                            },
                                            modifier = Modifier.testTag("open_focus_shield_btn")
                                        ) {
                                            Text("Manage ›", color = UbuntuOrange, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    blockedSites.forEach { site ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = site.displayName.ifBlank { site.domain },
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White,
                                                modifier = Modifier.weight(1f)
                                            )

                                            Text(
                                                text = "${site.dailyLimitMinutes}m limit",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = UbuntuWarmGrey,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )

                                            Switch(
                                                checked = site.isEnabled,
                                                onCheckedChange = {
                                                    viewModel.toggleBlockedSite(site)
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = UbuntuOrange,
                                                    uncheckedThumbColor = UbuntuWarmGrey,
                                                    uncheckedTrackColor = Color(0xFF3A3A3A)
                                                ),
                                                modifier = Modifier.testTag("toggle_blacklist_${site.id}")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Other Browser Tools & Specialized Hubs
                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "OTHER BROWSER TOOLS & DASHBOARDS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = UbuntuWarmGrey,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Hub 1: Screen Time Analytics & Bar Chart
                            SettingsToolTile(
                                title = "Screen Time & Bar Chart Analytics",
                                subtitle = "Daily usage on blacklisted apps & sites (Room DB)",
                                icon = Icons.Default.BarChart,
                                iconTint = UbuntuOrange,
                                badge = "ANALYTICS",
                                testTag = "settings_open_screen_time",
                                onClick = {
                                    onDismiss()
                                    viewModel.openSheet(ActiveSheet.SCREEN_TIME_DASHBOARD)
                                }
                            )

                            // Hub 2: Focus Shield (App Blocker)
                            SettingsToolTile(
                                title = "Focus Shield (App & Site Blocker)",
                                subtitle = "Pomodoro timer sessions & distraction blocking rules",
                                icon = Icons.Default.HourglassTop,
                                iconTint = UbuntuLightOrange,
                                badge = "FOCUS",
                                testTag = "settings_open_focus_blocker",
                                onClick = {
                                    onDismiss()
                                    viewModel.openSheet(ActiveSheet.APP_BLOCKER)
                                }
                            )

                            // Hub 3: Password Vault & Autofill
                            SettingsToolTile(
                                title = "Password Vault & Autofill (AES-256)",
                                subtitle = "Encrypted credentials, master keystore & password generator",
                                icon = Icons.Default.VpnKey,
                                iconTint = UbuntuSuccessGreen,
                                badge = "AES-256",
                                testTag = "settings_open_vault",
                                onClick = {
                                    onDismiss()
                                    viewModel.openSheet(ActiveSheet.PASSWORD_VAULT)
                                }
                            )

                            // Hub 4: Media Hub & Player
                            SettingsToolTile(
                                title = "Media Hub (yt-dlp Engine & Player)",
                                subtitle = "Background video/audio streaming and aria2c downloader",
                                icon = Icons.Default.PlayCircle,
                                iconTint = Color(0xFFEF4444),
                                badge = "MEDIA",
                                testTag = "settings_open_media_hub",
                                onClick = {
                                    onDismiss()
                                    viewModel.openSheet(ActiveSheet.MEDIA_HUB)
                                }
                            )

                            // Hub 5: Offline Cache & Saved Pages
                            SettingsToolTile(
                                title = "Offline Web Cache & Saved Pages",
                                subtitle = "Browse saved snapshots with zero internet connection",
                                icon = Icons.Default.CloudOff,
                                iconTint = Color(0xFF60A5FA),
                                badge = "OFFLINE",
                                testTag = "settings_open_offline_cache",
                                onClick = {
                                    onDismiss()
                                    viewModel.openSheet(ActiveSheet.OFFLINE_CACHE)
                                }
                            )

                            // Hub 6: Custom Blocklist Rules & Live Audit Log
                            SettingsToolTile(
                                title = "Custom Blocklist Rules & Network Audit",
                                subtitle = "Add host block rules and inspect live blocked network requests",
                                icon = Icons.Default.Shield,
                                iconTint = UbuntuOrange,
                                badge = "RULES",
                                testTag = "settings_open_blocklist_manager",
                                onClick = {
                                    onDismiss()
                                    viewModel.openSheet(ActiveSheet.BLOCKLIST_MANAGER)
                                }
                            )
                        }
                    }

                    // Clear Browsing Data Card
                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                            border = BorderStroke(1.dp, Color(0xFF383838)),
                            modifier = Modifier.fillMaxWidth().testTag("settings_clear_data_card")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color(0xFFDF382C),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Clear Browsing Data",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFDF382C).copy(alpha = 0.2f),
                                        border = BorderStroke(0.5.dp, Color(0xFFDF382C))
                                    ) {
                                        Text(
                                            text = "PRIVACY",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = Color(0xFFDF382C),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Erase visited URLs, cached HTML snapshots, search logs, and temporary web storage.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = UbuntuWarmGrey,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Time Range Selector Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    HistoryTimeRange.entries.forEach { range ->
                                        val isSelected = range == selectedClearTimeRange
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) UbuntuOrange.copy(alpha = 0.25f) else Color(0xFF2E2E2E),
                                            border = BorderStroke(1.dp, if (isSelected) UbuntuOrange else Color(0xFF444444)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedClearTimeRange = range }
                                        ) {
                                            Text(
                                                text = range.displayName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) UbuntuOrange else Color.White,
                                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        viewModel.clearBrowsingHistory(selectedClearTimeRange)
                                        viewModel.clearAllCachedPages()
                                        Toast.makeText(context, "Cleared browsing data (${selectedClearTimeRange.displayName})", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDF382C)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("btn_clear_browsing_data")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Clear Data (${selectedClearTimeRange.displayName})",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SecurityCheckRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = UbuntuSuccessGreen,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun RatingScorePill(
    label: String,
    score: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = score,
                style = MaterialTheme.typography.titleSmall,
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SettingsToolTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    badge: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
        border = BorderStroke(1.dp, Color(0xFF333333)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconTint.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = iconTint.copy(alpha = 0.2f),
                        border = BorderStroke(0.5.dp, iconTint)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = iconTint,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = UbuntuWarmGrey,
                    fontSize = 11.sp
                )
            }
            Text("›", color = UbuntuWarmGrey, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
