package com.zafar.ichatai.network

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zafar.ichatai.BuildConfig
import com.zafar.ichatai.data.AIModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor() {
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val interval = if (BuildConfig.DEBUG) 0L else 3600L
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(interval)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    suspend fun fetchAndActivate(): Boolean {
        return try {
            val result = remoteConfig.fetchAndActivate().await()
            Log.d("RemoteConfig", "Fetch and activate result: $result")
            result
        } catch (e: Exception) {
            Log.e("RemoteConfig", "Fetch failed", e)
            false
        }
    }

    fun getAIModels(): List<AIModel> {
        val json = remoteConfig.getString("ai_models")
        Log.d("RemoteConfig", "ai_models JSON: $json")
        return if (json.isNotEmpty()) {
            try {
                val type = object : TypeToken<List<AIModel>>() {}.type
                val models: List<AIModel> = Gson().fromJson(json, type)
                Log.d("RemoteConfig", "Parsed ${models.size} models")
                models
            } catch (e: Exception) {
                Log.e("RemoteConfig", "Parsing failed", e)
                emptyList()
            }
        } else {
            Log.w("RemoteConfig", "ai_models is empty")
            emptyList()
        }
    }
}
