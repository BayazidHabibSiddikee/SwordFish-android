package com.example.ui.components

import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CachedPageEntity
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.UbuntuDarkSurface
import com.example.ui.theme.UbuntuDeepAubergine
import com.example.ui.theme.UbuntuLightOrange
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuWarmGrey
import com.example.viewmodel.BrowserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineCacheSheet(
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val cachedPages by viewModel.filteredCachedPages.collectAsState()
    val totalCount by viewModel.cachedPageCount.collectAsState()
    val totalBytes by viewModel.totalCacheSizeBytes.collectAsState()
    val searchQuery by viewModel.cacheSearchQuery.collectAsState()

    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var pageToDelete by remember { mutableStateOf<CachedPageEntity?>(null) }

    val formattedSize = remember(totalBytes) {
        Formatter.formatFileSize(context, totalBytes)
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
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // Header with Ubuntu Aubergine Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(UbuntuDeepAubergine, UbuntuAubergine)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = UbuntuOrange.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Offline Cache",
                                    tint = UbuntuOrange,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Offline Web Cache",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "$totalCount pages • $formattedSize saved in Room database",
                                style = MaterialTheme.typography.bodySmall,
                                color = UbuntuWarmGrey
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = UbuntuDarkSurface,
                            modifier = Modifier.clickable { onDismiss() }
                        ) {
                            Text(
                                text = "Done",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = UbuntuOrange,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setCacheSearchQuery(it) },
                        placeholder = { Text("Search cached pages or domains...", color = Color.Gray, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = UbuntuOrange, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setCacheSearchQuery("") }) {
                                    Icon(Icons.Default.ClearAll, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
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
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("offline_cache_search_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons: Prune >14d, Clear All
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.pruneOldCachedPages(14)
                                Toast.makeText(context, "Pruned unpinned pages older than 14 days", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFF4F4F4F)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("prune_cache_button")
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(14.dp), tint = UbuntuOrange)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prune Old (>14d)", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showClearConfirmDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFF4F4F4F)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B)),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("clear_all_cache_button")
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFF6B6B))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear Cache", fontSize = 12.sp)
                        }
                    }
                }
            }

            // List of cached pages
            if (cachedPages.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp, horizontal = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No cached pages match '$searchQuery'" else "No Cached Pages Yet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Webpages you browse online are automatically stored in the local Room database, letting you read them anytime without an internet connection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = UbuntuWarmGrey,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    items(cachedPages, key = { it.url }) { page ->
                        CachedPageItemCard(
                            page = page,
                            onOpen = {
                                viewModel.loadCachedPage(page)
                                onDismiss()
                            },
                            onTogglePin = {
                                viewModel.togglePinCachedPage(page.url, !page.isPinnedOffline)
                            },
                            onDelete = {
                                pageToDelete = page
                            }
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog for clearing all cache
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All Offline Cache?", color = Color.White) },
            text = {
                Text(
                    "This will delete all $totalCount cached webpage snapshots from local storage. Unsaved pages will require an internet connection to view again.",
                    color = UbuntuWarmGrey
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllCachedPages()
                        showClearConfirmDialog = false
                        Toast.makeText(context, "Offline cache cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = UbuntuDarkSurface
        )
    }

    // Confirmation dialog for deleting single page
    pageToDelete?.let { page ->
        AlertDialog(
            onDismissRequest = { pageToDelete = null },
            title = { Text("Delete Cached Page?", color = Color.White) },
            text = { Text("Remove offline cache snapshot for '${page.title}'?", color = UbuntuWarmGrey) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCachedPage(page.url)
                        pageToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pageToDelete = null }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = UbuntuDarkSurface
        )
    }
}

@Composable
fun CachedPageItemCard(
    page: CachedPageEntity,
    onOpen: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formattedDate = remember(page.cachedAtTimestamp) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(page.cachedAtTimestamp))
    }
    val formattedSize = remember(page.contentSizeBytes) {
        Formatter.formatFileSize(context, page.contentSizeBytes)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
        border = BorderStroke(
            1.dp,
            if (page.isPinnedOffline) UbuntuOrange.copy(alpha = 0.6f) else Color(0xFF383838)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("cached_page_item_${page.url.hashCode()}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Domain Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = UbuntuDeepAubergine,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = page.domain,
                        style = MaterialTheme.typography.labelSmall,
                        color = UbuntuLightOrange,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Pin Offline button
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(28.dp).testTag("pin_cache_btn_${page.url.hashCode()}")
                ) {
                    Icon(
                        imageVector = if (page.isPinnedOffline) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (page.isPinnedOffline) "Pinned Offline" else "Pin Offline",
                        tint = if (page.isPinnedOffline) UbuntuOrange else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_cache_btn_${page.url.hashCode()}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Cache",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Title
            Text(
                text = page.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Snippet preview
            if (page.plainTextSnippet.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = page.plainTextSnippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Metadata row: size, visits, timestamp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$formattedSize • ${page.visitCount} visits • Cached $formattedDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = UbuntuWarmGrey,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpen() }
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = "Open Offline",
                        tint = UbuntuOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Open",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = UbuntuOrange,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
