package com.zafar.ichatai.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000} mins ago"
            diff < 86400000 -> "${diff / 3600000} hours ago"
            else -> {
                val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }
}
