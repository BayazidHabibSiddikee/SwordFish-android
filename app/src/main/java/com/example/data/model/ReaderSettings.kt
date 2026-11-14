package com.example.data.model

import androidx.compose.ui.graphics.Color
import org.json.JSONObject

enum class ReaderTheme(
    val displayName: String,
    val backgroundColor: Long,
    val surfaceColor: Long,
    val textColor: Long,
    val textSecondaryColor: Long,
    val accentColor: Long,
    val isDark: Boolean
) {
    UBUNTU_DARK(
        displayName = "Ubuntu Dark",
        backgroundColor = 0xFF1E1E1EL,
        surfaceColor = 0xFF262626L,
        textColor = 0xFFECECECL,
        textSecondaryColor = 0xFFB3B3B3L,
        accentColor = 0xFFE95420L,
        isDark = true
    ),
    UBUNTU_AUBERGINE(
        displayName = "Ubuntu Aubergine",
        backgroundColor = 0xFF2C001EL,
        surfaceColor = 0xFF3D082CL,
        textColor = 0xFFF5E5F0L,
        textSecondaryColor = 0xFFD1B0C8L,
        accentColor = 0xFFFF8254L,
        isDark = true
    ),
    LIGHT_PAPER(
        displayName = "Paper Light",
        backgroundColor = 0xFFFDFBF7L,
        surfaceColor = 0xFFF3EFE9L,
        textColor = 0xFF1C1917L,
        textSecondaryColor = 0xFF57534EL,
        accentColor = 0xFFE95420L,
        isDark = false
    ),
    SEPIA_WARM(
        displayName = "Sepia Warm",
        backgroundColor = 0xFFF4ECD8L,
        surfaceColor = 0xFFE7DEC5L,
        textColor = 0xFF382D21L,
        textSecondaryColor = 0xFF6D5D4FL,
        accentColor = 0xFFB8451DL,
        isDark = false
    ),
    AMOLED_BLACK(
        displayName = "AMOLED Black",
        backgroundColor = 0xFF000000L,
        surfaceColor = 0xFF141414L,
        textColor = 0xFFFFFFFFL,
        textSecondaryColor = 0xFFA0A0A0L,
        accentColor = 0xFFE95420L,
        isDark = true
    );

    val composeBackgroundColor: Color get() = Color(backgroundColor)
    val composeSurfaceColor: Color get() = Color(surfaceColor)
    val composeTextColor: Color get() = Color(textColor)
    val composeSecondaryTextColor: Color get() = Color(textSecondaryColor)
    val composeAccentColor: Color get() = Color(accentColor)
}

enum class ReaderFontFamily(val displayName: String) {
    SANS_SERIF("Sans-Serif"),
    SERIF("Serif"),
    MONOSPACE("Monospace")
}

enum class ReaderLineHeight(val displayName: String, val multiplier: Float) {
    TIGHT("Tight", 1.35f),
    NORMAL("Normal", 1.65f),
    RELAXED("Relaxed", 1.95f)
}

enum class ReaderContentWidth(val displayName: String, val maxWidthDp: Int) {
    COMPACT("Compact", 580),
    BALANCED("Balanced", 720),
    FULL("Full", 1200)
}

data class ReaderSettings(
    val theme: ReaderTheme = ReaderTheme.UBUNTU_DARK,
    val fontFamily: ReaderFontFamily = ReaderFontFamily.SANS_SERIF,
    val fontSizeSp: Int = 18,
    val lineHeight: ReaderLineHeight = ReaderLineHeight.NORMAL,
    val contentWidth: ReaderContentWidth = ReaderContentWidth.BALANCED,
    val autoEnterOnArticlePages: Boolean = false,
    val showEstimatedReadingTime: Boolean = true,
    val speechRate: Float = 1.0f
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("theme", theme.name)
        put("fontFamily", fontFamily.name)
        put("fontSizeSp", fontSizeSp)
        put("lineHeight", lineHeight.name)
        put("contentWidth", contentWidth.name)
        put("autoEnterOnArticlePages", autoEnterOnArticlePages)
        put("showEstimatedReadingTime", showEstimatedReadingTime)
        put("speechRate", speechRate.toDouble())
    }

    companion object {
        fun fromJson(json: JSONObject): ReaderSettings {
            val theme = try { ReaderTheme.valueOf(json.optString("theme", ReaderTheme.UBUNTU_DARK.name)) } catch (_: Exception) { ReaderTheme.UBUNTU_DARK }
            val font = try { ReaderFontFamily.valueOf(json.optString("fontFamily", ReaderFontFamily.SANS_SERIF.name)) } catch (_: Exception) { ReaderFontFamily.SANS_SERIF }
            val fontSize = json.optInt("fontSizeSp", 18).coerceIn(12, 28)
            val lineHeight = try { ReaderLineHeight.valueOf(json.optString("lineHeight", ReaderLineHeight.NORMAL.name)) } catch (_: Exception) { ReaderLineHeight.NORMAL }
            val contentWidth = try { ReaderContentWidth.valueOf(json.optString("contentWidth", ReaderContentWidth.BALANCED.name)) } catch (_: Exception) { ReaderContentWidth.BALANCED }
            val autoEnter = json.optBoolean("autoEnterOnArticlePages", false)
            val showReadingTime = json.optBoolean("showEstimatedReadingTime", true)
            val speechRate = json.optDouble("speechRate", 1.0).toFloat().coerceIn(0.5f, 2.0f)

            return ReaderSettings(
                theme = theme,
                fontFamily = font,
                fontSizeSp = fontSize,
                lineHeight = lineHeight,
                contentWidth = contentWidth,
                autoEnterOnArticlePages = autoEnter,
                showEstimatedReadingTime = showReadingTime,
                speechRate = speechRate
            )
        }

        fun fromJsonString(jsonStr: String): ReaderSettings {
            return try {
                fromJson(JSONObject(jsonStr))
            } catch (_: Exception) {
                ReaderSettings()
            }
        }
    }
}
