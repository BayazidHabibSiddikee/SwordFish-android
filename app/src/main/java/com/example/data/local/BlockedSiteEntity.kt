package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_sites")
data class BlockedSiteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,                       // package name (e.g. com.zhiliaoapp.musically) or web domain (e.g. tiktok.com)
    val displayName: String = "",             // Readable title e.g. "TikTok"
    val category: String = "Social Media",    // "Social Media", "Entertainment", "Games", "Custom"
    val dailyLimitMinutes: Int = 30,          // Daily usage limit in minutes before block
    val isAppPackage: Boolean = false,        // True if Android package, false if web domain
    val isEnabled: Boolean = true
)
