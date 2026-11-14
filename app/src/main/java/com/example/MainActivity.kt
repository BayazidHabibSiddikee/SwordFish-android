package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.components.AppBlockerSheet
import com.example.ui.components.AutofillPromptBanner
import com.example.ui.components.BlockedOverlayView
import com.example.ui.components.BookmarksHistorySheet
import com.example.ui.components.BrowserBottomBar
import com.example.ui.components.BrowserTopBar
import com.example.ui.components.BrowserWebView
import com.example.ui.components.MediaHubSheet
import com.example.ui.components.NewTabPage
import com.example.ui.components.OfflineCacheSheet
import com.example.ui.components.PasswordManagerSheet
import com.example.ui.components.ReaderModeView
import com.example.ui.components.SavePasswordDialog
import com.example.ui.components.ScreenTimeDashboardSheet
import com.example.ui.components.SettingsSheet
import com.example.ui.components.ShieldModalSheet
import com.example.ui.components.TabsManagerSheet
import com.example.ui.components.BlocklistManagerSheet
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ActiveSheet
import com.example.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {

    private val browserViewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by browserViewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                BrowserApp(viewModel = browserViewModel)
            }
        }
    }
}

@Composable
fun BrowserApp(viewModel: BrowserViewModel) {
    val tabs by viewModel.tabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val activeSheet by viewModel.activeSheet.collectAsState()
    val theme = com.example.ui.theme.LocalUbuntuColors.current

    val currentTab = tabs.find { it.id == activeTabId } ?: tabs.firstOrNull() ?: return

    // Android System Back Button Handling
    BackHandler(enabled = true) {
        when {
            activeSheet != ActiveSheet.NONE -> {
                viewModel.closeSheet()
            }
            currentTab.isReaderMode -> {
                viewModel.exitReaderMode()
            }
            currentTab.isBlockedByFocus -> {
                viewModel.dismissBlockedOverlayAndGoHome()
            }
            currentTab.canGoBack -> {
                viewModel.goBack()
            }
            currentTab.url.isNotBlank() -> {
                viewModel.dismissBlockedOverlayAndGoHome()
            }
        }
    }

    Scaffold(
        topBar = {
            BrowserTopBar(
                tab = currentTab,
                viewModel = viewModel,
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            BrowserBottomBar(
                tab = currentTab,
                viewModel = viewModel
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(theme.background)
        ) {
            when {
                currentTab.isBlockedByFocus -> {
                    BlockedOverlayView(
                        domain = currentTab.blockedDomain,
                        viewModel = viewModel
                    )
                }
                currentTab.isReaderMode && currentTab.readerArticle != null -> {
                    ReaderModeView(
                        article = currentTab.readerArticle!!,
                        viewModel = viewModel,
                        onExit = { viewModel.exitReaderMode() }
                    )
                }
                currentTab.url.isBlank() -> {
                    NewTabPage(viewModel = viewModel)
                }
                else -> {
                    BrowserWebView(
                        tab = currentTab,
                        viewModel = viewModel
                    )
                }
            }

            // Floating Autofill prompt above bottom bar when login form is detected
            AutofillPromptBanner(
                viewModel = viewModel,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Save Password confirmation dialog
        SavePasswordDialog(viewModel = viewModel)

        // Active Bottom Sheets
        when (activeSheet) {
            ActiveSheet.SHIELD_STATS -> {
                ShieldModalSheet(
                    tab = currentTab,
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.APP_BLOCKER -> {
                AppBlockerSheet(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.TABS_MANAGER -> {
                TabsManagerSheet(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.BOOKMARKS_HISTORY -> {
                BookmarksHistorySheet(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.SCREEN_TIME_DASHBOARD -> {
                ScreenTimeDashboardSheet(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.MEDIA_HUB -> {
                MediaHubSheet(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.PASSWORD_VAULT -> {
                PasswordManagerSheet(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.SETTINGS -> {
                SettingsSheet(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.OFFLINE_CACHE -> {
                OfflineCacheSheet(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.BLOCKLIST_MANAGER -> {
                BlocklistManagerSheet(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.NONE -> Unit
        }
    }
}
