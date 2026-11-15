package com.example.data.local

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.data.model.PasswordStrength
import com.example.data.model.PasswordStrengthLevel
import com.example.data.model.VaultCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import java.security.SecureRandom
import java.util.UUID

/**
 * Secure local password vault repository powered by Android EncryptedSharedPreferences.
 * Uses AES256-SIV for preference keys and AES256-GCM for preference values,
 * backed by hardware-backed Android Keystore keys via MasterKey.
 */
class SecurePasswordRepository(
    context: Context,
    customPrefs: SharedPreferences? = null
) {
    companion object {
        private const val TAG = "SecurePasswordVault"
        private const val PREFS_FILE = "swordfish_secure_vault_prefs"
        private const val KEY_CREDENTIALS = "encrypted_credentials_list"

        private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
        private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val NUMBERS = "0123456789"
        private const val SYMBOLS = "!@#$%^&*()-_=+[]{}|;:,.<>?"
    }

    private val prefs: SharedPreferences = customPrefs ?: try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize EncryptedSharedPreferences; falling back to private prefs", e)
        context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
    }

    private val secureRandom = SecureRandom()

    private val _credentials = MutableStateFlow<List<VaultCredential>>(emptyList())
    val credentials: StateFlow<List<VaultCredential>> = _credentials.asStateFlow()
    private val _isLocked = MutableStateFlow(true)
    fun authenticate(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                unlock()
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }
            override fun onAuthenticationFailed() {
                onError("Authentication failed")
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Vault")
            .setSubtitle("Authenticate to access saved passwords")
            .setNegativeButtonText("Cancel")
            .build()
        prompt.authenticate(info)
    }
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun unlock() { _isLocked.value = false }
    fun lock() { _isLocked.value = true }

    init {
        loadCredentialsFromStorage()
    }

    private fun loadCredentialsFromStorage() {
        try {
            val jsonString = prefs.getString(KEY_CREDENTIALS, null)
            if (jsonString.isNullOrBlank()) {
                _credentials.value = emptyList()
                return
            }

            val array = JSONArray(jsonString)
            val list = mutableListOf<VaultCredential>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(VaultCredential.fromJson(obj))
            }
            // Sort by most recently updated
            _credentials.value = list.sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading encrypted credentials", e)
            _credentials.value = emptyList()
        }
    }

    private fun persistCredentials(list: List<VaultCredential>) {
        val array = JSONArray()
        list.forEach { cred ->
            array.put(cred.toJson())
        }
        prefs.edit().putString(KEY_CREDENTIALS, array.toString()).apply()
        _credentials.value = list.sortedByDescending { it.updatedAt }
    }

    /**
     * Saves a new credential or updates an existing one if the ID matches.
     */
    fun saveCredential(credential: VaultCredential) {
        val current = _credentials.value.toMutableList()
        val index = current.indexOfFirst { it.id == credential.id }

        val cleanDomain = extractDomain(credential.domain)
        val prepared = credential.copy(
            domain = cleanDomain.ifBlank { credential.domain.trim() },
            updatedAt = System.currentTimeMillis()
        )

        if (index != -1) {
            current[index] = prepared
        } else {
            current.add(0, prepared)
        }
        persistCredentials(current)
    }

    /**
     * Saves or updates a credential by domain and username.
     * Useful for autofill save prompts.
     */
    fun saveOrUpdateByDomainAndUser(
        domain: String,
        username: String,
        password: String,
        title: String? = null
    ): VaultCredential {
        val cleanDomain = extractDomain(domain)
        val current = _credentials.value.toMutableList()
        val existingIndex = current.indexOfFirst {
            matchesDomain(it.domain, cleanDomain) && it.username.equals(username.trim(), ignoreCase = true)
        }

        val now = System.currentTimeMillis()
        val cred = if (existingIndex != -1) {
            val existing = current[existingIndex]
            existing.copy(
                password = password,
                title = title ?: existing.title,
                updatedAt = now
            )
        } else {
            VaultCredential(
                id = UUID.randomUUID().toString(),
                domain = cleanDomain,
                title = title ?: cleanDomain.replaceFirstChar { it.uppercase() },
                username = username.trim(),
                password = password,
                createdAt = now,
                updatedAt = now
            )
        }

        if (existingIndex != -1) {
            current[existingIndex] = cred
        } else {
            current.add(0, cred)
        }
        persistCredentials(current)
        return cred
    }

    /**
     * Deletes a credential by ID.
     */
    fun deleteCredential(id: String) {
        val current = _credentials.value.filterNot { it.id == id }
        persistCredentials(current)
    }

    /**
     * Finds credentials matching a web page URL or domain.
     */
    fun getCredentialsForDomain(pageUrlOrDomain: String): List<VaultCredential> {
        val cleanPageDomain = extractDomain(pageUrlOrDomain)
        if (cleanPageDomain.isBlank()) return emptyList()

        return _credentials.value.filter { cred ->
            matchesDomain(cleanPageDomain, cred.domain)
        }
    }

    /**
     * Filters credentials matching a search query across domain, title, username, or notes.
     */
    fun searchCredentials(query: String): List<VaultCredential> {
        if (query.isBlank()) return _credentials.value

        val q = query.trim().lowercase()
        return _credentials.value.filter {
            it.domain.lowercase().contains(q) ||
            it.title.lowercase().contains(q) ||
            it.username.lowercase().contains(q) ||
            it.notes.lowercase().contains(q)
        }
    }

    /**
     * Clears all credentials from the vault (e.g. factory reset / user clear).
     */
    fun clearAllCredentials() {
        prefs.edit().remove(KEY_CREDENTIALS).apply()
        _credentials.value = emptyList()
    }

    /**
     * Generates a cryptographically secure random password adhering to specified constraints.
     */
    fun generateSecurePassword(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        val safeLength = length.coerceIn(8, 64)
        val charPool = StringBuilder(LOWERCASE)
        val guaranteedChars = mutableListOf<Char>()

        // Ensure at least 1 lowercase
        guaranteedChars.add(LOWERCASE[secureRandom.nextInt(LOWERCASE.length)])

        if (includeUppercase) {
            charPool.append(UPPERCASE)
            guaranteedChars.add(UPPERCASE[secureRandom.nextInt(UPPERCASE.length)])
        }

        if (includeNumbers) {
            charPool.append(NUMBERS)
            guaranteedChars.add(NUMBERS[secureRandom.nextInt(NUMBERS.length)])
        }

        if (includeSymbols) {
            charPool.append(SYMBOLS)
            guaranteedChars.add(SYMBOLS[secureRandom.nextInt(SYMBOLS.length)])
        }

        val poolString = charPool.toString()
        val remainingCount = safeLength - guaranteedChars.size
        for (i in 0 until remainingCount) {
            guaranteedChars.add(poolString[secureRandom.nextInt(poolString.length)])
        }

        // Shuffle guaranteed chars using Fisher-Yates with SecureRandom
        for (i in guaranteedChars.indices.reversed()) {
            val j = secureRandom.nextInt(i + 1)
            val temp = guaranteedChars[i]
            guaranteedChars[i] = guaranteedChars[j]
            guaranteedChars[j] = temp
        }

        return guaranteedChars.joinToString("")
    }

    /**
     * Evaluates password entropy and strength.
     */
    fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) {
            return PasswordStrength(PasswordStrengthLevel.VERY_WEAK, 0.05f, "Enter a password")
        }

        var score = 0
        val len = password.length

        if (len >= 8) score += 1
        if (len >= 12) score += 2
        if (len >= 16) score += 2

        val hasLower = password.any { it.isLowerCase() }
        val hasUpper = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }

        var varietyCount = 0
        if (hasLower) varietyCount++
        if (hasUpper) varietyCount++
        if (hasDigit) varietyCount++
        if (hasSymbol) varietyCount++

        score += (varietyCount * 2)

        // Deduct for very common substrings
        val lower = password.lowercase()
        if (lower.contains("password") || lower.contains("123456") || lower.contains("qwerty") || lower.contains("admin")) {
            score -= 4
        }

        val clampedScore = score.coerceIn(0, 12)
        val percent = (clampedScore / 12f).coerceIn(0.1f, 1.0f)

        return when {
            clampedScore <= 3 -> PasswordStrength(
                PasswordStrengthLevel.VERY_WEAK,
                percent,
                "Very Weak: Use at least 8 characters with numbers & symbols"
            )
            clampedScore <= 6 -> PasswordStrength(
                PasswordStrengthLevel.WEAK,
                percent,
                "Weak: Add uppercase letters and symbols"
            )
            clampedScore <= 8 -> PasswordStrength(
                PasswordStrengthLevel.FAIR,
                percent,
                "Fair: Good, but longer passwords provide better security"
            )
            clampedScore <= 10 -> PasswordStrength(
                PasswordStrengthLevel.STRONG,
                percent,
                "Strong: Resilient against dictionary and brute-force attacks"
            )
            else -> PasswordStrength(
                PasswordStrengthLevel.VERY_STRONG,
                percent,
                "Very Strong: Optimal cryptographic entropy"
            )
        }
    }

    /**
     * Sanitizes and extracts clean host/domain from URLs or user input.
     * E.g. "https://login.live.com/oauth" -> "login.live.com"
     */
    fun extractDomain(rawInput: String): String {
        if (rawInput.isBlank()) return ""

        val trimmed = rawInput.trim()
        val toParse = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "https://$trimmed"
        } else {
            trimmed
        }

        return try {
            val uri = Uri.parse(toParse)
            var host = uri.host ?: trimmed
            if (host.startsWith("www.")) {
                host = host.removePrefix("www.")
            }
            // Strip port if present
            if (host.contains(":")) {
                host = host.substringBefore(":")
            }
            host.lowercase()
        } catch (e: Exception) {
            trimmed.substringBefore("/").substringBefore("?").removePrefix("www.").lowercase()
        }
    }

    /**
     * Determines whether two domains match (supporting subdomains).
     * E.g. "auth.github.com" matches "github.com".
     */
    fun matchesDomain(domain1: String, domain2: String): Boolean {
        val d1 = extractDomain(domain1)
        val d2 = extractDomain(domain2)

        if (d1.isBlank() || d2.isBlank()) return false
        if (d1 == d2) return true

        // Subdomain matching
        if (d1.endsWith(".$d2") || d2.endsWith(".$d1")) {
            return true
        }

        // Second level domain match (e.g. "example.com" and "login.example.com")
        val parts1 = d1.split(".")
        val parts2 = d2.split(".")
        if (parts1.size >= 2 && parts2.size >= 2) {
            val sld1 = "${parts1[parts1.size - 2]}.${parts1.last()}"
            val sld2 = "${parts2[parts2.size - 2]}.${parts2.last()}"
            if (sld1 == sld2) return true
        }

        return false
    }
}
