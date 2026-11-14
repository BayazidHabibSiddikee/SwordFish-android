package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.ui.theme.ThemeOnAccent
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.blocker.AppBlockerEngine
import com.example.data.local.BookmarkEntity
import com.example.data.local.MostVisitedSite
import com.example.data.model.FilterSettings
import com.example.data.model.SearchEngine
import com.example.ui.theme.LocalUbuntuColors
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuCardDark
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.UbuntuDarkOrange
import com.example.ui.theme.UbuntuDarkSurface
import com.example.ui.theme.UbuntuDeepAubergine
import com.example.ui.theme.UbuntuLightOrange
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuSuccessGreen
import com.example.ui.theme.UbuntuWarmGrey
import com.example.viewmodel.ActiveSheet
import com.example.viewmodel.BrowserViewModel

@Composable
fun NewTabPage(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    var queryText by remember { mutableStateOf("") }
    val shieldStats by viewModel.shieldStats.collectAsState()
    val isFocusActive by viewModel.isFocusModeActive.collectAsState()
    val focusSeconds by viewModel.focusTimeRemainingSeconds.collectAsState()
    val allBookmarks by viewModel.bookmarks.collectAsState()
    val quickBookmarks by viewModel.quickAccessBookmarks.collectAsState()
    val mostVisited by viewModel.mostVisitedSites.collectAsState()
    val selectedSearchEngine by viewModel.selectedSearchEngine.collectAsState()
    val filterSettings by viewModel.filterSettings.collectAsState()
    val cachedPageCount by viewModel.cachedPageCount.collectAsState()
    val totalCacheSize by viewModel.totalCacheSizeBytes.collectAsState()
    val frequentlyVisitedCached by viewModel.frequentlyVisitedCachedPages.collectAsState()
    val bookmarkFolders by viewModel.bookmarkFolders.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val theme = LocalUbuntuColors.current

    var selectedQuickCategory by remember { mutableStateOf("All") }
    var showAddFavoriteDialog by remember { mutableStateOf(false) }
    var editingFavorite by remember { mutableStateOf<BookmarkEntity?>(null) }

    val categories = remember(bookmarkFolders) {
        val names = bookmarkFolders.map { it.name }.toMutableList()
        if (!names.contains("Favorites")) names.add(0, "Favorites")
        (listOf("All") + names).distinct()
    }

    // Effective bookmarks to display in Quick Access
    val effectiveFavorites = remember(quickBookmarks, allBookmarks, selectedQuickCategory) {
        val baseList = if (quickBookmarks.isNotEmpty()) quickBookmarks else allBookmarks
        if (selectedQuickCategory == "All") {
            baseList
        } else {
            baseList.filter {
                it.folder.equals(selectedQuickCategory, ignoreCase = true) ||
                it.category.equals(selectedQuickCategory, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(theme.surface, theme.background)
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top bar: wordmark + theme switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        Brush.linearGradient(listOf(UbuntuOrange, UbuntuLightOrange)),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Swordfish",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                onClick = { viewModel.toggleTheme() },
                shape = CircleShape,
                color = theme.card,
                border = BorderStroke(1.dp, theme.border),
                modifier = Modifier.testTag("new_tab_theme_toggle")
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isDarkTheme) {
                        "Switch to light theme"
                    } else {
                        "Switch to dark theme"
                    },
                    tint = theme.textSecondary,
                    modifier = Modifier
                        .padding(9.dp)
                        .size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Hero mark, wordmark and tagline
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .background(
                    Brush.linearGradient(listOf(UbuntuOrange, UbuntuLightOrange)),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Swordfish",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Private browsing, built in.",
            style = MaterialTheme.typography.bodyMedium,
            color = theme.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Search
        OutlinedTextField(
            value = queryText,
            onValueChange = { queryText = it },
            placeholder = {
                Text(
                    "Search or enter address",
                    color = theme.textSecondary,
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = theme.textSecondary
                )
            },
            trailingIcon = {
                if (queryText.isNotBlank()) {
                    IconButton(onClick = {
                        val input = queryText
                        queryText = ""
                        focusManager.clearFocus()
                        viewModel.loadUrl(input)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Go",
                            tint = UbuntuOrange
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = theme.card,
                unfocusedContainerColor = theme.card,
                focusedBorderColor = UbuntuOrange,
                unfocusedBorderColor = theme.border,
                focusedTextColor = theme.textPrimary,
                unfocusedTextColor = theme.textPrimary,
                cursorColor = UbuntuOrange
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = {
                val input = queryText
                queryText = ""
                focusManager.clearFocus()
                viewModel.loadUrl(input)
            }),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_search_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search engine picker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            SearchEngine.entries.forEach { engine ->
                val isSelected = engine == selectedSearchEngine
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) {
                        UbuntuOrange.copy(alpha = 0.16f)
                    } else {
                        Color.Transparent
                    },
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) UbuntuOrange.copy(alpha = 0.7f) else theme.border
                    ),
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable { viewModel.setSearchEngine(engine) }
                ) {
                    Text(
                        text = engine.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) UbuntuOrange else theme.textSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ==========================================================
        // SECTION 1: QUICK ACCESS FAVORITES
        // ==========================================================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = theme.card),
            border = BorderStroke(1.dp, theme.border),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quick_access_favorites_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .background(UbuntuOrange.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = UbuntuOrange,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Favorites",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                            Text(
                                text = "${allBookmarks.size} saved",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.textSecondary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick Add Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = UbuntuOrange,
                            modifier = Modifier
                                .clickable {
                                    editingFavorite = null
                                    showAddFavoriteDialog = true
                                }
                                .testTag("quick_add_favorite_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add favorite",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Manage",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textSecondary,
                            modifier = Modifier
                                .clickable { viewModel.openSheet(ActiveSheet.BOOKMARKS_HISTORY) }
                                .testTag("open_bookmarks_manager_btn")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Filter Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat.equals(selectedQuickCategory, ignoreCase = true)
                        val count = if (cat == "All") {
                            allBookmarks.size
                        } else {
                            allBookmarks.count { it.category.equals(cat, ignoreCase = true) }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) {
                                UbuntuOrange.copy(alpha = 0.16f)
                            } else {
                                Color.Transparent
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) UbuntuOrange.copy(alpha = 0.7f) else theme.border
                            ),
                            modifier = Modifier
                                .clickable { selectedQuickCategory = cat }
                                .testTag("quick_cat_chip_${cat.lowercase()}")
                        ) {
                            Text(
                                text = if (cat == "All") "All ($count)" else "$cat ($count)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) UbuntuOrange else theme.textSecondary,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Grid of Favorite Bookmarks (plus Add Favorite Tile)
                if (allBookmarks.isEmpty()) {
                    // Empty state banner
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = theme.surface,
                        border = BorderStroke(1.dp, theme.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.seedUbuntuFavorites()
                                Toast.makeText(context, "Starter bookmarks loaded", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Nothing saved yet",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = theme.textPrimary
                                )
                                Text(
                                    text = "Add a site to see it here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = theme.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Surface(
                                color = UbuntuOrange,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "Add starters",
                                    color = ThemeOnAccent,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Chunk into rows of 4 items
                    val itemsToShow = effectiveFavorites.take(12)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        itemsToShow.chunked(4).forEach { rowFavorites ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                rowFavorites.forEach { fav ->
                                    QuickFavoriteTile(
                                        bookmark = fav,
                                        onOpen = { viewModel.loadUrl(fav.url) },
                                        onOpenNewTab = {
                                            viewModel.openNewTab(fav.url)
                                            Toast.makeText(context, "Opened in new tab", Toast.LENGTH_SHORT).show()
                                        },
                                        onEdit = {
                                            editingFavorite = fav
                                            showAddFavoriteDialog = true
                                        },
                                        onCopy = {
                                            clipboardManager.setText(AnnotatedString(fav.url))
                                            Toast.makeText(context, "Copied URL: ${fav.url}", Toast.LENGTH_SHORT).show()
                                        },
                                        onDelete = {
                                            viewModel.removeQuickFavorite(fav)
                                            Toast.makeText(context, "Removed favorite", Toast.LENGTH_SHORT).show()
                                        },
                                        onTogglePin = {
                                            viewModel.togglePinBookmark(fav)
                                        }
                                    )
                                }
                                // Fill empty slots in row if less than 4
                                repeat(4 - rowFavorites.size) {
                                    Spacer(modifier = Modifier.width(76.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================================
        // SECTION 2: MOST VISITED SITES (CALCULATED FROM ROOM DB)
        // ==========================================================
        if (mostVisited.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("most_visited_sites_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(UbuntuOrange.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = UbuntuOrange,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Most visited",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.textPrimary
                            )
                        }

                        Text(
                            text = "${mostVisited.size} sites",
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(mostVisited) { site ->
                            MostVisitedChip(
                                site = site,
                                isBookmarked = allBookmarks.any { it.url == site.url },
                                onClick = { viewModel.loadUrl(site.url) },
                                onBookmark = {
                                    viewModel.addQuickFavorite(
                                        title = site.title,
                                        url = site.url,
                                        category = "Favorites"
                                    )
                                    Toast.makeText(context, "Added to Favorites!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // ==========================================================
        // SECTION: OFFLINE WEB CACHE (ROOM DATABASE PERSISTENCE)
        // ==========================================================
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = theme.card),
            border = BorderStroke(1.dp, theme.border),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.openOfflineCacheManager() }
                .testTag("offline_cache_home_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                UbuntuOrange.copy(alpha = 0.14f),
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = UbuntuOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Offline pages",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = if (cachedPageCount > 0)
                                "$cachedPageCount saved • ${android.text.format.Formatter.formatFileSize(context, totalCacheSize)}"
                            else
                                "Pages you visit are cached for offline reading",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = theme.textSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // If cached pages exist, show quick offline access row
                if (frequentlyVisitedCached.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Recent offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.textSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(frequentlyVisitedCached) { page ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = theme.surface,
                                border = BorderStroke(1.dp, theme.border),
                                modifier = Modifier
                                    .clickable { viewModel.loadCachedPage(page) }
                                    .testTag("offline_chip_${page.url.hashCode()}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudOff,
                                        contentDescription = null,
                                        tint = UbuntuOrange,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = page.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = theme.textPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${page.domain} • ${page.visitCount} visits",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = theme.textSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================================
        // SECTION 3: SWORDFISH PRIVACY & SECURITY SHIELD STATUS
        // ==========================================================
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = theme.card),
            border = BorderStroke(1.dp, theme.border),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.openSettings() }
                .testTag("shield_status_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                Brush.linearGradient(listOf(UbuntuOrange, UbuntuDeepAubergine)),
                                RoundedCornerShape(11.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield Active",
                            tint = ThemeOnAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Shield is on",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = "${shieldStats.totalAdsBlocked} ads • ${shieldStats.totalTrackersBlocked} trackers blocked",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Shield settings",
                    tint = theme.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================================
        // SECTION 4: QUICK BROWSER TOOLS & NAVIGATION TILES (4 TILES)
        // ==========================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tile 1: Bookmarks & History
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = UbuntuDarkSurface,
                border = BorderStroke(1.dp, Color(0xFF333333)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.openSheet(ActiveSheet.BOOKMARKS_HISTORY) }
                    .testTag("home_tile_bookmarks")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(UbuntuOrange.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = UbuntuOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Bookmarks",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${allBookmarks.size} saved",
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuWarmGrey,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Tile 2: Downloads & Media
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = UbuntuDarkSurface,
                border = BorderStroke(1.dp, Color(0xFF333333)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.openSheet(ActiveSheet.MEDIA_HUB) }
                    .testTag("home_tile_downloads")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF2563EB).copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Downloads",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Files & Media",
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuWarmGrey,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tile 3: Password Vault
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = UbuntuDarkSurface,
                border = BorderStroke(1.dp, Color(0xFF333333)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.openSheet(ActiveSheet.PASSWORD_VAULT) }
                    .testTag("home_tile_vault")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(UbuntuSuccessGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = UbuntuSuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Vault",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "AES-256",
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuWarmGrey,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Tile 4: Settings & Blockers
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = UbuntuDarkSurface,
                border = BorderStroke(1.dp, Color(0xFF333333)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.openSettings() }
                    .testTag("home_tile_settings")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(UbuntuOrange.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = UbuntuOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${filterSettings.totalActiveFilters} filters",
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuWarmGrey,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }

    // Add / Edit Favorite Modal Dialog directly on New Tab Page
    if (showAddFavoriteDialog) {
        QuickAddFavoriteDialog(
            initialBookmark = editingFavorite,
            onDismiss = {
                showAddFavoriteDialog = false
                editingFavorite = null
            },
            onSave = { title, url, category, notes ->
                if (editingFavorite != null && editingFavorite!!.id > 0) {
                    viewModel.updateBookmark(
                        id = editingFavorite!!.id,
                        title = title,
                        url = url,
                        folder = category,
                        notes = notes
                    )
                    Toast.makeText(context, "Favorite updated!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addQuickFavorite(
                        title = title,
                        url = url,
                        category = category,
                        notes = notes
                    )
                    Toast.makeText(context, "Added to Favorites & Quick Access!", Toast.LENGTH_SHORT).show()
                }
                showAddFavoriteDialog = false
                editingFavorite = null
            }
        )
    }
}

@Composable
fun QuickFavoriteTile(
    bookmark: BookmarkEntity,
    onOpen: () -> Unit,
    onOpenNewTab: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val displayHost = remember(bookmark.url) {
        try {
            Uri.parse(bookmark.url).host?.removePrefix("www.") ?: bookmark.url
        } catch (_: Exception) {
            bookmark.url
        }
    }

    val initialLetter = remember(bookmark.title, displayHost) {
        val src = bookmark.title.ifBlank { displayHost }
        src.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "F"
    }

    val tileBrush = remember(bookmark.category) {
        when (bookmark.category.lowercase()) {
            "ubuntu" -> Brush.linearGradient(listOf(UbuntuOrange, UbuntuDeepAubergine))
            "dev" -> Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF1E293B)))
            "media" -> Brush.linearGradient(listOf(Color(0xFFDC2626), UbuntuOrange))
            "news" -> Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF047857)))
            "social" -> Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF4C1D95)))
            else -> Brush.linearGradient(listOf(UbuntuOrange, UbuntuLightOrange))
        }
    }

    Box(contentAlignment = Alignment.TopCenter) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(76.dp)
                .clickable(onClick = onOpen)
                .padding(vertical = 4.dp)
                .testTag("quick_favorite_${bookmark.id}")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(tileBrush, RoundedCornerShape(14.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            ) {
                Text(
                    text = initialLetter,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Pin indicator if pinned
                if (bookmark.isPinnedToNewTab) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(10.dp)
                            .background(UbuntuOrange, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = bookmark.title.ifBlank { displayHost },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            if (bookmark.visitCount > 0) {
                Text(
                    text = "${bookmark.visitCount} visits",
                    style = MaterialTheme.typography.labelSmall,
                    color = UbuntuWarmGrey,
                    fontSize = 9.sp
                )
            }
        }

        // 3-dots Menu trigger
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 2.dp, end = 2.dp)
        ) {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(24.dp).testTag("fav_menu_btn_${bookmark.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(UbuntuDarkSurface)
            ) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open in New Tab", color = Color.White, fontSize = 13.sp)
                        }
                    },
                    onClick = {
                        showMenu = false
                        onOpenNewTab()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy Link", color = Color.White, fontSize = 13.sp)
                        }
                    },
                    onClick = {
                        showMenu = false
                        onCopy()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Favorite", color = Color.White, fontSize = 13.sp)
                        }
                    },
                    onClick = {
                        showMenu = false
                        onEdit()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (bookmark.isPinnedToNewTab) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                contentDescription = null,
                                tint = UbuntuOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (bookmark.isPinnedToNewTab) "Unpin from Quick Access" else "Pin to Quick Access",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    },
                    onClick = {
                        showMenu = false
                        onTogglePin()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Remove", color = Color(0xFFEF4444), fontSize = 13.sp)
                        }
                    },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
fun MostVisitedChip(
    site: MostVisitedSite,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onBookmark: () -> Unit
) {
    val displayHost = remember(site.url) {
        try {
            Uri.parse(site.url).host?.removePrefix("www.") ?: site.url
        } catch (_: Exception) {
            site.url
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = UbuntuDarkSurface,
        border = BorderStroke(1.dp, UbuntuAubergine),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("most_visited_${displayHost.replace(".", "_")}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .background(UbuntuAubergine.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            ) {
                Text(
                    text = displayHost.firstOrNull { it.isLetter() }?.uppercase() ?: "W",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = UbuntuLightOrange
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = site.title.ifBlank { displayHost },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 120.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${site.visitCount} visits",
                        style = MaterialTheme.typography.labelSmall,
                        color = UbuntuOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            if (!isBookmarked) {
                IconButton(
                    onClick = onBookmark,
                    modifier = Modifier.size(24.dp).testTag("bookmark_most_visited_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Save to favorites",
                        tint = UbuntuOrange,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickAddFavoriteDialog(
    initialBookmark: BookmarkEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, url: String, category: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf(initialBookmark?.title ?: "") }
    var url by remember { mutableStateOf(initialBookmark?.url ?: "") }
    var category by remember { mutableStateOf(initialBookmark?.folder ?: initialBookmark?.category ?: "Favorites") }
    var notes by remember { mutableStateOf(initialBookmark?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presetCategories = remember {
        listOf("Favorites", "Ubuntu", "Dev", "Media", "News", "Social")
    }

    val quickPresets = remember {
        listOf(
            Triple("DuckDuckGo", "https://duckduckgo.com", "Favorites"),
            Triple("Ubuntu", "https://ubuntu.com", "Ubuntu"),
            Triple("GitHub", "https://github.com", "Dev"),
            Triple("YouTube", "https://m.youtube.com", "Media"),
            Triple("Wikipedia", "https://www.wikipedia.org", "Favorites")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UbuntuDarkBackground,
        shape = RoundedCornerShape(18.dp),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(UbuntuOrange, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialBookmark != null) "Edit Favorite" else "Add Quick Favorite",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(listOf(UbuntuOrange, UbuntuAubergine, Color.Transparent))
                        )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Presets chip row if adding new
                if (initialBookmark == null) {
                    Text(
                        text = "Quick Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        color = UbuntuWarmGrey
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickPresets.forEach { (pTitle, pUrl, pCat) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = UbuntuDarkSurface,
                                border = BorderStroke(1.dp, Color(0xFF3F3F3F)),
                                modifier = Modifier.clickable {
                                    title = pTitle
                                    url = pUrl
                                    category = pCat
                                }
                            ) {
                                Text(
                                    text = pTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = UbuntuOrange,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Title", color = UbuntuWarmGrey) },
                    placeholder = { Text("e.g. Ubuntu Official", color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = UbuntuDarkSurface,
                        unfocusedContainerColor = UbuntuDarkSurface,
                        focusedBorderColor = UbuntuOrange,
                        unfocusedBorderColor = Color(0xFF3F3F3F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("favorite_title_input")
                )

                // URL Input
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        errorMessage = null
                    },
                    label = { Text("URL", color = UbuntuWarmGrey) },
                    placeholder = { Text("https://...", color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = UbuntuDarkSurface,
                        unfocusedContainerColor = UbuntuDarkSurface,
                        focusedBorderColor = UbuntuOrange,
                        unfocusedBorderColor = Color(0xFF3F3F3F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("favorite_url_input")
                )

                // Category Selector
                Text(
                    text = "Category:",
                    style = MaterialTheme.typography.labelSmall,
                    color = UbuntuWarmGrey
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetCategories.forEach { cat ->
                        val isSelected = cat.equals(category, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) UbuntuOrange else UbuntuDarkSurface,
                            border = BorderStroke(1.dp, if (isSelected) UbuntuOrange else Color(0xFF3F3F3F)),
                            modifier = Modifier.clickable { category = cat }
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else UbuntuWarmGrey,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Error message
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rawUrl = url.trim()
                    if (rawUrl.isBlank()) {
                        errorMessage = "Please enter a valid website URL."
                        return@Button
                    }
                    val finalUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
                        "https://$rawUrl"
                    } else {
                        rawUrl
                    }
                    val finalTitle = title.trim().ifBlank {
                        try {
                            Uri.parse(finalUrl).host ?: finalUrl
                        } catch (_: Exception) {
                            finalUrl
                        }
                    }
                    onSave(finalTitle, finalUrl, category, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_favorite_dialog_btn")
            ) {
                Text("Save to Room DB", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

@Composable
fun StatPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = UbuntuWarmGrey
        )
    }
}
