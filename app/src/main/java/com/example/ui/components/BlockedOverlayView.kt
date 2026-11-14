package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.blocker.AppBlockerEngine
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.UbuntuDeepAubergine
import com.example.ui.theme.UbuntuLightOrange
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuWarmGrey
import com.example.viewmodel.ActiveSheet
import com.example.viewmodel.BrowserViewModel

@Composable
fun BlockedOverlayView(
    domain: String?,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val remainingSeconds by viewModel.focusTimeRemainingSeconds.collectAsState()
    val isFocusActive by viewModel.isFocusModeActive.collectAsState()
    val isTimerPaused by viewModel.isFocusTimerPaused.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(UbuntuDeepAubergine, UbuntuAubergine.copy(alpha = 0.4f), UbuntuDarkBackground)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Glowing Ubuntu Swordfish Shield Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale)
                    .background(UbuntuOrange.copy(alpha = 0.2f), CircleShape)
                    .border(2.dp, UbuntuOrange, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield Active",
                    tint = UbuntuOrange,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Distraction Blocked",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = UbuntuOrange.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, UbuntuOrange.copy(alpha = 0.6f)),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = domain ?: "Restricted Website",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = UbuntuLightOrange,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isFocusActive) {
                    "Swordfish Focus Shield is active. This site is blocked to safeguard your attention and productivity."
                } else {
                    "This website is on your Swordfish blacklist. You can manage blocked sites anytime in App Blocker."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = UbuntuWarmGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            if (isFocusActive) {
                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    color = UbuntuOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, UbuntuOrange.copy(alpha = 0.4f))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = "Timer",
                                tint = UbuntuOrange,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Focus Session: ${AppBlockerEngine.formatSecondsToTime(remainingSeconds)}" + if (isTimerPaused) " (PAUSED)" else "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isTimerPaused) UbuntuLightOrange else Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = { viewModel.addFocusMinutes(5) },
                                shape = RoundedCornerShape(8.dp),
                                color = UbuntuOrange.copy(alpha = 0.35f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, UbuntuOrange.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "+5 Min",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                onClick = { viewModel.togglePauseResumeFocusTimer() },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF333333),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                            ) {
                                Text(
                                    text = if (isTimerPaused) "Resume" else "Pause",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                onClick = { viewModel.stopFocusSession() },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF7A1B1B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDF382C))
                            ) {
                                Text(
                                    text = "End Focus",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Button(
                onClick = { viewModel.dismissBlockedOverlayAndGoHome() },
                colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp)
                    .testTag("return_home_button")
            ) {
                Text(
                    text = "Return to Safe Browsing",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.openSheet(ActiveSheet.APP_BLOCKER) },
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, UbuntuOrange),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(48.dp)
                    .testTag("manage_blocker_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Manage",
                    tint = UbuntuOrange,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manage Blocked Sites")
            }
        }
    }
}
