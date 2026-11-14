package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import com.example.R
import com.example.data.BrowserRepository
import com.example.data.blocker.AdBlockEngine
import com.example.data.blocker.AppBlockerEngine
import com.example.data.local.BlockedSiteEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.BookmarkFolderEntity
import com.example.data.model.ReaderArticle
import com.example.data.model.ReaderContentWidth
import com.example.data.model.ReaderFontFamily
import com.example.data.model.ReaderLineHeight
import com.example.data.model.ReaderSettings
import com.example.data.model.ReaderTheme
import com.example.data.reader.ReaderModeEngine
import com.example.data.local.BrowserDatabase
import com.example.data.local.FilterSettingsRepository
import com.example.data.local.HistoryEntity
import com.example.data.local.MostVisitedSite
import com.example.data.local.PasswordAutofillEngine
import com.example.data.local.SecurePasswordRepository
import com.example.data.local.UsageTimeEntity
import com.example.data.model.AdBlockFilters
import com.example.data.model.BrowserTab
import com.example.data.model.CliToolInfo
import com.example.data.model.DistractionFilters
import com.example.data.model.DownloadStatus
import com.example.data.model.FilterPreset
import com.example.data.model.FilterSettings
import com.example.data.model.MediaDownloadTask
import com.example.data.model.MediaItem
import com.example.data.model.PasswordStrength
import com.example.data.model.SearchEngine
import com.example.data.model.SecurityFilters
import com.example.data.model.ShieldStats
import com.example.data.model.TrackingFilters
import com.example.data.model.VaultCredential
import com.example.data.model.HistoryTimeRange
import com.example.data.local.CachedPageEntity
import com.example.data.service.FocusUsageMonitorService
import com.example.data.util.IncognitoSessionManager
import com.example.data.util.NetworkUtils
import com.example.data.model.BlocklistRule
import com.example.data.model.BlocklistType
import com.example.data.model.BlockedRequestLog
import com.example.data.service.BlocklistService
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ActiveSheet {
    NONE,
    SHIELD_STATS,
    APP_BLOCKER,
    TABS_MANAGER,
    BOOKMARKS_HISTORY,
    SCREEN_TIME_DASHBOARD,
    MEDIA_HUB,
    PASSWORD_VAULT,
    SETTINGS,
    OFFLINE_CACHE,
    BLOCKLIST_MANAGER
}

enum class BookmarkSortOption(val displayName: String) {
    NEWEST("Newest"),
    OLDEST("Oldest"),
    TITLE_AZ("Title A-Z"),
    CATEGORY("Category")
}

sealed class WebCommand {
    data object GoBack : WebCommand()
    data object GoForward : WebCommand()
    data object Reload : WebCommand()
    data class LoadUrl(val url: String) : WebCommand()
    data class LoadCachedHtml(val baseUrl: String, val html: String) : WebCommand()
    data class EvaluateJs(val script: String) : WebCommand()
}

data class DetectedLoginForm(
    val domain: String,
    val usernameSelector: String,
    val passwordSelector: String,
    val matchedCredentials: List<VaultCredential>
)

