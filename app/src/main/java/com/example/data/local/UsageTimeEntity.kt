package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_times")
data class UsageTimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetIdentifier: String, // package name (e.g. com.instagram.android) or web domain (e.g. instagram.com)
    val dateString: String,       // YYYY-MM-DD
    val activeSecondsToday: Long = 0,
    val lastActiveTime: Long = System.currentTimeMillis(),
    val timesBlockedToday: Int = 0
)
