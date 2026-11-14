package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing an organizer folder for bookmarks.
 * Enables users to create custom folders, rename them, and group saved URLs.
 */
@Entity(tableName = "bookmark_folders")
data class BookmarkFolderEntity(
    @PrimaryKey
    val name: String,
    val colorHex: String = "#E95420", // Default Ubuntu Orange
    val iconName: String = "folder",
    val isSystem: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
