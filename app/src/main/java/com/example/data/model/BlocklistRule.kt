package com.example.data.model

import java.util.UUID

enum class BlocklistType(val displayName: String) {
    ADVERTISING("Advertising"),
    TRACKING("Tracking & Telemetry")
}

enum class MatchType {
    DOMAIN_SUFFIX,
    CONTAINS_KEYWORD,
    EXACT_MATCH
}

data class BlocklistRule(
    val id: String = UUID.randomUUID().toString(),
    val pattern: String,
    val type: BlocklistType,
    val matchType: MatchType = MatchType.DOMAIN_SUFFIX,
    val isEnabled: Boolean = true,
    val isCustom: Boolean = false,
    val description: String = "",
    val hitCount: Int = 0,
    val lastBlockedTimestamp: Long? = null
)

data class BlockedRequestLog(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val host: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: BlocklistType,
    val rulePattern: String
)

data class BlocklistFilterResult(
    val isBlocked: Boolean,
    val matchedRule: BlocklistRule? = null,
    val type: BlocklistType? = null,
    val reason: String = ""
)
