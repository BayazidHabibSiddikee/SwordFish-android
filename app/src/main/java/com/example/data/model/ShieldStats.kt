package com.example.data.model

data class ShieldStats(
    val totalAdsBlocked: Int = 0,
    val totalTrackersBlocked: Int = 0,
    val totalDataSavedKb: Long = 0,
    val isShieldEnabled: Boolean = true,
    val isCosmeticBlockerEnabled: Boolean = true
)
