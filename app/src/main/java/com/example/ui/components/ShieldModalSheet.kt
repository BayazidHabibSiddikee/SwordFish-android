package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserTab
import com.example.data.model.SearchEngine
import com.example.data.security.SecurityEngine
import com.example.data.security.SecurityThreatLevel
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.UbuntuDarkSurface
import com.example.ui.theme.UbuntuLightOrange
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuSuccessGreen
import com.example.ui.theme.UbuntuWarmGrey
import com.example.viewmodel.ActiveSheet
import com.example.viewmodel.BrowserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShieldModalSheet(
    tab: BrowserTab,
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val shieldStats by viewModel.shieldStats.collectAsState()
    val selectedSearchEngine by viewModel.selectedSearchEngine.collectAsState()
    val filterSettings by viewModel.filterSettings.collectAsState()
    val securityReport = remember(tab.url) { SecurityEngine.inspectUrl(tab.url) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = UbuntuDarkBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .background(UbuntuOrange.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, UbuntuOrange, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = UbuntuOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Swordfish Shield",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (shieldStats.isShieldEnabled) "JS & YouTube Protection Active" else "Shield Paused",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (shieldStats.isShieldEnabled) UbuntuOrange else Color.Gray
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Current Page Stats Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F3F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ON THIS PAGE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = UbuntuOrange
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${tab.adsBlockedOnPage}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = UbuntuOrange
                            )
                            Text(
                                text = "Ads Blocked",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${tab.trackersBlockedOnPage}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = UbuntuLightOrange
                            )
                            Text(
                                text = "Trackers Stopped",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dedicated YouTube Blocker Status
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = UbuntuAubergine.copy(alpha = 0.35f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, UbuntuOrange.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = Color(0xFFFF4500),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "YouTube AdBlocker (JS Engine)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Auto-skips pre-rolls, suppresses banner & feed ads",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbuntuWarmGrey,
                            fontSize = 12.sp
                        )
                    }
                    Surface(
                        color = UbuntuSuccessGreen,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ON",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // All-Time Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F3F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ALL-TIME STATS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatPill("Total Ads", "${shieldStats.totalAdsBlocked}", UbuntuOrange)
                        StatPill("Trackers", "${shieldStats.totalTrackersBlocked}", UbuntuLightOrange)
                        StatPill("Data Saved", "${shieldStats.totalDataSavedKb / 1024} MB", UbuntuSuccessGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECURITY SYSTEMS & PROTOCOLS
            Text(
                text = "SECURITY SYSTEMS & PROTOCOLS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = UbuntuOrange,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = UbuntuAubergine.copy(alpha = 0.35f)),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (securityReport.isSecure) UbuntuSuccessGreen.copy(alpha = 0.6f) else Color.Red
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("security_status_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (securityReport.isSecure) Icons.Default.GppGood else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (securityReport.isSecure) UbuntuSuccessGreen else Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (securityReport.isSecure) "Verified Safe Connection" else "Security Advisory",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = securityReport.protocol,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (securityReport.isSecure) UbuntuSuccessGreen else Color.Red
                                )
                            }
                        }

                        Surface(
                            color = if (securityReport.threatLevel == SecurityThreatLevel.SAFE) UbuntuSuccessGreen.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = securityReport.threatLevel.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (securityReport.threatLevel == SecurityThreatLevel.SAFE) UbuntuSuccessGreen else Color.Red,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = securityReport.threatDescription ?: "Active real-time security systems guarding web session.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Security Indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• HTTPS-Only Mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (filterSettings.security.httpsOnlyMode) UbuntuSuccessGreen else Color.Gray
                        )
                        Text(
                            text = if (filterSettings.security.httpsOnlyMode) "ENFORCED" else "DISABLED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (filterSettings.security.httpsOnlyMode) UbuntuSuccessGreen else Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• Phishing & Malware Shield",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (filterSettings.security.maliciousSiteShield) UbuntuSuccessGreen else Color.Gray
                        )
                        Text(
                            text = if (filterSettings.security.maliciousSiteShield) "ACTIVE" else "OFF",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (filterSettings.security.maliciousSiteShield) UbuntuSuccessGreen else Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• Strict 3rd-Party Cookies",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (filterSettings.security.strictThirdPartyCookies) UbuntuSuccessGreen else Color.Gray
                        )
                        Text(
                            text = if (filterSettings.security.strictThirdPartyCookies) "ISOLATED" else "STANDARD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (filterSettings.security.strictThirdPartyCookies) UbuntuSuccessGreen else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SECURITY & SHIELD CONTROLS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // HTTPS-Only Mode Toggle
            ShieldToggleRow(
                icon = Icons.Default.Lock,
                title = "HTTPS-Only Mode",
                subtitle = "Enforces encrypted HTTPS connections and automatically upgrades HTTP",
                checked = filterSettings.security.httpsOnlyMode,
                onCheckedChange = { viewModel.updateSecurityFilters { s -> s.copy(httpsOnlyMode = it) } }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Malicious Site Shield Toggle
            ShieldToggleRow(
                icon = Icons.Default.GppGood,
                title = "Malicious & Phishing Shield",
                subtitle = "Blocks suspicious hostnames, punycode attacks, and known scam sites",
                checked = filterSettings.security.maliciousSiteShield,
                onCheckedChange = { viewModel.updateSecurityFilters { s -> s.copy(maliciousSiteShield = it) } }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Toggle Master Shield
            ShieldToggleRow(
                icon = Icons.Default.Security,
                title = "Block Ads & Trackers",
                subtitle = "Intercepts ad networks, analytics, and telemetry requests",
                checked = shieldStats.isShieldEnabled,
                onCheckedChange = { viewModel.toggleShield(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Toggle JS MutationObserver Filter
            ShieldToggleRow(
                icon = Icons.Default.FilterAlt,
                title = "Advanced JS Mutation AdBlocker",
                subtitle = "Removes ad elements dynamically, kills anti-adblock modals",
                checked = shieldStats.isCosmeticBlockerEnabled,
                onCheckedChange = { viewModel.toggleCosmeticBlocker(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Desktop Site Mode Toggle
            ShieldToggleRow(
                icon = Icons.Default.DesktopWindows,
                title = "Desktop Site Mode",
                subtitle = "Requests desktop version of current webpage",
                checked = tab.isDesktopMode,
                onCheckedChange = { viewModel.toggleDesktopMode() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Configure Individual Filters Card (17 granular toggles)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = UbuntuAubergine.copy(alpha = 0.35f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, UbuntuOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        viewModel.openSettings()
                    }
                    .testTag("btn_configure_individual_filters")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .background(UbuntuOrange.copy(alpha = 0.25f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = UbuntuOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Configure Individual Filters",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "17 granular ad-block, tracking & distraction filters",
                                style = MaterialTheme.typography.bodySmall,
                                color = UbuntuWarmGrey,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Text(
                        text = "Customize ›",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = UbuntuOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Manage Blocklist Service URLs & Live Interception Log Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F3F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        viewModel.openSheet(ActiveSheet.BLOCKLIST_MANAGER)
                    }
                    .testTag("btn_open_blocklist_manager")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .background(UbuntuOrange.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = UbuntuOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Blocklist URLs & Live Log",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Manage blocked ad & tracking URLs, custom rules",
                                style = MaterialTheme.typography.bodySmall,
                                color = UbuntuWarmGrey,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Text(
                        text = "Manage ›",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = UbuntuOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "DEFAULT SEARCH ENGINE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // Search engine options
            SearchEngine.entries.forEach { engine ->
                val isSelected = engine == selectedSearchEngine
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) UbuntuOrange.copy(alpha = 0.2f) else UbuntuDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) UbuntuOrange else Color(0xFF3F3F3F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.setSearchEngine(engine) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = if (isSelected) UbuntuOrange else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = engine.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(UbuntuOrange, CircleShape)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ShieldToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F3F)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) UbuntuOrange else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = UbuntuOrange,
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color(0xFF3F3F3F)
                )
            )
        }
    }
}
