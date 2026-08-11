package com.zafar.ichatai.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.zafar.ichatai.data.repository.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VibrationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationRepository: NotificationRepository
) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private fun vibrate(duration: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = notificationRepository.preferences.first()
            if (prefs?.vibrationEnabled == true && prefs.allowNotifications) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(duration)
                }
            }
        }
    }

    fun vibrateClick() {
        vibrate(30)
    }

    fun vibrateSuccess() {
        vibrate(50)
    }

    fun vibrateError() {
        // Double pulse for error
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = notificationRepository.preferences.first()
            if (prefs?.vibrationEnabled == true && prefs.allowNotifications) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(longArrayOf(0, 50, 100, 50), -1)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 50, 100, 50), -1)
                }
            }
        }
    }

    fun vibrateMessageReceived() {
        vibrate(100)
    }
}
