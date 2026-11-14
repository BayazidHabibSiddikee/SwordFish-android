package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserTab
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.UbuntuDarkSurface
import com.example.ui.theme.UbuntuDeepAubergine
import com.example.ui.theme.UbuntuLightOrange
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuWarmGrey
import com.example.viewmodel.BrowserViewModel

private enum class TabFilterCategory {
    ALL,
    REGULAR,
    PRIVATE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabsManagerSheet(
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tabs by viewModel.tabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(TabFilterCategory.ALL) }

    val regularCount = tabs.count { !it.isIncognito }
    val privateCount = tabs.count { it.isIncognito }

    val filteredTabs = remember(tabs, searchQuery, selectedFilter) {
        tabs.filter { tab ->
            val matchesFilter = when (selectedFilter) {
                TabFilterCategory.ALL -> true
                TabFilterCategory.REGULAR -> !tab.isIncognito
                TabFilterCategory.PRIVATE -> tab.isIncognito
            }
            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                tab.title.lowercase().contains(q) || tab.url.lowercase().contains(q)
            }
            matchesFilter && matchesQuery
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
            // Drag handle pill
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFF4A4A4A), CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
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
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = UbuntuOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Tab Manager",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${tabs.size} open ${if (tabs.size == 1) "tab" else "tabs"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbuntuWarmGrey
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (tabs.size > 1) {
                        TextButton(
                            onClick = { viewModel.closeInactiveTabs() },
                            modifier = Modifier.testTag("close_inactive_tabs_button")
                        ) {
                            Text(
                                text = "Close Inactive",
                                style = MaterialTheme.typography.labelSmall,
                                color = UbuntuLightOrange
                            )
                        }

                        TextButton(
                            onClick = { viewModel.closeAllTabs() },
                            modifier = Modifier.testTag("close_all_tabs_button")
                        ) {
                            Text(
                                text = "Close All",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_tab_manager_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Tab Switcher",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row: + New Tab and + New Incognito Tab
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.openNewTab()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("new_regular_tab_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Tab", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.openNewTab(isIncognito = true)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE89EE1)),
                    border = BorderStroke(1.dp, UbuntuAubergine),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("new_incognito_tab_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color(0xFFE89EE1),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Private Tab", color = Color(0xFFE89EE1), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Tabs Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(UbuntuDarkSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF3F3F3F), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Tabs",
                        tint = UbuntuWarmGrey,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 13.sp
                        ),
                        cursorBrush = SolidColor(UbuntuOrange),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_search_input"),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search open tabs by title or URL...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = UbuntuWarmGrey.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = UbuntuWarmGrey,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips Bar (All, Regular, Private)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedFilter == TabFilterCategory.ALL,
                    onClick = { selectedFilter = TabFilterCategory.ALL },
                    label = { Text("All (${tabs.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = UbuntuOrange,
                        selectedLabelColor = Color.White,
                        containerColor = UbuntuDarkSurface,
                        labelColor = UbuntuWarmGrey
                    ),
                    border = BorderStroke(1.dp, if (selectedFilter == TabFilterCategory.ALL) UbuntuOrange else Color(0xFF3F3F3F)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("tab_filter_all")
                )

                FilterChip(
                    selected = selectedFilter == TabFilterCategory.REGULAR,
                    onClick = { selectedFilter = TabFilterCategory.REGULAR },
                    label = { Text("Standard ($regularCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = UbuntuOrange,
                        selectedLabelColor = Color.White,
                        containerColor = UbuntuDarkSurface,
                        labelColor = UbuntuWarmGrey
                    ),
                    border = BorderStroke(1.dp, if (selectedFilter == TabFilterCategory.REGULAR) UbuntuOrange else Color(0xFF3F3F3F)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("tab_filter_regular")
                )

                FilterChip(
                    selected = selectedFilter == TabFilterCategory.PRIVATE,
                    onClick = { selectedFilter = TabFilterCategory.PRIVATE },
                    label = { Text("Private ($privateCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = UbuntuAubergine,
                        selectedLabelColor = Color.White,
                        containerColor = UbuntuDarkSurface,
                        labelColor = UbuntuWarmGrey
                    ),
                    border = BorderStroke(1.dp, if (selectedFilter == TabFilterCategory.PRIVATE) UbuntuAubergine else Color(0xFF3F3F3F)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("tab_filter_private")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // List-Based Tab Switcher UI
            if (filteredTabs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = UbuntuWarmGrey.copy(alpha = 0.5f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No matching tabs found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = UbuntuWarmGrey
                        )
                        if (searchQuery.isNotEmpty()) {
                            TextButton(onClick = { searchQuery = "" }) {
                                Text("Clear search query", color = UbuntuOrange)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTabs, key = { it.id }) { tab ->
                        val isActive = tab.id == activeTabId
                        TabListItemCard(
                            tab = tab,
                            isActive = isActive,
                            onSelect = {
                                viewModel.selectTab(tab.id)
                            },
                            onClose = {
                                viewModel.closeTab(tab.id)
                            },
                            onDuplicate = {
                                viewModel.duplicateTab(tab.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabListItemCard(
    tab: BrowserTab,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit,
    onDuplicate: () -> Unit
) {
    val cardBackground = if (tab.isIncognito) {
        if (isActive) UbuntuDeepAubergine else Color(0xFF241525)
    } else {
        if (isActive) Color(0xFF2C2420) else UbuntuDarkSurface
    }

    val borderColor = if (isActive) {
        if (tab.isIncognito) Color(0xFFE89EE1) else UbuntuOrange
    } else {
        Color(0xFF383838)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(if (isActive) 1.8.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag(if (isActive) "tab_item_active_${tab.id}" else "tab_item_${tab.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Status Stripe / Indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .background(
                        color = if (isActive) {
                            if (tab.isIncognito) Color(0xFFE89EE1) else UbuntuOrange
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Tab Icon
            val tabIcon: ImageVector = when {
                tab.isIncognito -> Icons.Default.VisibilityOff
                tab.isReaderMode -> Icons.Default.MenuBook
                tab.url.isBlank() -> Icons.Default.Home
                else -> Icons.Default.Public
            }

            val tabIconTint: Color = when {
                tab.isIncognito -> Color(0xFFE89EE1)
                isActive -> UbuntuOrange
                else -> UbuntuWarmGrey
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (tab.isIncognito) UbuntuAubergine.copy(alpha = 0.35f) else Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = tabIcon,
                    contentDescription = null,
                    tint = tabIconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Title, URL, and Status Badges
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = tab.title.ifBlank { "New Tab" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isActive) Color.White else Color(0xFFE0E0E0),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isActive) {
                        Surface(
                            color = UbuntuOrange,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    if (tab.isIncognito) {
                        Surface(
                            color = UbuntuAubergine,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "PRIVATE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE89EE1),
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    if (tab.isReaderMode) {
                        Surface(
                            color = Color(0xFF334155),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "READER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF93C5FD),
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (tab.url.isBlank()) "about:blank" else tab.url,
                        style = MaterialTheme.typography.labelSmall,
                        color = UbuntuWarmGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    val totalBlocked = tab.adsBlockedOnPage + tab.trackersBlockedOnPage
                    if (totalBlocked > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = UbuntuLightOrange,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$totalBlocked blocked",
                                style = MaterialTheme.typography.labelSmall,
                                color = UbuntuLightOrange,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action: Duplicate Tab Button (48dp touch target)
            IconButton(
                onClick = onDuplicate,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("duplicate_tab_${tab.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Duplicate Tab",
                    tint = UbuntuWarmGrey,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Action: Close Tab Button (48dp touch target)
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("close_tab_${tab.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Tab ${tab.title}",
                    tint = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
