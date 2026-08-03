package com.zafar.ichatai.utils

object SessionManager {
    val sessionStartTime = System.currentTimeMillis()

    fun getSessionDurationMinutes(): Long {
        val durationMs = System.currentTimeMillis() - sessionStartTime
        return durationMs / (1000 * 60)
    }

    fun getSessionDurationFormatted(): String {
        val durationMs = System.currentTimeMillis() - sessionStartTime
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60))
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
