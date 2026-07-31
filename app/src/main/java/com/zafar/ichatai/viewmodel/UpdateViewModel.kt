package com.zafar.ichatai.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.zafar.ichatai.model.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import javax.inject.Inject

sealed class UpdateUIState {
    object Idle : UpdateUIState()
    object Checking : UpdateUIState()
    data class UpdateAvailable(val updateInfo: UpdateInfo) : UpdateUIState()
    object UpToDate : UpdateUIState()
    data class Error(val message: String) : UpdateUIState()
}

@HiltViewModel
class UpdateViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<UpdateUIState>(UpdateUIState.Idle)
    val uiState = _uiState.asStateFlow()

    private val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private val remoteConfig = FirebaseRemoteConfig.getInstance().apply {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (isDebug) 0 else 3600)
            .build()
        setConfigSettingsAsync(configSettings)
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _uiState.value = UpdateUIState.Checking
            try {
                remoteConfig.fetchAndActivate().await()
                val updateJson = remoteConfig.getString("android_update_info")
                if (updateJson.isNotEmpty()) {
                    val jsonObject = JSONObject(updateJson)
                    val latestVersionCode = jsonObject.optInt("latest_version_code", 0)
                    val latestVersionName = jsonObject.optString("latest_version_name", "")
                    val releaseDate = jsonObject.optString("release_date", "")
                    val isForceUpdate = jsonObject.optBoolean("critical_force_update", false)
                    
                    val changelogArray = jsonObject.optJSONArray("changelog")
                    val changelog = mutableListOf<String>()
                    if (changelogArray != null) {
                        for (i in 0 until changelogArray.length()) {
                            changelog.add(changelogArray.getString(i))
                        }
                    }
                    
                    val platformsArray = jsonObject.optJSONArray("platforms")
                    val platforms = mutableListOf<String>()
                    if (platformsArray != null) {
                        for (i in 0 until platformsArray.length()) {
                            platforms.add(platformsArray.getString(i))
                        }
                    }
                    
                    val seeMoreUrl = jsonObject.optString("see_more", "")

                    val updateInfo = UpdateInfo(
                        latestVersionCode = latestVersionCode,
                        latestVersionName = latestVersionName,
                        releaseDate = releaseDate,
                        isForceUpdate = isForceUpdate,
                        changelog = changelog,
                        platforms = platforms,
                        seeMoreUrl = seeMoreUrl
                    )

                    val (currentVersionCode, currentVersionName) = try {
                        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                        val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            packageInfo.longVersionCode.toInt()
                        } else {
                            @Suppress("DEPRECATION")
                            packageInfo.versionCode
                        }
                        val name = packageInfo.versionName ?: ""
                        Pair(code, name)
                    } catch (_: PackageManager.NameNotFoundException) {
                        Pair(0, "")
                    }

                    if (latestVersionCode > currentVersionCode || isNewerVersion(latestVersionName, currentVersionName)) {
                        _uiState.value = UpdateUIState.UpdateAvailable(updateInfo)
                    } else {
                        _uiState.value = UpdateUIState.UpToDate
                    }
                } else {
                    _uiState.value = UpdateUIState.UpToDate
                }
            } catch (e: Exception) {
                _uiState.value = UpdateUIState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetState() {
        _uiState.value = UpdateUIState.Idle
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        if (latest == current) return false
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val maxLength = maxOf(latestParts.size, currentParts.size)
        
        for (i in 0 until maxLength) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}

