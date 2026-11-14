package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.data.model.DownloadStatus
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserTab
import com.example.ui.theme.LocalUbuntuColors
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.UbuntuDarkSurface
import com.example.ui.theme.UbuntuDeepAubergine
import com.example.ui.theme.UbuntuIncognitoSurface
import com.example.ui.theme.UbuntuLavender
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuSuccessGreen
import com.example.viewmodel.ActiveSheet
import com.example.viewmodel.BrowserViewModel

@Composable
fun BrowserTopBar(
    tab: BrowserTab,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(tab.url)) }
    val bookmarks by viewModel.bookmarks.collectAsState()
    val isBookmarked = bookmarks.any { it.url == tab.url && tab.url.isNotBlank() }
    val shieldStats by viewModel.shieldStats.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val theme = LocalUbuntuColors.current
    var showDownloadOptions by remember { mutableStateOf(false) }
    val activeDownloads by viewModel.activeDownloads.collectAsState()
    val hasActiveDownloads = activeDownloads.any { it.status == DownloadStatus.DOWNLOADING }

    LaunchedEffect(tab.url) {
        if (!isFocused) {
            textFieldValue = TextFieldValue(tab.url)
        }
    }

    val displayHost = remember(tab.url, tab.isIncognito) {
        if (tab.url.isBlank()) {
            if (tab.isIncognito) "Search privately or enter URL" else "Search or enter URL"
        } else {
            try {
                Uri.parse(tab.url).host ?: tab.url
            } catch (_: Exception) {
                tab.url
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (tab.isIncognito) UbuntuDeepAubergine else theme.background)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Shield Icon & Blocked Count Badge
            Surface(
                onClick = { viewModel.openSheet(ActiveSheet.SHIELD_STATS) },
                shape = RoundedCornerShape(16.dp),
                color = if (shieldStats.isShieldEnabled) UbuntuOrange.copy(alpha = 0.2f) else theme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (shieldStats.isShieldEnabled) UbuntuOrange else theme.border
                ),
                modifier = Modifier
                    .padding(end = 6.dp)
                    .testTag("shield_status_badge")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Swordfish Shield",
                        tint = if (shieldStats.isShieldEnabled) UbuntuOrange else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    if (tab.adsBlockedOnPage > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${tab.adsBlockedOnPage}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = UbuntuOrange
                        )
                    }
                }
            }

            // Omnibox / Address Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(
                        if (tab.isIncognito) UbuntuIncognitoSurface else theme.surface,
                        RoundedCornerShape(22.dp)
                    )
                    .border(
                        1.dp,
                        if (isFocused) UbuntuOrange else theme.border,
                        RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Leading Indicator: SSL Lock or Search
                    if (tab.url.startsWith("https://") && !isFocused) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure SSL",
                            tint = UbuntuSuccessGreen,
                            modifier = Modifier
                                .size(15.dp)
                                .padding(end = 4.dp)
                        )
                    } else if (isFocused || tab.url.isBlank()) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search or Web Address",
                            tint = if (isFocused) UbuntuOrange else (if (tab.isIncognito) Color.White.copy(alpha = 0.5f) else theme.textSecondary),
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    // Native, stable text field — always mounted, never throws IllegalStateException
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        textStyle = TextStyle(
                            color = if (tab.isIncognito) Color.White else theme.textPrimary,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(UbuntuOrange),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Go,
                            keyboardType = KeyboardType.Uri
                        ),
                        keyboardActions = KeyboardActions(onGo = {
                            val input = textFieldValue.text.trim()
                            focusManager.clearFocus()
                            if (input.isNotBlank()) {
                                viewModel.loadUrl(input)
                            }
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { state ->
                                if (state.isFocused && !isFocused) {
                                    textFieldValue = textFieldValue.copy(
                                        selection = TextRange(0, textFieldValue.text.length)
                                    )
                                }
                                isFocused = state.isFocused
                            }
                            .testTag("omnibox_input_active"),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (!isFocused && tab.url.isNotBlank()) {
                                    Text(
                                        text = displayHost,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (tab.isIncognito) Color.White else theme.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.testTag("omnibox_display_text")
                                    )
                                } else if (textFieldValue.text.isEmpty()) {
                                    Text(
                                        text = if (tab.isIncognito) "Search privately or enter URL" else "Search or enter URL",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (tab.isIncognito) Color.White.copy(alpha = 0.5f) else theme.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .alpha(if (!isFocused && tab.url.isNotBlank()) 0f else 1f)
                                ) {
                                    innerTextField()
                                }
                            }
                        }
                    )

                    // Clear button when focused and text exists
                    if (isFocused && textFieldValue.text.isNotBlank()) {
                        IconButton(
                            onClick = { textFieldValue = TextFieldValue("") },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("omnibox_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear address bar",
                                tint = if (tab.isIncognito) Color.White.copy(alpha = 0.7f) else theme.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Navigate / Go Arrow button when focused and text exists
                    if (isFocused && textFieldValue.text.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val input = textFieldValue.text.trim()
                                focusManager.clearFocus()
                                if (input.isNotBlank()) {
                                    viewModel.loadUrl(input)
                                }
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("omnibox_go_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Navigate to URL",
                                tint = UbuntuOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Bookmark Star Action when not focused
                    if (!isFocused && tab.url.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val wasBookmarked = isBookmarked
                                viewModel.toggleBookmarkCurrentTab()
                                if (wasBookmarked) {
                                    android.widget.Toast.makeText(context, "Removed from Bookmarks", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "★ Saved to Favorites & New Tab Quick Access!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("bookmark_star_button")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = if (isBookmarked) "Bookmarked" else "Bookmark",
                                tint = if (isBookmarked) UbuntuOrange else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Reader Mode Action when not focused
                    if (!isFocused && tab.url.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.toggleReaderMode() },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("reader_mode_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (tab.isReaderMode) Icons.Default.AutoStories else Icons.Default.MenuBook,
                                    contentDescription = if (tab.isReaderMode) "Exit Reader Mode" else "Reader Mode",
                                    tint = if (tab.isReaderMode || tab.isReaderAvailable) UbuntuOrange else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                                if (tab.isReaderAvailable && !tab.isReaderMode) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(6.dp)
                                            .background(UbuntuOrange, CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    // Reload Action when not focused
                    if (!isFocused && tab.url.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.reload() },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("reload_button")
                        ) {
                            Icon(
                                imageVector = if (tab.isLoading) Icons.Default.Close else Icons.Default.Refresh,
                                contentDescription = if (tab.isLoading) "Stop" else "Reload",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Download Button at top bar (Media Hub & Offline Downloader)
            IconButton(
                onClick = {
                    if (tab.url.isNotBlank() && !tab.url.startsWith("about:")) {
                        showDownloadOptions = true
                    } else {
                        viewModel.openMediaDownloader()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("top_bar_download_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Downloads & Media Hub",
                        tint = if (hasActiveDownloads) UbuntuOrange else (if (tab.isIncognito) Color.White.copy(alpha = 0.85f) else theme.textPrimary.copy(alpha = 0.85f)),
                        modifier = Modifier.size(20.dp)
                    )
                    if (hasActiveDownloads) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(7.dp)
                                .background(UbuntuOrange, CircleShape)
                        )
                    }
                }
            }

            // Global Theme Toggle (Light Mode <-> High-Contrast Ubuntu Dark)
            IconButton(
                onClick = { viewModel.toggleTheme() },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("global_theme_toggle_btn")
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to High-Contrast Ubuntu Dark",
                    tint = if (isDarkTheme) UbuntuOrange else UbuntuAubergine,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = { viewModel.openSettings() },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("top_bar_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Protection & Filter Settings",
                    tint = if (tab.isIncognito) Color.White.copy(alpha = 0.85f) else theme.textPrimary.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Animated Loading Progress Bar
        AnimatedVisibility(visible = tab.isLoading) {
            LinearProgressIndicator(
                progress = { tab.progress },
                color = UbuntuOrange,
                trackColor = UbuntuDarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
        }

        // Offline Cache Snapshot Banner
        AnimatedVisibility(visible = tab.isLoadedFromCache) {
            Surface(
                color = UbuntuAubergine,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_cache_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline Cache",
                        tint = UbuntuOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Offline View" + (tab.cachedTimestamp?.let {
                            " • " + SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(it))
                        } ?: ""),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        onClick = { viewModel.retryOnlineCurrentPage() },
                        shape = RoundedCornerShape(12.dp),
                        color = UbuntuOrange,
                        modifier = Modifier.testTag("offline_cache_reload_btn")
                    ) {
                        Text(
                            text = "Try Live",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Download Options Dialog
        if (showDownloadOptions) {
            AlertDialog(
                onDismissRequest = { showDownloadOptions = false },
                containerColor = UbuntuDarkSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = UbuntuOrange,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Downloads & Media",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Choose an action for ${if (tab.title.isNotBlank()) tab.title else tab.url}:",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Option 1: Save Page Offline
                        Surface(
                            onClick = {
                                showDownloadOptions = false
                                viewModel.saveCurrentPageOffline(tab) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "✓ Webpage saved offline! Available anytime in Offline Cache.",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = UbuntuAubergine.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, UbuntuOrange.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_save_page_offline_btn")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "💾 Save Page for Offline Reading",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Download full snapshot into local encrypted Room database",
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        // Option 2: Download Media
                        Surface(
                            onClick = {
                                showDownloadOptions = false
                                viewModel.openMediaDownloader()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2A2A2A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444444)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_download_media_btn")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🎬 Download Video / Audio (yt-dlp)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Extract video or audio streams with yt-dlp & aria2c",
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        // Option 3: Open Downloads Manager
                        Surface(
                            onClick = {
                                showDownloadOptions = false
                                viewModel.openMediaDownloader()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2A2A2A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444444)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_open_downloads_queue_btn")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📂 Open Downloads & Media Queue",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${activeDownloads.size} total tasks in downloader queue",
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showDownloadOptions = false },
                        modifier = Modifier.testTag("dialog_cancel_download_btn")
                    ) {
                        Text("Cancel", color = UbuntuOrange)
                    }
                }
            )
        }
    }
}

