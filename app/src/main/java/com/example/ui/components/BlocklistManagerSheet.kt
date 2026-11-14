package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlocklistRule
import com.example.data.model.BlocklistType
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuCardDark
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.UbuntuDarkSurface
import com.example.ui.theme.UbuntuLightOrange
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuSuccessGreen
import com.example.ui.theme.UbuntuWarmGrey
import com.example.viewmodel.BrowserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class BlocklistTab {
    RULES,
    AUDIT_LOG
}

private enum class RuleFilterCategory {
    ALL,
    ADS,
    TRACKING,
    CUSTOM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlocklistManagerSheet(
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rules by viewModel.blocklistRules.collectAsState()
    val auditLog by viewModel.blockedRequestsLog.collectAsState()
    val totalAdsBlocked by viewModel.totalAdsBlockedCount.collectAsState()
    val totalTrackersBlocked by viewModel.totalTrackersBlockedCount.collectAsState()

    var activeTab by remember { mutableStateOf(BlocklistTab.RULES) }
    var activeCategory by remember { mutableStateOf(RuleFilterCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddRuleDialog by remember { mutableStateOf(false) }

    val adRulesCount = rules.count { it.type == BlocklistType.ADVERTISING }
    val trackerRulesCount = rules.count { it.type == BlocklistType.TRACKING }
    val customRulesCount = rules.count { it.isCustom }

    val filteredRules = remember(rules, activeCategory, searchQuery) {
        rules.filter { rule ->
            val matchesCategory = when (activeCategory) {
                RuleFilterCategory.ALL -> true
                RuleFilterCategory.ADS -> rule.type == BlocklistType.ADVERTISING
                RuleFilterCategory.TRACKING -> rule.type == BlocklistType.TRACKING
                RuleFilterCategory.CUSTOM -> rule.isCustom
            }
            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase(Locale.ROOT)
                rule.pattern.lowercase(Locale.ROOT).contains(q) ||
                    rule.description.lowercase(Locale.ROOT).contains(q)
            }
            matchesCategory && matchesQuery
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = UbuntuDarkBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            // Pill Handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFF4A4A4A), CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .background(UbuntuOrange.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, UbuntuOrange.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = UbuntuOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Blocklist Service",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${rules.size} URLs & patterns monitored",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbuntuWarmGrey
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.resetBlocklistToDefaults() },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("reset_blocklist_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Blocklist to Defaults",
                            tint = UbuntuWarmGrey,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("close_blocklist_manager_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Blocklist Manager",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stat Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF333333))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Ad Rules", style = MaterialTheme.typography.labelSmall, color = UbuntuWarmGrey)
                        Text(
                            "$adRulesCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = UbuntuOrange
                        )
                        Text(
                            "$totalAdsBlocked blocked",
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuLightOrange,
                            fontSize = 10.sp
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF333333))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Trackers", style = MaterialTheme.typography.labelSmall, color = UbuntuWarmGrey)
                        Text(
                            "$trackerRulesCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE89EE1)
                        )
                        Text(
                            "$totalTrackersBlocked blocked",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE89EE1),
                            fontSize = 10.sp
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF333333))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Custom", style = MaterialTheme.typography.labelSmall, color = UbuntuWarmGrey)
                        Text(
                            "$customRulesCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = UbuntuSuccessGreen
                        )
                        Text(
                            "user rules",
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuWarmGrey,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Tab Switcher (Rules vs Audit Log) + Add Rule Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { activeTab = BlocklistTab.RULES },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == BlocklistTab.RULES) UbuntuOrange else UbuntuDarkSurface,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("tab_blocklist_rules")
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rules (${rules.size})", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { activeTab = BlocklistTab.AUDIT_LOG },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == BlocklistTab.AUDIT_LOG) UbuntuAubergine else UbuntuDarkSurface,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("tab_blocklist_audit_log")
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Live Log (${auditLog.size})", fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = { showAddRuleDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("add_custom_rule_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Rule", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (activeTab == BlocklistTab.RULES) {
                // Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(UbuntuDarkSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF3F3F3F), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = UbuntuWarmGrey,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                            cursorBrush = SolidColor(UbuntuOrange),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_rules_input"),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Search rules by domain or pattern...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = UbuntuWarmGrey.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                }
                                inner()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = UbuntuWarmGrey, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = activeCategory == RuleFilterCategory.ALL,
                        onClick = { activeCategory = RuleFilterCategory.ALL },
                        label = { Text("All (${rules.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = UbuntuOrange,
                            selectedLabelColor = Color.White,
                            containerColor = UbuntuDarkSurface,
                            labelColor = UbuntuWarmGrey
                        ),
                        border = BorderStroke(1.dp, if (activeCategory == RuleFilterCategory.ALL) UbuntuOrange else Color(0xFF3F3F3F)),
                        shape = RoundedCornerShape(6.dp)
                    )

