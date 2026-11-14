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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.blocker.AppBlockerEngine
import com.example.data.local.BlockedSiteEntity
import com.example.data.local.UsageTimeEntity
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.UbuntuDarkSurface
import com.example.ui.theme.UbuntuLightOrange
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuSuccessGreen
import com.example.ui.theme.UbuntuWarmGrey
import com.example.viewmodel.BrowserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBlockerSheet(
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    val isFocusActive by viewModel.isFocusModeActive.collectAsState()
    val focusSeconds by viewModel.focusTimeRemainingSeconds.collectAsState()
    val blockedSites by viewModel.blockedSites.collectAsState()
    val usageTimes by viewModel.usageTimes.collectAsState()
    val isServiceRunning by viewModel.isBackgroundServiceActive.collectAsState()
    val activeMonitoredSeconds by viewModel.activeMonitoredSeconds.collectAsState()

    var newTargetInput by remember { mutableStateOf("") }
    var newDisplayNameInput by remember { mutableStateOf("") }
    var selectedLimitMinutes by remember { mutableIntStateOf(30) }
    var isNewAppPackage by remember { mutableStateOf(false) }

    var selectedTabFilter by remember { mutableStateOf("All") }
    val tabFilters = listOf("All", "Apps", "Websites", "Usage Today")

    val filteredSites = remember(blockedSites, selectedTabFilter) {
        when (selectedTabFilter) {
            "Apps" -> blockedSites.filter { it.isAppPackage }
            "Websites" -> blockedSites.filter { !it.isAppPackage }
            else -> blockedSites
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
                .padding(20.dp)
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
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = UbuntuOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Distraction & App Blocker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isServiceRunning || isFocusActive) UbuntuSuccessGreen else Color.Gray,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isServiceRunning || isFocusActive) "Service Running (${AppBlockerEngine.formatSecondsToTime(activeMonitoredSeconds.toInt())})" else "Service Standby",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isServiceRunning || isFocusActive) UbuntuSuccessGreen else Color.Gray
                            )
                        }
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

            Spacer(modifier = Modifier.height(16.dp))

            // Background Service & Focus Session Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFocusActive) Color(0xFF3D1405) else UbuntuDarkSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isFocusActive) UbuntuOrange else Color(0xFF3F3F3F)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isFocusActive) Icons.Default.HourglassTop else Icons.Default.Timer,
                                contentDescription = null,
                                tint = UbuntuOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Background Usage Monitor",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        if (isFocusActive) {
                            Text(
                                text = AppBlockerEngine.formatSecondsToTime(focusSeconds),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = UbuntuOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isFocusActive) {
                        val isTimerPaused by viewModel.isFocusTimerPaused.collectAsState()
                        Text(
                            text = "Background service is monitoring active usage and shielding blacklisted apps & websites in real-time." + if (isTimerPaused) " (PAUSED)" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isTimerPaused) UbuntuLightOrange else UbuntuOrange
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.addFocusMinutes(5) },
                                colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange.copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+5m", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.togglePauseResumeFocusTimer() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text(if (isTimerPaused) "Resume" else "Pause", fontSize = 12.sp, color = Color.White)
                            }
                            Button(
                                onClick = { viewModel.stopFocusSession() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDF382C)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Stop", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    } else {
                        Text(
                            text = "Start a monitored session to enforce daily limits and block distracting packages.",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbuntuWarmGrey
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15, 25, 45, 60).forEach { minutes ->
                                Button(
                                    onClick = { viewModel.startFocusSession(minutes) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("${minutes}m", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation filter tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tabFilters.forEach { filter ->
                    val isSelected = filter == selectedTabFilter
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) UbuntuOrange else UbuntuDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) UbuntuOrange else Color(0xFF3F3F3F)
                        ),
                        modifier = Modifier.clickable { selectedTabFilter = filter }
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTabFilter == "Usage Today") {
                // Today's Active Monitored Usage Breakdown from Room DB
                Text(
                    text = "ACTIVE USAGE MONITORED TODAY (ROOM DB)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = UbuntuOrange,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (usageTimes.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Text(
                            text = "No active usage recorded yet today.\nUsage is tracked automatically as you browse or run sessions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(usageTimes, key = { it.id }) { usage ->
                            val matchedSite = blockedSites.find { it.domain == usage.targetIdentifier }
                            val limitMinutes = matchedSite?.dailyLimitMinutes ?: 30
                            val usedMinutes = (usage.activeSecondsToday / 60).toInt()
                            val progress = (usedMinutes.toFloat() / limitMinutes.toFloat()).coerceIn(0f, 1f)

                            UsageProgressCard(
                                title = matchedSite?.displayName ?: usage.targetIdentifier,
                                identifier = usage.targetIdentifier,
                                usedMinutes = usedMinutes,
                                limitMinutes = limitMinutes,
                                progress = progress,
                                isAppPackage = matchedSite?.isAppPackage == true
                            )
                        }
                    }
                }
            } else {
                // Add new package or site input row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newTargetInput,
                        onValueChange = { newTargetInput = it },
                        placeholder = {
                            Text(
                                if (isNewAppPackage) "Package (e.g. com.snapchat.android)..." else "Domain (e.g. tiktok.com)...",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = UbuntuDarkSurface,
                            unfocusedContainerColor = UbuntuDarkSurface,
                            focusedBorderColor = UbuntuOrange,
                            unfocusedBorderColor = Color(0xFF3F3F3F),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newTargetInput.isNotBlank()) {
                                viewModel.addBlockedSite(
                                    domainOrPackage = newTargetInput,
                                    displayName = newDisplayNameInput.ifBlank { newTargetInput },
                                    category = if (isNewAppPackage) "App Package" else "Web",
                                    dailyLimitMinutes = selectedLimitMinutes,
                                    isAppPackage = isNewAppPackage
                                )
                                newTargetInput = ""
                                newDisplayNameInput = ""
                                focusManager.clearFocus()
                            }
                        }),
                        modifier = Modifier.weight(1f).testTag("add_blocked_target_input")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Toggle whether target is an App Package or Website
                    IconButton(
                        onClick = { isNewAppPackage = !isNewAppPackage },
                        modifier = Modifier
                            .background(
                                if (isNewAppPackage) UbuntuOrange else Color(0xFF333333),
                                RoundedCornerShape(10.dp)
                            )
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (isNewAppPackage) Icons.Default.Android else Icons.Default.Language,
                            contentDescription = "Toggle Package or Web",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = {
                            if (newTargetInput.isNotBlank()) {
                                viewModel.addBlockedSite(
                                    domainOrPackage = newTargetInput,
                                    displayName = newDisplayNameInput.ifBlank { newTargetInput },
                                    category = if (isNewAppPackage) "App Package" else "Web",
                                    dailyLimitMinutes = selectedLimitMinutes,
                                    isAppPackage = isNewAppPackage
                                )
                                newTargetInput = ""
                                newDisplayNameInput = ""
                                focusManager.clearFocus()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp).testTag("add_target_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Blacklisted items list from Room Database
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSites, key = { it.id }) { site ->
                        BlacklistedPackageRow(
                            site = site,
                            onToggle = { viewModel.toggleBlockedSite(site) },
                            onDelete = { viewModel.removeBlockedSite(site) },
                            onLimitChange = { newLimit -> viewModel.updateDailyLimit(site, newLimit) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UsageProgressCard(
    title: String,
    identifier: String,
    usedMinutes: Int,
    limitMinutes: Int,
    progress: Float,
    isAppPackage: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = UbuntuDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F3F)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAppPackage) Icons.Default.Android else Icons.Default.Language,
                        contentDescription = null,
                        tint = UbuntuOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Text(
                    text = "$usedMinutes / ${limitMinutes}m",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (progress >= 1f) Color(0xFFDF382C) else UbuntuOrange
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = if (progress >= 1f) Color(0xFFDF382C) else UbuntuOrange,
                trackColor = Color(0xFF333333),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )
        }
    }
}

@Composable
fun BlacklistedPackageRow(
    site: BlockedSiteEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onLimitChange: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = UbuntuDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F3F)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = if (site.isAppPackage) Icons.Default.Android else Icons.Default.Language,
                    contentDescription = null,
                    tint = if (site.isEnabled) UbuntuOrange else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = site.displayName.ifBlank { site.domain },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (site.isEnabled) Color.White else Color.White.copy(alpha = 0.4f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = site.domain,
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuWarmGrey,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${site.dailyLimitMinutes}m limit",
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuOrange,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = site.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = UbuntuOrange,
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color(0xFF3F3F3F)
                    ),
                    modifier = Modifier.padding(end = 4.dp)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
