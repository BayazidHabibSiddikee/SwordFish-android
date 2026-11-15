package com.example

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.example.data.BrowserRepository
import com.example.data.blocker.AdBlockEngine
import com.example.data.blocker.AppBlockerEngine
import com.example.data.blocker.SwordfishAdBlockBridge
import com.example.data.blocker.SwordfishAdBlockWebViewClient
import com.example.data.local.BlockedSiteEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.BookmarkFolderEntity
import com.example.data.local.BrowserDatabase
import com.example.data.local.CachedPageEntity
import com.example.data.local.HistoryEntity
import com.example.data.model.HistoryTimeRange
import com.example.data.local.FilterSettingsRepository
import com.example.data.local.PasswordAutofillEngine
import com.example.data.local.SecurePasswordRepository
import com.example.data.local.UsageTimeEntity
import com.example.data.model.AdBlockFilters
import com.example.data.model.DistractionFilters
import com.example.data.model.FilterPreset
import com.example.data.model.FilterSettings
import com.example.data.model.PasswordStrengthLevel
import com.example.data.model.ReaderArticle
import com.example.data.model.ReaderFontFamily
import com.example.data.model.ReaderLineHeight
import com.example.data.model.ReaderSettings
import com.example.data.model.ReaderTheme
import com.example.data.model.BlocklistType
import com.example.data.model.TrackingFilters
import com.example.data.model.VaultCredential
import com.example.data.reader.ReaderModeEngine
import com.example.data.service.BlocklistService
import com.example.data.service.FocusUsageMonitorService
import com.example.viewmodel.BrowserViewModel
import com.example.viewmodel.WebCommand
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Swordfish", appName)
    }

    @Test
    fun `ad blocker intercepts known ad domains`() {
        val adUri = Uri.parse("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js")
        assertTrue(AdBlockEngine.isAdOrTracker(adUri))

        val doubleClickUri = Uri.parse("https://ad.doubleclick.net/ddm/trackclk/12345")
        assertTrue(AdBlockEngine.isAdOrTracker(doubleClickUri))

        val benignUri = Uri.parse("https://en.wikipedia.org/wiki/Kotlin")
        assertFalse(AdBlockEngine.isAdOrTracker(benignUri))
    }

    @Test
    fun `ad blocker intercepts youtube ad endpoints`() {
        val ytAdUri = Uri.parse("https://m.youtube.com/api/stats/ads?adformat=1_2")
        assertTrue(AdBlockEngine.isAdOrTracker(ytAdUri))

        val ytPageadUri = Uri.parse("https://www.youtube.com/pagead/parallelverify")
        assertTrue(AdBlockEngine.isAdOrTracker(ytPageadUri))

        assertTrue(AdBlockEngine.isYouTubeUrl("https://m.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertTrue(AdBlockEngine.isYouTubeUrl("https://youtu.be/dQw4w9WgXcQ"))
        assertFalse(AdBlockEngine.isYouTubeUrl("https://duckduckgo.com"))
    }

    @Test
    fun `javascript bridge layer dispatches callbacks to host`() {
        var reportedSelector = ""
        var reportedCount = 0
        var reportedVideoDuration = 0.0
        var reportedPopupUrl = ""

        val bridge = SwordfishAdBlockBridge(
            onAdRemoved = { sel, count ->
                reportedSelector = sel
                reportedCount = count
            },
            onVideoAdSkipped = { duration ->
                reportedVideoDuration = duration
            },
            onPopupBlocked = { url ->
                reportedPopupUrl = url
            },
            onAntiAdblockNeutralized = {}
        )

        val selectorsJson = bridge.getCustomBlockSelectors()
        assertTrue(selectorsJson.contains(".adsbygoogle"))
        assertTrue(selectorsJson.contains("iframe[src*='doubleclick']"))

        bridge.reportAdBlocked(".adsbygoogle", 3)
        bridge.reportVideoAdSkipped(30.0)
        bridge.reportPopupBlocked("https://adpop.example.com")

        ShadowLooper.idleMainLooper()

        assertEquals(".adsbygoogle", reportedSelector)
        assertEquals(3, reportedCount)
        assertEquals(30.0, reportedVideoDuration, 0.001)
        assertEquals("https://adpop.example.com", reportedPopupUrl)
    }

    @Test
    fun `app blocker intercepts distracting domains in focus mode`() {
        val blockedList = listOf("tiktok.com", "instagram.com", "reddit.com")

        val (isBlocked1, domain1) = AppBlockerEngine.checkBlocked(
            "https://m.tiktok.com/explore",
            blockedList,
            isFocusModeActive = true
        )
        assertTrue(isBlocked1)
        assertEquals("tiktok.com", domain1)

        val (isBlocked2, _) = AppBlockerEngine.checkBlocked(
            "https://developer.android.com",
            blockedList,
            isFocusModeActive = true
        )
        assertFalse(isBlocked2)
    }

    @Test
    fun `blocked site entity supports app packages and daily limits`() {
        val appEntity = BlockedSiteEntity(
            domain = "com.instagram.android",
            displayName = "Instagram (App)",
            category = "Social Media",
            dailyLimitMinutes = 30,
            isAppPackage = true
        )
        assertTrue(appEntity.isAppPackage)
        assertEquals(30, appEntity.dailyLimitMinutes)
        assertEquals("com.instagram.android", appEntity.domain)
    }

    @Test
    fun `usage time entity tracks active usage seconds`() {
        val usage = UsageTimeEntity(
            targetIdentifier = "com.zhiliaoapp.musically",
            dateString = "2026-10-30",
            activeSecondsToday = 1200L, // 20 minutes
            timesBlockedToday = 4
        )
        assertEquals("2026-10-30", usage.dateString)
        assertEquals(1200L, usage.activeSecondsToday)
        assertEquals(4, usage.timesBlockedToday)
    }

    @Test
    fun `focus usage monitor service companion updates state`() {
        FocusUsageMonitorService.setActiveTarget("tiktok.com")
        assertEquals("tiktok.com", FocusUsageMonitorService.currentActiveTarget.value)

        FocusUsageMonitorService.setActiveTarget(null)
        assertEquals(null, FocusUsageMonitorService.currentActiveTarget.value)
    }

    @Test
    fun `time formatting works correctly`() {
        assertEquals("25:00", AppBlockerEngine.formatSecondsToTime(1500))
        assertEquals("00:45", AppBlockerEngine.formatSecondsToTime(45))
        assertEquals("01:05", AppBlockerEngine.formatSecondsToTime(65))
    }

    @Test
    fun `bookmark database saves and queries bookmarks with category and search`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BrowserDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.browserDao()

        val id1 = dao.insertBookmark(
            BookmarkEntity(
                title = "Ubuntu Homepage",
                url = "https://ubuntu.com",
                category = "Ubuntu",
                notes = "Official OS site"
            )
        )
        val id2 = dao.insertBookmark(
            BookmarkEntity(
                title = "GitHub",
                url = "https://github.com",
                category = "Dev",
                notes = "Git repositories"
            )
        )

        val all = dao.getAllBookmarks().first()
        assertEquals(2, all.size)

        val ubuntuCategory = dao.getBookmarksByCategory("Ubuntu").first()
        assertEquals(1, ubuntuCategory.size)
        assertEquals("Ubuntu Homepage", ubuntuCategory[0].title)

        val searchResults = dao.searchBookmarks("repositories").first()
        assertEquals(1, searchResults.size)
        assertEquals("GitHub", searchResults[0].title)

        dao.deleteBookmarkById(id1)
        val afterDelete = dao.getAllBookmarks().first()
        assertEquals(1, afterDelete.size)
        assertEquals("GitHub", afterDelete[0].title)

        db.close()
    }

    @Test
    fun `browser repository manages bookmark lifecycle and seeder`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BrowserDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = BrowserRepository(db.browserDao())

        repository.seedUbuntuStarterBookmarks()
        val seeded = repository.allBookmarks.first()
        assertTrue(seeded.isNotEmpty())
        assertTrue(seeded.any { it.title.contains("Ubuntu") })

        val isBookmarked = repository.isBookmarked("https://ubuntu.com").first()
        assertTrue(isBookmarked)

        repository.removeBookmarkByUrl("https://ubuntu.com")
        val isStillBookmarked = repository.isBookmarked("https://ubuntu.com").first()
        assertFalse(isStillBookmarked)

        repository.clearAllBookmarks()
        val cleared = repository.allBookmarks.first()
        assertTrue(cleared.isEmpty())

        db.close()
    }

    @Test
    fun `quick access bookmarks are stored and queried from room database with pin status`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BrowserDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.browserDao()

        val id1 = dao.insertBookmark(
            BookmarkEntity(
                title = "DuckDuckGo",
                url = "https://duckduckgo.com",
                category = "Favorites",
                isPinnedToNewTab = true,
                visitCount = 10
            )
        )
        val id2 = dao.insertBookmark(
            BookmarkEntity(
                title = "Private Notes",
                url = "https://notes.example.com",
                category = "Work",
                isPinnedToNewTab = false,
                visitCount = 2
            )
        )

        val quickAccess = dao.getQuickAccessBookmarks().first()
        assertEquals(1, quickAccess.size)
        assertEquals("DuckDuckGo", quickAccess[0].title)
        assertTrue(quickAccess[0].isPinnedToNewTab)

        // Toggle pin
        dao.setBookmarkPinned(id2, true)
        val updatedQuick = dao.getQuickAccessBookmarks().first()
        assertEquals(2, updatedQuick.size)

        // Increment visit count
        dao.incrementBookmarkVisitCount(id1)
        val updatedDDG = dao.getBookmarkByUrl("https://duckduckgo.com")
        assertEquals(11, updatedDDG?.visitCount)

        db.close()
    }

    @Test
    fun `most visited sites query aggregates history entries by visit frequency`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BrowserDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = BrowserRepository(db.browserDao())

        // Record visits
        repository.addHistory("YouTube", "https://m.youtube.com")
        repository.addHistory("YouTube", "https://m.youtube.com")
        repository.addHistory("YouTube", "https://m.youtube.com")
        repository.addHistory("Ubuntu", "https://ubuntu.com")
        repository.addHistory("Ubuntu", "https://ubuntu.com")
        repository.addHistory("GitHub", "https://github.com")

        val mostVisited = repository.mostVisitedSites.first()
        assertEquals(3, mostVisited.size)
        assertEquals("https://m.youtube.com", mostVisited[0].url)
        assertEquals(3, mostVisited[0].visitCount)
        assertEquals("https://ubuntu.com", mostVisited[1].url)
        assertEquals(2, mostVisited[1].visitCount)
        assertEquals("https://github.com", mostVisited[2].url)
        assertEquals(1, mostVisited[2].visitCount)

        db.close()
    }

    @Test
    fun `secure password repository saves, encrypts, and retrieves credentials`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SecurePasswordRepository(context)
        repo.clearAllCredentials()

        val cred1 = VaultCredential(
            domain = "github.com",
            title = "GitHub Developer",
            username = "linus",
            password = "UbuntuLinuxPassword123!",
            notes = "Work account"
        )
        repo.saveCredential(cred1)

        val cred2 = VaultCredential(
            domain = "gitlab.com",
            title = "GitLab",
            username = "torvalds@ubuntu.com",
            password = "SecretGitLabPass456#",
            notes = "Kernel repo"
        )
        repo.saveCredential(cred2)

        val all = repo.credentials.value
        assertEquals(2, all.size)

        val githubCreds = repo.getCredentialsForDomain("https://github.com/login")
        assertEquals(1, githubCreds.size)
        assertEquals("linus", githubCreds[0].username)
        assertEquals("UbuntuLinuxPassword123!", githubCreds[0].password)

        // Test search
        val searchResults = repo.searchCredentials("torvalds")
        assertEquals(1, searchResults.size)
        assertEquals("gitlab.com", searchResults[0].domain)

        // Test update
        val updated = githubCreds[0].copy(password = "NewSuperSecretPass789!")
        repo.saveCredential(updated)
        val afterUpdate = repo.getCredentialsForDomain("github.com").first()
        assertEquals("NewSuperSecretPass789!", afterUpdate.password)

        // Test delete
        repo.deleteCredential(cred2.id)
        val afterDelete = repo.credentials.value
        assertEquals(1, afterDelete.size)
        assertEquals("github.com", afterDelete[0].domain)

        repo.clearAllCredentials()
    }

    @Test
    fun `secure password repository matches domains and subdomains accurately`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SecurePasswordRepository(context)

        assertTrue(repo.matchesDomain("github.com", "github.com"))
        assertTrue(repo.matchesDomain("https://github.com/login", "github.com"))
        assertTrue(repo.matchesDomain("auth.github.com", "github.com"))
        assertTrue(repo.matchesDomain("github.com", "auth.github.com"))
        assertTrue(repo.matchesDomain("https://accounts.google.com/signin", "google.com"))
        assertTrue(repo.matchesDomain("login.live.com", "live.com"))

        assertFalse(repo.matchesDomain("google.com", "facebook.com"))
        assertFalse(repo.matchesDomain("notgithub.com", "github.com"))
    }

    @Test
    fun `secure password generator generates passwords satisfying length, uppercase, numbers, and symbols constraints`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SecurePasswordRepository(context)

        val pass1 = repo.generateSecurePassword(length = 20, includeUppercase = true, includeNumbers = true, includeSymbols = true)
        assertEquals(20, pass1.length)
        assertTrue(pass1.any { it.isUpperCase() })
        assertTrue(pass1.any { it.isLowerCase() })
        assertTrue(pass1.any { it.isDigit() })
        assertTrue(pass1.any { !it.isLetterOrDigit() })

        val pass2 = repo.generateSecurePassword(length = 12, includeUppercase = false, includeNumbers = true, includeSymbols = false)
        assertEquals(12, pass2.length)
        assertFalse(pass2.any { it.isUpperCase() })
        assertTrue(pass2.any { it.isDigit() })

        // Check randomness
        val pass3 = repo.generateSecurePassword(length = 16)
        val pass4 = repo.generateSecurePassword(length = 16)
        assertTrue(pass3 != pass4)
    }

    @Test
    fun `password strength evaluation accurately calculates entropy and feedback`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SecurePasswordRepository(context)

        val weak = repo.calculatePasswordStrength("123456")
        assertTrue(weak.level == PasswordStrengthLevel.VERY_WEAK || weak.level == PasswordStrengthLevel.WEAK)

        val strong = repo.calculatePasswordStrength("K9#mQ!z8@XvL2%pW")
        assertTrue(strong.level == PasswordStrengthLevel.STRONG || strong.level == PasswordStrengthLevel.VERY_STRONG)
        assertTrue(strong.percent > 0.7f)
    }

    @Test
    fun `password autofill engine builds safe and valid JavaScript injection script`() {
        val script = PasswordAutofillEngine.buildAutofillScript(
            usernameSelector = "#username",
            passwordSelector = "#password",
            usernameValue = "admin@ubuntu.com",
            passwordValue = "P@ssw0rd!\"'test"
        )
        assertTrue(script.contains("#username"))
        assertTrue(script.contains("#password"))
        assertTrue(script.contains("admin@ubuntu.com"))
        assertTrue(script.contains("dispatchEvent"))
        assertTrue(script.contains("input"))
        assertTrue(script.contains("change"))
    }

    @Test
    fun `swordfish adblock bridge reports login form detection and password submission`() {
        var detectedDomain = ""
        var detectedUserSel = ""
        var detectedPassSel = ""
        var submittedDomain = ""
        var submittedUser = ""
        var submittedPass = ""

        val bridge = SwordfishAdBlockBridge(
            onLoginFormDetected = { domain, uSel, pSel ->
                detectedDomain = domain
                detectedUserSel = uSel
                detectedPassSel = pSel
            },
            onPasswordFormSubmitted = { domain, user, pass ->
                submittedDomain = domain
                submittedUser = user
                submittedPass = pass
            }
        )

        bridge.reportLoginFormDetected("github.com", "#login_field", "#password")
        bridge.reportPasswordFormSubmitted("github.com", "linus", "secret123")

        ShadowLooper.idleMainLooper()

        assertEquals("github.com", detectedDomain)
        assertEquals("#login_field", detectedUserSel)
        assertEquals("#password", detectedPassSel)
        assertEquals("github.com", submittedDomain)
        assertEquals("linus", submittedUser)
        assertEquals("secret123", submittedPass)
    }

    @Test
    fun `filter settings repository saves and retrieves custom filter configurations`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = FilterSettingsRepository(context)
        repo.resetToDefaults()

        val initial = repo.getSettings()
        assertTrue(initial.isMasterShieldEnabled)
        assertEquals(FilterPreset.BALANCED, initial.preset)
        assertTrue(initial.adBlock.networkAdInterception)

        val customized = initial.copy(
            preset = FilterPreset.CUSTOM,
            adBlock = initial.adBlock.copy(networkAdInterception = false, socialFloatingWidgets = true)
        )
        repo.saveSettings(customized)

        val updated = repo.getSettings()
        assertEquals(FilterPreset.CUSTOM, updated.preset)
        assertFalse(updated.adBlock.networkAdInterception)
        assertTrue(updated.adBlock.socialFloatingWidgets)

        // New repo instance should read persisted preferences
        val repo2 = FilterSettingsRepository(context)
        assertEquals(FilterPreset.CUSTOM, repo2.getSettings().preset)
        assertFalse(repo2.getSettings().adBlock.networkAdInterception)

        // Reset should restore balanced preset
        repo2.resetToDefaults()
        assertEquals(FilterPreset.BALANCED, repo2.getSettings().preset)
        assertTrue(repo2.getSettings().adBlock.networkAdInterception)
    }

    @Test
    fun `filter presets properly configure strict balanced and permissive thresholds`() {
        val strict = FilterPreset.STRICT.createFilters()
        assertEquals(FilterPreset.STRICT, strict.preset)
        assertEquals(17, strict.totalActiveCount())
        assertEquals(6, strict.adBlock.activeCount())
        assertEquals(6, strict.tracking.activeCount())
        assertEquals(5, strict.distraction.activeCount())

        val balanced = FilterPreset.BALANCED.createFilters()
        assertEquals(FilterPreset.BALANCED, balanced.preset)
        assertTrue(balanced.totalActiveCount() >= 13)
        assertTrue(balanced.adBlock.networkAdInterception)
        assertTrue(balanced.tracking.analyticsTelemetryBlocker)
        assertTrue(balanced.adBlock.socialFloatingWidgets)

        val permissive = FilterPreset.PERMISSIVE.createFilters()
        assertEquals(FilterPreset.PERMISSIVE, permissive.preset)
        assertTrue(permissive.totalActiveCount() < balanced.totalActiveCount())
        assertFalse(permissive.distraction.shortFormVideoBlocker)
        assertFalse(permissive.adBlock.antiAdblockNeutralizer)
    }

    @Test
    fun `tracking parameter stripping removes marketing identifiers and retains legitimate query params`() {
        val rawUrl = "https://example.com/item?id=4821&utm_source=twitter&utm_medium=cpc&gclid=999&fbclid=xyz&category=phones"
        val cleaned = AdBlockEngine.stripTrackingParameters(rawUrl)

        assertFalse(cleaned.contains("utm_source"))
        assertFalse(cleaned.contains("utm_medium"))
        assertFalse(cleaned.contains("gclid"))
        assertFalse(cleaned.contains("fbclid"))
        assertTrue(cleaned.contains("id=4821"))
        assertTrue(cleaned.contains("category=phones"))

        // URLs without tracking parameters should remain unchanged
        val cleanUrl = "https://en.wikipedia.org/wiki/Ubuntu?search=desktop"
        assertEquals(cleanUrl, AdBlockEngine.stripTrackingParameters(cleanUrl))

        // URLs with only tracking params should have query stripped cleanly
        val onlyTracking = "https://example.com/landing?utm_campaign=winter_sale&msclkid=123"
        assertEquals("https://example.com/landing", AdBlockEngine.stripTrackingParameters(onlyTracking))
    }

    @Test
    fun `ad block engine respects granular filter settings for ad vs telemetry requests`() {
        val adUri = Uri.parse("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js")
        val trackerUri = Uri.parse("https://www.google-analytics.com/analytics.js")

        // 1. Both enabled
        val allEnabled = FilterPreset.STRICT.createFilters()
        assertTrue(AdBlockEngine.isAdOrTracker(adUri, allEnabled))
        assertTrue(AdBlockEngine.isAdOrTracker(trackerUri, allEnabled))

        // 2. Network ad interception disabled -> ads should pass through, tracker blocked
        val adsDisabled = allEnabled.copy(
            adBlock = allEnabled.adBlock.copy(networkAdInterception = false)
        )
        assertFalse(AdBlockEngine.isAdOrTracker(adUri, adsDisabled))
        assertTrue(AdBlockEngine.isAdOrTracker(trackerUri, adsDisabled))

        // 3. Analytics blocker disabled -> trackers should pass through, ad blocked
        val trackersDisabled = allEnabled.copy(
            tracking = allEnabled.tracking.copy(analyticsTelemetryBlocking = false)
        )
        assertTrue(AdBlockEngine.isAdOrTracker(adUri, trackersDisabled))
        assertFalse(AdBlockEngine.isAdOrTracker(trackerUri, trackersDisabled))

        // 4. Master shield disabled -> all requests pass through
        val masterOff = allEnabled.copy(isMasterShieldEnabled = false)
        assertFalse(AdBlockEngine.isAdOrTracker(adUri, masterOff))
        assertFalse(AdBlockEngine.isAdOrTracker(trackerUri, masterOff))
    }

    @Test
    fun `app blocker engine accurately flags short-form video loops for reels shorts and tiktok`() {
        assertTrue(AppBlockerEngine.isShortFormVideo("https://www.tiktok.com/@creator/video/123456789"))
        assertTrue(AppBlockerEngine.isShortFormVideo("https://tiktok.com/t/ZT8abcdef/"))
        assertTrue(AppBlockerEngine.isShortFormVideo("https://www.youtube.com/shorts/dQw4w9WgXcQ"))
        assertTrue(AppBlockerEngine.isShortFormVideo("https://m.youtube.com/shorts/abc123xyz"))
        assertTrue(AppBlockerEngine.isShortFormVideo("https://www.instagram.com/reels/C4abcdef/"))
        assertTrue(AppBlockerEngine.isShortFormVideo("https://instagram.com/reel/C4xyz123/"))

        // Non-short form video pages should not be flagged
        assertFalse(AppBlockerEngine.isShortFormVideo("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertFalse(AppBlockerEngine.isShortFormVideo("https://m.youtube.com/feed/subscriptions"))
        assertFalse(AppBlockerEngine.isShortFormVideo("https://www.instagram.com/ubuntu/"))
        assertFalse(AppBlockerEngine.isShortFormVideo("https://en.wikipedia.org/wiki/Short_film"))
    }

    @Test
    fun `browser viewmodel applies presets and updates individual category filters`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = BrowserViewModel(app)

        // Default should be balanced
        val initial = vm.filterSettings.value
        assertEquals(FilterPreset.BALANCED, initial.preset)
        assertTrue(initial.adBlock.networkAdInterception)

        // Apply Strict preset
        vm.applyFilterPreset(FilterPreset.STRICT)
        assertEquals(FilterPreset.STRICT, vm.filterSettings.value.preset)
        assertEquals(17, vm.filterSettings.value.totalActiveCount())

        // Toggle a single filter updates preset to CUSTOM
        vm.updateDistractionFilters { it.copy(shortFormVideoBlocker = false) }
        assertEquals(FilterPreset.CUSTOM, vm.filterSettings.value.preset)
        assertFalse(vm.filterSettings.value.distraction.shortFormVideoBlocker)

        // Update tracking filter
        vm.updateTrackingFilters { it.copy(urlTrackingParamStripping = false) }
        assertFalse(vm.filterSettings.value.tracking.stripTrackingParameters)

        // Reset to default
        vm.resetFilterSettings()
        assertEquals(FilterPreset.BALANCED, vm.filterSettings.value.preset)
        assertTrue(vm.filterSettings.value.tracking.stripTrackingParameters)
    }

    @Test
    fun `reader article parses accurately from json structure with title byline and word count`() {
        val json = """
            {
                "title": "Kernel Architecture in Modern Operating Systems",
                "byline": "Linus Torvalds",
                "siteName": "kernel.org",
                "publishedDate": "2026-10-15",
                "contentHtml": "<p>First paragraph detailing monolithic kernels.</p><p>Second paragraph on scheduler performance.</p>",
                "plainText": "First paragraph detailing monolithic kernels.\n\nSecond paragraph on scheduler performance.",
                "wordCount": 11,
                "readingTimeMinutes": 1,
                "url": "https://kernel.org/doc/kernel.html",
                "topImageUrl": "https://kernel.org/img/tux.png"
            }
        """.trimIndent()

        val article = ReaderArticle.fromJsonString(json)
        org.junit.Assert.assertNotNull(article)
        assertEquals("Kernel Architecture in Modern Operating Systems", article?.title)
        assertEquals("Linus Torvalds", article?.byline)
        assertEquals("kernel.org", article?.siteName)
        assertEquals("2026-10-15", article?.publishedDate)
        assertEquals(11, article?.wordCount)
        assertEquals(1, article?.readingTimeMinutes)
        assertEquals("https://kernel.org/img/tux.png", article?.topImageUrl)

        // Helper word counting and reading time calculations
        assertEquals(5, ReaderArticle.countWords("One two three four five"))
        assertEquals(0, ReaderArticle.countWords("   "))
        assertEquals(3, ReaderArticle.calculateReadingTime(550))
    }

    @Test
    fun `reader mode engine accurately identifies reader eligible urls`() {
        assertTrue(ReaderModeEngine.isReaderEligibleUrl("https://en.wikipedia.org/wiki/Ubuntu"))
        assertTrue(ReaderModeEngine.isReaderEligibleUrl("https://canonical.com/blog/2026-announcement"))
        assertTrue(ReaderModeEngine.isReaderEligibleUrl("http://example.org/article?id=42"))

        // Non-eligible URLs should be rejected
        assertFalse(ReaderModeEngine.isReaderEligibleUrl("about:blank"))
        assertFalse(ReaderModeEngine.isReaderEligibleUrl("chrome://flags"))
        assertFalse(ReaderModeEngine.isReaderEligibleUrl("https://m.youtube.com/watch?v=12345"))
        assertFalse(ReaderModeEngine.isReaderEligibleUrl("https://www.tiktok.com/@user/video/123"))
        assertFalse(ReaderModeEngine.isReaderEligibleUrl("https://auth.github.com/login"))
        assertFalse(ReaderModeEngine.isReaderEligibleUrl(""))
        assertFalse(ReaderModeEngine.isReaderEligibleUrl(null))
    }

    @Test
    fun `reader mode engine extracts clean readable paragraphs from article model`() {
        val article = ReaderArticle(
            title = "Ubuntu Yaru Design",
            plainText = "Ubuntu Yaru is the official visual theme.\n\nIt features aubergine backgrounds.\n\nAccent elements use vibrant orange."
        )

        val paragraphs = ReaderModeEngine.extractParagraphs(article)
        assertEquals(3, paragraphs.size)
        assertEquals("Ubuntu Yaru is the official visual theme.", paragraphs[0])
        assertEquals("It features aubergine backgrounds.", paragraphs[1])
        assertEquals("Accent elements use vibrant orange.", paragraphs[2])
    }

    @Test
    fun `reader settings defaults and appearance update functions retain configuration`() {
        val settings = ReaderSettings()
        assertEquals(ReaderTheme.UBUNTU_DARK, settings.theme)
        assertEquals(ReaderFontFamily.SANS_SERIF, settings.fontFamily)
        assertEquals(18, settings.fontSizeSp)
        assertEquals(ReaderLineHeight.NORMAL, settings.lineHeight)

        // JSON Serialization roundtrip
        val json = settings.toJson()
        val restored = ReaderSettings.fromJson(json)
        assertEquals(settings.theme, restored.theme)
        assertEquals(settings.fontSizeSp, restored.fontSizeSp)

        // Theme palette verification
        assertEquals(0xFF1E1E1E, ReaderTheme.UBUNTU_DARK.backgroundColor)
        assertEquals(0xFF2C001E, ReaderTheme.UBUNTU_AUBERGINE.backgroundColor)
        assertEquals(0xFFFDFBF7, ReaderTheme.LIGHT_PAPER.backgroundColor)
        assertEquals(0xFFF4ECD8, ReaderTheme.SEPIA_WARM.backgroundColor)
        assertEquals(0xFF000000, ReaderTheme.AMOLED_BLACK.backgroundColor)
    }

    @Test
    fun `browser viewmodel triggers reader mode extraction and updates active tab state`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = BrowserViewModel(app)

        vm.openNewTab("https://canonical.com/blog/ubuntu-release")
        assertFalse(vm.activeTab?.isReaderMode == true)

        // Reader availability signal from JS
        vm.onReaderAvailabilityChanged(true)
        assertTrue(vm.activeTab?.isReaderAvailable == true)

        // Simulate reader content extracted from WebView JS
        val json = """
            {
                "title": "Ubuntu 26.04 LTS Released",
                "byline": "Canonical Team",
                "siteName": "canonical.com",
                "plainText": "Canonical announces the general availability of Ubuntu 26.04 LTS.",
                "url": "https://canonical.com/blog/ubuntu-release",
                "wordCount": 9,
                "readingTimeMinutes": 1
            }
        """.trimIndent()

        vm.onReaderContentExtracted(json)
        assertTrue(vm.activeTab?.isReaderMode == true)
        assertEquals("Ubuntu 26.04 LTS Released", vm.activeTab?.readerArticle?.title)

        // Appearance settings updates
        vm.updateReaderTheme(ReaderTheme.UBUNTU_AUBERGINE)
        assertEquals(ReaderTheme.UBUNTU_AUBERGINE, vm.readerSettings.value.theme)

        vm.updateReaderFontSize(2)
        assertEquals(20, vm.readerSettings.value.fontSizeSp)

        // Exit Reader Mode returns to normal web view
        vm.exitReaderMode()
        assertFalse(vm.activeTab?.isReaderMode == true)
    }

    @Test
    fun `swordfish adblock bridge reports reader content extraction and availability changes`() {
        var extractedJson = ""
        var isAvailable = false
        var unavailableReason = ""

        val bridge = SwordfishAdBlockBridge(
            onReaderContentExtracted = { json -> extractedJson = json },
            onReaderAvailabilityChanged = { avail -> isAvailable = avail },
            onReaderModeUnavailable = { reason -> unavailableReason = reason }
        )

        bridge.reportReaderAvailable(true)
        bridge.reportReaderContent("""{"title":"Sample Read"}""")
        bridge.reportReaderModeUnavailable("Too short")

        ShadowLooper.idleMainLooper()

        assertTrue(isAvailable)
        assertEquals("""{"title":"Sample Read"}""", extractedJson)
        assertEquals("Too short", unavailableReason)
    }

    @Test
    fun `cached page entity stores and retrieves html content via Room DAO`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BrowserDatabase::class.java).build()
        val dao = db.browserDao()

        val sampleHtml = "<html><body><h1>Ubuntu Desktop</h1><p>A fast, open-source Linux operating system.</p></body></html>"
        val cachedPage = CachedPageEntity(
            url = "https://ubuntu.com/desktop",
            title = "Ubuntu Desktop",
            domain = "ubuntu.com",
            htmlContent = sampleHtml,
            plainTextSnippet = "Ubuntu Desktop A fast, open-source Linux operating system.",
            contentSizeBytes = sampleHtml.toByteArray().size.toLong(),
            visitCount = 1,
            isPinnedOffline = true
        )

        dao.insertOrUpdateCachedPage(cachedPage)

        val retrieved = dao.getCachedPageByUrl("https://ubuntu.com/desktop")
        assertEquals("Ubuntu Desktop", retrieved?.title)
        assertEquals("ubuntu.com", retrieved?.domain)
        assertEquals(sampleHtml, retrieved?.htmlContent)
        assertTrue(retrieved?.isPinnedOffline == true)
        assertEquals(1, retrieved?.visitCount)

        db.close()
    }

    @Test
    fun `cached page visit count increments and prioritizes frequently visited`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BrowserDatabase::class.java).build()
        val dao = db.browserDao()

        dao.insertOrUpdateCachedPage(
            CachedPageEntity(
                url = "https://askubuntu.com",
                title = "Ask Ubuntu",
                domain = "askubuntu.com",
                htmlContent = "<html><body>Questions</body></html>",
                visitCount = 2
            )
        )

        dao.insertOrUpdateCachedPage(
            CachedPageEntity(
                url = "https://omgubuntu.co.uk",
                title = "OMG! Ubuntu",
                domain = "omgubuntu.co.uk",
                htmlContent = "<html><body>News</body></html>",
                visitCount = 8
            )
        )

        // Increment visit count for askubuntu twice
        dao.incrementCachedPageVisit("https://askubuntu.com")
        dao.incrementCachedPageVisit("https://askubuntu.com")

        val frequentlyVisited = dao.getFrequentlyVisitedCachedPages(limit = 5).first()
        assertEquals(2, frequentlyVisited.size)
        assertEquals("https://omgubuntu.co.uk", frequentlyVisited[0].url) // 8 visits
        assertEquals("https://askubuntu.com", frequentlyVisited[1].url) // 4 visits

        db.close()
    }

    @Test
    fun `cache pruning deletes old unpinned pages while keeping pinned pages`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BrowserDatabase::class.java).build()
        val dao = db.browserDao()

        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
        val cutoff = now - (14L * 24 * 60 * 60 * 1000)

        // Old but PINNED offline (must survive pruning)
        dao.insertOrUpdateCachedPage(
            CachedPageEntity(
                url = "https://docs.ubuntu.com",
                title = "Ubuntu Docs (Pinned)",
                domain = "docs.ubuntu.com",
                htmlContent = "<html><body>Docs</body></html>",
                cachedAtTimestamp = thirtyDaysAgo,
                isPinnedOffline = true
            )
        )

        // Old and UNPINNED (must be deleted)
        dao.insertOrUpdateCachedPage(
            CachedPageEntity(
                url = "https://temporary.example.com",
                title = "Temp Page",
                domain = "example.com",
                htmlContent = "<html><body>Temp</body></html>",
                cachedAtTimestamp = thirtyDaysAgo,
                isPinnedOffline = false
            )
        )

        // Recent unpinned page (must survive pruning)
        dao.insertOrUpdateCachedPage(
            CachedPageEntity(
                url = "https://fresh.example.com",
                title = "Fresh Page",
                domain = "fresh.example.com",
                htmlContent = "<html><body>Fresh</body></html>",
                cachedAtTimestamp = now,
                isPinnedOffline = false
            )
        )

        val prunedCount = dao.pruneOldPages(cutoff)
        assertEquals(1, prunedCount)

        val allRemaining = dao.getAllCachedPages().first()
        assertEquals(2, allRemaining.size)
        assertTrue(allRemaining.any { it.url == "https://docs.ubuntu.com" })
        assertTrue(allRemaining.any { it.url == "https://fresh.example.com" })
        assertFalse(allRemaining.any { it.url == "https://temporary.example.com" })

        db.close()
    }

    @Test
    fun `browser repository cleans script tags and derives readable plain text snippet`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BrowserDatabase::class.java).build()
        val repo = BrowserRepository(db.browserDao())

        val messyHtml = """
            <!DOCTYPE html>
            <html>
                <head>
                    <title>Article Title</title>
                    <style>body { color: red; }</style>
                    <script>console.log("tracker");</script>
                </head>
                <body>
                    <h1>Linux Kernel 6.8</h1>
                    <p>New hardware support and performance improvements.</p>
                </body>
            </html>
        """.trimIndent()

        repo.cacheWebpage(
            url = "https://kernel.org/release/6.8",
            title = "Linux Kernel 6.8",
            htmlContent = messyHtml
        )

        val cached = repo.getCachedPage("https://kernel.org/release/6.8")
        assertEquals("Linux Kernel 6.8", cached?.title)
        assertEquals("kernel.org", cached?.domain)
        assertTrue(cached!!.plainTextSnippet.contains("Linux Kernel 6.8"))
        assertTrue(cached.plainTextSnippet.contains("New hardware support"))
        assertFalse(cached.plainTextSnippet.contains("console.log"))
        assertFalse(cached.plainTextSnippet.contains("color: red"))
        assertTrue(cached.contentSizeBytes > 0)

        db.close()
    }

    @Test
    fun `browser viewmodel loads cached page offline with badge indicator`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = BrowserViewModel(app)

        val cachedPage = CachedPageEntity(
            url = "https://distrowatch.com",
            title = "DistroWatch",
            domain = "distrowatch.com",
            htmlContent = "<html><body><h1>Top Distributions</h1><p>1. Ubuntu</p></body></html>",
            cachedAtTimestamp = System.currentTimeMillis() - 5000,
            contentSizeBytes = 512L,
            visitCount = 3
        )

        vm.loadCachedPage(cachedPage)

        val currentTab = vm.activeTab
        assertEquals("https://distrowatch.com", currentTab?.url)
        assertEquals("DistroWatch", currentTab?.title)
        assertTrue(currentTab?.isLoadedFromCache == true)
        assertEquals(cachedPage.cachedAtTimestamp, currentTab?.cachedTimestamp)

        // Retry online resets cache indicator
        vm.retryOnlineCurrentPage()
        assertFalse(vm.activeTab?.isLoadedFromCache == true)
    }

    @Test
    fun `browser viewmodel never caches incognito tabs for privacy`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = BrowserViewModel(app)

        vm.openNewIncognitoTab("https://private.duckduckgo.com")
        assertTrue(vm.isCurrentTabIncognito())

        // Attempt to extract and cache HTML while incognito
        vm.onPageHtmlExtracted("https://private.duckduckgo.com", "<html><body>Private query results</body></html>")

        // Search offline cache for this URL
        val cached = vm.filteredCachedPages.value.find { it.url == "https://private.duckduckgo.com" }
        assertEquals(null, cached)
    }

    @Test
    fun `bookmark entity custom labels extraction and matching`() {
        val bookmark = BookmarkEntity(
            title = "Linux Kernel Archives",
            url = "https://kernel.org",
            folder = "Dev",
            labels = "linux, kernel, #c-lang, open-source"
        )

        val labels = bookmark.getLabelList()
        assertEquals(4, labels.size)
        assertTrue(labels.contains("linux"))
        assertTrue(labels.contains("kernel"))
        assertTrue(labels.contains("c-lang"))
        assertTrue(labels.contains("open-source"))

        assertTrue(bookmark.hasLabel("linux"))
        assertTrue(bookmark.hasLabel("#linux"))
        assertTrue(bookmark.hasLabel("C-LANG"))
        assertFalse(bookmark.hasLabel("javascript"))
    }

    @Test
    fun `sqlite bookmarks manager custom folders creation renaming and deletion`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BrowserDatabase::class.java).build()
        val repo = BrowserRepository(db.browserDao())

        // Create custom folder
        repo.createFolder("Linux Tutorials", "#E95420")
        val folders = repo.allFolders.first()
        assertTrue(folders.any { it.name == "Linux Tutorials" })

        // Add bookmarks into folder
        val b1 = repo.addBookmark(
            title = "Ubuntu CLI Guide",
            url = "https://ubuntu.com/tutorials/command-line-for-beginners",
            folder = "Linux Tutorials",
            labels = "cli, ubuntu, beginner"
        )
        val b2 = repo.addBookmark(
            title = "Bash Scripting",
            url = "https://tldp.org/LDP/Bash-Beginners-Guide/html/",
            folder = "Linux Tutorials",
            labels = "bash, scripting"
        )

        val inTutorials = repo.getBookmarksByFolder("Linux Tutorials").first()
        assertEquals(2, inTutorials.size)

        // Rename folder
        repo.renameFolder("Linux Tutorials", "Guides & HowTos")
        val renamedFolders = repo.allFolders.first()
        assertTrue(renamedFolders.any { it.name == "Guides & HowTos" })
        assertFalse(renamedFolders.any { it.name == "Linux Tutorials" })

        val inRenamed = repo.getBookmarksByFolder("Guides & HowTos").first()
        assertEquals(2, inRenamed.size)
        assertEquals("Guides & HowTos", inRenamed[0].folder)

        // Move one bookmark to Favorites
        repo.moveBookmarkToFolder(b1, "Favorites")
        assertEquals(1, repo.getBookmarksByFolder("Guides & HowTos").first().size)
        assertEquals(1, repo.getBookmarksByFolder("Favorites").first().size)

        // Delete folder without deleting bookmarks (reassigns to Favorites)
        repo.deleteFolder("Guides & HowTos", deleteBookmarks = false)
        val remainingInFavorites = repo.getBookmarksByFolder("Favorites").first()
        assertEquals(2, remainingInFavorites.size)

        db.close()
    }

    @Test
    fun `sqlite bookmarks manager custom label filtering and search`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BrowserDatabase::class.java).build()
        val repo = BrowserRepository(db.browserDao())

        repo.addBookmark(
            title = "Ask Ubuntu Q&A",
            url = "https://askubuntu.com",
            folder = "Ubuntu",
            labels = "q&a, community, support",
            notes = "Community questions for desktop Linux"
        )
        repo.addBookmark(
            title = "OMG Ubuntu Desktop News",
            url = "https://omgubuntu.co.uk",
            folder = "News",
            labels = "desktop, customisation, news",
            notes = "Themes, icons, and distro releases"
        )
        repo.addBookmark(
            title = "GitHub Open Source",
            url = "https://github.com",
            folder = "Dev",
            labels = "git, repos, dev",
            notes = "Hosting git repositories"
        )

        // All distinct labels flow
        val allLabels = repo.allLabels.first()
        assertTrue(allLabels.contains("community"))
        assertTrue(allLabels.contains("desktop"))
        assertTrue(allLabels.contains("git"))

        // Search by label keyword
        val labelSearchResults = repo.searchBookmarks("customisation").first()
        assertEquals(1, labelSearchResults.size)
        assertEquals("OMG Ubuntu Desktop News", labelSearchResults[0].title)

        // Search across notes
        val notesResults = repo.searchBookmarks("distro releases").first()
        assertEquals(1, notesResults.size)
        assertEquals("OMG Ubuntu Desktop News", notesResults[0].title)

        // Search across folder
        val folderResults = repo.searchBookmarks("Ubuntu").first()
        assertTrue(folderResults.any { it.folder == "Ubuntu" })

        db.close()
    }

    @Test
    fun `browser viewmodel organizes bookmarks by folder and label`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = BrowserViewModel(app)

        vm.setSelectedBookmarkFolder("Dev")
        assertEquals("Dev", vm.selectedBookmarkFolder.value)

        vm.setSelectedBookmarkLabel("kernel")
        assertEquals("kernel", vm.selectedBookmarkLabel.value)

        vm.setSelectedBookmarkLabel(null)
        assertEquals(null, vm.selectedBookmarkLabel.value)

        vm.setSelectedBookmarkFolder("All")
        assertEquals("All", vm.selectedBookmarkFolder.value)
    }

    @Test
    fun `blocklist service manages common advertising and tracking urls and intercepts matching requests`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = BlocklistService(context)
        service.resetToDefaults()

        val rules = service.rules.value
        assertTrue("Expected rules to contain doubleclick.net", rules.any { it.pattern == "doubleclick.net" && it.type == BlocklistType.ADVERTISING })
        assertTrue("Expected rules to contain google-analytics.com", rules.any { it.pattern == "google-analytics.com" && it.type == BlocklistType.TRACKING })
        assertTrue("Expected rules to contain criteo.com", rules.any { it.pattern == "criteo.com" })
        assertTrue("Expected rules to contain hotjar.com", rules.any { it.pattern == "hotjar.com" })

        // 1. Intercept common advertising request
        val adUri = Uri.parse("https://securepubads.g.doubleclick.net/gampad/ads?iu=/1234/ad")
        val adResult = service.shouldInterceptUrl(adUri)
        assertTrue("Expected ad URL to be blocked", adResult.isBlocked)
        assertEquals(BlocklistType.ADVERTISING, adResult.type)

        // 2. Intercept common tracking request
        val trackingUri = Uri.parse("https://www.google-analytics.com/g/collect?v=2&tid=G-1234")
        val trackingResult = service.shouldInterceptUrl(trackingUri)
        assertTrue("Expected tracking URL to be blocked", trackingResult.isBlocked)
        assertEquals(BlocklistType.TRACKING, trackingResult.type)

        // 3. Normal webpage is not intercepted
        val cleanUri = Uri.parse("https://ubuntu.com/desktop")
        val cleanResult = service.shouldInterceptUrl(cleanUri)
        assertFalse("Expected legitimate website not to be blocked", cleanResult.isBlocked)

        // 4. Disabling a category allows requests to pass through
        service.setCategoryEnabled(BlocklistType.ADVERTISING, false)
        val adResultDisabled = service.shouldInterceptUrl(adUri)
        assertFalse("Expected ad URL to pass when category disabled", adResultDisabled.isBlocked)

        // Re-enable and verify blocked again
        service.setCategoryEnabled(BlocklistType.ADVERTISING, true)
        val adResultReenabled = service.shouldInterceptUrl(adUri)
        assertTrue("Expected ad URL to be blocked after re-enabling", adResultReenabled.isBlocked)
    }

    @Test
    fun `blocklist service allows adding custom rules and logs intercepted requests`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = BlocklistService(context)
        service.clearLog()

        // Add custom tracking rule
        val customDomain = "custom-tracker-telemetry.example.org"
        val added = service.addCustomRule(
            pattern = customDomain,
            type = BlocklistType.TRACKING,
            description = "Custom test tracking domain"
        )
        assertTrue(added)

        val targetUri = Uri.parse("https://api.$customDomain/v1/event")
        val result = service.shouldInterceptUrl(targetUri)
        assertTrue(result.isBlocked)
        assertEquals(BlocklistType.TRACKING, result.type)
        assertEquals("custom-tracker-telemetry.example.org", result.matchedRule?.pattern)

        // Verify hit counter incremented and logged in audit log
        val log = service.blockedRequestsLog.value
        assertTrue("Audit log should contain intercepted request", log.any { it.host == "api.$customDomain" })

        // Remove custom rule
        val ruleId = result.matchedRule?.id
        if (ruleId != null) {
            val removed = service.removeRule(ruleId)
            assertTrue(removed)

            // Subsequent requests should not be blocked
            val afterRemoval = service.shouldInterceptUrl(targetUri)
            assertFalse(afterRemoval.isBlocked)
        }
    }

    @Test
    fun `tab management system opens switches between duplicates and closes multiple browsing tabs`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = BrowserViewModel(app)

        // Initial default tab
        assertEquals(1, vm.tabs.value.size)
        val tab1Id = vm.tabs.value[0].id
        assertEquals(tab1Id, vm.activeTabId.value)

        // Open a new standard tab
        vm.openNewTab(url = "https://ubuntu.com")
        assertEquals(2, vm.tabs.value.size)
        val tab2Id = vm.activeTabId.value
        assertFalse(tab1Id == tab2Id)
        assertEquals("https://ubuntu.com", vm.activeTab?.url)

        // Open an incognito tab
        vm.openNewTab(url = "https://askubuntu.com", isIncognito = true)
        assertEquals(3, vm.tabs.value.size)
        val tab3 = vm.activeTab
        assertTrue(tab3?.isIncognito == true)

        // Switch back to tab 1
        vm.selectTab(tab1Id)
        assertEquals(tab1Id, vm.activeTabId.value)

        // Duplicate tab 2 (https://ubuntu.com)
        vm.duplicateTab(tab2Id)
        assertEquals(4, vm.tabs.value.size)
        assertEquals("https://ubuntu.com", vm.activeTab?.url)

        // Close duplicated tab
        val duplicatedId = vm.activeTabId.value
        vm.closeTab(duplicatedId)
        assertEquals(3, vm.tabs.value.size)

        // Close tab 2
        vm.closeTab(tab2Id)
        assertEquals(2, vm.tabs.value.size)
    }

    @Test
    fun `tab management system closes inactive tabs and incognito tabs`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = BrowserViewModel(app)

        vm.openNewTab(url = "https://canonical.com")
        vm.openNewTab(url = "https://omgubuntu.co.uk")
        vm.openNewTab(url = "https://duckduckgo.com", isIncognito = true)
        assertEquals(4, vm.tabs.value.size)

        // Close all incognito tabs
        vm.closeAllIncognitoTabs()
        assertEquals(3, vm.tabs.value.size)
        assertFalse(vm.tabs.value.any { it.isIncognito })

        // Currently active tab is the 3rd tab (omgubuntu)
        val activeId = vm.activeTabId.value

        // Close inactive tabs
        vm.closeInactiveTabs()
        assertEquals(1, vm.tabs.value.size)
        assertEquals(activeId, vm.activeTabId.value)

        // Close the only remaining tab -> resets to empty "New Tab"
        vm.closeTab(activeId)
        assertEquals(1, vm.tabs.value.size)
        assertEquals("New Tab", vm.tabs.value[0].title)
        assertEquals("", vm.tabs.value[0].url)
    }

    @Test
    fun `swordfish webview client intercepts ad-related javascript sources and network requests`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val blocklistService = BlocklistService(context)
        var blockedCount = 0
        var wasScriptBlocked = false
        var wasTrackerBlocked = false

        val client = SwordfishAdBlockWebViewClient(
            blocklistService = blocklistService,
            isShieldEnabled = { true },
            getFilterSettings = { FilterSettings() },
            onBlocked = { _, isScript, isTracker ->
                blockedCount++
                wasScriptBlocked = isScript
                wasTrackerBlocked = isTracker
            }
        )

        // 1. Intercept known ad-related JavaScript source (adsbygoogle.js)
        val adScriptUrl = "https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"
        val adScriptResponse = client.shouldInterceptRequest(null, adScriptUrl)
        org.junit.Assert.assertNotNull(adScriptResponse)
        assertEquals("application/javascript", adScriptResponse?.mimeType)
        assertEquals(200, adScriptResponse?.statusCode)
        assertTrue(wasScriptBlocked)

        // Verify benign stub is returned to prevent page crash
        val stubBytes = adScriptResponse?.data?.readBytes()?.toString(Charsets.UTF_8) ?: ""
        assertTrue(stubBytes.contains("adsbygoogle"))

        // 2. Intercept analytics JavaScript source (analytics.js)
        val analyticsUrl = "https://www.google-analytics.com/analytics.js"
        val analyticsResponse = client.shouldInterceptRequest(null, analyticsUrl)
        org.junit.Assert.assertNotNull(analyticsResponse)
        assertEquals("application/javascript", analyticsResponse?.mimeType)
        assertTrue(wasTrackerBlocked)

        // 3. Intercept ad network request (doubleclick.net image/feed)
        val adNetworkUrl = "https://ad.doubleclick.net/ddm/trackclk/12345"
        val adNetworkResponse = client.shouldInterceptRequest(null, adNetworkUrl)
        org.junit.Assert.assertNotNull(adNetworkResponse)
        assertEquals(200, adNetworkResponse?.statusCode)

        // 4. Pass-through legitimate non-ad scripts and assets
        val legitimateScriptUrl = "https://ubuntu.com/static/js/main.js"
        val legitimateResponse = client.shouldInterceptRequest(null, legitimateScriptUrl)
        org.junit.Assert.assertNull(legitimateResponse)

        val legitimateImageUrl = "https://assets.ubuntu.com/v1/ubuntu-logo.png"
        val legitimateImageResponse = client.shouldInterceptRequest(null, legitimateImageUrl)
        org.junit.Assert.assertNull(legitimateImageResponse)

        // 5. When shield is disabled, requests should not be intercepted
        val disabledClient = SwordfishAdBlockWebViewClient(
            blocklistService = blocklistService,
            isShieldEnabled = { false },
            getFilterSettings = { FilterSettings(isMasterShieldEnabled = false) }
        )
        val responseWhenDisabled = disabledClient.shouldInterceptRequest(null, adScriptUrl)
        org.junit.Assert.assertNull(responseWhenDisabled)
    }

    @Test
    fun `local history manager stores visited urls and filters by query and date in room database`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BrowserDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.browserDao()
        val repository = BrowserRepository(dao)

        // 1. Verify initially empty history
        val initialCount = dao.getHistoryCount().first()
        assertEquals(0, initialCount)

        // 2. Add history records
        val id1 = dao.insertHistory(
            HistoryEntity(
                title = "Ubuntu Linux Official Homepage",
                url = "https://ubuntu.com",
                visitedAt = System.currentTimeMillis() - 1000L
            )
        )
        val id2 = dao.insertHistory(
            HistoryEntity(
                title = "Android Jetpack Compose Docs",
                url = "https://developer.android.com/jetpack/compose",
                visitedAt = System.currentTimeMillis() - 500L
            )
        )
        val id3 = dao.insertHistory(
            HistoryEntity(
                title = "DuckDuckGo Privacy Search",
                url = "https://duckduckgo.com",
                visitedAt = System.currentTimeMillis()
            )
        )

        // 3. Verify total history count is 3
        val countAfterInsert = dao.getHistoryCount().first()
        assertEquals(3, countAfterInsert)

        // 4. Test search history by title and URL query
        val searchUbuntu = dao.searchHistory("ubuntu").first()
        assertEquals(1, searchUbuntu.size)
        assertEquals("https://ubuntu.com", searchUbuntu[0].url)

        val searchAndroid = dao.searchHistory("compose").first()
        assertEquals(1, searchAndroid.size)
        assertEquals(id2, searchAndroid[0].id)

        val searchDuck = dao.searchHistory("duckduckgo.com").first()
        assertEquals(1, searchDuck.size)
        assertEquals("DuckDuckGo Privacy Search", searchDuck[0].title)

        val searchNone = dao.searchHistory("nonexistent-domain-xyz").first()
        assertTrue(searchNone.isEmpty())

        // 5. Test individual item deletion by ID
        repository.deleteHistoryById(id1)
        val countAfterDelete = dao.getHistoryCount().first()
        assertEquals(2, countAfterDelete)

        val searchDeleted = dao.searchHistory("ubuntu").first()
        assertTrue(searchDeleted.isEmpty())

        // 6. Test individual item deletion by URL
        repository.deleteHistoryByUrl("https://duckduckgo.com")
        val countAfterUrlDelete = dao.getHistoryCount().first()
        assertEquals(1, countAfterUrlDelete)
        assertEquals(id2, dao.getRecentHistory().first()[0].id)

        db.close()
    }

    @Test
    fun `local history manager clears history by time range and deletes individual items`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BrowserDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.browserDao()
        val repository = BrowserRepository(dao)

        val now = System.currentTimeMillis()
        val halfHourAgo = now - (30 * 60 * 1000L) // inside LAST_HOUR
        val twoHoursAgo = now - (2 * 60 * 60 * 1000L) // inside LAST_24_HOURS, outside LAST_HOUR
        val threeDaysAgo = now - (3 * 24 * 60 * 60 * 1000L) // inside LAST_7_DAYS, outside LAST_24_HOURS
        val twoWeeksAgo = now - (14 * 24 * 60 * 60 * 1000L) // older than 7 days

        dao.insertHistory(HistoryEntity(title = "Recent 30m Site", url = "https://recent.com", visitedAt = halfHourAgo))
        dao.insertHistory(HistoryEntity(title = "2h Ago Site", url = "https://twohours.com", visitedAt = twoHoursAgo))
        dao.insertHistory(HistoryEntity(title = "3d Ago Site", url = "https://threedays.com", visitedAt = threeDaysAgo))
        dao.insertHistory(HistoryEntity(title = "Old 2w Site", url = "https://oldweeks.com", visitedAt = twoWeeksAgo))

        assertEquals(4, dao.getHistoryCount().first())

        // 1. Clear last hour: should remove the 30m site
        repository.clearHistoryTimeRange(HistoryTimeRange.LAST_HOUR)
        var remaining = dao.getRecentHistory().first()
        assertEquals(3, remaining.size)
        assertTrue(remaining.none { it.url == "https://recent.com" })

        // 2. Clear last 24 hours: should remove the 2h ago site
        repository.clearHistoryTimeRange(HistoryTimeRange.LAST_24_HOURS)
        remaining = dao.getRecentHistory().first()
        assertEquals(2, remaining.size)
        assertTrue(remaining.none { it.url == "https://twohours.com" })
        assertTrue(remaining.any { it.url == "https://threedays.com" })
        assertTrue(remaining.any { it.url == "https://oldweeks.com" })

        // 3. Clear all time: permanently removes everything from local SQLite
        repository.clearHistoryTimeRange(HistoryTimeRange.ALL_TIME)
        assertEquals(0, dao.getHistoryCount().first())
        assertTrue(dao.getRecentHistory().first().isEmpty())

        db.close()
    }

    @Test
    fun `global theme toggle switches between high contrast dark and light mode and persists`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val prefs = app.getSharedPreferences("swordfish_theme_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        val viewModel = BrowserViewModel(app)

        // 1. Verify default theme is high-contrast dark theme
        assertTrue(viewModel.isDarkTheme.value)

        // 2. Toggle to light mode
        viewModel.toggleTheme()
        assertFalse(viewModel.isDarkTheme.value)
        assertFalse(prefs.getBoolean("is_dark_theme", true))

        // 3. Toggle back to dark theme
        viewModel.toggleTheme()
        assertTrue(viewModel.isDarkTheme.value)
        assertTrue(prefs.getBoolean("is_dark_theme", false))

        // 4. Set explicitly to light mode
        viewModel.setDarkTheme(false)
        assertFalse(viewModel.isDarkTheme.value)
        assertFalse(prefs.getBoolean("is_dark_theme", true))

        // 5. Set explicitly to dark mode
        viewModel.setDarkTheme(true)
        assertTrue(viewModel.isDarkTheme.value)
        assertTrue(prefs.getBoolean("is_dark_theme", false))
    }

    @Test
    fun `loadUrl correctly formats domain inputs and emits WebCommand with replay`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = BrowserViewModel(app)

        // Ensure active tab starts blank
        assertEquals("", viewModel.activeTab?.url)

        // Load a domain without scheme (e.g., youtube.com)
        viewModel.loadUrl("youtube.com")
        ShadowLooper.idleMainLooper()

        // Active tab must be updated to https://youtube.com
        val activeTab = viewModel.activeTab
        assertEquals("https://youtube.com", activeTab?.url)
        assertTrue(activeTab?.isLoading == true)

        // Replay cache must have WebCommand.LoadUrl
        val command = viewModel.webCommands.replayCache.firstOrNull()
        assertTrue(command is WebCommand.LoadUrl)
        assertEquals("https://youtube.com", (command as WebCommand.LoadUrl).url)

        // Load direct URL with scheme
        viewModel.loadUrl("https://m.youtube.com")
        ShadowLooper.idleMainLooper()

        assertEquals("https://m.youtube.com", viewModel.activeTab?.url)
        val nextCommand = viewModel.webCommands.replayCache.firstOrNull()
        assertTrue(nextCommand is WebCommand.LoadUrl)
        assertEquals("https://m.youtube.com", (nextCommand as WebCommand.LoadUrl).url)
    }

    @Test
    fun `swordfish web view client never intercepts main frame navigation requests`() {
        val client = SwordfishAdBlockWebViewClient(
            isShieldEnabled = { true }
        )

        val mainFrameRequest = object : WebResourceRequest {
            override fun getUrl(): Uri = Uri.parse("https://m.youtube.com")
            override fun isForMainFrame(): Boolean = true
            override fun isRedirect(): Boolean = false
            override fun hasGesture(): Boolean = false
            override fun getMethod(): String = "GET"
            override fun getRequestHeaders(): Map<String, String> = emptyMap()
        }

        val response = client.shouldInterceptRequest(null, mainFrameRequest)
        // Main frame request should NOT be intercepted (returns null so system WebView loads normally)
        org.junit.Assert.assertNull(response)
    }

    @Test
    fun `swordfish web view client intercepts ad subresources when shield is active`() {
        val client = SwordfishAdBlockWebViewClient(
            isShieldEnabled = { true }
        )

        val adSubresourceRequest = object : WebResourceRequest {
            override fun getUrl(): Uri = Uri.parse("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js")
            override fun isForMainFrame(): Boolean = false
            override fun isRedirect(): Boolean = false
            override fun hasGesture(): Boolean = false
            override fun getMethod(): String = "GET"
            override fun getRequestHeaders(): Map<String, String> = emptyMap()
        }

        val response = client.shouldInterceptRequest(null, adSubresourceRequest)
        // Ad subresource should be intercepted and return benign script response
        org.junit.Assert.assertNotNull(response)
        assertEquals("application/javascript", response?.mimeType)
    }
}


