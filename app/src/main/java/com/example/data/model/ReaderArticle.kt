package com.example.data.model

import org.json.JSONObject

/**
 * Encapsulates pristine, extracted article content stripped of distracting advertisements,
 * sidebars, navigation bars, popups, and clutter.
 */
data class ReaderArticle(
    val title: String,
    val byline: String? = null,
    val siteName: String = "",
    val publishedDate: String? = null,
    val contentHtml: String = "",
    val plainText: String = "",
    val wordCount: Int = 0,
    val readingTimeMinutes: Int = 1,
    val url: String = "",
    val topImageUrl: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("title", title)
        put("byline", byline ?: "")
        put("siteName", siteName)
        put("publishedDate", publishedDate ?: "")
        put("contentHtml", contentHtml)
        put("plainText", plainText)
        put("wordCount", wordCount)
        put("readingTimeMinutes", readingTimeMinutes)
        put("url", url)
        put("topImageUrl", topImageUrl ?: "")
    }

    companion object {
        fun fromJson(json: JSONObject): ReaderArticle {
            val title = json.optString("title", "Article")
            val byline = json.optString("byline", "").takeIf { it.isNotBlank() }
            val siteName = json.optString("siteName", "")
            val publishedDate = json.optString("publishedDate", "").takeIf { it.isNotBlank() }
            val contentHtml = json.optString("contentHtml", "")
            val plainText = json.optString("plainText", "")
            val wordCount = json.optInt("wordCount", countWords(plainText))
            val readingTime = json.optInt("readingTimeMinutes", calculateReadingTime(wordCount))
            val url = json.optString("url", "")
            val topImageUrl = json.optString("topImageUrl", "").takeIf { it.isNotBlank() }

            return ReaderArticle(
                title = title,
                byline = byline,
                siteName = siteName,
                publishedDate = publishedDate,
                contentHtml = contentHtml,
                plainText = plainText,
                wordCount = wordCount,
                readingTimeMinutes = readingTime,
                url = url,
                topImageUrl = topImageUrl
            )
        }

        fun fromJsonString(jsonStr: String): ReaderArticle? {
            return try {
                fromJson(JSONObject(jsonStr))
            } catch (_: Exception) {
                null
            }
        }

        fun countWords(text: String): Int {
            if (text.isBlank()) return 0
            return text.trim().split("\\s+".toRegex()).size
        }

        fun calculateReadingTime(words: Int): Int {
            // Average adult reading speed: ~200-250 words per minute
            val minutes = (words + 199) / 200
            return maxOf(1, minutes)
        }
    }
}
