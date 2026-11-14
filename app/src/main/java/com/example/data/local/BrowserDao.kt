package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class MostVisitedSite(
    val url: String,
    val title: String,
    val visitCount: Int
)

@Dao
interface BrowserDao {

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE isPinnedToNewTab = 1 ORDER BY visitCount DESC, createdAt DESC")
    fun getQuickAccessBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY visitCount DESC, createdAt DESC LIMIT :limit")
    fun getTopVisitedBookmarks(limit: Int = 8): Flow<List<BookmarkEntity>>

    // Folders
    @Query("SELECT * FROM bookmark_folders ORDER BY isSystem DESC, name ASC")
    fun getAllFolders(): Flow<List<BookmarkFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folder: BookmarkFolderEntity)

    @Query("DELETE FROM bookmark_folders WHERE name = :name")
    suspend fun deleteFolder(name: String)

    @Query("UPDATE bookmark_folders SET name = :newName WHERE name = :oldName")
    suspend fun renameFolder(oldName: String, newName: String)

    @Query("UPDATE bookmarks SET folder = :newFolder, category = :newFolder WHERE folder = :oldFolder OR category = :oldFolder")
    suspend fun moveBookmarksToFolder(oldFolder: String, newFolder: String)

    @Query("DELETE FROM bookmarks WHERE folder = :folder OR category = :folder")
    suspend fun deleteBookmarksByFolder(folder: String)

    @Query("UPDATE bookmarks SET folder = :folder, category = :folder WHERE id = :id")
    suspend fun updateBookmarkFolder(id: Long, folder: String)

    @Query("UPDATE bookmarks SET labels = :labels WHERE id = :id")
    suspend fun updateBookmarkLabels(id: Long, labels: String)

    @Query("SELECT * FROM bookmarks WHERE folder = :folder OR category = :folder ORDER BY createdAt DESC")
    fun getBookmarksByFolder(folder: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE folder = :category OR category = :category ORDER BY createdAt DESC")
    fun getBookmarksByCategory(category: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE labels LIKE '%' || :label || '%' ORDER BY createdAt DESC")
    fun getBookmarksByLabel(label: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR folder LIKE '%' || :query || '%' OR labels LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchBookmarks(query: String): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url)")
    fun isBookmarked(url: String): Flow<Boolean>

    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    suspend fun getBookmarkByUrl(url: String): BookmarkEntity?

    @Query("UPDATE bookmarks SET isPinnedToNewTab = :isPinned WHERE id = :id")
    suspend fun setBookmarkPinned(id: Long, isPinned: Boolean)

    @Query("UPDATE bookmarks SET visitCount = visitCount + 1 WHERE id = :id")
    suspend fun incrementBookmarkVisitCount(id: Long)

    @Query("UPDATE bookmarks SET visitCount = visitCount + 1 WHERE url = :url")
    suspend fun incrementBookmarkVisitCountByUrl(url: String)

    @Query("DELETE FROM bookmarks")
    suspend fun clearAllBookmarks()

    // History & Most Visited Sites
    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT 200")
    fun getRecentHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT COUNT(*) FROM history")
    fun getHistoryCount(): Flow<Int>

    @Query("SELECT * FROM history WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY visitedAt DESC")
    fun searchHistory(query: String): Flow<List<HistoryEntity>>

    @Query("SELECT url, title, COUNT(*) as visitCount FROM history GROUP BY url ORDER BY visitCount DESC, MAX(visitedAt) DESC LIMIT :limit")
    fun getMostVisitedSitesFromHistory(limit: Int = 8): Flow<List<MostVisitedSite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    @Delete
    suspend fun deleteHistoryItem(history: HistoryEntity)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM history WHERE url = :url")
    suspend fun deleteHistoryByUrl(url: String)

    @Query("DELETE FROM history WHERE visitedAt >= :sinceTimestamp")
    suspend fun deleteHistorySince(sinceTimestamp: Long)

    // Blocked Sites & App Packages
    @Query("SELECT * FROM blocked_sites ORDER BY domain ASC")
    fun getAllBlockedSites(): Flow<List<BlockedSiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedSite(site: BlockedSiteEntity): Long

    @Query("UPDATE blocked_sites SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setBlockedSiteEnabled(id: Long, isEnabled: Boolean)

    @Query("UPDATE blocked_sites SET dailyLimitMinutes = :limitMinutes WHERE id = :id")
    suspend fun updateDailyLimit(id: Long, limitMinutes: Int)

    @Delete
    suspend fun deleteBlockedSite(site: BlockedSiteEntity)

    @Query("DELETE FROM blocked_sites WHERE domain = :domain")
    suspend fun deleteBlockedSiteByDomain(domain: String)

    // Usage Times Queries
    @Query("SELECT * FROM usage_times WHERE dateString = :date ORDER BY activeSecondsToday DESC")
    fun getUsageTimesForDate(date: String): Flow<List<UsageTimeEntity>>

    @Query("SELECT * FROM usage_times WHERE targetIdentifier = :target AND dateString = :date LIMIT 1")
    suspend fun getUsageForTarget(target: String, date: String): UsageTimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUsage(usage: UsageTimeEntity): Long

    @Query("UPDATE usage_times SET activeSecondsToday = activeSecondsToday + :addedSeconds, lastActiveTime = :now WHERE targetIdentifier = :target AND dateString = :date")
    suspend fun incrementUsageSeconds(target: String, date: String, addedSeconds: Long, now: Long): Int

    @Query("UPDATE usage_times SET timesBlockedToday = timesBlockedToday + 1 WHERE targetIdentifier = :target AND dateString = :date")
    suspend fun incrementTimesBlocked(target: String, date: String): Int

    // Cached Offline Webpages
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCachedPage(page: CachedPageEntity)

    @Query("SELECT * FROM cached_pages WHERE url = :url LIMIT 1")
    suspend fun getCachedPageByUrl(url: String): CachedPageEntity?

    @Query("SELECT * FROM cached_pages ORDER BY isPinnedOffline DESC, visitCount DESC, cachedAtTimestamp DESC")
    fun getAllCachedPages(): Flow<List<CachedPageEntity>>

    @Query("SELECT * FROM cached_pages ORDER BY visitCount DESC, cachedAtTimestamp DESC LIMIT :limit")
    fun getFrequentlyVisitedCachedPages(limit: Int = 10): Flow<List<CachedPageEntity>>

    @Query("SELECT * FROM cached_pages WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' OR plainTextSnippet LIKE '%' || :query || '%' ORDER BY isPinnedOffline DESC, visitCount DESC")
    fun searchCachedPages(query: String): Flow<List<CachedPageEntity>>

    @Query("DELETE FROM cached_pages WHERE url = :url")
    suspend fun deleteCachedPage(url: String)

    @Query("DELETE FROM cached_pages")
    suspend fun clearAllCachedPages()

    @Query("DELETE FROM cached_pages WHERE isPinnedOffline = 0 AND cachedAtTimestamp < :cutoffTimestamp")
    suspend fun pruneOldPages(cutoffTimestamp: Long): Int

    @Query("UPDATE cached_pages SET isPinnedOffline = :isPinned WHERE url = :url")
    suspend fun setCachedPagePinned(url: String, isPinned: Boolean)

    @Query("UPDATE cached_pages SET visitCount = visitCount + 1, cachedAtTimestamp = :now WHERE url = :url")
    suspend fun incrementCachedPageVisit(url: String, now: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM cached_pages")
    fun getCachedPageCount(): Flow<Int>

    @Query("SELECT SUM(contentSizeBytes) FROM cached_pages")
    fun getTotalCacheSizeBytes(): Flow<Long?>
}