data class PendingSaveCredential(
    val domain: String,
    val username: String,
    val password: String
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BrowserRepository
    val passwordRepository: SecurePasswordRepository
    val filterSettingsRepository: FilterSettingsRepository
    val blocklistService: BlocklistService

    init {
        val db = BrowserDatabase.getInstance(application)
        repository = BrowserRepository(db.browserDao())
        passwordRepository = SecurePasswordRepository(application)
        filterSettingsRepository = FilterSettingsRepository(application)
        blocklistService = BlocklistService.getInstance(application)
    }

    val filterSettings: StateFlow<FilterSettings> = filterSettingsRepository.filterSettings

    // Managed Blocklist Service Flows
    val blocklistRules: StateFlow<List<BlocklistRule>> = blocklistService.rules
    val blockedRequestsLog: StateFlow<List<BlockedRequestLog>> = blocklistService.blockedRequestsLog
    val totalAdsBlockedCount: StateFlow<Int> = blocklistService.totalAdsBlocked
    val totalTrackersBlockedCount: StateFlow<Int> = blocklistService.totalTrackersBlocked

    // Reader Mode Settings & Persistence
    private val readerPrefs = application.getSharedPreferences("swordfish_reader_prefs", Context.MODE_PRIVATE)
    private val _readerSettings = MutableStateFlow(loadReaderSettings())
    val readerSettings: StateFlow<ReaderSettings> = _readerSettings.asStateFlow()

    private fun loadReaderSettings(): ReaderSettings {
        val jsonStr = readerPrefs.getString("reader_settings_json", null)
        return if (!jsonStr.isNullOrBlank()) {
            ReaderSettings.fromJsonString(jsonStr)
        } else {
            ReaderSettings()
        }
    }

    private fun saveReaderSettings(settings: ReaderSettings) {
        readerPrefs.edit().putString("reader_settings_json", settings.toJson().toString()).apply()
        _readerSettings.value = settings
    }

    // Global Theme Mode State & Persistence (High-Contrast Ubuntu Dark vs Ubuntu Light)
    private val themePrefs = application.getSharedPreferences("swordfish_theme_prefs", Context.MODE_PRIVATE)
    private val _isDarkTheme = MutableStateFlow(themePrefs.getBoolean("is_dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        val next = !_isDarkTheme.value
        themePrefs.edit().putBoolean("is_dark_theme", next).apply()
        _isDarkTheme.value = next
    }

    fun setDarkTheme(isDark: Boolean) {
        themePrefs.edit().putBoolean("is_dark_theme", isDark).apply()
        _isDarkTheme.value = isDark
    }

    // Text-To-Speech (TTS) Read Aloud Engine
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private val _isReadingAloud = MutableStateFlow(false)
    val isReadingAloud: StateFlow<Boolean> = _isReadingAloud.asStateFlow()

    val vaultCredentials: StateFlow<List<VaultCredential>> = passwordRepository.credentials

    private val _vaultSearchQuery = MutableStateFlow("")
    val vaultSearchQuery: StateFlow<String> = _vaultSearchQuery.asStateFlow()

    val filteredVaultCredentials: StateFlow<List<VaultCredential>> = combine(
        vaultCredentials,
        _vaultSearchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else {
            val q = query.trim().lowercase()
            list.filter {
                it.domain.lowercase().contains(q) ||
                it.title.lowercase().contains(q) ||
                it.username.lowercase().contains(q) ||
                it.notes.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _detectedLoginForm = MutableStateFlow<DetectedLoginForm?>(null)
    val detectedLoginForm: StateFlow<DetectedLoginForm?> = _detectedLoginForm.asStateFlow()

    private val _pendingSaveCredential = MutableStateFlow<PendingSaveCredential?>(null)
    val pendingSaveCredential: StateFlow<PendingSaveCredential?> = _pendingSaveCredential.asStateFlow()

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quickAccessBookmarks: StateFlow<List<BookmarkEntity>> = repository.quickAccessBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostVisitedSites: StateFlow<List<MostVisitedSite>> = repository.mostVisitedSites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkFolders: StateFlow<List<BookmarkFolderEntity>> = repository.allFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkLabels: StateFlow<List<String>> = repository.allLabels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _bookmarkSearchQuery = MutableStateFlow("")
    val bookmarkSearchQuery: StateFlow<String> = _bookmarkSearchQuery.asStateFlow()

    private val _selectedBookmarkCategory = MutableStateFlow("All")
    val selectedBookmarkCategory: StateFlow<String> = _selectedBookmarkCategory.asStateFlow()

    private val _selectedBookmarkFolder = MutableStateFlow("All")
    val selectedBookmarkFolder: StateFlow<String> = _selectedBookmarkFolder.asStateFlow()

    private val _selectedBookmarkLabel = MutableStateFlow<String?>(null)
    val selectedBookmarkLabel: StateFlow<String?> = _selectedBookmarkLabel.asStateFlow()

    private val _bookmarkSortOption = MutableStateFlow(BookmarkSortOption.NEWEST)
    val bookmarkSortOption: StateFlow<BookmarkSortOption> = _bookmarkSortOption.asStateFlow()

    val filteredBookmarks: StateFlow<List<BookmarkEntity>> = combine(
        bookmarks,
        _bookmarkSearchQuery,
        _selectedBookmarkFolder,
        _selectedBookmarkLabel,
        _bookmarkSortOption
    ) { list, query, folder, selectedLabel, sortOption ->
        var result = list

        if (folder != "All") {
            result = result.filter {
                it.folder.equals(folder, ignoreCase = true) || it.category.equals(folder, ignoreCase = true)
            }
        }

        if (!selectedLabel.isNullOrBlank()) {
            val cleanLabel = selectedLabel.trim().removePrefix("#")
            result = result.filter { it.hasLabel(cleanLabel) }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                it.url.lowercase().contains(q) ||
                it.notes.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.folder.lowercase().contains(q) ||
                it.labels.lowercase().contains(q)
            }
        }

        when (sortOption) {
            BookmarkSortOption.NEWEST -> result.sortedByDescending { it.createdAt }
            BookmarkSortOption.OLDEST -> result.sortedBy { it.createdAt }
            BookmarkSortOption.TITLE_AZ -> result.sortedBy { it.title.lowercase() }
            BookmarkSortOption.CATEGORY -> result.sortedWith(compareBy({ it.folder.lowercase() }, { it.title.lowercase() }))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = repository.recentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyCount: StateFlow<Int> = repository.historyCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _selectedHistoryPeriod = MutableStateFlow("All")
    val selectedHistoryPeriod: StateFlow<String> = _selectedHistoryPeriod.asStateFlow()

    val filteredHistory: StateFlow<List<HistoryEntity>> = combine(
        history,
        _historySearchQuery,
        _selectedHistoryPeriod
    ) { list, query, period ->
        val now = System.currentTimeMillis()
        val oneDayMs = 86_400_000L
        var result = list

        if (period != "All") {
            result = result.filter { item ->
                val diff = now - item.visitedAt
                when (period) {
                    "Today" -> diff < oneDayMs
                    "Yesterday" -> diff in oneDayMs until (2 * oneDayMs)
                    "Older" -> diff >= (2 * oneDayMs)
                    else -> true
                }
            }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) || it.url.lowercase().contains(q)
            }
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedSites: StateFlow<List<BlockedSiteEntity>> = repository.allBlockedSites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val usageTimes: StateFlow<List<UsageTimeEntity>> = repository.getUsageTimesForToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isBackgroundServiceActive: StateFlow<Boolean> = FocusUsageMonitorService.isServiceRunning
    val activeMonitoredSeconds: StateFlow<Long> = FocusUsageMonitorService.currentSessionActiveSeconds

    // Cached Offline Webpages State
    val allCachedPages: StateFlow<List<CachedPageEntity>> = repository.allCachedPages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val frequentlyVisitedCachedPages: StateFlow<List<CachedPageEntity>> = repository.frequentlyVisitedCachedPages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cachedPageCount: StateFlow<Int> = repository.cachedPageCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCacheSizeBytes: StateFlow<Long> = repository.totalCacheSizeBytes
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _cacheSearchQuery = MutableStateFlow("")
    val cacheSearchQuery: StateFlow<String> = _cacheSearchQuery.asStateFlow()

    val filteredCachedPages: StateFlow<List<CachedPageEntity>> = combine(
        allCachedPages,
        _cacheSearchQuery
    ) { pages, query ->
        if (query.isBlank()) {
            pages
        } else {
            val q = query.trim().lowercase()
            pages.filter {
                it.title.lowercase().contains(q) ||
                it.url.lowercase().contains(q) ||
                it.domain.lowercase().contains(q) ||
                it.plainTextSnippet.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tabs Management
    private val initialTab = BrowserTab(title = "New Tab", url = "")
    private val _tabs = MutableStateFlow(listOf(initialTab))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(initialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    // Active Sheet / Modal
    private val _activeSheet = MutableStateFlow(ActiveSheet.NONE)
    val activeSheet: StateFlow<ActiveSheet> = _activeSheet.asStateFlow()

    // Shield Stats
    private val _shieldStats = MutableStateFlow(
        ShieldStats(
            totalAdsBlocked = 142,
            totalTrackersBlocked = 89,
            totalDataSavedKb = 21400,
            isShieldEnabled = true,
            isCosmeticBlockerEnabled = true
        )
    )
    val shieldStats: StateFlow<ShieldStats> = _shieldStats.asStateFlow()

    // Search Engine
    private val _selectedSearchEngine = MutableStateFlow(SearchEngine.DUCKDUCKGO)
    val selectedSearchEngine: StateFlow<SearchEngine> = _selectedSearchEngine.asStateFlow()

    // Focus / App Blocker Session
    private val _isFocusModeActive = MutableStateFlow(false)
    val isFocusModeActive: StateFlow<Boolean> = _isFocusModeActive.asStateFlow()

    private val _focusTimeRemainingSeconds = MutableStateFlow(0)
    val focusTimeRemainingSeconds: StateFlow<Int> = _focusTimeRemainingSeconds.asStateFlow()

    private val _focusSessionTotalMinutes = MutableStateFlow(25)
    val focusSessionTotalMinutes: StateFlow<Int> = _focusSessionTotalMinutes.asStateFlow()

    private val _isFocusTimerPaused = MutableStateFlow(false)
    val isFocusTimerPaused: StateFlow<Boolean> = _isFocusTimerPaused.asStateFlow()

    private val _selectedMediaHubTab = MutableStateFlow(0)
    val selectedMediaHubTab: StateFlow<Int> = _selectedMediaHubTab.asStateFlow()

    private var focusTimerJob: Job? = null

    // WebView commands channel with replay to prevent missed navigation commands during initial composition
    private val _webCommands = MutableSharedFlow<WebCommand>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val webCommands = _webCommands.asSharedFlow()

    val activeTab: BrowserTab?
        get() = _tabs.value.find { it.id == _activeTabId.value } ?: _tabs.value.firstOrNull()

    fun openSheet(sheet: ActiveSheet) {
        _activeSheet.value = sheet
    }

    fun closeSheet() {
        _activeSheet.value = ActiveSheet.NONE
    }

    fun setSearchEngine(engine: SearchEngine) {
        _selectedSearchEngine.value = engine
    }

    fun toggleShield(enabled: Boolean) {
        _shieldStats.update { it.copy(isShieldEnabled = enabled) }
    }

    fun toggleCosmeticBlocker(enabled: Boolean) {
        _shieldStats.update { it.copy(isCosmeticBlockerEnabled = enabled) }
    }

    // Tab Operations
    fun openNewTab(url: String = "", isIncognito: Boolean = false) {
        val newTab = BrowserTab(
            url = url,
            title = if (url.isBlank()) "New Tab" else "Loading...",
            isIncognito = isIncognito
        )
        _tabs.update { it + newTab }
        _activeTabId.value = newTab.id
        _activeSheet.value = ActiveSheet.NONE

        if (url.isNotBlank()) {
            loadUrl(url)
        }
    }

    fun openNewIncognitoTab(url: String = "") {
        openNewTab(url = url, isIncognito = true)
    }

    fun selectTab(tabId: String) {
        _activeTabId.value = tabId
        _activeSheet.value = ActiveSheet.NONE
    }

    fun closeTab(tabId: String) {
        val currentTabs = _tabs.value
        val tabToClose = currentTabs.find { it.id == tabId }

        // Clear private session cookies, DOM storage, and credentials on incognito close
        if (tabToClose?.isIncognito == true) {
            IncognitoSessionManager.clearSessionCookiesAndStorage(getApplication())
        }

        if (currentTabs.size <= 1) {
            // Reset the only tab to blank
            val resetTab = BrowserTab(title = "New Tab", url = "")
            _tabs.value = listOf(resetTab)
            _activeTabId.value = resetTab.id
            return
        }

        val updatedTabs = currentTabs.filter { it.id != tabId }
        _tabs.value = updatedTabs
        if (_activeTabId.value == tabId) {
            _activeTabId.value = updatedTabs.last().id
        }
    }

    fun closeAllTabs() {
        val hadIncognito = _tabs.value.any { it.isIncognito }
        if (hadIncognito) {
            IncognitoSessionManager.clearSessionCookiesAndStorage(getApplication())
        }

        val resetTab = BrowserTab(title = "New Tab", url = "")
        _tabs.value = listOf(resetTab)
        _activeTabId.value = resetTab.id
        _activeSheet.value = ActiveSheet.NONE
    }

    fun closeAllIncognitoTabs() {
        val currentTabs = _tabs.value
        val hadIncognito = currentTabs.any { it.isIncognito }
        if (!hadIncognito) return

        IncognitoSessionManager.clearSessionCookiesAndStorage(getApplication())

        val remainingTabs = currentTabs.filterNot { it.isIncognito }
        if (remainingTabs.isEmpty()) {
            val resetTab = BrowserTab(title = "New Tab", url = "")
            _tabs.value = listOf(resetTab)
            _activeTabId.value = resetTab.id
        } else {
            _tabs.value = remainingTabs
            if (currentTabs.find { it.id == _activeTabId.value }?.isIncognito == true) {
                _activeTabId.value = remainingTabs.last().id
            }
        }
    }

    fun duplicateTab(tabId: String) {
        val tab = _tabs.value.find { it.id == tabId } ?: return
        val duplicated = BrowserTab(
            url = tab.url,
            title = tab.title,
            isIncognito = tab.isIncognito,
            isDesktopMode = tab.isDesktopMode
        )
        _tabs.update { it + duplicated }
        _activeTabId.value = duplicated.id
        _activeSheet.value = ActiveSheet.NONE

        if (duplicated.url.isNotBlank()) {
            loadUrl(duplicated.url)
        }
    }

    fun closeInactiveTabs() {
        val currentActiveId = _activeTabId.value
        val active = _tabs.value.find { it.id == currentActiveId } ?: return
        val closedIncognito = _tabs.value.filter { it.id != currentActiveId && it.isIncognito }
        if (closedIncognito.isNotEmpty()) {
            IncognitoSessionManager.clearSessionCookiesAndStorage(getApplication())
        }
        _tabs.value = listOf(active)
    }

    fun isCurrentTabIncognito(): Boolean {
        return activeTab?.isIncognito == true
    }

    // Blocklist Service Operations
    fun addCustomBlocklistRule(
        pattern: String,
        type: BlocklistType,
        isKeyword: Boolean = false,
        description: String = ""
    ): Boolean {
        return blocklistService.addCustomRule(pattern, type, isKeyword, description)
    }

    fun removeBlocklistRule(ruleId: String): Boolean {
        return blocklistService.removeRule(ruleId)
    }

    fun toggleBlocklistRule(ruleId: String, isEnabled: Boolean) {
        blocklistService.toggleRule(ruleId, isEnabled)
    }

    fun setBlocklistCategoryEnabled(type: BlocklistType, enabled: Boolean) {
        blocklistService.setCategoryEnabled(type, enabled)
    }

    fun resetBlocklistToDefaults() {
        blocklistService.resetToDefaults()
    }

    fun clearBlocklistLog() {
        blocklistService.clearLog()
    }

    // Navigation & URL Handling
    fun loadUrl(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return

        var targetUrl = formatUrlOrQuery(trimmed)

        // Strip tracking parameters if tracking filter is active
        if (filterSettings.value.tracking.urlTrackingParamStripping) {
            targetUrl = AdBlockEngine.stripTrackingParameters(targetUrl)
        }

        // Check if blocked by Focus Shield or Distraction Filters
        val enabledBlockedDomains = blockedSites.value.filter { it.isEnabled }.map { it.domain }
        val (isBlocked, domain) = AppBlockerEngine.checkBlocked(
            targetUrl,
            enabledBlockedDomains,
            isFocusModeActive = _isFocusModeActive.value,
            filterSettings = filterSettings.value
        )

        updateActiveTab {
            it.copy(
                url = targetUrl,
                isLoading = true,
                isBlockedByFocus = isBlocked,
                blockedDomain = domain,
                isLoadedFromCache = false,
                cachedTimestamp = null
            )
        }

        if (!isBlocked) {
            _webCommands.tryEmit(WebCommand.LoadUrl(targetUrl))
            val currentTab = activeTab
            if (currentTab != null && !currentTab.isIncognito && targetUrl.isNotBlank()) {
                viewModelScope.launch {
                    val host = try {
                        android.net.Uri.parse(targetUrl).host?.removePrefix("www.") ?: targetUrl
                    } catch (_: Exception) {
                        targetUrl
                    }
                    repository.addHistory(title = host, url = targetUrl)
                }
            }
            viewModelScope.launch {
                val isOnline = NetworkUtils.isOnline(getApplication())
                val cached = if (!isOnline) {
                    repository.getCachedPage(targetUrl) ?: repository.getCachedPage(targetUrl.trimEnd('/'))
                } else null

                if (cached != null) {
                    updateActiveTab {
                        it.copy(
                            url = cached.url,
                            title = cached.title,
                            isLoading = false,
                            progress = 1.0f,
                            isLoadedFromCache = true,
                            cachedTimestamp = cached.cachedAtTimestamp
                        )
                    }
                    _webCommands.emit(WebCommand.LoadCachedHtml(cached.url, cached.htmlContent))
                    repository.recordCachedPageVisit(cached.url)
                }
            }
        }
    }

    fun loadCachedPage(cachedPage: CachedPageEntity) {
        _activeSheet.value = ActiveSheet.NONE
        updateActiveTab {
            it.copy(
                url = cachedPage.url,
                title = cachedPage.title,
                isLoading = false,
                progress = 1.0f,
                isLoadedFromCache = true,
                cachedTimestamp = cachedPage.cachedAtTimestamp,
                isBlockedByFocus = false,
                blockedDomain = null
            )
        }
        viewModelScope.launch {
            _webCommands.emit(WebCommand.LoadCachedHtml(cachedPage.url, cachedPage.htmlContent))
            repository.recordCachedPageVisit(cachedPage.url)
            repository.addHistory(title = cachedPage.title, url = cachedPage.url)
        }
    }

    fun handleOfflineNavigation(failedUrl: String) {
        if (failedUrl.isBlank() || failedUrl.startsWith("data:") || failedUrl.startsWith("about:")) return
        viewModelScope.launch {
            val cached = repository.getCachedPage(failedUrl)
                ?: repository.getCachedPage(failedUrl.trimEnd('/'))
                ?: repository.getCachedPage("$failedUrl/")
            if (cached != null) {
                updateActiveTab {
                    it.copy(
                        url = cached.url,
                        title = cached.title,
                        isLoading = false,
                        progress = 1.0f,
                        isLoadedFromCache = true,
                        cachedTimestamp = cached.cachedAtTimestamp
                    )
                }
                _webCommands.emit(WebCommand.LoadCachedHtml(cached.url, cached.htmlContent))
                repository.recordCachedPageVisit(cached.url)
            } else {
                updateActiveTab {
                    it.copy(
                        isLoading = false,
                        progress = 1.0f
                    )
                }
            }
        }
    }

    fun retryOnlineCurrentPage() {
        val tab = activeTab ?: return
        if (tab.url.isNotBlank()) {
            updateActiveTab {
                it.copy(
                    isLoadedFromCache = false,
                    cachedTimestamp = null
                )
            }
            loadUrl(tab.url)
        }
    }

    fun onPageHtmlExtracted(url: String, html: String) {
        val tab = activeTab ?: return
        if (tab.isIncognito || tab.isBlockedByFocus || url.isBlank() ||
            url.startsWith("about:") || url.startsWith("data:") || html.length < 50) {
            return
        }

        viewModelScope.launch {
            repository.cacheWebpage(
                url = url,
                title = tab.title,
                htmlContent = html
            )
        }
    }

    fun openOfflineCacheManager() {
        _activeSheet.value = ActiveSheet.OFFLINE_CACHE
    }

    fun setCacheSearchQuery(query: String) {
        _cacheSearchQuery.value = query
    }

    fun deleteCachedPage(url: String) {
        viewModelScope.launch {
            repository.deleteCachedPage(url)
        }
    }

    fun clearAllCachedPages() {
        viewModelScope.launch {
            repository.clearAllCachedPages()
        }
    }

    fun pruneOldCachedPages(olderThanDays: Int = 14) {
        viewModelScope.launch {
            repository.pruneOldPages(olderThanDays)
        }
    }

    fun togglePinCachedPage(url: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.setPagePinnedOffline(url, isPinned)
        }
    }

    private fun formatUrlOrQuery(input: String): String {
        // If it starts with scheme
        if (input.startsWith("http://") || input.startsWith("https://") || input.startsWith("about:")) {
            return input
        }

        // Check if looks like a domain name
        val domainRegex = Regex("""^[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}(:\d+)?(/.*)?$""")
        return if (domainRegex.matches(input) || input.contains("localhost") || input.startsWith("192.168.")) {
            "https://$input"
        } else {
            // Search Query
            val queryEncoded = java.net.URLEncoder.encode(input, "UTF-8")
            "${_selectedSearchEngine.value.searchUrl}$queryEncoded"
        }
    }

    fun goBack() {
        viewModelScope.launch {
            _webCommands.emit(WebCommand.GoBack)
        }
    }

    fun goForward() {
        viewModelScope.launch {
            _webCommands.emit(WebCommand.GoForward)
        }
    }

    fun reload() {
        viewModelScope.launch {
            _webCommands.emit(WebCommand.Reload)
        }
    }

    fun toggleDesktopMode() {
        updateActiveTab { it.copy(isDesktopMode = !it.isDesktopMode) }
        reload()
    }

    fun onPageStarted(url: String) {
        _detectedLoginForm.value = null
        stopReadingAloud()

        // Check focus blocker again for redirects
        val enabledBlockedDomains = blockedSites.value.filter { it.isEnabled }.map { it.domain }
        val (isBlocked, domain) = AppBlockerEngine.checkBlocked(
            url,
            enabledBlockedDomains,
            isFocusModeActive = _isFocusModeActive.value,
            filterSettings = filterSettings.value
        )

        updateActiveTab {
            it.copy(
                url = url,
                isLoading = true,
                progress = 0.1f,
                adsBlockedOnPage = 0,
                trackersBlockedOnPage = 0,
                isBlockedByFocus = isBlocked,
                blockedDomain = domain,
                isReaderMode = false,
                isReaderAvailable = false,
                readerArticle = null
            )
        }
    }

    // Central Filter & Protection Settings Methods
    fun openSettings() {
        _activeSheet.value = ActiveSheet.SETTINGS
    }

    fun updateAdFilters(transform: (AdBlockFilters) -> AdBlockFilters) {
        filterSettingsRepository.updateAdFilters(transform)
    }

    fun updateTrackingFilters(transform: (TrackingFilters) -> TrackingFilters) {
        filterSettingsRepository.updateTrackingFilters(transform)
    }

    fun updateDistractionFilters(transform: (DistractionFilters) -> DistractionFilters) {
        filterSettingsRepository.updateDistractionFilters(transform)
    }

    fun applyFilterPreset(preset: FilterPreset) {
        filterSettingsRepository.applyPreset(preset)
    }

    fun resetFilterSettings() {
        filterSettingsRepository.resetToDefaults()
    }

    // Password Manager Vault & Autofill Methods
    fun openPasswordVault() {
        _activeSheet.value = ActiveSheet.PASSWORD_VAULT
    }

    fun setVaultSearchQuery(query: String) {
        _vaultSearchQuery.value = query
    }

    fun saveVaultCredential(credential: VaultCredential) {
        passwordRepository.saveCredential(credential)
    }

    fun deleteVaultCredential(id: String) {
        passwordRepository.deleteCredential(id)
    }

    fun generatePassword(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        return passwordRepository.generateSecurePassword(length, includeUppercase, includeNumbers, includeSymbols)
    }

    fun calculatePasswordStrength(password: String): PasswordStrength {
        return passwordRepository.calculatePasswordStrength(password)
    }

    fun onLoginFormDetected(domain: String, userSelector: String, passSelector: String) {
        val matched = passwordRepository.getCredentialsForDomain(domain)
        if (matched.isNotEmpty()) {
            _detectedLoginForm.value = DetectedLoginForm(
                domain = domain,
                usernameSelector = userSelector,
                passwordSelector = passSelector,
                matchedCredentials = matched
            )
        }
    }

    fun onPasswordFormSubmitted(domain: String, username: String, pass: String) {
        if (username.isBlank() || pass.isBlank()) return
        val cleanDomain = passwordRepository.extractDomain(domain)
        val existing = passwordRepository.getCredentialsForDomain(cleanDomain)
            .find { it.username.equals(username.trim(), ignoreCase = true) }

        if (existing == null || existing.password != pass) {
            _pendingSaveCredential.value = PendingSaveCredential(
                domain = cleanDomain,
                username = username.trim(),
                password = pass
            )
        }
    }

    fun autofillCredential(credential: VaultCredential) {
        val detection = _detectedLoginForm.value ?: return
        val script = PasswordAutofillEngine.buildAutofillScript(
            usernameSelector = detection.usernameSelector,
            passwordSelector = detection.passwordSelector,
            usernameValue = credential.username,
            passwordValue = credential.password
        )
        viewModelScope.launch {
            _webCommands.emit(WebCommand.EvaluateJs(script))
            _detectedLoginForm.value = null
        }
    }

    fun dismissAutofillPrompt() {
        _detectedLoginForm.value = null
    }

    fun dismissSavePasswordPrompt() {
        _pendingSaveCredential.value = null
    }

    fun savePendingCredential(neverForThisSite: Boolean = false) {
        val pending = _pendingSaveCredential.value ?: return
        if (!neverForThisSite) {
            passwordRepository.saveOrUpdateByDomainAndUser(
                domain = pending.domain,
                username = pending.username,
                password = pending.password
            )
        }
        _pendingSaveCredential.value = null
    }

    fun onPageFinished(title: String, url: String, canGoBack: Boolean, canGoForward: Boolean) {
        val tab = activeTab ?: return
        val cleanTitle = title.ifBlank { tab.url }

        updateActiveTab {
            it.copy(
                title = cleanTitle,
                url = url,
                isLoading = false,
                progress = 1.0f,
                canGoBack = canGoBack,
                canGoForward = canGoForward
            )
        }

        // Add to history if not incognito and not blank
        if (!tab.isIncognito && url.isNotBlank() && !tab.isBlockedByFocus) {
            viewModelScope.launch {
                repository.addHistory(cleanTitle, url)
            }
        }
    }

    fun onProgressChanged(progress: Int) {
        updateActiveTab {
            it.copy(
                progress = progress / 100f,
                isLoading = progress < 100
            )
        }
    }

    fun onAdOrTrackerBlocked(isTracker: Boolean) {
        updateActiveTab {
            if (isTracker) {
                it.copy(trackersBlockedOnPage = it.trackersBlockedOnPage + 1)
            } else {
                it.copy(adsBlockedOnPage = it.adsBlockedOnPage + 1)
            }
        }

        _shieldStats.update {
            it.copy(
                totalAdsBlocked = if (!isTracker) it.totalAdsBlocked + 1 else it.totalAdsBlocked,
                totalTrackersBlocked = if (isTracker) it.totalTrackersBlocked + 1 else it.totalTrackersBlocked,
                totalDataSavedKb = it.totalDataSavedKb + (if (isTracker) 35 else 160)
            )
        }
    }

    fun onJsAdElementBlocked(selector: String, count: Int) {
        updateActiveTab {
            it.copy(adsBlockedOnPage = it.adsBlockedOnPage + count)
        }
        _shieldStats.update {
            it.copy(
                totalAdsBlocked = it.totalAdsBlocked + count,
                totalDataSavedKb = it.totalDataSavedKb + (count * 120L)
            )
        }
    }

    fun onVideoAdSkipped(durationSec: Double) {
        updateActiveTab {
            it.copy(adsBlockedOnPage = it.adsBlockedOnPage + 1)
        }
        _shieldStats.update {
            it.copy(
                totalAdsBlocked = it.totalAdsBlocked + 1,
                totalDataSavedKb = it.totalDataSavedKb + 3500L
            )
        }
    }

    fun onPopupBlocked(url: String) {
        updateActiveTab {
            it.copy(adsBlockedOnPage = it.adsBlockedOnPage + 1)
        }
        _shieldStats.update {
            it.copy(totalAdsBlocked = it.totalAdsBlocked + 1)
        }
    }

    fun onAntiAdblockNeutralized(details: String) {
        // Tracks neutralized anti-adblock modals
    }

    fun dismissBlockedOverlayAndGoHome() {
        updateActiveTab {
            it.copy(
                url = "",
                title = "New Tab",
                isBlockedByFocus = false,
                blockedDomain = null
            )
        }
    }

    // Focus Session Management (App Blocker)
    fun startFocusSession(minutes: Int) {
        focusTimerJob?.cancel()
        _isFocusTimerPaused.value = false
        _focusSessionTotalMinutes.value = minutes
        _focusTimeRemainingSeconds.value = minutes * 60
        _isFocusModeActive.value = true

        // Launch background & foreground monitor service
        try {
            FocusUsageMonitorService.start(getApplication(), minutes)
        } catch (_: Exception) {}

        startFocusTimerLoop()
    }

    private fun startFocusTimerLoop() {
        focusTimerJob?.cancel()
        focusTimerJob = viewModelScope.launch {
            while (_focusTimeRemainingSeconds.value > 0) {
                delay(1000)
                if (!_isFocusTimerPaused.value) {
                    _focusTimeRemainingSeconds.update { (it - 1).coerceAtLeast(0) }
                }
            }
            // Timer expired - unlock session
            _isFocusModeActive.value = false
            _isFocusTimerPaused.value = false
            _tabs.update { currentTabs ->
                currentTabs.map { tab ->
                    if (tab.isBlockedByFocus) {
                        tab.copy(isBlockedByFocus = false, blockedDomain = null)
                    } else tab
                }
            }
            try {
                FocusUsageMonitorService.stop(getApplication())
            } catch (_: Exception) {}
        }
    }

    fun pauseFocusTimer() {
        _isFocusTimerPaused.value = true
    }

    fun resumeFocusTimer() {
        _isFocusTimerPaused.value = false
    }

    fun togglePauseResumeFocusTimer() {
        _isFocusTimerPaused.update { !it }
    }

    fun addFocusMinutes(minutes: Int = 5) {
        _focusTimeRemainingSeconds.update { it + (minutes * 60) }
        _focusSessionTotalMinutes.update { it + minutes }
    }

    fun stopFocusSession() {
        focusTimerJob?.cancel()
        _isFocusModeActive.value = false
        _isFocusTimerPaused.value = false
        _focusTimeRemainingSeconds.value = 0
        // Automatically unblock any tabs that were blocked by focus mode
        _tabs.update { currentTabs ->
            currentTabs.map { tab ->
                if (tab.isBlockedByFocus) {
                    tab.copy(isBlockedByFocus = false, blockedDomain = null)
                } else tab
            }
        }
        try {
            FocusUsageMonitorService.stop(getApplication())
        } catch (_: Exception) {}
    }

    // Media & Downloads Navigation
    fun openMediaDownloader() {
        _selectedMediaHubTab.value = 1
        _activeSheet.value = ActiveSheet.MEDIA_HUB
    }

    fun saveCurrentPageOffline(tab: BrowserTab, onSaved: () -> Unit) {
        if (tab.url.isBlank()) return
        viewModelScope.launch {
            val existing = repository.getCachedPage(tab.url)
            val title = tab.title.ifBlank { tab.url }
            val content = existing?.htmlContent ?: "<!DOCTYPE html><html><head><meta charset='utf-8'><title>${title}</title><style>body{font-family:sans-serif;padding:20px;background:#2d0922;color:#fff;}</style></head><body><h1>${title}</h1><p>Offline snapshot saved from: <a href='${tab.url}' style='color:#E95420;'>${tab.url}</a></p></body></html>"
            repository.cacheWebpage(tab.url, title, content)
            onSaved()
        }
    }

    // Security Systems Toggles
    fun updateSecurityFilters(transform: (SecurityFilters) -> SecurityFilters) {
        filterSettingsRepository.updateSecurityFilters(transform)
    }

    fun toggleBlockedSite(site: BlockedSiteEntity) {
        viewModelScope.launch {
            repository.setBlockedSiteEnabled(site.id, !site.isEnabled)
        }
    }

    fun addBlockedSite(
        domainOrPackage: String,
        displayName: String = "",
        category: String = "Custom",
        dailyLimitMinutes: Int = 30,
        isAppPackage: Boolean = false
    ) {
        viewModelScope.launch {
            repository.addBlockedSite(domainOrPackage, displayName, category, dailyLimitMinutes, isAppPackage)
        }
    }

    fun updateDailyLimit(site: BlockedSiteEntity, limitMinutes: Int) {
        viewModelScope.launch {
            repository.updateDailyLimit(site.id, limitMinutes)
        }
    }

    fun removeBlockedSite(site: BlockedSiteEntity) {
        viewModelScope.launch {
            repository.removeBlockedSite(site)
        }
    }

    // Bookmarks & Folders Management
    fun setBookmarkSearchQuery(query: String) {
        _bookmarkSearchQuery.value = query
    }

    fun setSelectedBookmarkCategory(category: String) {
        _selectedBookmarkCategory.value = category
        _selectedBookmarkFolder.value = category
    }

    fun setSelectedBookmarkFolder(folder: String) {
        _selectedBookmarkFolder.value = folder
        _selectedBookmarkCategory.value = folder
    }

    fun setSelectedBookmarkLabel(label: String?) {
        _selectedBookmarkLabel.value = label
    }

    fun setBookmarkSortOption(option: BookmarkSortOption) {
        _bookmarkSortOption.value = option
    }

    fun addBookmark(
        title: String,
        url: String,
        folder: String = "Favorites",
        labels: String = "",
        notes: String = "",
        isPinnedToNewTab: Boolean = true
    ) {
        viewModelScope.launch {
            repository.addBookmark(
                title = title,
                url = url,
                folder = folder,
                labels = labels,
                notes = notes,
                isPinnedToNewTab = isPinnedToNewTab
            )
        }
    }

    fun updateBookmark(
        id: Long,
        title: String,
        url: String,
        folder: String = "Favorites",
        labels: String = "",
        notes: String = "",
        isPinnedToNewTab: Boolean = true
    ) {
        viewModelScope.launch {
            repository.updateBookmark(
                BookmarkEntity(
                    id = id,
                    title = title.ifBlank { url },
                    url = url,
                    category = folder.ifBlank { "Favorites" },
                    folder = folder.ifBlank { "Favorites" },
                    labels = labels.trim(),
                    notes = notes,
                    isPinnedToNewTab = isPinnedToNewTab
                )
            )
        }
    }

    fun createBookmarkFolder(name: String, colorHex: String = "#E95420") {
        viewModelScope.launch {
            repository.createFolder(name, colorHex)
        }
    }

    fun deleteBookmarkFolder(name: String, deleteBookmarks: Boolean = false) {
        viewModelScope.launch {
            repository.deleteFolder(name, deleteBookmarks)
            if (_selectedBookmarkFolder.value.equals(name, ignoreCase = true)) {
                _selectedBookmarkFolder.value = "All"
                _selectedBookmarkCategory.value = "All"
            }
        }
    }

    fun renameBookmarkFolder(oldName: String, newName: String) {
        viewModelScope.launch {
            repository.renameFolder(oldName, newName)
            if (_selectedBookmarkFolder.value.equals(oldName, ignoreCase = true)) {
                _selectedBookmarkFolder.value = newName.trim()
                _selectedBookmarkCategory.value = newName.trim()
            }
        }
    }

    fun moveBookmarkToFolder(id: Long, newFolder: String) {
        viewModelScope.launch {
            repository.moveBookmarkToFolder(id, newFolder)
        }
    }

    fun updateBookmarkLabels(id: Long, labels: String) {
        viewModelScope.launch {
            repository.updateBookmarkLabels(id, labels)
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.removeBookmark(bookmark)
        }
    }

    fun deleteBookmarkById(id: Long) {
        viewModelScope.launch {
            repository.removeBookmarkById(id)
        }
    }

    fun clearAllBookmarks() {
        viewModelScope.launch {
            repository.clearAllBookmarks()
        }
    }

    fun seedUbuntuFavorites() {
        viewModelScope.launch {
            repository.seedUbuntuStarterBookmarks()
        }
    }

    fun toggleBookmarkCurrentTab() {
        val tab = activeTab ?: return
        if (tab.url.isBlank()) return

        val isAlreadyBookmarked = bookmarks.value.any { it.url == tab.url }
        viewModelScope.launch {
            if (isAlreadyBookmarked) {
                repository.removeBookmarkByUrl(tab.url)
            } else {
                val detectedCategory = when {
                    tab.url.contains("ubuntu") || tab.url.contains("canonical") -> "Ubuntu"
                    tab.url.contains("github") || tab.url.contains("gitlab") || tab.url.contains("stackoverflow") -> "Dev"
                    tab.url.contains("youtube") || tab.url.contains("vimeo") || tab.url.contains("twitch") || tab.url.contains("netflix") -> "Media"
                    tab.url.contains("news") || tab.url.contains("bbc") || tab.url.contains("cnn") || tab.url.contains("reuters") -> "News"
                    tab.url.contains("twitter") || tab.url.contains("x.com") || tab.url.contains("reddit") || tab.url.contains("facebook") || tab.url.contains("instagram") -> "Social"
                    else -> "Favorites"
                }
                repository.addBookmark(
                    title = tab.title.ifBlank { tab.url },
                    url = tab.url,
                    folder = detectedCategory,
                    labels = detectedCategory.lowercase(),
                    isPinnedToNewTab = true
                )
            }
        }
    }

    fun addQuickFavorite(
        title: String,
        url: String,
        category: String = "Favorites",
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.addBookmark(
                title = title.ifBlank { url },
                url = url,
                folder = category,
                labels = category.lowercase(),
                notes = notes,
                isPinnedToNewTab = true
            )
        }
    }

    fun togglePinBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.setBookmarkPinned(bookmark.id, !bookmark.isPinnedToNewTab)
        }
    }

    fun removeQuickFavorite(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.removeBookmark(bookmark)
        }
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun setSelectedHistoryPeriod(period: String) {
        _selectedHistoryPeriod.value = period
    }

    fun clearBrowsingHistory(timeRange: HistoryTimeRange = HistoryTimeRange.ALL_TIME) {
        viewModelScope.launch {
            repository.clearHistoryTimeRange(timeRange)
        }
    }

    fun deleteHistoryItem(historyItem: HistoryEntity) {
        viewModelScope.launch {
            repository.deleteHistoryItem(historyItem)
        }
    }

    fun deleteHistoryById(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }


    // Media Player & Library State
    private var mediaPlayer: MediaPlayer? = null

    private val sampleAudio = MediaItem(
        id = "sample_focus_audio",
        title = "Swordfish Ambient Focus Chime",
        uriString = "",
        isVideo = false,
        durationMs = 6000L,
        sizeBytes = 54903L,
        format = "m4a",
        author = "Swordfish Focus Beats",
        rawResId = R.raw.focus_ambience
    )

    private val sampleVideo = MediaItem(
        id = "sample_test_video",
        title = "Swordfish HD Video Test Stream",
        uriString = "",
        isVideo = true,
        durationMs = 4000L,
        sizeBytes = 116066L,
        format = "mp4",
        author = "Ubuntu Video Engine",
        rawResId = R.raw.sample_video
    )

    private val _mediaLibrary = MutableStateFlow(listOf(sampleAudio, sampleVideo))
    val mediaLibrary: StateFlow<List<MediaItem>> = _mediaLibrary.asStateFlow()

    private val _currentPlayingMedia = MutableStateFlow<MediaItem?>(sampleAudio)
    val currentPlayingMedia: StateFlow<MediaItem?> = _currentPlayingMedia.asStateFlow()

    private val _isMediaPlaying = MutableStateFlow(false)
    val isMediaPlaying: StateFlow<Boolean> = _isMediaPlaying.asStateFlow()

    private val _mediaPlaybackPositionMs = MutableStateFlow(0L)
    val mediaPlaybackPositionMs: StateFlow<Long> = _mediaPlaybackPositionMs.asStateFlow()

    private val _mediaPlaybackDurationMs = MutableStateFlow(6000L)
    val mediaPlaybackDurationMs: StateFlow<Long> = _mediaPlaybackDurationMs.asStateFlow()

    private val _mediaPlaybackSpeed = MutableStateFlow(1.0f)
    val mediaPlaybackSpeed: StateFlow<Float> = _mediaPlaybackSpeed.asStateFlow()

    private val _isMediaLooping = MutableStateFlow(true)
    val isMediaLooping: StateFlow<Boolean> = _isMediaLooping.asStateFlow()

    private val _activeDownloads = MutableStateFlow<List<MediaDownloadTask>>(emptyList())
    val activeDownloads: StateFlow<List<MediaDownloadTask>> = _activeDownloads.asStateFlow()

    fun playMedia(item: MediaItem) {
        _currentPlayingMedia.value = item
        _mediaPlaybackDurationMs.value = item.durationMs
        _mediaPlaybackPositionMs.value = 0L

        if (!item.isVideo) {
            startAudioPlayback(item)
        } else {
            releaseAudioPlayer()
            _isMediaPlaying.value = true
        }
    }

    private fun startAudioPlayback(item: MediaItem) {
        releaseAudioPlayer()
        try {
            val player = if (item.rawResId != null) {
                MediaPlayer.create(getApplication(), item.rawResId)
            } else if (item.uriString.isNotBlank()) {
                MediaPlayer().apply {
                    setDataSource(getApplication(), Uri.parse(item.uriString))
                    prepare()
                }
            } else {
                null
            }

            if (player != null) {
                mediaPlayer = player
                player.isLooping = _isMediaLooping.value
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    player.playbackParams = player.playbackParams.setSpeed(_mediaPlaybackSpeed.value)
                }
                player.setOnCompletionListener {
                    if (!_isMediaLooping.value) {
                        _isMediaPlaying.value = false
                        _mediaPlaybackPositionMs.value = 0L
                    }
                }
                player.start()
                _isMediaPlaying.value = true
                _mediaPlaybackDurationMs.value = player.duration.toLong()
            }
        } catch (_: Exception) {
            _isMediaPlaying.value = false
        }
    }

    private fun releaseAudioPlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    fun toggleMediaPlayPause() {
        val currentItem = _currentPlayingMedia.value ?: return
        if (currentItem.isVideo) {
            _isMediaPlaying.update { !it }
        } else {
            val player = mediaPlayer
            if (player != null) {
                if (player.isPlaying) {
                    player.pause()
                    _isMediaPlaying.value = false
                } else {
                    player.start()
                    _isMediaPlaying.value = true
                }
            } else {
                startAudioPlayback(currentItem)
            }
        }
    }

    fun seekMediaTo(positionMs: Long) {
        _mediaPlaybackPositionMs.value = positionMs
        mediaPlayer?.let { player ->
            try {
                player.seekTo(positionMs.toInt())
            } catch (_: Exception) {}
        }
    }

    fun setMediaSpeed(speed: Float) {
        _mediaPlaybackSpeed.value = speed
        mediaPlayer?.let { player ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    player.playbackParams = player.playbackParams.setSpeed(speed)
                }
            } catch (_: Exception) {}
        }
    }

    fun toggleMediaLoop() {
        val newLoop = !_isMediaLooping.value
        _isMediaLooping.value = newLoop
        mediaPlayer?.isLooping = newLoop
    }

    fun tickMediaPlayback() {
        val player = mediaPlayer
        if (player != null && player.isPlaying) {
            _mediaPlaybackPositionMs.value = player.currentPosition.toLong()
            _mediaPlaybackDurationMs.value = player.duration.toLong()
        } else if (_isMediaPlaying.value && _currentPlayingMedia.value?.isVideo == true) {
            val next = _mediaPlaybackPositionMs.value + 500L
            val dur = _mediaPlaybackDurationMs.value
            if (dur > 0 && next >= dur) {
                if (_isMediaLooping.value) {
                    _mediaPlaybackPositionMs.value = 0L
                } else {
                    _isMediaPlaying.value = false
                    _mediaPlaybackPositionMs.value = dur
                }
            } else {
                _mediaPlaybackPositionMs.value = next
            }
        }
    }

    fun startDownload(url: String, format: String, isVideo: Boolean, title: String) {
        val taskId = "dl_${System.currentTimeMillis()}"
        val task = MediaDownloadTask(
            id = taskId,
            url = url,
            title = title,
            format = format,
            isVideo = isVideo,
            progress = 0.05f,
            status = DownloadStatus.DOWNLOADING
        )
        _activeDownloads.update { listOf(task) + it }

        viewModelScope.launch {
            for (p in 1..10) {
                delay(200)
                _activeDownloads.update { list ->
                    list.map { if (it.id == taskId) it.copy(progress = p * 0.1f) else it }
                }
            }

            _activeDownloads.update { list ->
                list.map { if (it.id == taskId) it.copy(progress = 1.0f, status = DownloadStatus.COMPLETED) else it }
            }

            val downloadedItem = MediaItem(
                id = "downloaded_$taskId",
                title = title,
                uriString = url,
                isVideo = isVideo,
                durationMs = if (isVideo) 45000L else 180000L,
                sizeBytes = if (isVideo) 14500000L else 4200000L,
                format = format,
                author = "Downloaded via yt-dlp"
            )
            _mediaLibrary.update { listOf(downloadedItem) + it }
        }
    }

    fun deleteMediaItem(item: MediaItem) {
        if (_currentPlayingMedia.value?.id == item.id) {
            releaseAudioPlayer()
            _isMediaPlaying.value = false
            _currentPlayingMedia.value = null
        }
        _mediaLibrary.update { list -> list.filterNot { it.id == item.id } }
    }

    // ==========================================================
    // READER MODE CONTROLLER & ACTIONS
    // ==========================================================
    fun toggleReaderMode() {
        val tab = activeTab ?: return
        if (tab.isReaderMode) {
            exitReaderMode()
        } else {
            enterReaderMode()
        }
    }

    fun enterReaderMode() {
        val tab = activeTab ?: return
        if (tab.readerArticle != null) {
            updateActiveTab { it.copy(isReaderMode = true) }
        } else {
            // Trigger JavaScript extraction in web view
            viewModelScope.launch {
                _webCommands.emit(WebCommand.EvaluateJs(ReaderModeEngine.READER_EXTRACTOR_JS))
            }
        }
    }

    fun exitReaderMode() {
        stopReadingAloud()
        updateActiveTab { it.copy(isReaderMode = false) }
    }

    fun onReaderContentExtracted(jsonString: String) {
        val article = ReaderArticle.fromJsonString(jsonString) ?: return
        updateActiveTab {
            it.copy(
                isReaderMode = true,
                isReaderAvailable = true,
                readerArticle = article
            )
        }
    }

    fun onReaderModeUnavailable(reason: String) {
        updateActiveTab {
            it.copy(isReaderAvailable = false)
        }
    }

    fun onReaderAvailabilityChanged(available: Boolean) {
        updateActiveTab {
            it.copy(isReaderAvailable = available)
        }
        if (available && _readerSettings.value.autoEnterOnArticlePages && activeTab?.isReaderMode == false) {
            enterReaderMode()
        }
    }

    fun updateReaderTheme(theme: ReaderTheme) {
        saveReaderSettings(_readerSettings.value.copy(theme = theme))
    }

    fun updateReaderFontFamily(fontFamily: ReaderFontFamily) {
        saveReaderSettings(_readerSettings.value.copy(fontFamily = fontFamily))
    }

    fun updateReaderFontSize(deltaSp: Int) {
        val current = _readerSettings.value.fontSizeSp
        val newSize = (current + deltaSp).coerceIn(12, 28)
        saveReaderSettings(_readerSettings.value.copy(fontSizeSp = newSize))
    }

    fun updateReaderLineHeight(lineHeight: ReaderLineHeight) {
        saveReaderSettings(_readerSettings.value.copy(lineHeight = lineHeight))
    }

    fun updateReaderContentWidth(contentWidth: ReaderContentWidth) {
        saveReaderSettings(_readerSettings.value.copy(contentWidth = contentWidth))
    }

    fun toggleAutoEnterReaderMode() {
        val current = _readerSettings.value.autoEnterOnArticlePages
        saveReaderSettings(_readerSettings.value.copy(autoEnterOnArticlePages = !current))
    }

    // ==========================================================
    // TEXT-TO-SPEECH (TTS) ENGINE FOR READER MODE
    // ==========================================================
    private fun initTtsIfNeeded() {
        if (tts == null) {
            tts = TextToSpeech(getApplication()) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.getDefault()
                    isTtsInitialized = true
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isReadingAloud.value = true
                        }
                        override fun onDone(utteranceId: String?) {
                            _isReadingAloud.value = false
                        }
                        override fun onError(utteranceId: String?) {
                            _isReadingAloud.value = false
                        }
                    })
                }
            }
        }
    }

    fun toggleReadAloud(text: String) {
        if (_isReadingAloud.value) {
            stopReadingAloud()
        } else {
            startReadingAloud(text)
        }
    }

    fun startReadingAloud(text: String) {
        if (text.isBlank()) return
        initTtsIfNeeded()
        _isReadingAloud.value = true
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "reader_utterance_${System.currentTimeMillis()}")
    }

    fun stopReadingAloud() {
        _isReadingAloud.value = false
        tts?.stop()
    }

    override fun onCleared() {
        super.onCleared()
        releaseAudioPlayer()
        stopReadingAloud()
        tts?.shutdown()
        tts = null
    }

    private fun updateActiveTab(transform: (BrowserTab) -> BrowserTab) {
        val currentId = _activeTabId.value
        _tabs.update { list ->
            list.map { if (it.id == currentId) transform(it) else it }
        }
    }
}
