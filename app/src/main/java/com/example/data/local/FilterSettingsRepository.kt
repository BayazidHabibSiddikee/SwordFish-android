package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AdBlockFilters
import com.example.data.model.DistractionFilters
import com.example.data.model.FilterPreset
import com.example.data.model.FilterSettings
import com.example.data.model.SecurityFilters
import com.example.data.model.TrackingFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository responsible for managing and persisting granular individual ad-blocking,
 * tracking protection, and distraction-blocking filters.
 */
class FilterSettingsRepository(
    context: Context,
    customPrefs: SharedPreferences? = null
) {
    companion object {
        private const val PREFS_NAME = "swordfish_filter_settings_prefs"
        private const val KEY_FILTER_SETTINGS = "filter_settings_json"
    }

    private val prefs: SharedPreferences = customPrefs ?: context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _filterSettings = MutableStateFlow(loadSettings())
    val filterSettings: StateFlow<FilterSettings> = _filterSettings.asStateFlow()

    private fun loadSettings(): FilterSettings {
        val jsonStr = prefs.getString(KEY_FILTER_SETTINGS, null)
        return if (!jsonStr.isNullOrBlank()) {
            FilterSettings.fromJsonString(jsonStr)
        } else {
            FilterSettings()
        }
    }

    fun getSettings(): FilterSettings = _filterSettings.value

    fun saveSettings(settings: FilterSettings) {
        prefs.edit().putString(KEY_FILTER_SETTINGS, settings.toJsonString()).apply()
        _filterSettings.value = settings
    }

    fun updateSettings(settings: FilterSettings) {
        saveSettings(settings)
    }

    // Ad-Blocking Individual Toggles
    fun updateAdFilters(transform: (AdBlockFilters) -> AdBlockFilters) {
        val current = _filterSettings.value
        saveSettings(current.copy(preset = FilterPreset.CUSTOM, adBlock = transform(current.adBlock)))
    }

    // Tracking Protection Individual Toggles
    fun updateTrackingFilters(transform: (TrackingFilters) -> TrackingFilters) {
        val current = _filterSettings.value
        saveSettings(current.copy(preset = FilterPreset.CUSTOM, tracking = transform(current.tracking)))
    }

    // Distraction Blocking Individual Toggles
    fun updateDistractionFilters(transform: (DistractionFilters) -> DistractionFilters) {
        val current = _filterSettings.value
        saveSettings(current.copy(preset = FilterPreset.CUSTOM, distraction = transform(current.distraction)))
    }

    // Security Systems Individual Toggles
    fun updateSecurityFilters(transform: (SecurityFilters) -> SecurityFilters) {
        val current = _filterSettings.value
        saveSettings(current.copy(preset = FilterPreset.CUSTOM, security = transform(current.security)))
    }

    fun applyPreset(preset: FilterPreset) {
        saveSettings(FilterSettings.createPreset(preset))
    }

    fun resetToDefaults() {
        saveSettings(FilterSettings.createPreset(FilterPreset.BALANCED))
    }
}
