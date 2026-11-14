package com.example.data

import com.example.data.local.BlockedSiteEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.BookmarkFolderEntity
import com.example.data.local.BrowserDao
import com.example.data.local.CachedPageEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.MostVisitedSite
import com.example.data.local.UsageTimeEntity
import com.example.data.model.HistoryTimeRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BrowserRepository(private val dao: BrowserDao) {

    val allBookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    val quickAccessBookmarks: Flow<List<BookmarkEntity>> = dao.getQuickAccessBookmarks()
    val allFolders: Flow<List<BookmarkFolderEntity>> = dao.getAllFolders()
    val allLabels: Flow<List<String>> = dao.getAllBookmarks().map { list ->
        list.flatMap { it.getLabelList() }.distinct().sorted()
    }
    val recentHistory: Flow<List<HistoryEntity>> = dao.getRecentHistory()
    val historyCount: Flow<Int> = dao.getHistoryCount()
    val mostVisitedSites: Flow<List<MostVisitedSite>> = dao.getMostVisitedSitesFromHistory(limit = 8)
    val allBlockedSites: Flow<List<BlockedSiteEntity>> = dao.getAllBlockedSites()
    val allCachedPages: Flow<List<CachedPageEntity>> = dao.getAllCachedPages()
    val frequentlyVisitedCachedPages: Flow<List<CachedPageEntity>> = dao.getFrequentlyVisitedCachedPages(limit = 10)
    val cachedPageCount: Flow<Int> = dao.getCachedPageCount()
    val totalCacheSizeBytes: Flow<Long?> = dao.getTotalCacheSizeBytes()

    fun isBookmarked(url: String): Flow<Boolean> = dao.isBookmarked(url)

    suspend fun getBookmarkByUrl(url: String): BookmarkEntity? = dao.getBookmarkByUrl(url)

    suspend fun addBookmark(
        title: String,
        url: String,
        folder: String = "Favorites",
        labels: String = "",
        notes: String = "",
        isPinnedToNewTab: Boolean = true
    ): Long {
        if (url.isNotBlank()) {
            val targetFolder = folder.ifBlank { "Favorites" }.trim()
            dao.insertFolder(BookmarkFolderEntity(name = targetFolder, isSystem = false))
            return dao.insertBookmark(
                BookmarkEntity(
                    title = title.ifBlank { url },
                    url = url,
                    category = targetFolder,
                    folder = targetFolder,
                    labels = labels.trim(),
                    notes = notes,
                    isPinnedToNewTab = isPinnedToNewTab
                )
            )
        }
        return -1L
    }

    suspend fun updateBookmark(bookmark: BookmarkEntity) {
        val targetFolder = bookmark.folder.ifBlank { bookmark.category.ifBlank { "Favorites" } }.trim()
        dao.insertFolder(BookmarkFolderEntity(name = targetFolder, isSystem = false))
        dao.updateBookmark(bookmark.copy(folder = targetFolder, category = targetFolder))
    }

    suspend fun createFolder(name: String, colorHex: String = "#E95420") {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            dao.insertFolder(BookmarkFolderEntity(name = trimmed, colorHex = colorHex, isSystem = false))
        }
    }

    suspend fun deleteFolder(name: String, deleteBookmarks: Boolean = false) {
        val trimmed = name.trim()
        if (deleteBookmarks) {
            dao.deleteBookmarksByFolder(trimmed)
        } else {
            dao.moveBookmarksToFolder(trimmed, "Favorites")
        }
        dao.deleteFolder(trimmed)
    }

    suspend fun renameFolder(oldName: String, newName: String) {
        val oldTrim = oldName.trim()
        val newTrim = newName.trim()
        if (newTrim.isNotBlank() && oldTrim != newTrim) {
            dao.insertFolder(BookmarkFolderEntity(name = newTrim, isSystem = false))
            dao.moveBookmarksToFolder(oldTrim, newTrim)
            dao.deleteFolder(oldTrim)
        }
    }

    suspend fun moveBookmarkToFolder(id: Long, newFolder: String) {
        val targetFolder = newFolder.trim().ifBlank { "Favorites" }
        dao.insertFolder(BookmarkFolderEntity(name = targetFolder, isSystem = false))
        dao.updateBookmarkFolder(id, targetFolder)
    }

    suspend fun updateBookmarkLabels(id: Long, labels: String) {
        dao.updateBookmarkLabels(id, labels.trim())
    }

    fun getBookmarksByFolder(folder: String): Flow<List<BookmarkEntity>> {
        return if (folder.equals("All", ignoreCase = true)) {
            dao.getAllBookmarks()
        } else {
            dao.getBookmarksByFolder(folder)
        }
    }

    fun getBookmarksByLabel(label: String): Flow<List<BookmarkEntity>> {
        val cleanLabel = label.trim().removePrefix("#")
        return if (cleanLabel.isBlank() || cleanLabel.equals("All", ignoreCase = true)) {
            dao.getAllBookmarks()
        } else {
            dao.getBookmarksByLabel(cleanLabel)
        }
    }

    suspend fun setBookmarkPinned(id: Long, isPinned: Boolean) {
        dao.setBookmarkPinned(id, isPinned)
    }

    suspend fun incrementBookmarkVisit(url: String) {
        dao.incrementBookmarkVisitCountByUrl(url)
    }

    suspend fun removeBookmark(bookmark: BookmarkEntity) {
        dao.deleteBookmark(bookmark)
    }

    suspend fun removeBookmarkById(id: Long) {
        dao.deleteBookmarkById(id)
    }

    suspend fun removeBookmarkByUrl(url: String) {
        dao.deleteBookmarkByUrl(url)
    }

    suspend fun clearAllBookmarks() {
        dao.clearAllBookmarks()
    }

    fun searchBookmarks(query: String): Flow<List<BookmarkEntity>> {
        return if (query.isBlank()) {
            dao.getAllBookmarks()
        } else {
            dao.searchBookmarks(query.trim())
        }
    }

    fun getBookmarksByCategory(category: String): Flow<List<BookmarkEntity>> {
        return getBookmarksByFolder(category)
    }

    suspend fun seedUbuntuStarterBookmarks() {
        val ubuntuBookmarks = listOf(
            BookmarkEntity(
                title = "DuckDuckGo Privacy Search",
                url = "https://duckduckgo.com",
                category = "Favorites",
                folder = "Favorites",
                labels = "search, privacy",
                notes = "Default privacy search engine",
                isPinnedToNewTab = true,
                visitCount = 12
            ),
            BookmarkEntity(
                title = "Ubuntu Homepage",
                url = "https://ubuntu.com",
                category = "Ubuntu",
                folder = "Ubuntu",
                labels = "os, linux, official",
                notes = "The world's most popular Linux operating system",
                isPinnedToNewTab = true,
                visitCount = 10
            ),
            BookmarkEntity(
                title = "Ask Ubuntu",
                url = "https://askubuntu.com",
                category = "Ubuntu",
                folder = "Ubuntu",
                labels = "support, q&a, community",
                notes = "Community questions and answers for Ubuntu users",
                isPinnedToNewTab = true,
                visitCount = 8
            ),
            BookmarkEntity(
                title = "OMG! Ubuntu",
                url = "https://www.omgubuntu.co.uk",
                category = "Ubuntu",
                folder = "Ubuntu",
                labels = "news, blog, customisation",
                notes = "Ubuntu news, apps, desktop customization",
                isPinnedToNewTab = true,
                visitCount = 6
            ),
            BookmarkEntity(
                title = "Canonical",
                url = "https://canonical.com",
                category = "Ubuntu",
                folder = "Ubuntu",
                labels = "canonical, enterprise, cloud",
                notes = "Enterprise open source solutions",
                isPinnedToNewTab = true,
                visitCount = 5
            ),
            BookmarkEntity(
                title = "GitHub",
                url = "https://github.com",
                category = "Dev",
                folder = "Dev",
                labels = "git, code, open-source",
                notes = "Open-source development and code repositories",
                isPinnedToNewTab = true,
                visitCount = 9
            ),
            BookmarkEntity(
                title = "Wikipedia",
                url = "https://www.wikipedia.org",
                category = "Favorites",
                folder = "Favorites",
                labels = "encyclopedia, reference, knowledge",
                notes = "Free online encyclopedia",
                isPinnedToNewTab = true,
                visitCount = 7
            ),
            BookmarkEntity(
                title = "YouTube",
                url = "https://m.youtube.com",
                category = "Media",
                folder = "Media",
                labels = "video, streaming, entertainment",
                notes = "Online video platform with Swordfish ad blocker",
                isPinnedToNewTab = true,
                visitCount = 15
            )
        )
        ubuntuBookmarks.forEach { dao.insertBookmark(it) }
    }

    fun searchHistory(query: String): Flow<List<HistoryEntity>> {
        return dao.searchHistory(query)
    }

    suspend fun addHistory(title: String, url: String) {
        val trimmed = url.trim()
        if (trimmed.isNotBlank() && !trimmed.startsWith("about:") && !trimmed.startsWith("data:")) {
            dao.insertHistory(HistoryEntity(title = title.ifBlank { trimmed }, url = trimmed))
            dao.incrementBookmarkVisitCountByUrl(trimmed)
        }
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }

    suspend fun clearHistoryTimeRange(timeRange: HistoryTimeRange) {
        val now = System.currentTimeMillis()
        when (timeRange) {
            HistoryTimeRange.LAST_HOUR -> dao.deleteHistorySince(now - 3_600_000L)
            HistoryTimeRange.LAST_24_HOURS -> dao.deleteHistorySince(now - 86_400_000L)
            HistoryTimeRange.LAST_7_DAYS -> dao.deleteHistorySince(now - 7 * 86_400_000L)
            HistoryTimeRange.ALL_TIME -> dao.clearHistory()
        }
    }

    suspend fun deleteHistoryItem(history: HistoryEntity) {
        dao.deleteHistoryItem(history)
    }

    suspend fun deleteHistoryById(id: Long) {
        dao.deleteHistoryById(id)
    }

    suspend fun deleteHistoryByUrl(url: String) {
        dao.deleteHistoryByUrl(url)
    }

    suspend fun addBlockedSite(
        domainOrPackage: String,
        displayName: String = "",
        category: String = "Custom",
        dailyLimitMinutes: Int = 30,
        isAppPackage: Boolean = false
    ) {
        val cleanIdentifier = domainOrPackage.trim().lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .split("/")[0]

        if (cleanIdentifier.isNotBlank()) {
            val title = displayName.ifBlank {
                cleanIdentifier.split(".").lastOrNull()?.replaceFirstChar { it.uppercase() } ?: cleanIdentifier
            }
            dao.insertBlockedSite(
                BlockedSiteEntity(
                    domain = cleanIdentifier,
                    displayName = title,
                    category = category,
                    dailyLimitMinutes = dailyLimitMinutes,
                    isAppPackage = isAppPackage
                )
            )
        }
    }

    suspend fun setBlockedSiteEnabled(id: Long, isEnabled: Boolean) {
        dao.setBlockedSiteEnabled(id, isEnabled)
    }

    suspend fun updateDailyLimit(id: Long, limitMinutes: Int) {
        dao.updateDailyLimit(id, limitMinutes)
    }

    suspend fun removeBlockedSite(site: BlockedSiteEntity) {
        dao.deleteBlockedSite(site)
    }

    // Usage Monitoring methods
    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getUsageTimesForToday(): Flow<List<UsageTimeEntity>> {
        return dao.getUsageTimesForDate(getTodayDateString())
    }

    suspend fun recordActiveUsage(target: String, seconds: Long) {
        val date = getTodayDateString()
        val now = System.currentTimeMillis()
        val rowsUpdated = dao.incrementUsageSeconds(target, date, seconds, now)
        if (rowsUpdated == 0) {
            dao.insertOrUpdateUsage(
                UsageTimeEntity(
                    targetIdentifier = target,
                    dateString = date,
                    activeSecondsToday = seconds,
                    lastActiveTime = now,
                    timesBlockedToday = 0
                )
            )
        }
    }

    suspend fun recordBlockedOccurrence(target: String) {
        val date = getTodayDateString()
        val rowsUpdated = dao.incrementTimesBlocked(target, date)
        if (rowsUpdated == 0) {
            dao.insertOrUpdateUsage(
                UsageTimeEntity(
                    targetIdentifier = target,
                    dateString = date,
                    activeSecondsToday = 0,
                    lastActiveTime = System.currentTimeMillis(),
                    timesBlockedToday = 1
                )
            )
        }
    }

    // Local Offline Webpage Caching
    suspend fun cacheWebpage(
        url: String,
        title: String,
        htmlContent: String,
        isPinned: Boolean = false,
        faviconUrl: String? = null
    ) {
        if (url.isBlank() || htmlContent.isBlank() || url.startsWith("about:") || url.startsWith("data:")) {
            return
        }

        val domain = try {
            java.net.URI(url).host ?: url
        } catch (_: Exception) {
            url.split("/").getOrNull(2) ?: url
        }

        val plainText = htmlContent
            .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<[^>]*>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(300)

        val contentSize = htmlContent.toByteArray(Charsets.UTF_8).size.toLong()
        val existing = dao.getCachedPageByUrl(url)
        val newVisitCount = (existing?.visitCount ?: 0) + 1
        val pinnedState = if (isPinned) true else (existing?.isPinnedOffline ?: false)

        dao.insertOrUpdateCachedPage(
            CachedPageEntity(
                url = url,
                title = title.ifBlank { domain },
                domain = domain,
                htmlContent = htmlContent,
                plainTextSnippet = plainText,
                cachedAtTimestamp = System.currentTimeMillis(),
                contentSizeBytes = contentSize,
                visitCount = newVisitCount,
                isPinnedOffline = pinnedState,
                faviconUrl = faviconUrl ?: existing?.faviconUrl
            )
        )
    }

    suspend fun getCachedPage(url: String): CachedPageEntity? = dao.getCachedPageByUrl(url)

    suspend fun deleteCachedPage(url: String) = dao.deleteCachedPage(url)

    suspend fun clearAllCachedPages() = dao.clearAllCachedPages()

    suspend fun pruneOldPages(olderThanDays: Int = 14): Int {
        val cutoffTimestamp = System.currentTimeMillis() - (olderThanDays * 24L * 60 * 60 * 1000)
        return dao.pruneOldPages(cutoffTimestamp)
    }

    suspend fun setPagePinnedOffline(url: String, isPinned: Boolean) {
        dao.setCachedPagePinned(url, isPinned)
    }

    suspend fun recordCachedPageVisit(url: String) {
        dao.incrementCachedPageVisit(url)
    }

    fun searchCachedPages(query: String): Flow<List<CachedPageEntity>> {
        return if (query.isBlank()) {
            dao.getAllCachedPages()
        } else {
            dao.searchCachedPages(query.trim())
        }
    }
}
