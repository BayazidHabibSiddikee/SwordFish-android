package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserTab
import com.example.ui.theme.LocalUbuntuColors
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.UbuntuOrange
import com.example.viewmodel.ActiveSheet
import com.example.viewmodel.BrowserViewModel

@Composable
fun BrowserBottomBar(
    tab: BrowserTab,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val tabs by viewModel.tabs.collectAsState()
    val theme = LocalUbuntuColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.surface)
            .border(
                width = 1.dp,
                color = theme.border,
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .navigationBarsPadding()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(
            onClick = { viewModel.goBack() },
            enabled = tab.canGoBack,
            modifier = Modifier
                .size(48.dp)
                .testTag("bottom_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = if (tab.canGoBack) theme.textPrimary else theme.textSecondary.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }

        // Forward Button
        IconButton(
            onClick = { viewModel.goForward() },
            enabled = tab.canGoForward,
            modifier = Modifier
                .size(48.dp)
                .testTag("bottom_forward_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Forward",
                tint = if (tab.canGoForward) theme.textPrimary else theme.textSecondary.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }

        // Home Button
        IconButton(
            onClick = { viewModel.dismissBlockedOverlayAndGoHome() },
            modifier = Modifier
                .size(48.dp)
                .testTag("bottom_home_button")
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = if (tab.url.isBlank()) UbuntuOrange else theme.textPrimary.copy(alpha = 0.85f),
                modifier = Modifier.size(24.dp)
            )
        }

        // Bookmarks & History Button
        IconButton(
            onClick = { viewModel.openSheet(ActiveSheet.BOOKMARKS_HISTORY) },
            modifier = Modifier
                .size(48.dp)
                .testTag("bottom_bookmarks_button")
        ) {
            Icon(
                imageVector = Icons.Default.Bookmarks,
                contentDescription = "Bookmarks and History",
                tint = theme.textPrimary.copy(alpha = 0.85f),
                modifier = Modifier.size(22.dp)
            )
        }

        // Tabs Switcher Button
        IconButton(
            onClick = { viewModel.openSheet(ActiveSheet.TABS_MANAGER) },
            modifier = Modifier
                .size(48.dp)
                .testTag("bottom_tabs_button")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .border(1.8.dp, UbuntuOrange, RoundedCornerShape(7.dp))
            ) {
                Text(
                    text = "${tabs.size}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    fontSize = 11.sp
                )
            }
        }

        // Settings Option Button
        IconButton(
            onClick = { viewModel.openSettings() },
            modifier = Modifier
                .size(48.dp)
                .testTag("bottom_settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings, Ratings & Blockers",
                tint = UbuntuOrange,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
