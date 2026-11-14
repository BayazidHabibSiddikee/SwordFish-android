package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ReaderArticle
import com.example.data.model.ReaderContentWidth
import com.example.data.model.ReaderFontFamily
import com.example.data.model.ReaderLineHeight
import com.example.data.model.ReaderSettings
import com.example.data.model.ReaderTheme
import com.example.data.reader.ReaderModeEngine
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuWarmGrey
import com.example.viewmodel.BrowserViewModel

@Composable
fun ReaderModeView(
    article: ReaderArticle,
    viewModel: BrowserViewModel,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val readerSettings by viewModel.readerSettings.collectAsState()
    val isReadingAloud by viewModel.isReadingAloud.collectAsState()

    var showAppearanceControls by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val currentTheme = readerSettings.theme
    val currentBgColor = currentTheme.composeBackgroundColor
    val currentSurfaceColor = currentTheme.composeSurfaceColor
    val currentTextColor = currentTheme.composeTextColor
    val currentSecondaryText = currentTheme.composeSecondaryTextColor
    val currentAccentColor = currentTheme.composeAccentColor

    val activeFontFamily = remember(readerSettings.fontFamily) {
        when (readerSettings.fontFamily) {
            ReaderFontFamily.SANS_SERIF -> FontFamily.SansSerif
            ReaderFontFamily.SERIF -> FontFamily.Serif
            ReaderFontFamily.MONOSPACE -> FontFamily.Monospace
        }
    }

    val contentMaxWidth = remember(readerSettings.contentWidth) {
        when (readerSettings.contentWidth) {
            ReaderContentWidth.COMPACT -> 580.dp
            ReaderContentWidth.BALANCED -> 720.dp
            ReaderContentWidth.FULL -> 1200.dp
        }
    }

    val paragraphs = remember(article) {
        ReaderModeEngine.extractParagraphs(article)
    }

    // Scroll reading progress (0f..1f)
    val readingProgress by remember {
        derivedStateOf {
            if (scrollState.maxValue > 0) {
                (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
            } else 0f
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(currentBgColor)
            .testTag("reader_mode_view")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Bar for Reader Mode
            Surface(
                color = currentSurfaceColor,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        // Exit Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onExit() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("btn_exit_reader_mode")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Exit Reader Mode",
                                tint = currentAccentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Exit Reader",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = currentTextColor
                            )
                        }

                        // Center: Shield & Reading time indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false).padding(horizontal = 4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = currentAccentColor.copy(alpha = 0.15f),
                                border = BorderStroke(0.8.dp, currentAccentColor)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoStories,
                                        contentDescription = null,
                                        tint = currentAccentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${article.readingTimeMinutes}m read",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = currentAccentColor,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Right Actions: TTS Read Aloud, Appearance (Aa), Share, Copy
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Text-To-Speech Read Aloud
                            IconButton(
                                onClick = {
                                    viewModel.toggleReadAloud(article.plainText)
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("btn_reader_read_aloud")
                            ) {
                                Icon(
                                    imageVector = if (isReadingAloud) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = if (isReadingAloud) "Stop Reading Aloud" else "Read Aloud",
                                    tint = if (isReadingAloud) Color(0xFFDF382C) else currentTextColor
                                )
                            }

                            // Appearance Settings (Aa)
                            IconButton(
                                onClick = { showAppearanceControls = !showAppearanceControls },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("btn_reader_appearance")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (showAppearanceControls) currentAccentColor.copy(alpha = 0.25f)
                                            else Color.Transparent,
                                            CircleShape
                                        )
                                ) {
                                    Text(
                                        text = "Aa",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        fontFamily = activeFontFamily,
                                        color = if (showAppearanceControls) currentAccentColor else currentTextColor
                                    )
                                }
                            }

                            // Copy Clean Text
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(article.plainText))
                                    Toast.makeText(context, "Clean article text copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("btn_copy_reader_text")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Text",
                                    tint = currentTextColor.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Share Clean Article
                            IconButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "${article.title}\n\n${article.url}\n\nCleaned via Swordfish Reader Mode"
                                        )
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Clean Article"))
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("btn_share_reader_article")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = currentTextColor.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Reading progress indicator
                    LinearProgressIndicator(
                        progress = { readingProgress },
                        color = currentAccentColor,
                        trackColor = currentSurfaceColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                    )
                }
            }

            // Collapsible Appearance Controls Tray
            AnimatedVisibility(
                visible = showAppearanceControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ReaderAppearanceControls(
                    settings = readerSettings,
                    currentSurfaceColor = currentSurfaceColor,
                    currentTextColor = currentTextColor,
                    currentAccentColor = currentAccentColor,
                    onUpdateTheme = { viewModel.updateReaderTheme(it) },
                    onUpdateFont = { viewModel.updateReaderFontFamily(it) },
                    onAdjustFontSize = { viewModel.updateReaderFontSize(it) },
                    onUpdateLineHeight = { viewModel.updateReaderLineHeight(it) },
                    onUpdateContentWidth = { viewModel.updateReaderContentWidth(it) },
                    onClose = { showAppearanceControls = false }
                )
            }

            // Main Article Body Scrollable
            SelectionContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = contentMaxWidth)
                    ) {

                        // Site & Security Tag
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (article.siteName.isNotBlank()) {
                                Surface(
                                    color = currentSurfaceColor,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, currentSecondaryText.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            tint = currentAccentColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = article.siteName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = currentTextColor,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Surface(
                                color = UbuntuOrange.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, UbuntuOrange.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = UbuntuOrange,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Distraction-Free Mode",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = UbuntuOrange,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Article Title
                        Text(
                            text = article.title,
                            fontFamily = activeFontFamily,
                            fontSize = (readerSettings.fontSizeSp + 8).sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTextColor,
                            lineHeight = (readerSettings.fontSizeSp + 14).sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reader_article_title")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Byline, Date & Word Count Metadata
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!article.byline.isNullOrBlank()) {
                                Text(
                                    text = "By ${article.byline}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = currentSecondaryText,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "•", color = currentSecondaryText)
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            if (!article.publishedDate.isNullOrBlank()) {
                                Text(
                                    text = article.publishedDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = currentSecondaryText,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "•", color = currentSecondaryText)
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            Text(
                                text = "${article.wordCount} words",
                                style = MaterialTheme.typography.bodySmall,
                                color = currentSecondaryText,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(
                            color = currentSecondaryText.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Optional Hero / Featured Image
                        if (!article.topImageUrl.isNullOrBlank()) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp),
                                colors = CardDefaults.cardColors(containerColor = currentSurfaceColor)
                            ) {
                                AsyncImage(
                                    model = article.topImageUrl,
                                    contentDescription = "Featured Article Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                )
                            }
                        }

                        // Clean Extracted Paragraphs
                        val lineMultiplier = readerSettings.lineHeight.multiplier
                        val calculatedLineHeight = (readerSettings.fontSizeSp * lineMultiplier).sp

                        if (paragraphs.isEmpty()) {
                            Text(
                                text = article.plainText.ifBlank { "Article body text extracted successfully without advertisements." },
                                fontFamily = activeFontFamily,
                                fontSize = readerSettings.fontSizeSp.sp,
                                lineHeight = calculatedLineHeight,
                                color = currentTextColor,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            paragraphs.forEachIndexed { index, para ->
                                val isHeading = para.length < 90 && !para.endsWith(".") && !para.endsWith("?") &&
                                        (para.startsWith("#") || index % 6 == 3 || para.all { it.isLetter() || it.isWhitespace() })

                                if (isHeading && index > 0) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = para.removePrefix("#").trim(),
                                        fontFamily = activeFontFamily,
                                        fontSize = (readerSettings.fontSizeSp + 4).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = currentAccentColor,
                                        lineHeight = (readerSettings.fontSizeSp + 8).sp,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                } else if (para.startsWith(">") || para.startsWith("\"")) {
                                    // Blockquote styling
                                    Surface(
                                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                                        color = currentSurfaceColor,
                                        border = BorderStroke(1.dp, currentSecondaryText.copy(alpha = 0.15f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(4.dp)
                                                    .height(48.dp)
                                                    .background(currentAccentColor, RoundedCornerShape(2.dp))
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = para.removePrefix(">").trim(),
                                                fontFamily = activeFontFamily,
                                                fontSize = readerSettings.fontSizeSp.sp,
                                                fontStyle = FontStyle.Italic,
                                                lineHeight = calculatedLineHeight,
                                                color = currentTextColor
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = para,
                                        fontFamily = activeFontFamily,
                                        fontSize = readerSettings.fontSizeSp.sp,
                                        lineHeight = calculatedLineHeight,
                                        color = currentTextColor,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        HorizontalDivider(
                            color = currentSecondaryText.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Footer Card: Distraction-free Summary & Exit
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = currentSurfaceColor),
                            border = BorderStroke(1.dp, currentAccentColor.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = currentAccentColor,
                                    modifier = Modifier.size(30.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "End of Article",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = currentTextColor
                                )
                                Text(
                                    text = "Ads, trackers, sidebars, and navigational elements were eliminated.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = currentSecondaryText,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onExit,
                                    colors = ButtonDefaults.buttonColors(containerColor = currentAccentColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Return to Original Webpage",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderAppearanceControls(
    settings: ReaderSettings,
    currentSurfaceColor: Color,
    currentTextColor: Color,
    currentAccentColor: Color,
    onUpdateTheme: (ReaderTheme) -> Unit,
    onUpdateFont: (ReaderFontFamily) -> Unit,
    onAdjustFontSize: (Int) -> Unit,
    onUpdateLineHeight: (ReaderLineHeight) -> Unit,
    onUpdateContentWidth: (ReaderContentWidth) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = currentSurfaceColor,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, currentAccentColor.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "READER APPEARANCE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = currentAccentColor,
                    fontSize = 11.sp
                )
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = currentTextColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Theme Color Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReaderTheme.entries.forEach { theme ->
                    val isSelected = theme == settings.theme
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(theme.backgroundColor),
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) currentAccentColor else Color.Gray.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .clickable { onUpdateTheme(theme) }
                            .testTag("theme_chip_${theme.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(Color(theme.surfaceColor), CircleShape)
                                    .border(1.dp, Color(theme.textColor), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = theme.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(theme.textColor),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = currentAccentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Font Family Selector & Font Size Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Font Family Pills
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ReaderFontFamily.entries.forEach { font ->
                        val isSelected = font == settings.fontFamily
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) currentAccentColor.copy(alpha = 0.2f) else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) currentAccentColor else currentTextColor.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.clickable { onUpdateFont(font) }
                        ) {
                            Text(
                                text = font.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) currentAccentColor else currentTextColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Font Size Stepper A- / A+
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = currentSurfaceColor,
                    border = BorderStroke(1.dp, currentTextColor.copy(alpha = 0.3f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { onAdjustFontSize(-1) }
                                .testTag("btn_font_size_decrease")
                        ) {
                            Text(
                                text = "A-",
                                fontWeight = FontWeight.Bold,
                                color = currentTextColor,
                                fontSize = 13.sp
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Text(
                                text = "${settings.fontSizeSp}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = currentAccentColor
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { onAdjustFontSize(1) }
                                .testTag("btn_font_size_increase")
                        ) {
                            Text(
                                text = "A+",
                                fontWeight = FontWeight.Bold,
                                color = currentTextColor,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Line Spacing & Width Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Line Spacing
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Spacing:",
                        style = MaterialTheme.typography.labelSmall,
                        color = currentTextColor.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                    ReaderLineHeight.entries.forEach { lh ->
                        val isSelected = lh == settings.lineHeight
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) currentAccentColor.copy(alpha = 0.2f) else Color.Transparent,
                            border = BorderStroke(
                                0.8.dp,
                                if (isSelected) currentAccentColor else currentTextColor.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.clickable { onUpdateLineHeight(lh) }
                        ) {
                            Text(
                                text = lh.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) currentAccentColor else currentTextColor,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Width
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Width:",
                        style = MaterialTheme.typography.labelSmall,
                        color = currentTextColor.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                    ReaderContentWidth.entries.forEach { w ->
                        val isSelected = w == settings.contentWidth
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) currentAccentColor.copy(alpha = 0.2f) else Color.Transparent,
                            border = BorderStroke(
                                0.8.dp,
                                if (isSelected) currentAccentColor else currentTextColor.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.clickable { onUpdateContentWidth(w) }
                        ) {
                            Text(
                                text = w.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) currentAccentColor else currentTextColor,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
