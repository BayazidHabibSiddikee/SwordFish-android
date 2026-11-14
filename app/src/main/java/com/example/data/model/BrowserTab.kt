package com.example.data.model

import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "",
    val title: String = "New Tab",
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isIncognito: Boolean = false,
    val isDesktopMode: Boolean = false,
    val adsBlockedOnPage: Int = 0,
    val trackersBlockedOnPage: Int = 0,
    val isBlockedByFocus: Boolean = false,
    val blockedDomain: String? = null,
    val isReaderMode: Boolean = false,
    val isReaderAvailable: Boolean = false,
    val readerArticle: ReaderArticle? = null,
    val isLoadedFromCache: Boolean = false,
    val cachedTimestamp: Long? = null
)
