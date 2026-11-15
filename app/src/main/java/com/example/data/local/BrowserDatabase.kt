package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BookmarkEntity::class,
        BookmarkFolderEntity::class,
        HistoryEntity::class,
        BlockedSiteEntity::class,
        UsageTimeEntity::class,
        CachedPageEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun browserDao(): BrowserDao

    companion object {
        @Volatile
        private var INSTANCE: BrowserDatabase? = null

        fun getInstance(context: Context): BrowserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrowserDatabase::class.java,
                    "swordfish_browser.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).browserDao()

                                // Pre-populate blacklisted apps and websites with daily limits
                                val defaultBlocked = listOf(
                                    // Social Media Sites & Packages
                                    BlockedSiteEntity(
                                        domain = "tiktok.com",
                                        displayName = "TikTok (Web)",
                                        category = "Social Media",
                                        dailyLimitMinutes = 20,
                                        isAppPackage = false
                                    ),
                                    BlockedSiteEntity(
                                        domain = "com.zhiliaoapp.musically",
                                        displayName = "TikTok (App)",
                                        category = "Social Media",
                                        dailyLimitMinutes = 20,
                                        isAppPackage = true
                                    ),
                                    BlockedSiteEntity(
                                        domain = "instagram.com",
                                        displayName = "Instagram (Web)",
                                        category = "Social Media",
                                        dailyLimitMinutes = 30,
                                        isAppPackage = false
                                    ),
                                    BlockedSiteEntity(
                                        domain = "com.instagram.android",
                                        displayName = "Instagram (App)",
                                        category = "Social Media",
                                        dailyLimitMinutes = 30,
                                        isAppPackage = true
                                    ),
                                    BlockedSiteEntity(
                                        domain = "x.com",
                                        displayName = "X / Twitter (Web)",
                                        category = "Social Media",
                                        dailyLimitMinutes = 30,
                                        isAppPackage = false
                                    ),
                                    BlockedSiteEntity(
                                        domain = "com.twitter.android",
                                        displayName = "X / Twitter (App)",
                                        category = "Social Media",
                                        dailyLimitMinutes = 30,
                                        isAppPackage = true
                                    ),
                                    BlockedSiteEntity(
                                        domain = "reddit.com",
                                        displayName = "Reddit (Web)",
                                        category = "Social Media",
                                        dailyLimitMinutes = 45,
                                        isAppPackage = false
                                    ),
                                    BlockedSiteEntity(
                                        domain = "com.reddit.frontpage",
                                        displayName = "Reddit (App)",
                                        category = "Social Media",
                                        dailyLimitMinutes = 45,
                                        isAppPackage = true
                                    ),
                                    BlockedSiteEntity(
                                        domain = "twitch.tv",
                                        displayName = "Twitch",
                                        category = "Entertainment",
                                        dailyLimitMinutes = 30,
                                        isAppPackage = false
                                    ),
                                    BlockedSiteEntity(
                                        domain = "netflix.com",
                                        displayName = "Netflix",
                                        category = "Entertainment",
                                        dailyLimitMinutes = 60,
                                        isAppPackage = false
                                    )
                                )
                                defaultBlocked.forEach { dao.insertBlockedSite(it) }

                                // Pre-populate default bookmark organizer folders
                                val defaultFolders = listOf(
                                    BookmarkFolderEntity(name = "Favorites", colorHex = "#E95420", isSystem = true),
                                    BookmarkFolderEntity(name = "Ubuntu", colorHex = "#77216F", isSystem = true),
                                    BookmarkFolderEntity(name = "Dev", colorHex = "#2C001E", isSystem = true),
                                    BookmarkFolderEntity(name = "Media", colorHex = "#E95420", isSystem = true),
                                    BookmarkFolderEntity(name = "News", colorHex = "#38B44A", isSystem = true),
                                    BookmarkFolderEntity(name = "Social", colorHex = "#19B6EE", isSystem = true),
                                    BookmarkFolderEntity(name = "Work", colorHex = "#762572", isSystem = false)
                                )
                                defaultFolders.forEach { dao.insertFolder(it) }

                                // Pre-populate helpful initial bookmarks with folders and custom labels
                                val defaultBookmarks = listOf(
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
                                        labels = "linux, canonical, os",
                                        notes = "The world's most popular Linux operating system",
                                        isPinnedToNewTab = true,
                                        visitCount = 10
                                    ),
                                    BookmarkEntity(
                                        title = "Ask Ubuntu",
                                        url = "https://askubuntu.com",
                                        category = "Ubuntu",
                                        folder = "Ubuntu",
                                        labels = "support, community, linux",
                                        notes = "Community troubleshooting and questions",
                                        isPinnedToNewTab = true,
                                        visitCount = 8
                                    ),
                                    BookmarkEntity(
                                        title = "OMG! Ubuntu",
                                        url = "https://www.omgubuntu.co.uk",
                                        category = "Ubuntu",
                                        folder = "Ubuntu",
                                        labels = "news, customization",
                                        notes = "Ubuntu news, apps, desktop customization",
                                        isPinnedToNewTab = true,
                                        visitCount = 6
                                    ),
                                    BookmarkEntity(
                                        title = "Canonical",
                                        url = "https://canonical.com",
                                        category = "Ubuntu",
                                        folder = "Ubuntu",
                                        labels = "enterprise, canonical",
                                        notes = "Publisher of Ubuntu",
                                        isPinnedToNewTab = true,
                                        visitCount = 5
                                    ),
                                    BookmarkEntity(
                                        title = "Wikipedia",
                                        url = "https://www.wikipedia.org",
                                        category = "Favorites",
                                        folder = "Favorites",
                                        labels = "reference, encyclopedia",
                                        notes = "Free online encyclopedia",
                                        isPinnedToNewTab = true,
                                        visitCount = 7
                                    ),
                                    BookmarkEntity(
                                        title = "GitHub",
                                        url = "https://github.com",
                                        category = "Dev",
                                        folder = "Dev",
                                        labels = "code, opensource, dev",
                                        notes = "Open-source development and code repositories",
                                        isPinnedToNewTab = true,
                                        visitCount = 9
                                    ),
                                    BookmarkEntity(
                                        title = "YouTube",
                                        url = "https://m.youtube.com",
                                        category = "Media",
                                        folder = "Media",
                                        labels = "video, entertainment",
                                        notes = "Online video platform with Swordfish ad blocker",
                                        isPinnedToNewTab = true,
                                        visitCount = 15
                                    )
                                )
                                defaultBookmarks.forEach { dao.insertBookmark(it) }
                            }
                        }
                    }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
