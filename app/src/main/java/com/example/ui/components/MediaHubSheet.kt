package com.example.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.CliToolInfo
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaItem
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuCardDark
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.UbuntuDarkSurface
import com.example.ui.theme.UbuntuDeepAubergine
import com.example.ui.theme.UbuntuLightOrange
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuSuccessGreen
import com.example.ui.theme.UbuntuWarmGrey
import com.example.ui.theme.UbuntuWarningYellow
import com.example.viewmodel.BrowserViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaHubSheet(
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val defaultTab by viewModel.selectedMediaHubTab.collectAsState()
    var selectedTab by remember(defaultTab) { mutableIntStateOf(defaultTab) }
    val tabs = listOf("Player", "Downloader", "Library & CLI")

    val tabsState by viewModel.tabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val currentBrowserTab = tabsState.find { it.id == activeTabId }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = UbuntuDarkBackground,
        dragHandle = null,
        modifier = Modifier.fillMaxWidth().testTag("media_hub_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        ) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UbuntuDeepAubergine)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .background(UbuntuOrange.copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, UbuntuOrange, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = UbuntuOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Swordfish Media Hub",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "yt-dlp Engine & Hardware Player",
                        style = MaterialTheme.typography.labelSmall,
                        color = UbuntuWarmGrey
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("media_hub_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Tabs Bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = UbuntuDarkSurface,
                contentColor = UbuntuOrange,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = UbuntuOrange,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) UbuntuOrange else Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.PlayArrow
                                    1 -> Icons.Default.Download
                                    else -> Icons.Default.Folder
                                },
                                contentDescription = title,
                                tint = if (selectedTab == index) UbuntuOrange else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("media_tab_$index")
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> MediaPlayerView(viewModel = viewModel)
                1 -> MediaDownloaderView(
                    viewModel = viewModel,
                    activeWebUrl = currentBrowserTab?.url ?: ""
                )
                2 -> MediaLibraryAndCliView(
                    viewModel = viewModel,
                    onPlayItem = { selectedTab = 0 }
                )
            }
        }
    }
}

