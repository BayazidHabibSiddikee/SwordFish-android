package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuCardDark
import com.example.ui.theme.UbuntuDarkSurface
import com.example.ui.theme.UbuntuErrorRed
import com.example.ui.theme.UbuntuLightOrange
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuSuccessGreen
import com.example.ui.theme.UbuntuWarmGrey
import com.example.ui.theme.UbuntuWarningYellow

data class BarChartDataPoint(
    val label: String,
    val identifier: String,
    val usedMinutes: Int,
    val limitMinutes: Int,
    val isAppPackage: Boolean,
    val timesBlocked: Int = 0
)

@Composable
fun ScreenTimeBarChart(
    dataPoints: List<BarChartDataPoint>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F3F)),
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = UbuntuOrange,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No Distraction Screen Time Recorded Today",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Browse or run monitored sessions to visualize daily minutes spent on blacklisted sites.",
                        style = MaterialTheme.typography.bodySmall,
                        color = UbuntuWarmGrey,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        return
    }

    var selectedPoint by remember { mutableStateOf<BarChartDataPoint?>(dataPoints.firstOrNull()) }
    val maxMinutes = remember(dataPoints) {
        val maxUsed = dataPoints.maxOfOrNull { it.usedMinutes } ?: 30
        val maxLimit = dataPoints.maxOfOrNull { it.limitMinutes } ?: 30
        maxOf(maxUsed, maxLimit, 35)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F3F)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("screen_time_bar_chart")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Chart Title & Legend
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Daily Screen Time (Minutes)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(UbuntuOrange, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Used", fontSize = 11.sp, color = UbuntuWarmGrey)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(8.dp).background(UbuntuErrorRed, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Over Limit", fontSize = 11.sp, color = UbuntuWarmGrey)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // The Visual Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dataPoints.take(6).forEach { point ->
                    val isSelected = point.identifier == selectedPoint?.identifier
                    val ratio = (point.usedMinutes.toFloat() / maxMinutes.toFloat()).coerceIn(0.04f, 1f)
                    val animatedHeightRatio by animateFloatAsState(
                        targetValue = ratio,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "barHeight"
                    )

                    val isOverLimit = point.usedMinutes >= point.limitMinutes
                    val isWarning = point.usedMinutes >= (point.limitMinutes * 0.75) && !isOverLimit

                    val barColor = when {
                        isOverLimit -> UbuntuErrorRed
                        isWarning -> UbuntuWarningYellow
                        else -> UbuntuOrange
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPoint = point }
                            .padding(horizontal = 4.dp)
                    ) {
                        // Minute value above bar
                        Text(
                            text = "${point.usedMinutes}m",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else UbuntuWarmGrey,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Vertical Bar Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                                .height(120.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Background slot track
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .background(Color(0xFF333333).copy(alpha = 0.5f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            )

                            // Filled bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(animatedHeightRatio)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                barColor,
                                                barColor.copy(alpha = 0.75f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                    )
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(1.5.dp, Color.White, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        } else Modifier
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Target Label below bar
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) UbuntuLightOrange else Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Item Detail Card
            selectedPoint?.let { sel ->
                val percentUsed = if (sel.limitMinutes > 0) {
                    ((sel.usedMinutes.toFloat() / sel.limitMinutes.toFloat()) * 100).toInt()
                } else 0

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = UbuntuCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, UbuntuOrange.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (sel.isAppPackage) Icons.Default.Android else Icons.Default.Language,
                                contentDescription = null,
                                tint = UbuntuOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = sel.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = sel.identifier,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = UbuntuWarmGrey,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${sel.usedMinutes}m / ${sel.limitMinutes}m ($percentUsed%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (percentUsed >= 100) UbuntuErrorRed else UbuntuOrange
                            )
                            if (sel.timesBlocked > 0) {
                                Text(
                                    text = "${sel.timesBlocked} times blocked",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = UbuntuWarningYellow,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
