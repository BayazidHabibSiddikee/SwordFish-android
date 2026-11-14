package com.example.data.model

import org.json.JSONObject
import java.util.UUID

/**
 * Represents a secure user login credential saved in Swordfish Password Vault.
 * Stored locally and encrypted via Android EncryptedSharedPreferences (AES256-GCM / AES256-SIV).
 */
data class VaultCredential(
    val id: String = UUID.randomUUID().toString(),
    val domain: String,
    val title: String,
    val username: String,
    val password: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("domain", domain)
            put("title", title)
            put("username", username)
            put("password", password)
            put("notes", notes)
            put("createdAt", createdAt)
            put("updatedAt", updatedAt)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): VaultCredential {
            return VaultCredential(
                id = json.optString("id", UUID.randomUUID().toString()),
                domain = json.optString("domain", ""),
                title = json.optString("title", ""),
                username = json.optString("username", ""),
                password = json.optString("password", ""),
                notes = json.optString("notes", ""),
                createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = json.optLong("updatedAt", System.currentTimeMillis())
            )
        }

        fun fromJsonString(jsonStr: String): VaultCredential? {
            return try {
                fromJson(JSONObject(jsonStr))
            } catch (e: Exception) {
                null
            }
        }
    }
}

enum class PasswordStrengthLevel(val label: String, val score: Int) {
    VERY_WEAK("Very Weak", 0),
    WEAK("Weak", 1),
    FAIR("Fair", 2),
    STRONG("Strong", 3),
    VERY_STRONG("Very Strong", 4)
}

data class PasswordStrength(
    val level: PasswordStrengthLevel,
    val percent: Float, // 0.0f to 1.0f
    val feedback: String
)