@Composable
private fun MediaPlayerView(viewModel: BrowserViewModel) {
    val context = LocalContext.current
    val currentItem by viewModel.currentPlayingMedia.collectAsState()
    val isPlaying by viewModel.isMediaPlaying.collectAsState()
    val currentPositionMs by viewModel.mediaPlaybackPositionMs.collectAsState()
    val durationMs by viewModel.mediaPlaybackDurationMs.collectAsState()
    val speed by viewModel.mediaPlaybackSpeed.collectAsState()
    val isLooping by viewModel.isMediaLooping.collectAsState()

    // Keep active playback position updated
    LaunchedEffect(isPlaying, currentItem) {
        while (isPlaying) {
            delay(500)
            viewModel.tickMediaPlayback()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentItem == null) {
            // Empty Player State
            Card(
                colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = UbuntuOrange.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Media Currently Selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose a sample track below or download media from any webpage",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbuntuWarmGrey,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val item = currentItem!!

            // Video Display or Audio Visualizer Wave Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, UbuntuOrange.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (item.isVideo) {
                        // Real Android VideoView for video items
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    if (item.rawResId != null) {
                                        val rawUri = Uri.parse("android.resource://${ctx.packageName}/${item.rawResId}")
                                        setVideoURI(rawUri)
                                    } else if (item.uriString.isNotBlank()) {
                                        setVideoURI(Uri.parse(item.uriString))
                                    }
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = isLooping
                                        if (isPlaying) start()
                                    }
                                }
                            },
                            update = { videoView ->
                                if (isPlaying && !videoView.isPlaying) {
                                    videoView.start()
                                } else if (!isPlaying && videoView.isPlaying) {
                                    videoView.pause()
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Ambient Audio Visualizer Wave Canvas
                        AudioVisualizerWave(isPlaying = isPlaying)
                    }

                    // Format Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(UbuntuAubergine.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                            .border(1.dp, UbuntuOrange.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.format.uppercase(),
                            color = UbuntuOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Track Title & Author Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = UbuntuWarmGrey
                    )
                }

                // Loop Toggle Button
                IconButton(
                    onClick = { viewModel.toggleMediaLoop() },
                    modifier = Modifier.testTag("media_loop_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Loop",
                        tint = if (isLooping) UbuntuOrange else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Seek Slider
            val maxDuration = if (durationMs > 0) durationMs.toFloat() else 1000f
            val currentPos = currentPositionMs.coerceIn(0L, durationMs).toFloat()

            Slider(
                value = currentPos,
                onValueChange = { newPos ->
                    viewModel.seekMediaTo(newPos.toLong())
                },
                valueRange = 0f..maxDuration,
                colors = SliderDefaults.colors(
                    thumbColor = UbuntuOrange,
                    activeTrackColor = UbuntuOrange,
                    inactiveTrackColor = Color(0xFF3A3A3A)
                ),
                modifier = Modifier.fillMaxWidth().testTag("media_seek_slider")
            )

            // Duration labels
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
            ) {
                Text(
                    text = formatMs(currentPositionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = UbuntuWarmGrey
                )
                Text(
                    text = formatMs(durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = UbuntuWarmGrey
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Playback Controls Row: [-10s, Play/Pause, +10s]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Rewind 10s
                IconButton(
                    onClick = { viewModel.seekMediaTo((currentPositionMs - 10000L).coerceAtLeast(0L)) },
                    modifier = Modifier.size(48.dp).testTag("media_rewind_10s")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10 seconds",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Play / Pause Button with Glowing Orange Aura
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Brush.linearGradient(listOf(UbuntuOrange, UbuntuLightOrange)),
                            CircleShape
                        )
                        .clickable { viewModel.toggleMediaPlayPause() }
                        .testTag("media_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Fast-Forward 10s
                IconButton(
                    onClick = { viewModel.seekMediaTo((currentPositionMs + 10000L).coerceAtMost(durationMs)) },
                    modifier = Modifier.size(48.dp).testTag("media_forward_10s")
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Forward 10 seconds",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback Speed Chips
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Speed:",
                    style = MaterialTheme.typography.labelSmall,
                    color = UbuntuWarmGrey,
                    modifier = Modifier.padding(end = 8.dp)
                )
                listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { s ->
                    val isSelected = (speed == s)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) UbuntuOrange else UbuntuDarkSurface)
                            .border(1.dp, if (isSelected) UbuntuOrange else Color(0xFF444444), RoundedCornerShape(8.dp))
                            .clickable { viewModel.setMediaSpeed(s) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${s}x",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Offline Bundled Tracks Selector
        Text(
            text = "BUILT-IN OFFLINE SAMPLES",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = UbuntuWarmGrey,
            modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, bottom = 6.dp)
        )

        val mediaLibrary by viewModel.mediaLibrary.collectAsState()
        mediaLibrary.take(3).forEach { track ->
            val isCurrent = currentItem?.id == track.id
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) UbuntuAubergine.copy(alpha = 0.4f) else UbuntuDarkSurface
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        1.dp,
                        if (isCurrent) UbuntuOrange else Color(0xFF333333),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { viewModel.playMedia(track) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = if (track.isVideo) Icons.Default.Videocam else Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isCurrent) UbuntuOrange else UbuntuWarmGrey,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) UbuntuOrange else Color.White
                        )
                        Text(
                            text = "${track.format.uppercase()} • ${formatMs(track.durationMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuWarmGrey
                        )
                    }
                    if (isCurrent && isPlaying) {
                        Text(
                            text = "PLAYING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = UbuntuOrange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioVisualizerWave(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_scale"
    )

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        val barCount = 18
        for (i in 0 until barCount) {
            val factor = if (isPlaying) {
                val offset = (i % 5) * 0.15f
                ((waveScale + offset) % 1.0f).coerceIn(0.2f, 1.0f)
            } else {
                0.25f
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .width(4.dp)
                    .height((60 * factor).dp)
                    .background(
                        Brush.verticalGradient(listOf(UbuntuLightOrange, UbuntuOrange)),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
private fun MediaDownloaderView(
    viewModel: BrowserViewModel,
    activeWebUrl: String
) {
    var urlInput by remember { mutableStateOf(activeWebUrl) }
    var selectedFormat by remember { mutableStateOf("mp4") } // "mp4" or "mp3"
    val downloads by viewModel.activeDownloads.collectAsState()

    LaunchedEffect(activeWebUrl) {
        if (urlInput.isBlank() && activeWebUrl.isNotBlank()) {
            urlInput = activeWebUrl
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Active Tab Auto-Detector Card
        if (activeWebUrl.isNotBlank() && (activeWebUrl.contains("youtube") || activeWebUrl.contains("youtu.be") || activeWebUrl.contains("vimeo") || activeWebUrl.endsWith(".mp4") || activeWebUrl.endsWith(".mp3"))) {
            Card(
                colors = CardDefaults.cardColors(containerColor = UbuntuAubergine.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .border(1.dp, UbuntuOrange, RoundedCornerShape(12.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = UbuntuSuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Media Stream Detected on Active Tab!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = activeWebUrl,
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuWarmGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = { urlInput = activeWebUrl },
                        colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Use URL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text(
            text = "DOWNLOAD VIDEO / AUDIO",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = UbuntuWarmGrey,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // URL Input Field
        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text("Video or Audio URL (YouTube, Vimeo, MP4, etc.)") },
            placeholder = { Text("https://m.youtube.com/watch?v=...") },
            trailingIcon = {
                if (urlInput.isNotBlank()) {
                    IconButton(onClick = { urlInput = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = UbuntuOrange,
                unfocusedBorderColor = Color(0xFF444444),
                focusedLabelColor = UbuntuOrange,
                unfocusedLabelColor = UbuntuWarmGrey,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = UbuntuDarkSurface,
                unfocusedContainerColor = UbuntuDarkSurface
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("downloader_url_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Format Selection Chips
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Format:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.padding(end = 12.dp)
            )

            FilterChip(
                selected = selectedFormat == "mp4",
                onClick = { selectedFormat = "mp4" },
                label = { Text("Video (MP4)") },
                leadingIcon = {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = UbuntuOrange,
                    selectedLabelColor = Color.White,
                    containerColor = UbuntuDarkSurface,
                    labelColor = Color.White.copy(alpha = 0.8f)
                ),
                modifier = Modifier.padding(end = 8.dp).testTag("chip_format_mp4")
            )

            FilterChip(
                selected = selectedFormat == "mp3",
                onClick = { selectedFormat = "mp3" },
                label = { Text("Audio (MP3/M4A)") },
                leadingIcon = {
                    Icon(Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = UbuntuOrange,
                    selectedLabelColor = Color.White,
                    containerColor = UbuntuDarkSurface,
                    labelColor = Color.White.copy(alpha = 0.8f)
                ),
                modifier = Modifier.testTag("chip_format_mp3")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Start Download Button
        Button(
            onClick = {
                if (urlInput.isNotBlank()) {
                    val isVideo = (selectedFormat == "mp4")
                    val derivedTitle = if (urlInput.contains("youtube") || urlInput.contains("youtu.be")) {
                        "YouTube Media (${if (isVideo) "Video" else "Audio"})"
                    } else {
                        urlInput.substringAfterLast("/").substringBefore("?").ifBlank { "Downloaded Media" }
                    }
                    viewModel.startDownload(
                        url = urlInput,
                        format = selectedFormat,
                        isVideo = isVideo,
                        title = derivedTitle
                    )
                }
            },
            enabled = urlInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = UbuntuOrange,
                disabledContainerColor = Color(0xFF333333)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("media_start_download_button")
        ) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Download with yt-dlp & aria2c", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Active / Recent Downloads Queue
        Text(
            text = "DOWNLOAD QUEUE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = UbuntuWarmGrey,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        if (downloads.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Text(
                    text = "No active downloads in queue.",
                    style = MaterialTheme.typography.bodySmall,
                    color = UbuntuWarmGrey
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                items(downloads) { task ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = when (task.status) {
                                        DownloadStatus.DOWNLOADING -> "${(task.progress * 100).toInt()}%"
                                        DownloadStatus.COMPLETED -> "Done"
                                        DownloadStatus.QUEUED -> "Queued"
                                        DownloadStatus.FAILED -> "Failed"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (task.status) {
                                        DownloadStatus.COMPLETED -> UbuntuSuccessGreen
                                        DownloadStatus.FAILED -> Color.Red
                                        else -> UbuntuOrange
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { task.progress },
                                color = UbuntuOrange,
                                trackColor = Color(0xFF333333),
                                modifier = Modifier.fillMaxWidth().height(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaLibraryAndCliView(
    viewModel: BrowserViewModel,
    onPlayItem: () -> Unit
) {
    val mediaList by viewModel.mediaLibrary.collectAsState()

    val cliTools = remember {
        listOf(
            CliToolInfo("yt-dlp", "2026.10.26", "/usr/local/bin/yt-dlp", "Universal stream extractor & video downloader"),
            CliToolInfo("ffmpeg", "4.4.2", "/usr/bin/ffmpeg", "Audio/video transcoder, muxer & normalization"),
            CliToolInfo("aria2c", "1.36.0", "/usr/bin/aria2c", "Multi-connection high-speed download accelerator"),
            CliToolInfo("mediainfo", "23.04", "/usr/bin/mediainfo", "Detailed codec, bitrate & container analyzer"),
            CliToolInfo("AtomicParsley", "2021", "/usr/bin/AtomicParsley", "ID3 & MP4 metadata and artwork tagging")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "DOWNLOADED & SAVED MEDIA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = UbuntuWarmGrey,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        items(mediaList) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
                    .clickable {
                        viewModel.playMedia(item)
                        onPlayItem()
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .background(UbuntuOrange.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = UbuntuOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.format.uppercase()} • ${formatBytes(item.sizeBytes)} • ${formatMs(item.durationMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuWarmGrey
                        )
                    }
                    IconButton(
                        onClick = { viewModel.deleteMediaItem(item) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "INSTALLED CLI ENGINE TOOLS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = UbuntuWarmGrey,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        items(cliTools) { tool ->
            Card(
                colors = CardDefaults.cardColors(containerColor = UbuntuAubergine.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, Color(0xFF442233), RoundedCornerShape(10.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = UbuntuOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tool.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "v${tool.version}",
                                style = MaterialTheme.typography.labelSmall,
                                color = UbuntuWarmGrey,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = tool.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = UbuntuWarmGrey,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(UbuntuSuccessGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "INSTALLED",
                            color = UbuntuSuccessGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val kb = bytes / 1024
    if (kb < 1024) return "$kb KB"
    val mb = kb / 1024f
    return String.format("%.1f MB", mb)
}
