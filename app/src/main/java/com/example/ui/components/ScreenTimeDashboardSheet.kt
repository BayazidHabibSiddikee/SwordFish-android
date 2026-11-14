package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.UbuntuDarkSurface
import com.example.ui.theme.UbuntuErrorRed
import com.example.ui.theme.UbuntuLightOrange
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuSuccessGreen
import com.example.ui.theme.UbuntuWarmGrey
import com.example.ui.theme.UbuntuWarningYellow
import com.example.viewmodel.ActiveSheet
import com.example.viewmodel.BrowserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTimeDashboardSheet(
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val blockedSites by viewModel.blockedSites.collectAsState()
    val usageTimes by viewModel.usageTimes.collectAsState()

    // Map Room DB data into chart data points
    val chartDataPoints = remember(blockedSites, usageTimes) {
        if (usageTimes.isNotEmpty()) {
            usageTimes.map { usage ->
                val matchedSite = blockedSites.find { it.domain == usage.targetIdentifier }
                BarChartDataPoint(
                    label = matchedSite?.displayName?.ifBlank { usage.targetIdentifier.split(".").first() } ?: usage.targetIdentifier.split(".").first(),
                    identifier = usage.targetIdentifier,
                    usedMinutes = (usage.activeSecondsToday / 60).toInt(),
                    limitMinutes = matchedSite?.dailyLimitMinutes ?: 30,
                    isAppPackage = matchedSite?.isAppPackage == true,
                    timesBlocked = usage.timesBlockedToday
                )
            }
        } else {
            // Provide default initial chart entries for active blacklisted sites so user sees visual bars right away
            blockedSites.take(5).mapIndexed { index, site ->
                val simulatedUsed = when (index) {
                    0 -> 18 // TikTok 18m / 20m
                    1 -> 14 // Instagram 14m / 30m
                    2 -> 28 // X / Twitter 28m / 30m
                    3 -> 9  // Reddit 9m / 45m
                    else -> 12
                }
                BarChartDataPoint(
                    label = site.displayName.ifBlank { site.domain.split(".").first().replaceFirstChar { it.uppercase() } },
                    identifier = site.domain,
                    usedMinutes = simulatedUsed,
                    limitMinutes = site.dailyLimitMinutes,
                    isAppPackage = site.isAppPackage,
                    timesBlocked = 2
                )
            }
        }
    }

    val totalUsedMinutes = chartDataPoints.sumOf { it.usedMinutes }
    val totalTimesBlocked = chartDataPoints.sumOf { it.timesBlocked }
    val overLimitCount = chartDataPoints.count { it.usedMinutes >= it.limitMinutes }

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
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = UbuntuOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Screen Time Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Daily Distraction Analytics & Limits",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbuntuWarmGrey
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

            Spacer(modifier = Modifier.height(16.dp))

            // Metric Summary Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F3F)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Total Screen Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuWarmGrey
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${totalUsedMinutes}m",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = UbuntuOrange
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F3F)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Interceptions",
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuWarmGrey
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalTimesBlocked",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = UbuntuSuccessGreen
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F3F)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Over Limit",
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuWarmGrey
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$overLimitCount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (overLimitCount > 0) UbuntuErrorRed else UbuntuWarmGrey
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The Jetpack Compose Bar Chart Component
            ScreenTimeBarChart(dataPoints = chartDataPoints)

            Spacer(modifier = Modifier.height(14.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        viewModel.startFocusSession(25)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("dashboard_start_focus_button")
                ) {
                    Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start 25m Focus", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onDismiss()
                        viewModel.openSheet(ActiveSheet.APP_BLOCKER)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("dashboard_manage_limits_button")
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Manage Limits")
                }
            }
        }
    }
}
