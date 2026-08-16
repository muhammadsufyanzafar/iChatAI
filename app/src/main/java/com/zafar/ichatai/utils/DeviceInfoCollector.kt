package com.zafar.ichatai.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.zafar.ichatai.data.local.UserPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DeviceInfoCollector {

    fun collectTelemetry(context: Context, userPreferences: UserPreferences, navigationTrail: String): String {
        val sb = StringBuilder()

        sb.append("### Application Information\n")
        sb.append("| Property | Value |\n")
        sb.append("| :--- | :--- |\n")
        sb.append("| Version Name | ${com.zafar.ichatai.BuildConfig.VERSION_NAME} |\n")
        sb.append("| Version Code | ${com.zafar.ichatai.BuildConfig.VERSION_CODE} |\n")
        sb.append("| Environment | PRODUCTION |\n\n")

        sb.append("### Device & OS\n")
        sb.append("| Property | Value |\n")
        sb.append("| :--- | :--- |\n")
        sb.append("| Manufacturer | ${Build.MANUFACTURER} |\n")
        sb.append("| Brand | ${Build.BRAND} |\n")
        sb.append("| Model | ${Build.MODEL} |\n")
        sb.append("| Platform | Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}) |\n\n")

        sb.append("### Memory & Storage\n")
        sb.append("| Property | Value |\n")
        sb.append("| :--- | :--- |\n")
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        sb.append("| RAM (Free/Total) | ${memoryInfo.availMem / (1024 * 1024)} MB / ${memoryInfo.totalMem / (1024 * 1024)} MB |\n")

        val internalStorage = StatFs(Environment.getDataDirectory().path)
        val freeInternal = internalStorage.availableBytes / (1024f * 1024f * 1024f)
        val totalInternal = internalStorage.totalBytes / (1024f * 1024f * 1024f)
        sb.append("| Internal Storage (Free/Total) | ${String.format(Locale.US, "%.2f", freeInternal)} GB / ${String.format(Locale.US, "%.2f", totalInternal)} GB |\n\n")

        sb.append("### Battery\n")
        sb.append("| Property | Value |\n")
        sb.append("| :--- | :--- |\n")
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = level * 100 / scale.toFloat()
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        sb.append("| Level | ${String.format(Locale.US, "%.0f", batteryPct)}% |\n")
        sb.append("| Charging | $isCharging |\n\n")

        sb.append("### Network\n")
        sb.append("| Property | Value |\n")
        sb.append("| :--- | :--- |\n")
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val type = when {
            capabilities == null -> "None"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
            else -> "Unknown"
        }
        val hasVpn = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false
        sb.append("| Connection | ${network != null} |\n")
        sb.append("| Type | $type |\n")
        sb.append("| VPN Active | $hasVpn |\n\n")

        sb.append("### Localization\n")
        sb.append("| Property | Value |\n")
        sb.append("| :--- | :--- |\n")
        val currentLocale = Locale.getDefault()
        sb.append("| Language | ${currentLocale.displayLanguage} (${currentLocale.language}) |\n")
        sb.append("| Region | ${currentLocale.displayCountry} (${currentLocale.country}) |\n")
        sb.append("| Timezone | ${TimeZone.getDefault().id} (${TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)}) |\n\n")

        sb.append("### User & Session\n")
        sb.append("| Property | Value |\n")
        sb.append("| :--- | :--- |\n")
        sb.append("| User ID | ${userPreferences.getUserEmail().ifBlank { "Anonymous" }} |\n")
        sb.append("| User Name | ${userPreferences.getUserName()} |\n")
        sb.append("| Session Duration | ${SessionManager.getSessionDurationFormatted()} |\n\n")

        sb.append("### Submission Details\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US)
        sb.append("| Detail | Value |\n")
        sb.append("| :--- | :--- |\n")
        sb.append("| Submitted At | ${sdf.format(Date())} |\n\n")

        sb.append("### Navigation Trail\n")
        sb.append("`$navigationTrail`")

        return sb.toString()
    }

    fun getCrashlyticsKeys(context: Context, userPreferences: UserPreferences): Map<String, Any> {
        val keys = mutableMapOf<String, Any>()

        keys["app_version"] = com.zafar.ichatai.BuildConfig.VERSION_NAME
        keys["app_code"] = com.zafar.ichatai.BuildConfig.VERSION_CODE
        keys["manufacturer"] = Build.MANUFACTURER
        keys["model"] = Build.MODEL
        keys["android_version"] = Build.VERSION.RELEASE
        keys["sdk_int"] = Build.VERSION.SDK_INT

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        keys["ram_free_mb"] = memoryInfo.availMem / (1024 * 1024)
        keys["ram_total_mb"] = memoryInfo.totalMem / (1024 * 1024)

        val internalStorage = StatFs(Environment.getDataDirectory().path)
        keys["storage_free_gb"] = internalStorage.availableBytes / (1024f * 1024f * 1024f)
        keys["storage_total_gb"] = internalStorage.totalBytes / (1024f * 1024f * 1024f)

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        keys["battery_pct"] = level * 100 / scale.toFloat()

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        keys["network_active"] = network != null
        keys["vpn_active"] = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false

        keys["user_email"] = userPreferences.getUserEmail()
        keys["user_name"] = userPreferences.getUserName()
        keys["session_duration"] = SessionManager.getSessionDurationFormatted()
        keys["navigation_trail"] = NavigationTracker.getTrail()

        return keys
    }
}
