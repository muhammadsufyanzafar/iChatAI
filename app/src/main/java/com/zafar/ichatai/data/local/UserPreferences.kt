package com.zafar.ichatai.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun getUserName(): String {
        val name = sharedPreferences.getString("user_name", null)
        return if (name.isNullOrBlank()) {
            val defaultName = "User${Random.nextInt(1000, 9999)}"
            saveUserName(defaultName)
            defaultName
        } else {
            name
        }
    }

    fun saveUserName(name: String) {
        sharedPreferences.edit().putString("user_name", name).apply()
    }

    fun getUserEmail(): String {
        return sharedPreferences.getString("user_email", "") ?: ""
    }

    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString("user_email", email).apply()
    }

    fun getGender(): String {
        return sharedPreferences.getString("user_gender", "Prefer not to say") ?: "Prefer not to say"
    }

    fun saveGender(gender: String) {
        sharedPreferences.edit().putString("user_gender", gender).apply()
    }

    fun getDateOfBirth(): String? {
        return sharedPreferences.getString("user_dob", null)
    }

    fun saveDateOfBirth(dob: String?) {
        sharedPreferences.edit().putString("user_dob", dob).apply()
    }

    fun getAvatarUri(): String? {
        return sharedPreferences.getString("user_avatar_uri", null)
    }

    fun saveAvatarUri(uri: String?) {
        sharedPreferences.edit().putString("user_avatar_uri", uri).apply()
    }

    fun isFirstRun(): Boolean {
        return sharedPreferences.getBoolean("is_first_run", true)
    }

    fun setFirstRunComplete() {
        sharedPreferences.edit().putBoolean("is_first_run", false).apply()
    }

    fun isAutoCleanupEnabled(): Boolean {
        return sharedPreferences.getBoolean("auto_cleanup_enabled", false)
    }

    fun setAutoCleanupEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("auto_cleanup_enabled", enabled).apply()
    }

    fun getAutoCleanupDays(): Int {
        return sharedPreferences.getInt("auto_cleanup_days", 30)
    }

    fun setAutoCleanupDays(days: Int) {
        sharedPreferences.edit().putInt("auto_cleanup_days", days).apply()
    }

    // Cloud Sync Preferences
    fun isCloudSyncEnabled(): Boolean = sharedPreferences.getBoolean("cloud_sync_enabled", false)
    fun setCloudSyncEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean("cloud_sync_enabled", enabled).apply()

    fun isSyncOverWifiOnly(): Boolean = sharedPreferences.getBoolean("sync_wifi_only", true)
    fun setSyncOverWifiOnly(onlyWifi: Boolean) = sharedPreferences.edit().putBoolean("sync_wifi_only", onlyWifi).apply()

    fun getLastSyncTime(): Long = sharedPreferences.getLong("last_sync_time", 0L)
    fun setLastSyncTime(time: Long) = sharedPreferences.edit().putLong("last_sync_time", time).apply()

    fun isSyncHistoryEnabled(): Boolean = sharedPreferences.getBoolean("sync_history", true)
    fun setSyncHistoryEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean("sync_history", enabled).apply()

    fun isSyncImagesEnabled(): Boolean = sharedPreferences.getBoolean("sync_images", false)
    fun setSyncImagesEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean("sync_images", enabled).apply()

    fun isSyncSettingsEnabled(): Boolean = sharedPreferences.getBoolean("sync_settings", true)
    fun setSyncSettingsEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean("sync_settings", enabled).apply()

    fun isSyncPromptsEnabled(): Boolean = sharedPreferences.getBoolean("sync_prompts", true)
    fun setSyncPromptsEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean("sync_prompts", enabled).apply()

    fun getSyncErrorLog(): String = sharedPreferences.getString("sync_error_log", "") ?: ""
    fun appendSyncError(error: String) {
        val currentLog = getSyncErrorLog()
        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val newLog = "$timestamp: $error\n$currentLog".take(5000) // Keep last 5000 chars
        sharedPreferences.edit().putString("sync_error_log", newLog).apply()
    }
    fun clearSyncErrorLog() = sharedPreferences.edit().remove("sync_error_log").apply()

    // Language Settings
    fun getSelectedLanguage(): String {
        return sharedPreferences.getString("selected_language", "en") ?: "en"
    }

    fun saveSelectedLanguage(languageCode: String) {
        sharedPreferences.edit().putString("selected_language", languageCode).apply()
    }

    fun isTranslateEnabled(): Boolean {
        return sharedPreferences.getBoolean("translate_enabled", false)
    }

    fun setTranslateEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("translate_enabled", enabled).apply()
    }

    // Appearance Settings
    fun getThemeMode(): String {
        return sharedPreferences.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
    }

    fun setThemeMode(mode: String) {
        sharedPreferences.edit().putString("theme_mode", mode).apply()
    }

    fun getAccentColor(): String {
        return sharedPreferences.getString("accent_color", "PURPLE") ?: "PURPLE"
    }

    fun setAccentColor(color: String) {
        sharedPreferences.edit().putString("accent_color", color).apply()
    }

    // AI Model Preferences
    fun getSelectedModelId(): String {
        return sharedPreferences.getString("selected_model_id", "openrouter/free") ?: "openrouter/free"
    }

    fun setSelectedModelId(modelId: String) {
        sharedPreferences.edit().putString("selected_model_id", modelId).apply()
    }

    fun getTemperature(): Float {
        return sharedPreferences.getFloat("ai_temperature", 0.7f)
    }

    fun setTemperature(temperature: Float) {
        sharedPreferences.edit().putFloat("ai_temperature", temperature).apply()
    }

    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }

    fun getAllPreferences(): Map<String, *> {
        return sharedPreferences.all.filterKeys { key ->
            key != "sync_error_log" && key != "last_sync_time" && key != "sync_device_id"
        }
    }

    fun importPreferences(prefs: Map<String, *>) {
        val editor = sharedPreferences.edit()
        prefs.forEach { (key, value) ->
            when (value) {
                is String -> editor.putString(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Float -> editor.putFloat(key, value)
                is Long -> editor.putLong(key, value)
                is Double -> {
                    // Gson often parses numbers as Double. Convert back based on common usage.
                    if (value == value.toInt().toDouble()) {
                        editor.putInt(key, value.toInt())
                    } else {
                        editor.putFloat(key, value.toFloat())
                    }
                }
            }
        }
        editor.apply()
    }

    fun getDeviceId(): String {
        var id = sharedPreferences.getString("sync_device_id", null)
        if (id == null) {
            id = java.util.UUID.randomUUID().toString()
            sharedPreferences.edit().putString("sync_device_id", id).apply()
        }
        return id
    }
}