                    FilterChip(
                        selected = activeCategory == RuleFilterCategory.ADS,
                        onClick = { activeCategory = RuleFilterCategory.ADS },
                        label = { Text("Ads ($adRulesCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = UbuntuOrange,
                            selectedLabelColor = Color.White,
                            containerColor = UbuntuDarkSurface,
                            labelColor = UbuntuWarmGrey
                        ),
                        border = BorderStroke(1.dp, if (activeCategory == RuleFilterCategory.ADS) UbuntuOrange else Color(0xFF3F3F3F)),
                        shape = RoundedCornerShape(6.dp)
                    )

                    FilterChip(
                        selected = activeCategory == RuleFilterCategory.TRACKING,
                        onClick = { activeCategory = RuleFilterCategory.TRACKING },
                        label = { Text("Trackers ($trackerRulesCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = UbuntuAubergine,
                            selectedLabelColor = Color.White,
                            containerColor = UbuntuDarkSurface,
                            labelColor = UbuntuWarmGrey
                        ),
                        border = BorderStroke(1.dp, if (activeCategory == RuleFilterCategory.TRACKING) UbuntuAubergine else Color(0xFF3F3F3F)),
                        shape = RoundedCornerShape(6.dp)
                    )

                    FilterChip(
                        selected = activeCategory == RuleFilterCategory.CUSTOM,
                        onClick = { activeCategory = RuleFilterCategory.CUSTOM },
                        label = { Text("Custom ($customRulesCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = UbuntuSuccessGreen,
                            selectedLabelColor = Color.White,
                            containerColor = UbuntuDarkSurface,
                            labelColor = UbuntuWarmGrey
                        ),
                        border = BorderStroke(1.dp, if (activeCategory == RuleFilterCategory.CUSTOM) UbuntuSuccessGreen else Color(0xFF3F3F3F)),
                        shape = RoundedCornerShape(6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Rules List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredRules, key = { it.id }) { rule ->
                        RuleItemCard(
                            rule = rule,
                            onToggle = { isChecked ->
                                viewModel.toggleBlocklistRule(rule.id, isChecked)
                            },
                            onDelete = if (rule.isCustom) {
                                { viewModel.removeBlocklistRule(rule.id) }
                            } else null
                        )
                    }
                }
            } else {
                // Live Audit Log View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Interceptions (${auditLog.size})",
                        style = MaterialTheme.typography.bodySmall,
                        color = UbuntuWarmGrey
                    )
                    if (auditLog.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearBlocklistLog() },
                            modifier = Modifier.testTag("clear_audit_log_button")
                        ) {
                            Text("Clear Log", color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    }
                }

                if (auditLog.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = UbuntuWarmGrey.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "No network requests intercepted yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = UbuntuWarmGrey
                            )
                            Text(
                                "Visiting pages will stream blocked ad & tracking URLs here live.",
                                style = MaterialTheme.typography.labelSmall,
                                color = UbuntuWarmGrey.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        items(auditLog, key = { it.id }) { entry ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                                border = BorderStroke(1.dp, Color(0xFF333333)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (entry.type == BlocklistType.ADVERTISING)
                                            UbuntuOrange.copy(alpha = 0.2f) else UbuntuAubergine.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (entry.type == BlocklistType.ADVERTISING) "AD" else "TRACK",
                                            color = if (entry.type == BlocklistType.ADVERTISING) UbuntuOrange else Color(0xFFE89EE1),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.host,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = entry.url,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = UbuntuWarmGrey,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(
                                        text = timeFormat.format(Date(entry.timestamp)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = UbuntuWarmGrey,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Custom Rule Dialog
    if (showAddRuleDialog) {
        var patternInput by remember { mutableStateOf("") }
        var typeInput by remember { mutableStateOf(BlocklistType.ADVERTISING) }
        var descInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddRuleDialog = false },
            containerColor = UbuntuCardDark,
            title = {
                Text("Add Custom Blocklist Rule", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter a domain name or URL pattern to block (e.g. adserver.site.com or /promo-banner/).",
                        style = MaterialTheme.typography.bodySmall,
                        color = UbuntuWarmGrey
                    )

                    OutlinedTextField(
                        value = patternInput,
                        onValueChange = { patternInput = it },
                        label = { Text("Blocked Domain or Pattern") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UbuntuOrange,
                            unfocusedBorderColor = Color(0xFF444444),
                            focusedLabelColor = UbuntuOrange,
                            unfocusedLabelColor = UbuntuWarmGrey,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_rule_pattern_input")
                    )

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Description (Optional)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UbuntuOrange,
                            unfocusedBorderColor = Color(0xFF444444),
                            focusedLabelColor = UbuntuOrange,
                            unfocusedLabelColor = UbuntuWarmGrey,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Rule Category", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = typeInput == BlocklistType.ADVERTISING,
                                onClick = { typeInput = BlocklistType.ADVERTISING },
                                colors = RadioButtonDefaults.colors(selectedColor = UbuntuOrange)
                            )
                            Text("Advertising", color = Color.White, fontSize = 12.sp)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = typeInput == BlocklistType.TRACKING,
                                onClick = { typeInput = BlocklistType.TRACKING },
                                colors = RadioButtonDefaults.colors(selectedColor = UbuntuAubergine)
                            )
                            Text("Tracking", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (patternInput.isNotBlank()) {
                            viewModel.addCustomBlocklistRule(
                                pattern = patternInput.trim(),
                                type = typeInput,
                                isKeyword = patternInput.contains("/"),
                                description = descInput.trim()
                            )
                            showAddRuleDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                    modifier = Modifier.testTag("confirm_add_rule_button")
                ) {
                    Text("Add Rule", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRuleDialog = false }) {
                    Text("Cancel", color = UbuntuWarmGrey)
                }
            }
        )
    }
}

@Composable
fun RuleItemCard(
    rule: BlocklistRule,
    onToggle: (Boolean) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
        border = BorderStroke(1.dp, if (rule.isEnabled) Color(0xFF383838) else Color(0xFF282828)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rule_card_${rule.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = rule.pattern,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (rule.isEnabled) Color.White else Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Surface(
                        color = if (rule.type == BlocklistType.ADVERTISING)
                            UbuntuOrange.copy(alpha = 0.2f) else UbuntuAubergine.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (rule.type == BlocklistType.ADVERTISING) "AD" else "TRACKING",
                            color = if (rule.type == BlocklistType.ADVERTISING) UbuntuOrange else Color(0xFFE89EE1),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    if (rule.isCustom) {
                        Surface(
                            color = UbuntuSuccessGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "CUSTOM",
                                color = UbuntuSuccessGreen,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = rule.description.ifBlank { "Matches ${rule.matchType.name.lowercase()}" },
                        style = MaterialTheme.typography.labelSmall,
                        color = UbuntuWarmGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (rule.hitCount > 0) {
                        Text(
                            text = "${rule.hitCount} hits",
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuLightOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("delete_rule_${rule.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Rule",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Switch(
                checked = rule.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = if (rule.type == BlocklistType.ADVERTISING) UbuntuOrange else UbuntuAubergine,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color(0xFF2A2A2A)
                ),
                modifier = Modifier.testTag("toggle_rule_${rule.id}")
            )
        }
    }
}
