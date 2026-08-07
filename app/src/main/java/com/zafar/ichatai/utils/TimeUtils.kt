package com.zafar.ichatai.utils

import android.content.Context
import com.zafar.ichatai.R
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun formatRelativeTime(timestamp: Long, context: Context): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60000 -> context.getString(R.string.just_now)
            diff < 3600000 -> context.getString(R.string.mins_ago, diff / 60000)
            diff < 86400000 -> context.getString(R.string.hours_ago, diff / 3600000)
            else -> {
                val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }
}
