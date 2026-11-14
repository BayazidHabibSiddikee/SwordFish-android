package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing an offline cached webpage.
 * Stores rendered HTML markup, page title, origin domain, plain text preview,
 * visit frequency metrics, size on disk, and offline retention status.
 */
@Entity(tableName = "cached_pages")
data class CachedPageEntity(
    @PrimaryKey
    val url: String,
    val title: String,
    val domain: String,
    val htmlContent: String,
    val plainTextSnippet: String = "",
    val mimeType: String = "text/html",
    val charset: String = "UTF-8",
    val cachedAtTimestamp: Long = System.currentTimeMillis(),
    val contentSizeBytes: Long = 0L,
    val visitCount: Int = 1,
    val isPinnedOffline: Boolean = false,
    val faviconUrl: String? = null
)
