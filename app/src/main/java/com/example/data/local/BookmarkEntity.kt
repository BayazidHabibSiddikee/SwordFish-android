package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing a saved bookmark URL.
 * Supports organization into custom folders and tagging with custom labels.
 */
@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val category: String = "Favorites",
    val folder: String = "Favorites",
    val labels: String = "", // Comma-separated custom labels / tags (e.g. "linux, dev, tutorial")
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isPinnedToNewTab: Boolean = true,
    val visitCount: Int = 0
) {
    /**
     * Extracts individual labels as a trimmed list.
     */
    fun getLabelList(): List<String> {
        if (labels.isBlank()) return emptyList()
        return labels.split(",")
            .map { it.trim().removePrefix("#") }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    /**
     * Checks if bookmark matches a specific label.
     */
    fun hasLabel(label: String): Boolean {
        val clean = label.trim().removePrefix("#")
        return getLabelList().any { it.equals(clean, ignoreCase = true) }
    }
}
