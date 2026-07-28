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

    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
}
