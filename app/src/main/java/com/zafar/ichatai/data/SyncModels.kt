package com.zafar.ichatai.data

import com.google.gson.annotations.SerializedName

data class SyncManifest(
    @SerializedName("version") val version: Int = 1,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("lastUpdated") val lastUpdated: Long,
    @SerializedName("checksum") val checksum: String? = null,
    @SerializedName("deviceInfo") val deviceInfo: String = android.os.Build.MODEL
)

data class BackupPackage(
    @SerializedName("manifest") val manifest: SyncManifest,
    @SerializedName("payload") val payload: Map<String, Any>
)
