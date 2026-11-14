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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookmarkEntity
import com.example.data.local.BookmarkFolderEntity
import com.example.data.local.HistoryEntity
import com.example.data.model.HistoryTimeRange
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.UbuntuDarkSurface
import com.example.ui.theme.UbuntuDeepAubergine
import com.example.ui.theme.UbuntuLightOrange
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuSuccessGreen
import com.example.ui.theme.UbuntuWarmGrey
import com.example.viewmodel.BookmarkSortOption
import com.example.viewmodel.BrowserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksHistorySheet(
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTab by remember { mutableIntStateOf(0) }
    val bookmarks by viewModel.bookmarks.collectAsState()
    val filteredBookmarks by viewModel.filteredBookmarks.collectAsState()
    val searchQuery by viewModel.bookmarkSearchQuery.collectAsState()
    val bookmarkFolders by viewModel.bookmarkFolders.collectAsState()
    val bookmarkLabels by viewModel.bookmarkLabels.collectAsState()
    val selectedFolder by viewModel.selectedBookmarkFolder.collectAsState()
    val selectedLabel by viewModel.selectedBookmarkLabel.collectAsState()
    val sortOption by viewModel.bookmarkSortOption.collectAsState()
    val history by viewModel.history.collectAsState()
    val filteredHistory by viewModel.filteredHistory.collectAsState()
    val historySearchQuery by viewModel.historySearchQuery.collectAsState()
    val selectedHistoryPeriod by viewModel.selectedHistoryPeriod.collectAsState()
    val cachedPages by viewModel.filteredCachedPages.collectAsState()
    val cachedPageCount by viewModel.cachedPageCount.collectAsState()
    val cacheSearchQuery by viewModel.cacheSearchQuery.collectAsState()

    // Dialog States
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingBookmark by remember { mutableStateOf<BookmarkEntity?>(null) }
    var bookmarkToDelete by remember { mutableStateOf<BookmarkEntity?>(null) }
    var bookmarkToMove by remember { mutableStateOf<BookmarkEntity?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var folderToManage by remember { mutableStateOf<BookmarkFolderEntity?>(null) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var selectedClearTimeRange by remember { mutableStateOf(HistoryTimeRange.ALL_TIME) }

    val activeTab = viewModel.activeTab
    val hasActivePage = activeTab != null && activeTab.url.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = UbuntuDarkBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .navigationBarsPadding()
        ) {
            // Ubuntu Window Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Window control dots (Ubuntu Style)
                    Box(modifier = Modifier.size(11.dp).background(UbuntuOrange, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(11.dp).background(UbuntuAubergine, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(11.dp).background(UbuntuWarmGrey, CircleShape))

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = when (selectedTab) {
                            0 -> "Bookmarks & Favorites"
                            1 -> "Browsing History"
                            else -> "Offline Web Cache"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = UbuntuOrange.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, UbuntuOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when (selectedTab) {
                                0 -> "${bookmarks.size}"
                                1 -> "${filteredHistory.size}"
                                else -> "$cachedPageCount"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = UbuntuOrange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedTab == 1 && history.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { showClearHistoryDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("clear_history_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear History",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Clear History",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp).testTag("close_sheet_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Selector
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = UbuntuDarkSurface,
                contentColor = UbuntuOrange,
                modifier = Modifier.background(UbuntuDarkSurface, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.height(48.dp).testTag("tab_bookmarks"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = if (selectedTab == 0) UbuntuOrange else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Bookmarks",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) UbuntuOrange else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.height(48.dp).testTag("tab_history"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = if (selectedTab == 1) UbuntuOrange else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "History",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) UbuntuOrange else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.height(48.dp).testTag("tab_offline_cache"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (selectedTab == 2) UbuntuOrange else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Offline Cache",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 2) UbuntuOrange else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                // Bookmarks Tab Content
                // Search Input with Ubuntu Terminal aesthetic
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setBookmarkSearchQuery(it) },
                    placeholder = {
                        Text(
                            "Search favorites, URLs, notes...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = UbuntuOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = { viewModel.setBookmarkSearchQuery("") },
                                modifier = Modifier.size(24.dp).testTag("clear_search_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bookmark_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons Row: Add Bookmark + Add Current Page + Create Folder + Ubuntu Starter + Sort
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Add Bookmark Button
                    Button(
                        onClick = {
                            editingBookmark = null
                            showAddEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(40.dp).testTag("add_bookmark_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }

                    // Add Current Page (if active tab has a URL)
                    if (hasActivePage) {
                        OutlinedButton(
                            onClick = {
                                editingBookmark = BookmarkEntity(
                                    title = activeTab?.title ?: "",
                                    url = activeTab?.url ?: "",
                                    category = "Favorites",
                                    folder = "Favorites"
                                )
                                showAddEditDialog = true
                            },
                            border = BorderStroke(1.dp, UbuntuOrange.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(40.dp).testTag("add_current_page_bookmark_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = UbuntuOrange,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Current Page", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    // New Custom Folder Button
                    OutlinedButton(
                        onClick = { showNewFolderDialog = true },
                        border = BorderStroke(1.dp, UbuntuOrange.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(40.dp).testTag("create_new_folder_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = null,
                            tint = UbuntuOrange,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Folder", color = Color.White, fontSize = 12.sp)
                    }

                    // Ubuntu Starter Pack Seeder Button
                    OutlinedButton(
                        onClick = {
                            viewModel.seedUbuntuFavorites()
                            Toast.makeText(context, "Ubuntu favorites loaded", Toast.LENGTH_SHORT).show()
                        },
                        border = BorderStroke(1.dp, Color(0xFF3F3F3F)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(40.dp).testTag("seed_ubuntu_bookmarks_btn")
                    ) {
                        Text("Ubuntu Pack", color = UbuntuLightOrange, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Sort Order Menu Button
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.size(40.dp).testTag("bookmark_sort_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort bookmarks",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(UbuntuDarkSurface)
                        ) {
                            BookmarkSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                option.displayName,
                                                color = if (sortOption == option) UbuntuOrange else Color.White,
                                                fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (sortOption == option) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = UbuntuOrange,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.setBookmarkSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Organizer Folders Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FOLDERS",
                        style = MaterialTheme.typography.labelSmall,
                        color = UbuntuWarmGrey,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${bookmarkFolders.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = UbuntuOrange,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "All" Folders Chip
                    val isAllSelected = selectedFolder.equals("All", ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAllSelected) UbuntuOrange else UbuntuDarkSurface,
                        border = BorderStroke(
                            1.dp,
                            if (isAllSelected) UbuntuOrange else Color(0xFF3A3A3A)
                        ),
                        modifier = Modifier
                            .clickable { viewModel.setSelectedBookmarkFolder("All") }
                            .testTag("bookmark_folder_chip_all")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "All",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isAllSelected) Color.White else Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${bookmarks.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAllSelected) Color.White.copy(alpha = 0.85f) else UbuntuWarmGrey,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Dynamic Custom Folders Chips
                    bookmarkFolders.forEach { folderEntity ->
                        val isSelected = folderEntity.name.equals(selectedFolder, ignoreCase = true)
                        val count = bookmarks.count {
                            it.folder.equals(folderEntity.name, ignoreCase = true) ||
                            it.category.equals(folderEntity.name, ignoreCase = true)
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) UbuntuOrange else UbuntuDarkSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) UbuntuOrange else Color(0xFF3A3A3A)
                            ),
                            modifier = Modifier
                                .clickable { viewModel.setSelectedBookmarkFolder(folderEntity.name) }
                                .testTag("bookmark_folder_chip_${folderEntity.name.lowercase()}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else UbuntuLightOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = folderEntity.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else UbuntuWarmGrey,
                                    fontSize = 11.sp
                                )

                                // Manage folder button
                                IconButton(
                                    onClick = { folderToManage = folderEntity },
                                    modifier = Modifier.size(24.dp).testTag("manage_folder_btn_${folderEntity.name.lowercase()}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Manage folder",
                                        tint = if (isSelected) Color.White.copy(alpha = 0.9f) else UbuntuWarmGrey,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Custom Labels Filter Chips Row (if any labels exist)
                if (bookmarkLabels.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = null,
                            tint = UbuntuLightOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "TAGS",
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuWarmGrey,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                        if (selectedLabel != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Clear filter",
                                style = MaterialTheme.typography.labelSmall,
                                color = UbuntuOrange,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .clickable { viewModel.setSelectedBookmarkLabel(null) }
                                    .testTag("clear_label_filter_btn")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bookmarkLabels.forEach { labelName ->
                            val isLabelSelected = labelName.equals(selectedLabel, ignoreCase = true)
                            val labelCount = bookmarks.count { it.hasLabel(labelName) }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isLabelSelected) UbuntuAubergine else Color(0xFF262626),
                                border = BorderStroke(
                                    1.dp,
                                    if (isLabelSelected) UbuntuOrange else Color(0xFF383838)
                                ),
                                modifier = Modifier
                                    .clickable {
                                        if (isLabelSelected) {
                                            viewModel.setSelectedBookmarkLabel(null)
                                        } else {
                                            viewModel.setSelectedBookmarkLabel(labelName)
                                        }
                                    }
                                    .testTag("bookmark_label_chip_${labelName.lowercase()}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "#$labelName",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isLabelSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isLabelSelected) UbuntuOrange else Color.White.copy(alpha = 0.75f),
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$labelCount",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isLabelSelected) Color.White else UbuntuWarmGrey,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bookmarks List
                if (filteredBookmarks.isEmpty()) {
                    UbuntuEmptyBookmarksView(
                        searchQuery = searchQuery,
                        selectedCategory = selectedFolder,
                        onAddClick = {
                            editingBookmark = null
                            showAddEditDialog = true
                        },
                        onSeedClick = {
                            viewModel.seedUbuntuFavorites()
                            Toast.makeText(context, "Ubuntu starter bookmarks added", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredBookmarks, key = { it.id }) { bookmark ->
                            UbuntuBookmarkRow(
                                bookmark = bookmark,
                                onOpen = {
                                    onDismiss()
                                    viewModel.loadUrl(bookmark.url)
                                },
                                onOpenNewTab = {
                                    viewModel.openNewTab(bookmark.url)
                                    onDismiss()
                                    Toast.makeText(context, "Opened in new tab", Toast.LENGTH_SHORT).show()
                                },
                                onEdit = {
                                    editingBookmark = bookmark
                                    showAddEditDialog = true
                                },
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(bookmark.url))
                                    Toast.makeText(context, "URL copied: ${bookmark.url}", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = {
                                    bookmarkToDelete = bookmark
                                },
                                onTogglePin = {
                                    viewModel.togglePinBookmark(bookmark)
                                },
                                onMove = {
                                    bookmarkToMove = bookmark
                                }
                            )
                        }
                    }
                }
            } else if (selectedTab == 1) {
                // Browsing History Tab Content
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Local SQLite Privacy Guarantee Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = UbuntuDarkSurface,
                        border = BorderStroke(1.dp, Color(0xFF383838)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("history_privacy_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(UbuntuSuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Privacy Protected",
                                    tint = UbuntuSuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Local SQLite Privacy",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = UbuntuAubergine.copy(alpha = 0.4f)
                                    ) {
                                        Text(
                                            text = "On-Device",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = UbuntuOrange,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Stored strictly in device Room database. Never synced, uploaded, or shared with cloud services. Incognito tabs completely bypass history logging.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    // Search Input
                    OutlinedTextField(
                        value = historySearchQuery,
                        onValueChange = { viewModel.setHistorySearchQuery(it) },
                        placeholder = {
                            Text(
                                "Search history by title or URL...",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search History",
                                tint = UbuntuOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (historySearchQuery.isNotBlank()) {
                                IconButton(
                                    onClick = { viewModel.setHistorySearchQuery("") },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UbuntuOrange,
                            unfocusedBorderColor = Color(0xFF3F3F3F),
                            focusedContainerColor = UbuntuDarkSurface,
                            unfocusedContainerColor = UbuntuDarkSurface,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .testTag("history_search_input")
                    )

                    // Date Period Filter Chips: All, Today, Yesterday, Older
                    val periodOptions = listOf("All", "Today", "Yesterday", "Older")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        periodOptions.forEach { period ->
                            val isSelected = selectedHistoryPeriod == period
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) UbuntuOrange else UbuntuDarkSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) UbuntuOrange else Color(0xFF404040)
                                ),
                                modifier = Modifier
                                    .clickable { viewModel.setSelectedHistoryPeriod(period) }
                                    .testTag("history_filter_${period.lowercase()}")
                            ) {
                                Text(
                                    text = period,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    if (filteredHistory.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = UbuntuWarmGrey,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (history.isEmpty()) {
                                        "No browsing history recorded yet.\nIncognito sessions do not store visited sites."
                                    } else {
                                        "No history matches \"$historySearchQuery\" in $selectedHistoryPeriod."
                                    },
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                if (history.isNotEmpty() && (historySearchQuery.isNotBlank() || selectedHistoryPeriod != "All")) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.setHistorySearchQuery("")
                                            viewModel.setSelectedHistoryPeriod("All")
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, UbuntuOrange),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = UbuntuOrange),
                                        modifier = Modifier.testTag("reset_history_filters_btn")
                                    ) {
                                        Text("Reset Filters")
                                    }
                                }
                            }
                        }
                    } else {
                        val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredHistory, key = { it.id }) { item ->
                                HistoryRow(
                                    item = item,
                                    timeFormatted = dateFormat.format(Date(item.visitedAt)),
                                    onClick = {
                                        onDismiss()
                                        viewModel.loadUrl(item.url)
                                    },
                                    onDelete = {
                                        viewModel.deleteHistoryById(item.id)
                                        Toast.makeText(context, "History entry deleted", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Offline Web Cache Tab Content
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = cacheSearchQuery,
                        onValueChange = { viewModel.setCacheSearchQuery(it) },
                        placeholder = { Text("Search cached pages or domains...", color = Color.Gray, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = UbuntuOrange, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UbuntuOrange,
                            unfocusedBorderColor = Color(0xFF3F3F3F),
                            focusedContainerColor = UbuntuDarkSurface,
                            unfocusedContainerColor = UbuntuDarkSurface,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                viewModel.pruneOldCachedPages(14)
                                Toast.makeText(context, "Pruned old pages (>14d)", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.weight(1f).height(34.dp)
                        ) {
                            Text("Prune Old", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.clearAllCachedPages()
                                Toast.makeText(context, "Offline cache cleared", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B)),
                            modifier = Modifier.weight(1f).height(34.dp)
                        ) {
                            Text("Clear All", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (cachedPages.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth().height(260.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CloudOff, contentDescription = null, tint = UbuntuWarmGrey, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No cached pages stored in Room database.", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cachedPages, key = { it.url }) { page ->
                                CachedPageItemCard(
                                    page = page,
                                    onOpen = {
                                        onDismiss()
                                        viewModel.loadCachedPage(page)
                                    },
                                    onTogglePin = {
                                        viewModel.togglePinCachedPage(page.url, !page.isPinnedOffline)
                                    },
                                    onDelete = {
                                        viewModel.deleteCachedPage(page.url)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Bookmark Dialog
    if (showAddEditDialog) {
        UbuntuAddEditBookmarkDialog(
            initialBookmark = editingBookmark,
            availableFolders = bookmarkFolders,
            availableLabels = bookmarkLabels,
            onDismiss = {
                showAddEditDialog = false
                editingBookmark = null
            },
            onSave = { title, url, folder, labels, notes ->
                if (editingBookmark != null && editingBookmark!!.id > 0) {
                    viewModel.updateBookmark(
                        id = editingBookmark!!.id,
                        title = title,
                        url = url,
                        folder = folder,
                        labels = labels,
                        notes = notes
                    )
                    Toast.makeText(context, "Bookmark updated", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addBookmark(
                        title = title,
                        url = url,
                        folder = folder,
                        labels = labels,
                        notes = notes
                    )
                    Toast.makeText(context, "Bookmark saved", Toast.LENGTH_SHORT).show()
                }
                showAddEditDialog = false
                editingBookmark = null
            }
        )
    }

    // Delete Confirmation Dialog
    bookmarkToDelete?.let { bookmark ->
        UbuntuDeleteConfirmationDialog(
            bookmark = bookmark,
            onDismiss = { bookmarkToDelete = null },
            onConfirm = {
                viewModel.deleteBookmark(bookmark)
                bookmarkToDelete = null
                Toast.makeText(context, "Bookmark deleted", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Create Folder Dialog
    if (showNewFolderDialog) {
        UbuntuCreateFolderDialog(
            onDismiss = { showNewFolderDialog = false },
            onCreate = { name, colorHex ->
                viewModel.createBookmarkFolder(name, colorHex)
                Toast.makeText(context, "Folder '$name' created", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Manage Folder Dialog (Rename / Delete)
    folderToManage?.let { folder ->
        UbuntuManageFolderDialog(
            folder = folder,
            onDismiss = { folderToManage = null },
            onRename = { newName ->
                viewModel.renameBookmarkFolder(folder.name, newName)
                folderToManage = null
                Toast.makeText(context, "Folder renamed to '$newName'", Toast.LENGTH_SHORT).show()
            },
            onDelete = { deleteBookmarks ->
                viewModel.deleteBookmarkFolder(folder.name, deleteBookmarks)
                folderToManage = null
                Toast.makeText(context, "Folder '${folder.name}' deleted", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Move Bookmark Dialog
    bookmarkToMove?.let { bookmark ->
        UbuntuMoveBookmarkDialog(
            bookmark = bookmark,
            availableFolders = bookmarkFolders,
            onDismiss = { bookmarkToMove = null },
            onMove = { targetFolder ->
                viewModel.moveBookmarkToFolder(bookmark.id, targetFolder)
                bookmarkToMove = null
                Toast.makeText(context, "Moved to '$targetFolder'", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Clear History Privacy Dialog
    if (showClearHistoryDialog) {
        ClearHistoryPrivacyDialog(
            selectedTimeRange = selectedClearTimeRange,
            onSelectTimeRange = { selectedClearTimeRange = it },
            onDismiss = { showClearHistoryDialog = false },
            onConfirm = {
                viewModel.clearBrowsingHistory(selectedClearTimeRange)
                showClearHistoryDialog = false
                Toast.makeText(
                    context,
                    "Cleared browsing history (${selectedClearTimeRange.displayName})",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

@Composable
fun UbuntuBookmarkRow(
    bookmark: BookmarkEntity,
    onOpen: () -> Unit,
    onOpenNewTab: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onMove: () -> Unit
) {
    val displayHost = remember(bookmark.url) {
        try {
            Uri.parse(bookmark.url).host?.removePrefix("www.") ?: bookmark.url
        } catch (_: Exception) {
            bookmark.url
        }
    }

    val initialLetter = remember(bookmark.title, displayHost) {
        val src = bookmark.title.ifBlank { displayHost }
        src.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "B"
    }

    val currentFolder = bookmark.folder.ifBlank { bookmark.category }
    val avatarBrush = remember(currentFolder) {
        when (currentFolder.lowercase()) {
            "ubuntu" -> Brush.linearGradient(listOf(UbuntuOrange, UbuntuDeepAubergine))
            "dev" -> Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF1E293B)))
            "media" -> Brush.linearGradient(listOf(Color(0xFFDC2626), UbuntuOrange))
            "news" -> Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF047857)))
            "social" -> Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF4C1D95)))
            else -> Brush.linearGradient(listOf(UbuntuOrange, UbuntuLightOrange))
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = UbuntuDarkSurface,
        border = BorderStroke(1.dp, Color(0xFF383838)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag("bookmark_card_${bookmark.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Ubuntu Category Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .background(avatarBrush, RoundedCornerShape(10.dp))
            ) {
                Text(
                    text = initialLetter,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Bookmark Information
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookmark.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (bookmark.url.startsWith("https://")) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure",
                            tint = UbuntuSuccessGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = displayHost,
                        style = MaterialTheme.typography.labelSmall,
                        color = UbuntuOrange,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Folder badge
                    Surface(
                        color = UbuntuAubergine.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, UbuntuAubergine),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = UbuntuLightOrange,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = currentFolder,
                                style = MaterialTheme.typography.labelSmall,
                                color = UbuntuLightOrange,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Custom Labels / Tags Badges Row
                val labelList = bookmark.getLabelList()
                if (labelList.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 3.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        labelList.forEach { tag ->
                            Surface(
                                color = Color(0xFF2C2C2C),
                                border = BorderStroke(1.dp, Color(0xFF4A4A4A)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFE95420),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                if (bookmark.notes.isNotBlank()) {
                    Text(
                        text = bookmark.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = UbuntuWarmGrey,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Quick Actions: Open New Tab, Copy, Pin, Move, Edit, Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(36.dp).testTag("bookmark_pin_btn_${bookmark.id}")
                ) {
                    Icon(
                        imageVector = if (bookmark.isPinnedToNewTab) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (bookmark.isPinnedToNewTab) "Pinned to Home" else "Unpinned",
                        tint = if (bookmark.isPinnedToNewTab) UbuntuOrange else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onOpenNewTab,
                    modifier = Modifier.size(36.dp).testTag("bookmark_new_tab_btn_${bookmark.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open in new tab",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(36.dp).testTag("bookmark_copy_btn_${bookmark.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy link",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onMove,
                    modifier = Modifier.size(36.dp).testTag("bookmark_move_btn_${bookmark.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DriveFileMove,
                        contentDescription = "Move bookmark to folder",
                        tint = UbuntuLightOrange,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp).testTag("bookmark_edit_btn_${bookmark.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit bookmark",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp).testTag("bookmark_delete_btn_${bookmark.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete bookmark",
                        tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UbuntuEmptyBookmarksView(
    searchQuery: String,
    selectedCategory: String,
    onAddClick: () -> Unit,
    onSeedClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = UbuntuDarkSurface,
        border = BorderStroke(1.dp, Color(0xFF383838)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .background(UbuntuOrange.copy(alpha = 0.2f), CircleShape)
                    .border(1.dp, UbuntuOrange, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = UbuntuOrange,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (searchQuery.isNotBlank()) "No Matching Bookmarks" else "No Bookmarks Saved",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (searchQuery.isNotBlank()) {
                    "No favorites matched query '$searchQuery' in category '$selectedCategory'."
                } else {
                    "Save your favorite websites to Room database or load the Ubuntu community collection."
                },
                style = MaterialTheme.typography.bodySmall,
                color = UbuntuWarmGrey,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("empty_add_bookmark_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Bookmark", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onSeedClick,
                    border = BorderStroke(1.dp, UbuntuOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("empty_seed_bookmarks_btn")
                ) {
                    Text("Load Ubuntu Pack", color = UbuntuLightOrange, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun UbuntuAddEditBookmarkDialog(
    initialBookmark: BookmarkEntity?,
    availableFolders: List<BookmarkFolderEntity>,
    availableLabels: List<String>,
    onDismiss: () -> Unit,
    onSave: (title: String, url: String, folder: String, labels: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf(initialBookmark?.title ?: "") }
    var url by remember { mutableStateOf(initialBookmark?.url ?: "") }
    var selectedFolder by remember { mutableStateOf(initialBookmark?.folder ?: initialBookmark?.category ?: "Favorites") }
    var customFolder by remember { mutableStateOf("") }
    var isCustomFolderMode by remember { mutableStateOf(false) }
    var labels by remember { mutableStateOf(initialBookmark?.labels ?: "") }
    var notes by remember { mutableStateOf(initialBookmark?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isEditing = initialBookmark != null && initialBookmark.id > 0
    val folderNames = remember(availableFolders) {
        val names = availableFolders.map { it.name }.toMutableList()
        if (!names.contains("Favorites")) names.add(0, "Favorites")
        names.distinct()
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
                        text = if (isEditing) "Edit Bookmark" else "Save to Bookmarks",
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
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Title", color = UbuntuWarmGrey) },
                    placeholder = { Text("e.g. Ubuntu Linux Official", color = Color.Gray) },
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
                    modifier = Modifier.fillMaxWidth().testTag("bookmark_dialog_title")
                )

                // URL Field
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        errorMessage = null
                    },
                    label = { Text("URL", color = UbuntuWarmGrey) },
                    placeholder = { Text("https://example.com", color = Color.Gray) },
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
                    modifier = Modifier.fillMaxWidth().testTag("bookmark_dialog_url")
                )

                // Folder Selection Section
                Text(
                    text = "Folder / Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = UbuntuWarmGrey,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    folderNames.forEach { fName ->
                        val isSelected = !isCustomFolderMode && fName.equals(selectedFolder, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) UbuntuOrange else UbuntuDarkSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) UbuntuOrange else Color(0xFF383838)
                            ),
                            modifier = Modifier
                                .clickable {
                                    isCustomFolderMode = false
                                    selectedFolder = fName
                                }
                                .testTag("bookmark_dialog_cat_${fName.lowercase()}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else UbuntuLightOrange,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = fName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // "+ Custom Folder" chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCustomFolderMode) UbuntuAubergine else Color(0xFF242424),
                        border = BorderStroke(1.dp, if (isCustomFolderMode) UbuntuOrange else Color(0xFF4A4A4A)),
                        modifier = Modifier
                            .clickable { isCustomFolderMode = true }
                            .testTag("bookmark_dialog_custom_folder_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = UbuntuOrange,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "New...",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCustomFolderMode) Color.White else UbuntuLightOrange,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (isCustomFolderMode) {
                    OutlinedTextField(
                        value = customFolder,
                        onValueChange = { customFolder = it },
                        label = { Text("Custom Folder Name", color = UbuntuWarmGrey) },
                        placeholder = { Text("e.g. Linux Tutorials", color = Color.Gray) },
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
                        modifier = Modifier.fillMaxWidth().testTag("bookmark_dialog_custom_folder_input")
                    )
                }

                // Custom Labels (Tags) Field
                OutlinedTextField(
                    value = labels,
                    onValueChange = { labels = it },
                    label = { Text("Labels / Tags (comma separated)", color = UbuntuWarmGrey) },
                    placeholder = { Text("e.g. linux, security, open-source", color = Color.Gray) },
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
                    modifier = Modifier.fillMaxWidth().testTag("bookmark_dialog_labels")
                )

                // Quick label suggestion chips
                if (availableLabels.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        availableLabels.take(8).forEach { suggestion ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2A2A2A),
                                border = BorderStroke(1.dp, Color(0xFF3F3F3F)),
                                modifier = Modifier.clickable {
                                    val currentList = labels.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    if (!currentList.contains(suggestion)) {
                                        labels = if (labels.isBlank()) suggestion else "$labels, $suggestion"
                                    }
                                }
                            ) {
                                Text(
                                    text = "+#$suggestion",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = UbuntuLightOrange,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)", color = UbuntuWarmGrey) },
                    placeholder = { Text("e.g. Saved for kernel documentation", color = Color.Gray) },
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
                    modifier = Modifier.fillMaxWidth().testTag("bookmark_dialog_notes")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedUrl = url.trim()
                    if (trimmedUrl.isBlank()) {
                        errorMessage = "URL cannot be blank"
                        return@Button
                    }
                    val validUrl = if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
                        "https://$trimmedUrl"
                    } else {
                        trimmedUrl
                    }
                    val finalTitle = title.trim().ifBlank { validUrl }
                    val finalFolder = if (isCustomFolderMode && customFolder.isNotBlank()) {
                        customFolder.trim()
                    } else {
                        selectedFolder.trim().ifBlank { "Favorites" }
                    }
                    onSave(finalTitle, validUrl, finalFolder, labels.trim(), notes.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("bookmark_dialog_save_btn")
            ) {
                Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("bookmark_dialog_cancel_btn")
            ) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

@Composable
fun UbuntuCreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, colorHex: String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#E95420") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val colorOptions = listOf(
        "#E95420", // Ubuntu Orange
        "#77216F", // Ubuntu Aubergine
        "#2C001E", // Ubuntu Deep Aubergine
        "#38B44A", // Green
        "#19B6EE", // Cyan
        "#EF4444", // Red
        "#F59E0B"  // Amber
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UbuntuDarkBackground,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CreateNewFolder,
                    contentDescription = null,
                    tint = UbuntuOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New Bookmark Folder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = {
                        folderName = it
                        errorMessage = null
                    },
                    label = { Text("Folder Name", color = UbuntuWarmGrey) },
                    placeholder = { Text("e.g. Research, Tech, Recipes", color = Color.Gray) },
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
                    modifier = Modifier.fillMaxWidth().testTag("new_folder_name_input")
                )

                Text(
                    text = "Folder Color",
                    style = MaterialTheme.typography.labelSmall,
                    color = UbuntuWarmGrey
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { hex ->
                        val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { UbuntuOrange }
                        val isSelected = selectedColor.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(color, CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = folderName.trim()
                    if (trimmed.isBlank()) {
                        errorMessage = "Folder name cannot be blank"
                        return@Button
                    }
                    onCreate(trimmed, selectedColor)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("create_folder_submit_btn")
            ) {
                Text("Create Folder", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("create_folder_cancel_btn")) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

@Composable
fun UbuntuManageFolderDialog(
    folder: BookmarkFolderEntity,
    onDismiss: () -> Unit,
    onRename: (newName: String) -> Unit,
    onDelete: (deleteBookmarks: Boolean) -> Unit
) {
    var newName by remember { mutableStateOf(folder.name) }
    var isRenaming by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteBookmarksWithFolder by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UbuntuDarkBackground,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = UbuntuOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Manage '${folder.name}'",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isRenaming) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = {
                            newName = it
                            errorMessage = null
                        },
                        label = { Text("New Folder Name", color = UbuntuWarmGrey) },
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
                        modifier = Modifier.fillMaxWidth().testTag("rename_folder_input")
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFEF4444),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else if (isDeleting) {
                    Text(
                        text = "Are you sure you want to delete folder '${folder.name}'?",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { deleteBookmarksWithFolder = !deleteBookmarksWithFolder }
                            .padding(vertical = 4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (deleteBookmarksWithFolder) UbuntuOrange else UbuntuDarkSurface,
                            border = BorderStroke(1.dp, if (deleteBookmarksWithFolder) UbuntuOrange else Color.Gray),
                            modifier = Modifier.size(20.dp)
                        ) {
                            if (deleteBookmarksWithFolder) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Also delete bookmarks inside this folder",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    Text(
                        text = "Organize or clean up bookmarks inside this folder.",
                        color = UbuntuWarmGrey,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = { isRenaming = true },
                        modifier = Modifier.fillMaxWidth().testTag("start_rename_folder_btn")
                    ) {
                        Icon(imageVector = Icons.Default.DriveFileRenameOutline, contentDescription = null, tint = UbuntuOrange)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rename Folder", color = Color.White)
                    }
                    OutlinedButton(
                        onClick = { isDeleting = true },
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth().testTag("start_delete_folder_btn")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Folder", color = Color(0xFFEF4444))
                    }
                }
            }
        },
        confirmButton = {
            if (isRenaming) {
                Button(
                    onClick = {
                        val trimmed = newName.trim()
                        if (trimmed.isBlank()) {
                            errorMessage = "Name cannot be blank"
                            return@Button
                        }
                        onRename(trimmed)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                    modifier = Modifier.testTag("submit_rename_folder_btn")
                ) {
                    Text("Rename", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else if (isDeleting) {
                Button(
                    onClick = {
                        onDelete(deleteBookmarksWithFolder)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("submit_delete_folder_btn")
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_manage_folder_btn")) {
                Text("Close", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

@Composable
fun UbuntuMoveBookmarkDialog(
    bookmark: BookmarkEntity,
    availableFolders: List<BookmarkFolderEntity>,
    onDismiss: () -> Unit,
    onMove: (targetFolder: String) -> Unit
) {
    var selectedTarget by remember { mutableStateOf(bookmark.folder.ifBlank { bookmark.category }) }
    var customNewFolder by remember { mutableStateOf("") }
    var isCreatingNew by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UbuntuDarkBackground,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DriveFileMove,
                    contentDescription = null,
                    tint = UbuntuOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Move Bookmark",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = bookmark.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = UbuntuWarmGrey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Select destination folder:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(availableFolders) { folder ->
                        val isSelected = !isCreatingNew && folder.name.equals(selectedTarget, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) UbuntuOrange else UbuntuDarkSurface,
                            border = BorderStroke(1.dp, if (isSelected) UbuntuOrange else Color(0xFF383838)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isCreatingNew = false
                                    selectedTarget = folder.name
                                }
                                .testTag("move_target_folder_${folder.name.lowercase()}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else UbuntuLightOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = folder.name,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Or enter custom new folder
                if (isCreatingNew) {
                    OutlinedTextField(
                        value = customNewFolder,
                        onValueChange = { customNewFolder = it },
                        label = { Text("New Folder Name", color = UbuntuWarmGrey) },
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
                        modifier = Modifier.fillMaxWidth().testTag("move_custom_folder_input")
                    )
                } else {
                    TextButton(
                        onClick = { isCreatingNew = true },
                        modifier = Modifier.testTag("move_create_new_folder_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = UbuntuOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Create New Folder", color = UbuntuLightOrange)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTarget = if (isCreatingNew) customNewFolder.trim().ifBlank { "Favorites" } else selectedTarget
                    onMove(finalTarget)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                modifier = Modifier.testTag("confirm_move_bookmark_btn")
            ) {
                Text("Move", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_move_bookmark_btn")) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

@Composable
fun UbuntuDeleteConfirmationDialog(
    bookmark: BookmarkEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UbuntuDarkBackground,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delete Bookmark?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"${bookmark.title}\"? This URL will be removed from your saved bookmarks.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("delete_dialog_confirm_btn")
            ) {
                Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("delete_dialog_cancel_btn")
            ) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

@Composable
fun HistoryRow(
    item: HistoryEntity,
    timeFormatted: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val host = remember(item.url) {
        try {
            Uri.parse(item.url).host ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = UbuntuDarkSurface,
        border = BorderStroke(1.dp, Color(0xFF383838)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("history_item_${item.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (item.title.isNotBlank()) item.title else item.url,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (host.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = UbuntuAubergine.copy(alpha = 0.35f),
                            border = BorderStroke(0.5.dp, UbuntuAubergine.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = host,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = UbuntuOrange,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = item.url,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $timeFormatted",
                        style = MaterialTheme.typography.labelSmall,
                        color = UbuntuWarmGrey,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("delete_history_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete history entry",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ClearHistoryPrivacyDialog(
    selectedTimeRange: HistoryTimeRange,
    onSelectTimeRange: (HistoryTimeRange) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UbuntuDarkBackground,
        shape = RoundedCornerShape(18.dp),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Clear Browsing History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFFEF4444), UbuntuOrange, Color.Transparent))
                        )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Select time period to clear from your local device database. Visited URLs in this range will be permanently erased.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                HistoryTimeRange.values().forEach { range ->
                    val isSelected = selectedTimeRange == range
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) UbuntuAubergine.copy(alpha = 0.35f) else UbuntuDarkSurface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) UbuntuOrange else Color(0xFF3F3F3F)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { onSelectTimeRange(range) }
                            .testTag("time_range_${range.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectTimeRange(range) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = UbuntuOrange,
                                    unselectedColor = Color.Gray
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = range.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = UbuntuDarkSurface,
                    border = BorderStroke(1.dp, Color(0xFF383838))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = UbuntuSuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Zero Cloud Sync: Stored in on-device SQLite only. Clearing is immediate and permanent.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_clear_history_btn")
            ) {
                Text(
                    text = "Clear History",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("cancel_clear_history_btn")
            ) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

